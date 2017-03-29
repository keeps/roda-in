package org.roda.rodain.core.creation;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.roda.rodain.core.Constants;
import org.roda.rodain.core.Constants.SipNameStrategy;
import org.roda.rodain.core.Controller;
import org.roda.rodain.core.I18n;
import org.roda.rodain.core.Pair;
import org.roda.rodain.core.rules.TreeNode;
import org.roda.rodain.core.schema.Sip;
import org.roda.rodain.core.sip.SipPreview;
import org.roda.rodain.core.sip.SipRepresentation;
import org.roda.rodain.ui.creation.CreationModalProcessing;
import org.roda_project.commons_ip.model.IPConstants;
import org.roda_project.commons_ip.model.IPContentType;
import org.roda_project.commons_ip.model.IPRepresentation;
import org.roda_project.commons_ip.model.SIP;
import org.roda_project.commons_ip.model.SIPObserver;
import org.roda_project.commons_ip.model.impl.bagit.BagitSIP;
import org.roda_project.commons_ip.model.impl.bagit.BagitUtils;
import org.roda_project.commons_ip.utils.IPEnums.IPStatus;
import org.roda_project.commons_ip.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 19/11/2015.
 */
public class BagitSipCreator extends SimpleSipCreator implements SIPObserver {
  private static final Logger LOGGER = LoggerFactory.getLogger(BagitSipCreator.class.getName());
  private int countFilesOfZip;
  private int currentSIPsize = 0;
  private int currentSIPadded = 0;
  private int repProcessingSize;

  private String prefix;
  private SipNameStrategy sipNameStrategy;

  /**
   * Creates a new BagIt exporter.
   *
   * @param outputPath
   *          The path to the output folder of the SIP exportation
   * @param previews
   *          The map with the SIPs that will be exported
   */
  public BagitSipCreator(Path outputPath, Map<Sip, List<String>> previews, boolean createReport, String prefix,
    SipNameStrategy sipNameStrategy) {
    super(outputPath, previews, createReport);
    this.prefix = prefix;
    this.sipNameStrategy = sipNameStrategy;
  }

  /**
   * Attempts to create a BagIt SIP of each SipPreview
   */
  @Override
  public void run() {
    Map<Path, Object> sips = new HashMap<>();
    for (Sip preview : previews.keySet()) {
      if (canceled) {
        break;
      }
      Pair pathBag = createBagit(preview);
      if (pathBag != null) {
        sips.put((Path) pathBag.getKey(), (SIP) pathBag.getValue());
      }
    }

    if (createReport) {
      createReport(sips);
    }

    currentAction = I18n.t(Constants.I18N_DONE);
  }

  private Pair createBagit(Sip descriptionObject) {
    Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
    // we add a timestamp to the beginning of the SIP name to avoid same name
    // conflicts
    currentSipName = descriptionObject.getTitle();
    currentAction = actionCreatingFolders;

    IPContentType contentType = descriptionObject instanceof SipPreview
      ? ((SipPreview) descriptionObject).getContentType() : IPContentType.getMIXED();

    SIP bagit = new BagitSIP(Controller.urlEncode(descriptionObject.getId()), contentType, agentName);
    bagit.addObserver(this);
    bagit.setStatus(IPStatus.NEW);

    try {
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

          for (TreeNode tn : files) {
            addFileToRepresentation(tn, new ArrayList<>(), rep);
          }

          bagit.addRepresentation(rep);
        }
      }

      bagit.setAncestors(Arrays.asList(descriptionObject.getParentId()));
      Map<String, String> metadataMap = new HashMap<>();
      metadataMap.put(IPConstants.BAGIT_ID, descriptionObject.getId());
      metadataMap.put(IPConstants.BAGIT_TITLE, descriptionObject.getTitle());
      metadataMap.put(IPConstants.BAGIT_LEVEL, IPConstants.BAGIT_ITEM_LEVEL);

      Map<String, String> list = descriptionObject.getMetadataWithReplaces();
      if (!list.isEmpty()) {
        list.forEach((id, content) -> metadataMap.put(Constants.CONF_K_PREFIX_METADATA + id, content));
      }

      Path metadataPath = tempDir.resolve(Utils.generateRandomAndPrefixedUUID());
      bagit.addDescriptiveMetadata(BagitUtils.createBagitMetadata(metadataMap, bagit.getAncestors(), metadataPath));

      currentAction = actionFinalizingSip;
      Path name = bagit.build(outputPath, createSipName(descriptionObject, prefix, sipNameStrategy));
      createdSipsCount++;
      return new Pair(name, bagit);
    } catch (Exception e) {
      LOGGER.error("Error creating SIP", e);
      unsuccessful.add(descriptionObject);
      CreationModalProcessing.showError(descriptionObject, e);
      return null;
    }
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

  @Override
  public void sipBuildRepresentationsProcessingStarted(int current) {
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
