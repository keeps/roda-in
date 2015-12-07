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
import org.roda.rodain.rules.sip.TemplateType;
import org.roda_project.commons_ip.model.SIP;
import org.roda_project.commons_ip.model.SIPDescriptiveMetadata;
import org.roda_project.commons_ip.model.SIPRepresentation;
import org.roda_project.commons_ip.model.impl.eark.EARKSIP;
import org.roda_project.commons_ip.utils.EARKEnums;
import org.roda_project.commons_ip.utils.METSEnums;
import org.roda_project.commons_ip.utils.SIPException;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 19/11/2015.
 */
public class EarkSipCreator extends SimpleSipCreator {
  public EarkSipCreator(Path outputPath, Map<SipPreview, String> previews) {
    super(outputPath, previews);
  }

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
      SIPRepresentation rep = new SIPRepresentation("rep1");

      // for now, we need to create a temporary folder to create the metadata
      // files, since the commons-ip library only accepts Paths
      if (!Files.exists(Paths.get(home)))
        new File(home).mkdir();

      currentAction = actionCopyingMetadata;
      TemplateType templateType = sip.getTemplateType();
      METSEnums.MetadataType metadataType = METSEnums.MetadataType.OTHER;

      if (templateType != null) {
        if (templateType == TemplateType.DUBLIN_CORE) {
          metadataName = "dc.xml";
          metadataType = METSEnums.MetadataType.DC;
        } else {
          metadataName = "ead.xml";
          metadataType = METSEnums.MetadataType.EAD;
        }
      }
      FileUtils.writeStringToFile(new File(home + metadataName), sip.getMetadataContent());
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
      e.printStackTrace();
      unsuccessful.add(sip);
    } catch (IOException e) {
      e.printStackTrace();
      unsuccessful.add(sip);
    } finally {
      if (Files.exists(Paths.get(home + metadataName))) {
        new File(home + metadataName).delete();
      }
    }
  }
}