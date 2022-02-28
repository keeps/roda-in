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
import org.roda.rodain.core.ConfigurationManager;
import org.roda.rodain.core.Constants;
import org.roda.rodain.core.Controller;
import org.roda.rodain.core.I18n;
import org.roda.rodain.core.Pair;
import org.roda.rodain.core.rules.TreeNode;
import org.roda.rodain.core.schema.DescriptiveMetadata;
import org.roda.rodain.core.schema.Sip;
import org.roda.rodain.core.sip.SipPreview;
import org.roda.rodain.core.sip.SipRepresentation;
import org.roda.rodain.core.sip.naming.SIPNameBuilder;
import org.roda.rodain.core.sip.naming.SIPNameBuilderEARK2;
import org.roda.rodain.core.sip.naming.SIPNameBuilderSIPS;
import org.roda.rodain.ui.creation.CreationModalProcessing;
import org.roda_project.commons_ip.model.IPHeader;
import org.roda_project.commons_ip.utils.IPEnums;
import org.roda_project.commons_ip.utils.METSEnums;
import org.roda_project.commons_ip2.model.IPAgent;
import org.roda_project.commons_ip2.model.IPAgentNoteTypeEnum;
import org.roda_project.commons_ip2.model.IPContentInformationType;
import org.roda_project.commons_ip2.model.IPContentType;
import org.roda_project.commons_ip2.model.IPDescriptiveMetadata;
import org.roda_project.commons_ip2.model.IPFile;
import org.roda_project.commons_ip2.model.IPRepresentation;
import org.roda_project.commons_ip2.model.MetadataType;
import org.roda_project.commons_ip2.model.SIP;
import org.roda_project.commons_ip2.model.SIPObserver;
import org.roda_project.commons_ip2.model.impl.eark.EARKSIP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@author João Gomes <jgomes@keep.pt>}.
 */
public abstract class SipCreator extends SimpleSipCreator implements SIPObserver, ISipCreator {
  /**
   * {@link LoggerFactory}.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(SipCreator.class.getName());

  /**
   * Number of files in ZIP.
   */
  private int countFilesOfZip;

  /**
   * Number of SIP's added.
   */
  private int currentSIPadded = 0;

  /**
   * Size of current SIP.
   */
  private int currentSIPsize = 0;

  /**
   * Size of representations processing.
   */
  private int repProcessingSize;

  /**
   * {@link SIPNameBuilder}.
   */
  private SIPNameBuilder sipNameBuilder;

  /**
   * {@link IPHeader}.
   */
  private IPHeader ipHeader;

  /**
   * Name of creator Agent.
   */
  private String sipAgentName;

  /**
   * Identification code of creator Agent.
   */
  private String sipAgentID;

  /**
   * Creates a new SIP exporter.
   * 
   * @param outputPath
   *          The path to the output folder of the SIP exportation.
   * @param previews
   *          The map with the SIPs that will be exported.
   * @param sipNameBuilder
   *          the {@link SIPNameBuilderEARK2} or {@link SIPNameBuilderSIPS}.
   * @param createReport
   *          a flag to create Report or not.
   * @param ipHeader
   *          the {@link IPHeader}.
   * @param sipAgentName
   *          the creator agent name.
   * @param sipAgentID
   *          the creator agent identification code.
   */
  protected SipCreator(final Path outputPath, final Map<Sip, List<String>> previews,
    final SIPNameBuilder sipNameBuilder, final boolean createReport, final IPHeader ipHeader, final String sipAgentName,
    final String sipAgentID) {
    super(outputPath, previews, createReport);
    this.sipNameBuilder = sipNameBuilder;
    this.ipHeader = ipHeader;
    this.sipAgentName = sipAgentName;
    this.sipAgentID = sipAgentID;
  }

  /**
   * 20191028 hsilva: postponing this dev/adaptation.
   * 
   * @return a flag always false;
   */
  public static boolean requiresMETSHeaderInfo() {
    return false;
  }

  /**
   * Attempts to create an EARK SIP of each SipPreview.
   */
  @Override
  public void run() {
    final Map<Path, Object> sips = new HashMap<>();
    for (Sip preview : previews.keySet()) {
      if (canceled) {
        break;
      }
      final Pair pathSIP = createEarkSip(preview);
      if (pathSIP != null) {
        sips.put((Path) pathSIP.getKey(), (SIP) pathSIP.getValue());
      }
    }
    if (createReport) {
      createReport(sips);
    }
    currentAction = I18n.t(Constants.I18N_DONE);
  }

  protected Pair createEarkSip(final Sip descriptionObject) {
    final Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
    try {
      final org.roda.rodain.core.schema.IPContentType userDefinedContentType = descriptionObject instanceof SipPreview
        ? ((SipPreview) descriptionObject).getContentType()
        : org.roda.rodain.core.schema.IPContentType.defaultIPContentType();

      final SIP earkSip = new EARKSIP(Controller.encodeId(descriptionObject.getId()),
        new IPContentType(userDefinedContentType.getValue()),
        new IPContentInformationType(Constants.SIP_DEFAULT_CONTENT_TYPE));
      if (IPContentType.IPContentTypeEnum.OTHER == earkSip.getContentType().getType()) {
        earkSip.getContentType().setOtherType(userDefinedContentType.getOtherValue());
      }

      earkSip.addObserver(this);
      earkSip.setAncestors(previews.get(descriptionObject));
      if (descriptionObject.isUpdateSIP()) {
        earkSip.setStatus(IPEnums.IPStatus.UPDATE);
      } else {
        earkSip.setStatus(IPEnums.IPStatus.NEW);
      }

      currentSipProgress = 0;
      currentSipName = descriptionObject.getTitle();
      currentAction = actionCopyingMetadata;

      for (DescriptiveMetadata descObjMetadata : descriptionObject.getMetadata()) {
        MetadataType metadataType = new MetadataType(MetadataType.MetadataTypeEnum.OTHER);

        final Path schemaPath = ConfigurationManager.getSchemaPath(descObjMetadata.getTemplateType());
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
        if (descObjMetadata.getCreatorOption() != Constants.MetadataOption.TEMPLATE
          && descObjMetadata.getCreatorOption() != Constants.MetadataOption.NEW_FILE && !descObjMetadata.isLoaded()) {
          metadataPath = descObjMetadata.getPath();
        }

        if (metadataPath == null) {
          final String content = descriptionObject.getMetadataWithReplaces(descObjMetadata);
          metadataPath = tempDir.resolve(descObjMetadata.getId());
          FileUtils.writeStringToFile(metadataPath.toFile(), content, Constants.RODAIN_DEFAULT_ENCODING);
        }

        final IPFile metadataFile = new IPFile(metadataPath);
        final IPDescriptiveMetadata metadata = new IPDescriptiveMetadata(descObjMetadata.getId(), metadataFile,
          metadataType, descObjMetadata.getMetadataVersion());

        earkSip.addDescriptiveMetadata(metadata);
      }

      currentAction = actionCopyingData;
      if (descriptionObject instanceof SipPreview) {
        final SipPreview sip = (SipPreview) descriptionObject;
        for (SipRepresentation sr : sip.getRepresentations()) {
          final IPRepresentation rep = new IPRepresentation(sr.getName());
          setContentTypeAndContentInformationType(earkSip, rep, sr);

          final Set<TreeNode> files = sr.getFiles();
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
        final Set<TreeNode> docs = sip.getDocumentation();
        for (TreeNode tn : docs) {
          addDocToSip(tn, new ArrayList<>(), earkSip);
        }
      }

      setHeader(earkSip);
      earkSip.addCreatorSoftwareAgent(Constants.SIP_DEFAULT_AGENT_NAME,
        Controller.getCurrentVersionSilently().orElse(Constants.SIP_AGENT_VERSION_UNKNOWN));
      earkSip.addAgent(new IPAgent(sipAgentName, "CREATOR", null, METSEnums.CreatorType.INDIVIDUAL, null, sipAgentID,
        IPAgentNoteTypeEnum.IDENTIFICATIONCODE));

      currentAction = I18n.t(Constants.I18N_SIMPLE_SIP_CREATOR_INIT_ZIP);
      final Path sipPath = earkSip.build(outputPath, createSipName(descriptionObject, sipNameBuilder),
        sipNameBuilder instanceof SIPNameBuilderSIPS ? IPEnums.SipType.EARK2S : IPEnums.SipType.EARK2);

      createdSipsCount++;
      return new Pair(sipPath, earkSip);
    } catch (final InterruptedException e) {
      canceled = true;
    } catch (final IOException e) {
      LOGGER.error("Error accessing the files", e);
      unsuccessful.add(descriptionObject);
      CreationModalProcessing.showError(descriptionObject, e);
    } catch (final Exception e) {
      LOGGER.error("Error exporting E-ARK SIP", e);
      unsuccessful.add(descriptionObject);
      CreationModalProcessing.showError(descriptionObject, e);
    }

    return null;
  }

  protected void setContentTypeAndContentInformationType(final SIP earkSip, final IPRepresentation rep,
    final SipRepresentation sr) {
    rep.setContentType(earkSip.getContentType());
    final IPContentInformationType contentInformationType = new IPContentInformationType(sr.getType().getValue());
    if (contentInformationType.getType() == IPContentInformationType.IPContentInformationTypeEnum.OTHER) {
      contentInformationType.setOtherType(sr.getType().getOtherValue());
    }
    rep.setContentInformationType(contentInformationType);
  }

  protected void setHeader(final SIP earkSip) {
    // 20191028 hsilva: see requiresMETSHeaderInfo()
  }

  protected abstract void addFileToRepresentation(TreeNode tn, List<String> relativePath, IPRepresentation rep);

  protected void addDocToSip(final TreeNode tn, final List<String> relativePath, final SIP earkSip) {
    if (Files.isDirectory(tn.getPath())) {
      // add this directory to the path list
      final List<String> newRelativePath = new ArrayList<>(relativePath);
      newRelativePath.add(tn.getPath().getFileName().toString());
      // recursive call to all the node's children
      for (TreeNode node : tn.getChildren().values()) {
        addDocToSip(node, newRelativePath, earkSip);
      }
    } else {
      // if it's a file, add it to the SIP
      final IPFile fileDoc = new IPFile(tn.getPath(), relativePath);
      earkSip.addDocumentation(fileDoc);
    }
  }

  @Override
  public void sipBuildRepresentationsProcessingStarted(final int i) {
    // do nothing
  }

  @Override
  public void sipBuildRepresentationProcessingStarted(final int size) {
    repProcessingSize = size;
  }

  @Override
  public void sipBuildRepresentationProcessingCurrentStatus(final int i) {
    final String format = I18n.t(Constants.I18N_CREATIONMODALPROCESSING_REPRESENTATION) + " (%d/%d)";
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
  public void sipBuildPackagingStarted(final int current) {
    countFilesOfZip = current;
  }

  @Override
  public void sipBuildPackagingCurrentStatus(final int current) {
    final String format = I18n.t(Constants.I18N_CREATIONMODALPROCESSING_EARK_PROGRESS);
    currentAction = String.format(format, current, countFilesOfZip);
    currentSipProgress = ((float) current) / countFilesOfZip;
    currentSipProgress /= sipPreviewCount;
  }

  @Override
  public void sipBuildPackagingEnded() {
    currentAction = actionFinalizingSip;
    currentSipProgress = 0;
  }

  public int getCountFilesOfZip() {
    return countFilesOfZip;
  }

  public void setCountFilesOfZip(final int countFilesOfZip) {
    this.countFilesOfZip = countFilesOfZip;
  }

  public int getCurrentSIPadded() {
    return currentSIPadded;
  }

  public void setCurrentSIPadded(final int currentSIPadded) {
    this.currentSIPadded = currentSIPadded;
  }

  public int getCurrentSIPsize() {
    return currentSIPsize;
  }

  public void setCurrentSIPsize(final int currentSIPsize) {
    this.currentSIPsize = currentSIPsize;
  }

  public int getRepProcessingSize() {
    return repProcessingSize;
  }

  public void setRepProcessingSize(final int repProcessingSize) {
    this.repProcessingSize = repProcessingSize;
  }

  public SIPNameBuilder getSipNameBuilder() {
    return sipNameBuilder;
  }

  public void setSipNameBuilder(final SIPNameBuilder sipNameBuilder) {
    this.sipNameBuilder = sipNameBuilder;
  }

  public IPHeader getIpHeader() {
    return ipHeader;
  }

  public void setIpHeader(final IPHeader ipHeader) {
    this.ipHeader = ipHeader;
  }

  public String getSipAgentName() {
    return sipAgentName;
  }

  public void setSipAgentName(final String sipAgentName) {
    this.sipAgentName = sipAgentName;
  }

  public String getSipAgentID() {
    return sipAgentID;
  }

  public void setSipAgentID(final String sipAgentID) {
    this.sipAgentID = sipAgentID;
  }

}
