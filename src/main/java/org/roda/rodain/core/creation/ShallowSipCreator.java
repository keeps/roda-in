package org.roda.rodain.core.creation;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.roda.rodain.core.Constants;
import org.roda.rodain.core.I18n;
import org.roda.rodain.core.Pair;
import org.roda.rodain.core.schema.RepresentationContentType;
import org.roda.rodain.core.schema.Sip;
import org.roda.rodain.core.sip.naming.SIPNameBuilder;
import org.roda_project.commons_ip.model.IPHeader;
import org.roda_project.commons_ip.model.SIPObserver;
import org.roda_project.commons_ip2.model.IPContentInformationType;
import org.roda_project.commons_ip2.model.IPContentType;
import org.roda_project.commons_ip2.model.SIP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@author João Gomes <jgomes@keep.pt>}.
 */
public class ShallowSipCreator extends SimpleSipCreator implements SIPObserver, SipCreator {
  private static final Logger LOGGER = LoggerFactory.getLogger(ShallowSipCreator.class.getName());

  private int countFilesOfZip;
  private int currentSIPadded = 0;
  private int currentSIPsize = 0;
  private int repProcessingSize;

  private SIPNameBuilder sipNameBuilder;
  private IPHeader ipHeader;

  public ShallowSipCreator(Path outputPath, Map<Sip, List<String>> previews, SIPNameBuilder sipNameBuilder,
    boolean createReport, IPHeader ipHeader) {
    super(outputPath, previews, createReport);
    this.sipNameBuilder = sipNameBuilder;
    this.ipHeader = ipHeader;
  }

  public static String getText() {
    return "SIP-S";
  }

  public static boolean requiresMETSHeaderInfo() {
    // 20191028 hsilva: postponing this dev/adaptation
    return false;
  }

  public static List<org.roda.rodain.core.schema.IPContentType> ipSpecificContentTypes() {
    List<org.roda.rodain.core.schema.IPContentType> res = new ArrayList<>();
    for (IPContentType.IPContentTypeEnum ipContentTypeEnum : IPContentType.IPContentTypeEnum.values()) {
      res.add(new org.roda.rodain.core.schema.IPContentType(getText(), ipContentTypeEnum.toString()));
    }
    return res;
  }

  public static List<RepresentationContentType> representationSpecificContentTypes() {
    List<RepresentationContentType> res = new ArrayList<>();
    for (IPContentInformationType.IPContentInformationTypeEnum ipContentInformationTypeEnum : IPContentInformationType.IPContentInformationTypeEnum.values()) {
      res.add(new RepresentationContentType(getText(), ipContentInformationTypeEnum.toString()));
    }
    return res;
  }

  /**
   * Attempts to create an SIP-S of each SipPreview.
   */
  @Override
  public void run() {
    Map<Path, Object> sips = new HashMap<>();
    for (Sip preview : previews.keySet()) {
      if (canceled) {
        break;
      }
      final Pair pathSIP = createSipS(preview);
      if (pathSIP != null) {
        sips.put((Path) pathSIP.getKey(), pathSIP.getValue());
      }
    }
    if (createReport) {
      createReport(sips);
    }
    currentAction = I18n.t(Constants.I18N_DONE);
  }

  private Pair createSipS(Sip descriptionObject) {
    return null;
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
