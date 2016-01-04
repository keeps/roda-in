package org.roda.rodain.creation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.roda.rodain.rules.TreeNode;
import org.roda.rodain.rules.sip.SipPreview;
import org.roda_project.commons_ip.model.SIP;
import org.roda_project.commons_ip.model.SIPDescriptiveMetadata;
import org.roda_project.commons_ip.model.SIPRepresentation;
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
public class EarkSipCreator extends SimpleSipCreator {
  private static final Logger log = LoggerFactory.getLogger(EarkSipCreator.class.getName());

  /**
   * Creates a new EARK SIP exporter.
   *
   * @param outputPath The path to the output folder of the SIP exportation
   * @param previews   The map with the SIPs that will be exported
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
    String home = System.getProperty("user.home") + "/.roda-in/";
    String metadataName = "metadata.xml";
    try {
      SIP earkSip = new EARKSIP(sip.getId(), EARKEnums.ContentType.mixed, "RODA-In");
      earkSip.setParent(schemaId);
      SIPRepresentation rep = new SIPRepresentation("rep1");

      // for now, we need to create a temporary folder to create the metadata
      // files, since the commons-ip library only accepts Paths
      if (!Files.exists(Paths.get(home)))
        new File(home).mkdir();

      currentAction = actionCopyingMetadata;
      String templateType = sip.getTemplateType();
      METSEnums.MetadataType metadataType = METSEnums.MetadataType.OTHER;

      if (templateType != null) {
        if (templateType.equals("dc")) {
          metadataName = "dc.xml";
          metadataType = METSEnums.MetadataType.DC;
        } else if (templateType.equals("ead")) {
          metadataName = "ead.xml";
          metadataType = METSEnums.MetadataType.EAD;
        } else {
          metadataName = "custom.xml";
          metadataType = METSEnums.MetadataType.OTHER;
        }
      }

      String content = sip.getMetadataContent();

      FileUtils.writeStringToFile(new File(home + metadataName), content);
      SIPDescriptiveMetadata metadata = new SIPDescriptiveMetadata(Paths.get(home + metadataName), null, metadataType);
      earkSip.addDescriptiveMetadata(metadata);

      currentAction = actionCopyingData;
      for (TreeNode tn : sip.getFiles()) {
        Set<String> paths = tn.getFullTreePaths();
        for (String pat : paths) {
          Path path = Paths.get(pat);
          if (!Files.isDirectory(path))
            rep.addData(path);
        }
      }

      earkSip.addRepresentation(rep);

      currentAction = actionFinalizingSip;
      Path result = earkSip.build();
      Files.move(result, outputPath.resolve(result.getFileName()));
      createdSipsCount++;
    } catch (SIPException e) {
      log.error("Commons IP exception", e);
      unsuccessful.add(sip);
    } catch (IOException e) {
      log.error("Error accessing the files", e);
      unsuccessful.add(sip);
    } finally {
      if (Files.exists(Paths.get(home + metadataName))) {
        new File(home + metadataName).delete();
      }
    }
  }
}
