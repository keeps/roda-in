package org.roda.rodain.core.creation;

import org.apache.commons.io.FileUtils;
import org.roda.rodain.core.ConfigurationManager;
import org.roda.rodain.core.Constants;
import org.roda.rodain.core.Constants.MetadataOption;
import org.roda.rodain.core.Controller;
import org.roda.rodain.core.I18n;
import org.roda.rodain.core.Pair;
import org.roda.rodain.core.rules.TreeNode;
import org.roda.rodain.core.schema.DescriptiveMetadata;
import org.roda.rodain.core.schema.RepresentationContentType;
import org.roda.rodain.core.schema.Sip;
import org.roda.rodain.core.sip.SipPreview;
import org.roda.rodain.core.sip.SipRepresentation;
import org.roda.rodain.core.sip.naming.SIPNameBuilder;
import org.roda.rodain.ui.creation.CreationModalProcessing;
import org.roda_project.commons_ip.model.IPHeader;
import org.roda_project.commons_ip.utils.IPEnums.IPStatus;
import org.roda_project.commons_ip.utils.METSEnums;
import org.roda_project.commons_ip2.model.IPAgent;
import org.roda_project.commons_ip2.model.IPAgentNoteTypeEnum;
import org.roda_project.commons_ip2.model.IPContentInformationType;
import org.roda_project.commons_ip2.model.IPContentInformationType.IPContentInformationTypeEnum;
import org.roda_project.commons_ip2.model.IPContentType;
import org.roda_project.commons_ip2.model.IPContentType.IPContentTypeEnum;
import org.roda_project.commons_ip2.model.IPDescriptiveMetadata;
import org.roda_project.commons_ip2.model.IPFile;
import org.roda_project.commons_ip2.model.IPRepresentation;
import org.roda_project.commons_ip2.model.MetadataType;
import org.roda_project.commons_ip2.model.SIP;
import org.roda_project.commons_ip2.model.SIPObserver;
import org.roda_project.commons_ip2.model.impl.eark.EARKSIP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Hélder Silva hsilva@keep.pt
 * @since 22/10/2019.
 */
public class EarkSip2Creator extends SimpleSipCreator implements SIPObserver, SipCreator {
  private static final Logger LOGGER = LoggerFactory.getLogger(EarkSip2Creator.class.getName());
  private int countFilesOfZip;
  private int currentSIPadded = 0;
  private int currentSIPsize = 0;
  private int repProcessingSize;

  private SIPNameBuilder sipNameBuilder;
  private IPHeader ipHeader;
  private String sipAgentName;
  private String sipAgentID;

  /**
   * Creates a new EARK SIP exporter.
   *
   * @param outputPath     The path to the output folder of the SIP exportation
   * @param previews       The map with the SIPs that will be exported
   * @param createReport
   * @param sipNameBuilder
   */
  public EarkSip2Creator(Path outputPath, Map<Sip, List<String>> previews, SIPNameBuilder sipNameBuilder,
                         boolean createReport, IPHeader ipHeader, String sipAgentName, String sipAgentID) {
    super(outputPath, previews, createReport);
    this.sipNameBuilder = sipNameBuilder;
    this.ipHeader = ipHeader;
    this.sipAgentName = sipAgentName;
    this.sipAgentID = sipAgentID;
  }

  public static String getText() {
    return "E-ARK2";
  }

  public static boolean requiresMETSHeaderInfo() {
    // 20191028 hsilva: postponing this dev/adaptation
    return false;
  }

  public static List<org.roda.rodain.core.schema.IPContentType> ipSpecificContentTypes() {
    List<org.roda.rodain.core.schema.IPContentType> res = new ArrayList<>();
    for (IPContentTypeEnum ipContentTypeEnum : IPContentTypeEnum.values()) {
      res.add(new org.roda.rodain.core.schema.IPContentType(getText(), ipContentTypeEnum.toString()));
    }
    return res;
  }

  public static List<RepresentationContentType> representationSpecificContentTypes() {
    List<RepresentationContentType> res = new ArrayList<>();
    for (IPContentInformationTypeEnum ipContentInformationTypeEnum : IPContentInformationTypeEnum.values()) {
      res.add(new RepresentationContentType(getText(), ipContentInformationTypeEnum.toString()));
    }
    return res;
  }

  /**
   * Attempts to create an EARK SIP of each SipPreview
   */
  @Override
  public void run() {
    Map<Path, Object> sips = new HashMap<>();
    for (Sip preview : previews.keySet()) {
      if (canceled) {
        break;
      }
      Pair pathSIP = createEarkSip(preview);
      if (pathSIP != null) {
        sips.put((Path) pathSIP.getKey(), (SIP) pathSIP.getValue());
      }
    }
    if (createReport) {
      createReport(sips);
    }
    currentAction = I18n.t(Constants.I18N_DONE);
  }

  private Pair createEarkSip(Sip descriptionObject) {
    Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
    try {
      org.roda.rodain.core.schema.IPContentType userDefinedContentType = descriptionObject instanceof SipPreview
              ? ((SipPreview) descriptionObject).getContentType()
              : org.roda.rodain.core.schema.IPContentType.defaultIPContentType();

      SIP earkSip = new EARKSIP(Controller.encodeId(descriptionObject.getId()),
              new IPContentType(userDefinedContentType.getValue()), new IPContentInformationType(Constants.SIP_DEFAULT_CONTENT_TYPE));
      if (IPContentTypeEnum.OTHER == earkSip.getContentType().getType()) {
        earkSip.getContentType().setOtherType(userDefinedContentType.getOtherValue());
      }

      earkSip.addObserver(this);
      earkSip.setAncestors(previews.get(descriptionObject));
      if (descriptionObject.isUpdateSIP()) {
        earkSip.setStatus(IPStatus.UPDATE);
      } else {
        earkSip.setStatus(IPStatus.NEW);
      }

      currentSipProgress = 0;
      currentSipName = descriptionObject.getTitle();
      currentAction = actionCopyingMetadata;

      for (DescriptiveMetadata descObjMetadata : descriptionObject.getMetadata()) {
        MetadataType metadataType = new MetadataType(MetadataType.MetadataTypeEnum.OTHER);

        Path schemaPath = ConfigurationManager.getSchemaPath(descObjMetadata.getTemplateType());
        if (schemaPath != null) {
          earkSip.addSchema(new IPFile(schemaPath));
        }

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
        IPDescriptiveMetadata metadata = new IPDescriptiveMetadata(descObjMetadata.getId(), metadataFile, metadataType,
                descObjMetadata.getMetadataVersion());

        earkSip.addDescriptiveMetadata(metadata);
      }

      currentAction = actionCopyingData;
      if (descriptionObject instanceof SipPreview) {
        SipPreview sip = (SipPreview) descriptionObject;
        for (SipRepresentation sr : sip.getRepresentations()) {
          IPRepresentation rep = new IPRepresentation(sr.getName());
          setContentTypeAndContentInformationType(earkSip, rep, sr);

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

          earkSip.addRepresentation(rep);
        }

        currentAction = I18n.t(Constants.I18N_SIMPLE_SIP_CREATOR_DOCUMENTATION);
        Set<TreeNode> docs = sip.getDocumentation();
        for (TreeNode tn : docs) {
          addDocToSip(tn, new ArrayList<>(), earkSip);
        }
      }

      setHeader(earkSip);
      earkSip.addCreatorSoftwareAgent(Constants.SIP_DEFAULT_AGENT_NAME,
              Controller.getCurrentVersionSilently().orElse(Constants.SIP_AGENT_VERSION_UNKNOWN));
      earkSip.addAgent(new IPAgent(sipAgentName, Constants.SIP_AGENT_ROLE_OTHER, Constants.SIP_AGENT_OTHERROLE_SUBMITTER, METSEnums.CreatorType.INDIVIDUAL, null, sipAgentID, IPAgentNoteTypeEnum.IDENTIFICATIONCODE));

      currentAction = I18n.t(Constants.I18N_SIMPLE_SIP_CREATOR_INIT_ZIP);
      Path sipPath = earkSip.build(outputPath, createSipName(descriptionObject, sipNameBuilder));

      createdSipsCount++;
      return new Pair(sipPath, earkSip);
    } catch (InterruptedException e) {
      canceled = true;
    } catch (IOException e) {
      LOGGER.error("Error accessing the files", e);
      unsuccessful.add(descriptionObject);
      CreationModalProcessing.showError(descriptionObject, e);
    } catch (Exception e) {
      LOGGER.error("Error exporting E-ARK SIP", e);
      unsuccessful.add(descriptionObject);
      CreationModalProcessing.showError(descriptionObject, e);
    }

    return null;
  }

  private void setContentTypeAndContentInformationType(SIP earkSip, IPRepresentation rep, SipRepresentation sr) {
    rep.setContentType(earkSip.getContentType());
    IPContentInformationType contentInformationType = new IPContentInformationType(sr.getType().getValue());
    if (contentInformationType.getType() == IPContentInformationTypeEnum.OTHER) {
      contentInformationType.setOtherType(sr.getType().getOtherValue());
    }
    rep.setContentInformationType(contentInformationType);
  }

  private void setHeader(SIP earkSip) {
    // 20191028 hsilva: see requiresMETSHeaderInfo()
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

  private void addDocToSip(TreeNode tn, List<String> relativePath, SIP earkSip) {
    if (Files.isDirectory(tn.getPath())) {
      // add this directory to the path list
      List<String> newRelativePath = new ArrayList<>(relativePath);
      newRelativePath.add(tn.getPath().getFileName().toString());
      // recursive call to all the node's children
      for (TreeNode node : tn.getChildren().values()) {
        addDocToSip(node, newRelativePath, earkSip);
      }
    } else {
      // if it's a file, add it to the SIP
      IPFile fileDoc = new IPFile(tn.getPath(), relativePath);
      earkSip.addDocumentation(fileDoc);
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
