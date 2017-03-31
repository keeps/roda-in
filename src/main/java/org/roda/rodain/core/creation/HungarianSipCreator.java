package org.roda.rodain.core.creation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.roda.rodain.core.Constants;
import org.roda.rodain.core.Constants.MetadataOption;
import org.roda.rodain.core.Constants.SipNameStrategy;
import org.roda.rodain.core.Controller;
import org.roda.rodain.core.I18n;
import org.roda.rodain.core.Pair;
import org.roda.rodain.core.rules.TreeNode;
import org.roda.rodain.core.schema.DescriptiveMetadata;
import org.roda.rodain.core.schema.Sip;
import org.roda.rodain.core.sip.SipPreview;
import org.roda.rodain.core.sip.SipRepresentation;
import org.roda.rodain.ui.creation.CreationModalProcessing;
import org.roda_project.commons_ip.model.IPContentType;
import org.roda_project.commons_ip.model.IPDescriptiveMetadata;
import org.roda_project.commons_ip.model.IPFile;
import org.roda_project.commons_ip.model.IPHeader;
import org.roda_project.commons_ip.model.IPRepresentation;
import org.roda_project.commons_ip.model.MetadataType;
import org.roda_project.commons_ip.model.SIP;
import org.roda_project.commons_ip.model.SIPObserver;
import org.roda_project.commons_ip.model.impl.hungarian.HungarianSIP;
import org.roda_project.commons_ip.utils.IPEnums.IPStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HungarianSipCreator extends SimpleSipCreator implements SIPObserver {
  private static final Logger LOGGER = LoggerFactory.getLogger(HungarianSipCreator.class.getName());
  private int countFilesOfZip;
  private int currentSIPadded = 0;
  private int currentSIPsize = 0;
  private int repProcessingSize;

  private String prefix;
  private SipNameStrategy sipNameStrategy;

  /**
   * Creates a new Hungarian SIP exporter.
   *
   * @param outputPath
   *          The path to the output folder of the SIP exportation
   * @param previews
   *          The map with the SIPs that will be exported
   * @param createReport
   */
  public HungarianSipCreator(Path outputPath, Map<Sip, List<String>> previews, String prefix,
    SipNameStrategy sipNameStrategy, boolean createReport) {
    super(outputPath, previews, createReport);
    this.prefix = prefix;
    this.sipNameStrategy = sipNameStrategy;
  }

  /**
   * Attempts to create an Hungarian SIP of each SipPreview
   */
  @Override
  public void run() {
    Map<Path, Object> sips = new HashMap<>();
    for (Sip preview : previews.keySet()) {
      if (canceled) {
        break;
      }

      Pair pathSIP = createHungarianSip(preview);
      if (pathSIP != null) {
        sips.put((Path) pathSIP.getKey(), (SIP) pathSIP.getValue());
      }
    }

    if (createReport) {
      createReport(sips);
    }

    currentAction = I18n.t(Constants.I18N_DONE);
  }

  private Pair createHungarianSip(Sip descriptionObject) {
    Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
    try {
      IPContentType contentType = descriptionObject instanceof SipPreview
        ? ((SipPreview) descriptionObject).getContentType() : IPContentType.getMIXED();

      SIP hungarianSip = new HungarianSIP(Controller.encodeId(descriptionObject.getId()), contentType, agentName);
      hungarianSip.addObserver(this);
      if (descriptionObject.isUpdateSIP()) {
        hungarianSip.setStatus(IPStatus.UPDATE);
      } else {
        hungarianSip.setStatus(IPStatus.NEW);
      }

      currentSipProgress = 0;
      currentSipName = descriptionObject.getTitle();
      currentAction = actionCopyingMetadata;

      for (DescriptiveMetadata descObjMetadata : descriptionObject.getMetadata()) {
        MetadataType metadataType = new MetadataType(MetadataType.MetadataTypeEnum.OTHER);

        // Check if one of the values from the enum can be used
        for (MetadataType.MetadataTypeEnum val : MetadataType.MetadataTypeEnum.values()) {
          if (descObjMetadata.getMetadataType().equalsIgnoreCase(val.getType())) {
            metadataType = new MetadataType(val);
            break;
          }
        }

        // If no value was found previously, set the Other type
        if (metadataType.getType() == MetadataType.MetadataTypeEnum.OTHER) {
          metadataType.setOtherType(descObjMetadata.getMetadataType());
        }

        Path metadataPath = null;
        if (descObjMetadata.getCreatorOption() != MetadataOption.TEMPLATE
          && descObjMetadata.getCreatorOption() != MetadataOption.NEW_FILE && !descObjMetadata.isLoaded()) {
          metadataPath = descObjMetadata.getPath();
        }

        if (metadataPath == null) {
          String content = descriptionObject.getMetadataWithReplaces(descObjMetadata);
          metadataPath = tempDir.resolve(descObjMetadata.getId());
          FileUtils.writeStringToFile(metadataPath.toFile(), content, Constants.RODAIN_DEFAULT_ENCODING);
        }

        IPFile metadataFile = new IPFile(metadataPath);
        metadataFile.setRelatedTags(descObjMetadata.getRelatedTags());
        IPDescriptiveMetadata metadata = new IPDescriptiveMetadata(descObjMetadata.getId(), metadataFile, metadataType,
          descObjMetadata.getMetadataVersion());

        hungarianSip.addDescriptiveMetadata(metadata);
      }

      currentAction = actionCopyingData;
      if (descriptionObject instanceof SipPreview) {
        SipPreview sip = (SipPreview) descriptionObject;
        for (SipRepresentation sr : sip.getRepresentations()) {
          IPRepresentation rep = new IPRepresentation(sr.getName());
          rep.setContentType(sr.getType());

          Set<TreeNode> files = sr.getFiles();
          currentSIPadded = 0;
          currentSIPsize = 0;

          // count files
          for (TreeNode tn : files) {
            currentSIPsize += tn.getFullTreePaths().size();
          }

          // add files to representation
          for (TreeNode tn : files) {
            addFileToRepresentation(tn, new ArrayList<>(), rep);
          }

          hungarianSip.addRepresentation(rep);
        }

        currentAction = I18n.t(Constants.I18N_SIMPLE_SIP_CREATOR_DOCUMENTATION);
        Set<TreeNode> docs = sip.getDocumentation();
        for (TreeNode tn : docs) {
          addDocToSip(tn, new ArrayList<>(), hungarianSip);
        }
      }

      currentAction = I18n.t(Constants.I18N_SIMPLE_SIP_CREATOR_INIT_ZIP);

      // FIXME get IP header
      IPHeader header = new IPHeader();
      hungarianSip.setHeader(header);
      Path sipPath = hungarianSip.build(outputPath, createSipName(descriptionObject, prefix, sipNameStrategy));

      createdSipsCount++;
      return new Pair(sipPath, hungarianSip);
    } catch (InterruptedException e) {
      canceled = true;
    } catch (IOException e) {
      LOGGER.error("Error accessing the files", e);
      unsuccessful.add(descriptionObject);
      CreationModalProcessing.showError(descriptionObject, e);
    } catch (Exception e) {
      LOGGER.error("Error exporting Hungarian SIP", e);
      unsuccessful.add(descriptionObject);
      CreationModalProcessing.showError(descriptionObject, e);
    }

    return null;
  }

  private void addFileToRepresentation(TreeNode tn, List<String> relativePath, IPRepresentation rep) {
    if (Files.isDirectory(tn.getPath())) {
      // add this directory to the path list
      List<String> newRelativePath = new ArrayList<>(relativePath);
      newRelativePath.add(tn.getPath().getFileName().toString());

      // recursive call to all the node's children
      for (TreeNode node : tn.getChildren().values()) {
        addFileToRepresentation(node, newRelativePath, rep);
      }
    } else {
      // if it's a file, add it to the representation
      rep.addFile(tn.getPath(), relativePath);
      currentSIPadded++;
      currentAction = String.format("%s (%d/%d)", actionCopyingData, currentSIPadded, currentSIPsize);
    }
  }

  private void addDocToSip(TreeNode tn, List<String> relativePath, SIP hungarianSip) {
    if (Files.isDirectory(tn.getPath())) {
      // add this directory to the path list
      List<String> newRelativePath = new ArrayList<>(relativePath);
      newRelativePath.add(tn.getPath().getFileName().toString());
      // recursive call to all the node's children
      for (TreeNode node : tn.getChildren().values()) {
        addDocToSip(node, newRelativePath, hungarianSip);
      }
    } else {
      // if it's a file, add it to the SIP
      IPFile fileDoc = new IPFile(tn.getPath(), relativePath);
      hungarianSip.addDocumentation(fileDoc);
    }
  }

  @Override
  public void sipBuildRepresentationsProcessingStarted(int i) {
    // do nothing
  }

  @Override
  public void sipBuildRepresentationProcessingStarted(int size) {
    repProcessingSize = size;
  }

  @Override
  public void sipBuildRepresentationProcessingCurrentStatus(int i) {
    String format = I18n.t(Constants.I18N_CREATIONMODALPROCESSING_REPRESENTATION) + " (%d/%d)";
    currentAction = String.format(format, i, repProcessingSize);
  }

  @Override
  public void sipBuildRepresentationProcessingEnded() {
    // do nothing
  }

  @Override
  public void sipBuildRepresentationsProcessingEnded() {
    // do nothing
  }

  @Override
  public void sipBuildPackagingStarted(int current) {
    countFilesOfZip = current;
  }

  @Override
  public void sipBuildPackagingCurrentStatus(int current) {
    String format = I18n.t(Constants.I18N_CREATIONMODALPROCESSING_EARK_PROGRESS);
    String progress = String.format(format, current, countFilesOfZip);
    currentAction = progress;
    currentSipProgress = ((float) current) / countFilesOfZip;
    currentSipProgress /= sipPreviewCount;
  }

  @Override
  public void sipBuildPackagingEnded() {
    currentAction = actionFinalizingSip;
    currentSipProgress = 0;
  }
}
