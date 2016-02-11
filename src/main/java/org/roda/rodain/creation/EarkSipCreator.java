package org.roda.rodain.creation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.roda.rodain.core.AppProperties;
import org.roda.rodain.rules.TreeNode;
import org.roda.rodain.rules.sip.SipPreview;
import org.roda_project.commons_ip.model.*;
import org.roda_project.commons_ip.model.impl.eark.EARKSIP;
import org.roda_project.commons_ip.utils.EARKEnums;
import org.roda_project.commons_ip.utils.METSEnums;
import org.roda_project.commons_ip.utils.SIPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 19/11/2015.
 */
public class EarkSipCreator extends SimpleSipCreator implements SIPObserver {
  private static final Logger log = LoggerFactory.getLogger(EarkSipCreator.class.getName());
  private int countFilesOfZip;

  /**
   * Creates a new EARK SIP exporter.
   *
   * @param outputPath
   *          The path to the output folder of the SIP exportation
   * @param previews
   *          The map with the SIPs that will be exported
   */
  public EarkSipCreator(Path outputPath, Map<SipPreview, String> previews) {
    super(outputPath, previews);
  }

  /**
   * Attempts to create an EARK SIP of each SipPreview
   */
  @Override
  public void run() {
    for (SipPreview preview : previews.keySet()) {
      if (canceled) {
        break;
      }
      createEarkSip(previews.get(preview), preview);
    }
  }

  private void createEarkSip(String schemaId, SipPreview sip) {
    Path rodainPath = AppProperties.rodainPath;
    String metadataName = "metadata.xml";
    try {
      SIP earkSip = new EARKSIP(sip.getId(), EARKEnums.ContentType.mixed, "RODA-In");
      earkSip.addObserver(this);
      earkSip.setParent(schemaId);
      IPRepresentation rep = new IPRepresentation("rep1");

      currentSipProgress = 0;
      currentSipName = sip.getName();
      currentAction = actionCopyingMetadata;
      String templateType = sip.getTemplateType();
      METSEnums.MetadataType metadataType = METSEnums.MetadataType.OTHER;

      if (templateType != null) {
        if (templateType.equals("dc")) {
          metadataName = "dc.xml";
          metadataType = METSEnums.MetadataType.DC;
        } else if (templateType.startsWith("ead")) {
          metadataName = "ead.xml";
          metadataType = METSEnums.MetadataType.EAD;
        } else {
          metadataName = "custom.xml";
          metadataType = METSEnums.MetadataType.OTHER;
        }
      }

      String content = sip.getMetadataContent();

      FileUtils.writeStringToFile(rodainPath.resolve(metadataName).toFile(), content);
      IPFile metadataFile = new IPFile(rodainPath.resolve(metadataName));
      IPDescriptiveMetadata metadata = new IPDescriptiveMetadata(metadataFile, metadataType, sip.getMetadataVersion());
      earkSip.addDescriptiveMetadata(metadata);

      currentAction = actionCopyingData;
      for (TreeNode tn : sip.getFiles()) {
        addFileToRepresentation(tn, new ArrayList<>(), rep);
      }

      earkSip.addRepresentation(rep);

      earkSip.build(outputPath);

      createdSipsCount++;
    } catch (SIPException e) {
      log.error("Commons IP exception", e);
      unsuccessful.add(sip);
    } catch (IOException e) {
      log.error("Error accessing the files", e);
      unsuccessful.add(sip);
    }
  }

  private void addFileToRepresentation(TreeNode tn, List<String> relativePath, IPRepresentation rep) {
    if (Files.isDirectory(tn.getPath())) {
      // add this directory to the path list
      List<String> newRelativePath = new ArrayList<>(relativePath);
      newRelativePath.add(tn.getPath().getFileName().toString());
      // recursive call to all the node's children
      for (TreeNode node : tn.getAllFiles().values()) {
        addFileToRepresentation(node, newRelativePath, rep);
      }
    } else {
      // if it's a file, add it to the representation
      rep.addFile(tn.getPath(), relativePath);
    }
  }

  @Override
  public void sipBuildStarted(int current) {
    countFilesOfZip = current;
  }

  @Override
  public void sipBuildCurrentStatus(int current) {
    String format = AppProperties.getLocalizedString("CreationModalProcessing.eark.progress");
    String progress = String.format(format, current, countFilesOfZip);
    currentAction = progress;
    currentSipProgress = ((float) current) / countFilesOfZip;
    currentSipProgress /= sipPreviewCount;
  }

  @Override
  public void sipBuildEnded() {
    currentAction = actionFinalizingSip;
  }
}
