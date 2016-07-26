package org.roda.rodain.creation;

import org.roda.rodain.core.RodaIn;
import org.roda.rodain.creation.ui.CreationModalPreparation;
import org.roda.rodain.schema.DescriptionObject;
import org.roda.rodain.sip.SipPreview;
import sun.security.krb5.internal.crypto.Des;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 19/11/2015.
 */
public class CreateSips {
  private SipTypes type;
  private Path outputPath;
  private SimpleSipCreator creator;
  private String prefix;
  private CreationModalPreparation.NAME_TYPES name_type;

  private int sipsCount;
  private long startedTime;
  private boolean exportAll;

  /**
   * Creates a new object of the SIP exporter
   *
   * @param outputPath
   *          The path of the output folder of the SIP exportation
   * @param type
   *          The format of the SIP output
   */
  public CreateSips(Path outputPath, SipTypes type, boolean exportAll, String prefix, CreationModalPreparation.NAME_TYPES name_type) {
    this.type = type;
    this.outputPath = outputPath;
    this.exportAll = exportAll;
    this.prefix = prefix;
    this.name_type = name_type;
  }

  /**
   * Starts the exportation process.
   */
  public void start() {
    Map<DescriptionObject, List<String>> sips;
    if (exportAll)
      sips = RodaIn.getAllDescriptionObjects();
    else
      sips = RodaIn.getSelectedDescriptionObjects();

    startedTime = System.currentTimeMillis();
    sipsCount = sips.size();
    if (type == SipTypes.BAGIT) {
      creator = new BagitSipCreator(outputPath, sips);
      creator.start();
    } else {
      creator = new EarkSipCreator(outputPath, sips, prefix, name_type);
      creator.start();
    }
  }

  /**
   * @return The total number of SIPs that will be created.
   */
  public int getSipsCount() {
    return sipsCount;
  }

  /**
   * @return The number of SIPs that have already been created.
   */
  public int getCreatedSipsCount() {
    return creator.getCreatedSipsCount();
  }

  /**
   * @return The number of SIPs not created due to an error.
   */
  public int getErrorCount() {
    return creator.getErrorCount();
  }

  /**
   * @return A double resulting of the division of the number of SIPs already
   *         created by the total number of SIPs.
   */
  public double getProgress() {
    return (creator.getCreatedSipsCount() / (sipsCount * 1.0)) + creator.currentSipProgress;
  }

  /**
   * @return The time remaining estimate of the SIP creator.
   */
  public double getTimeRemainingEstimate() {
    return creator.getTimeRemainingEstimate();
  }

  /**
   * @return The name of the SIP currently being processed.
   */
  public String getSipName() {
    return creator.getCurrentSipName();
  }

  /**
   * @return The action currently being done on the SIP.
   */
  public String getAction() {
    return creator.getCurrentAction();
  }

  /**
   * Halts the execution of the SIP creator.
   */
  public void cancel() {
    creator.cancel();
  }

  public long getStartedTime() {
    return startedTime;
  }

  public Path getOutputPath() {
    return outputPath;
  }
}
