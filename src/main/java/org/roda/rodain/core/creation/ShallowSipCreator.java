package org.roda.rodain.core.creation;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.roda.rodain.core.rules.TreeNode;
import org.roda.rodain.core.schema.RepresentationContentType;
import org.roda.rodain.core.schema.Sip;
import org.roda.rodain.core.shallowSipManager.ShallowSipUriCreator;
import org.roda.rodain.core.shallowSipManager.UriCreator;
import org.roda.rodain.core.sip.naming.SIPNameBuilder;
import org.roda.rodain.core.sip.naming.SIPNameBuilderSIPS;
import org.roda_project.commons_ip.model.IPHeader;
import org.roda_project.commons_ip.utils.IPException;
import org.roda_project.commons_ip2.mets_v1_12.beans.FileType;
import org.roda_project.commons_ip2.model.IPConstants;
import org.roda_project.commons_ip2.model.IPContentInformationType;
import org.roda_project.commons_ip2.model.IPContentType;
import org.roda_project.commons_ip2.model.IPFile;
import org.roda_project.commons_ip2.model.IPFileInterface;
import org.roda_project.commons_ip2.model.IPFileShallow;
import org.roda_project.commons_ip2.model.IPRepresentation;
import org.roda_project.commons_ip2.utils.METSUtils;
import org.roda_project.commons_ip2.utils.ZIPUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@author João Gomes <jgomes@keep.pt>}.
 */
public class ShallowSipCreator extends SipCreator {
  /**
   * {@link LoggerFactory}.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(ShallowSipCreator.class.getName());

  /**
   * Creates a new Shallow SIP exporter.
   *
   * @param outputPath
   *          The path to the output folder of the SIP exportation.
   * @param previews
   *          The map with the SIPs that will be exported.
   * @param sipNameBuilder
   *          the {@link SIPNameBuilderSIPS}.
   * @param createReport
   *          a flag to create Report or not.
   * @param ipHeader
   *          the {@link IPHeader}.
   * @param sipAgentName
   *          the creator agent name.
   * @param sipAgentID
   *          the creator agent identification code.
   */
  public ShallowSipCreator(final Path outputPath, final Map<Sip, List<String>> previews,
    final SIPNameBuilder sipNameBuilder, final boolean createReport, final IPHeader ipHeader, final String sipAgentName,
    final String sipAgentID) {
    super(outputPath, previews, sipNameBuilder, createReport, ipHeader, sipAgentName, sipAgentID);
  }

  public static String getText() {
    return "EARK2-S";
  }

  /**
   * @return {@link List} of {@link IPContentType}.
   */
  public static List<org.roda.rodain.core.schema.IPContentType> ipSpecificContentTypes() {
    final List<org.roda.rodain.core.schema.IPContentType> res = new ArrayList<>();
    for (IPContentType.IPContentTypeEnum ipContentTypeEnum : IPContentType.IPContentTypeEnum.values()) {
      res.add(new org.roda.rodain.core.schema.IPContentType(getText(), ipContentTypeEnum.toString()));
    }
    return res;
  }

  /**
   *
   * @return a {@link List} of {@link RepresentationContentType}.
   */
  public static List<RepresentationContentType> representationSpecificContentTypes() {
    final List<RepresentationContentType> res = new ArrayList<>();
    for (IPContentInformationType.IPContentInformationTypeEnum ipContentInformationTypeEnum : IPContentInformationType.IPContentInformationTypeEnum
      .values()) {
      res.add(new RepresentationContentType(getText(), ipContentInformationTypeEnum.toString()));
    }
    return res;
  }

  @Override
  protected void addFileToRepresentation(final TreeNode tn, final List<String> relativePath,
    final IPRepresentation rep) {
    if (Files.isDirectory(tn.getPath())) {
      // add this directory to the path list
      final List<String> newRelativePath = new ArrayList<>(relativePath);
      newRelativePath.add(tn.getPath().getFileName().toString());

      if (tn.getChildren().values().isEmpty()) {
        rep.addFile(new IPFileShallow(newRelativePath));
      }

      // recursive call to all the node's children
      for (TreeNode node : tn.getChildren().values()) {
        addFileToRepresentation(node, newRelativePath, rep);
      }
    } else {
      // if it's a file, add it to the representation
      final UriCreator uriCreator = new ShallowSipUriCreator();
      final Optional<URI> uri = uriCreator.convertPathToUri(tn.getPath());
      final FileType filetype = createFileType(tn.getPath());
      final IPFileInterface representationFile;
      if (uri.isPresent()) {
        representationFile = new IPFileShallow(uri.get(), filetype);
        ((IPFileShallow) representationFile).setRelativeFolders(relativePath);
      } else {
        representationFile = new IPFile(tn.getPath(), relativePath);
      }
      rep.addFile(representationFile);
      setCurrentSIPadded(getCurrentSIPadded() + 1);
      currentAction = String.format("%s (%d/%d)", actionCopyingData, getCurrentSIPadded(), getCurrentSIPsize());
    }
  }

  private FileType createFileType(final Path path) {
    final FileType filetype = new FileType();

    try {
      METSUtils.setFileBasicInformation(LOGGER, path, filetype);
    } catch (IPException | InterruptedException e) {
      // do nothing
    }

    final String checksumType = IPConstants.CHECKSUM_ALGORITHM;
    final Set<String> checksumAlgorithms = new HashSet<>();
    checksumAlgorithms.add(checksumType);
    try (InputStream inputStream = Files.newInputStream(path)) {
      final Map<String, String> checksums = ZIPUtils.calculateChecksums(Optional.empty(), inputStream,
        checksumAlgorithms);
      final String checksum = checksums.get(checksumType);
      filetype.setCHECKSUM(checksum);
      filetype.setCHECKSUMTYPE(checksumType);
    } catch (NoSuchAlgorithmException | IOException e) {
      // do nothing
    }
    return filetype;
  }
}
