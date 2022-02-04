package org.roda.rodain.core.creation;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.roda.rodain.core.rules.TreeNode;
import org.roda.rodain.core.schema.RepresentationContentType;
import org.roda.rodain.core.schema.Sip;
import org.roda.rodain.core.sip.naming.SIPNameBuilder;
import org.roda.rodain.core.sip.naming.SIPNameBuilderEARK2;
import org.roda_project.commons_ip.model.IPHeader;
import org.roda_project.commons_ip2.model.IPContentInformationType.IPContentInformationTypeEnum;
import org.roda_project.commons_ip2.model.IPContentType;
import org.roda_project.commons_ip2.model.IPContentType.IPContentTypeEnum;
import org.roda_project.commons_ip2.model.IPRepresentation;

/**
 * @author Hélder Silva hsilva@keep.pt
 * @since 22/10/2019.
 */
public class EarkSip2Creator extends SipCreator {

  /**
   * Creates a new EARK SIP 2 exporter.
   *
   * @param outputPath
   *          The path to the output folder of the SIP exportation.
   * @param previews
   *          The map with the SIPs that will be exported.
   * @param sipNameBuilder
   *          the {@link SIPNameBuilderEARK2}.
   * @param createReport
   *          a flag to create Report or not.
   * @param ipHeader
   *          the {@link IPHeader}.
   * @param sipAgentName
   *          the creator agent name.
   * @param sipAgentID
   *          the creator agent identification code.
   */
  public EarkSip2Creator(final Path outputPath, final Map<Sip, List<String>> previews,
    final SIPNameBuilder sipNameBuilder, final boolean createReport, final IPHeader ipHeader, final String sipAgentName,
    final String sipAgentID) {
    super(outputPath, previews, sipNameBuilder, createReport, ipHeader, sipAgentName, sipAgentID);
  }

  public static String getText() {
    return "E-ARK2";
  }

  /**
   * @return {@link List} of {@link IPContentType}.
   */
  public static List<org.roda.rodain.core.schema.IPContentType> ipSpecificContentTypes() {
    final List<org.roda.rodain.core.schema.IPContentType> res = new ArrayList<>();
    for (IPContentTypeEnum ipContentTypeEnum : IPContentTypeEnum.values()) {
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
    for (IPContentInformationTypeEnum ipContentInformationTypeEnum : IPContentInformationTypeEnum.values()) {
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
      // recursive call to all the node's children
      for (TreeNode node : tn.getChildren().values()) {
        addFileToRepresentation(node, newRelativePath, rep);
      }
    } else {
      // if it's a file, add it to the representation
      rep.addFile(tn.getPath(), relativePath);
      setCurrentSIPadded(getCurrentSIPadded() + 1);
      currentAction = String.format("%s (%d/%d)", actionCopyingData, getCurrentSIPadded(), getCurrentSIPsize());
    }
  }
}
