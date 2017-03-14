package org.roda.rodain.core.creation;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.roda.rodain.core.Constants.SipNameStrategy;
import org.roda.rodain.core.Constants.SipType;
import org.roda.rodain.core.schema.Sip;
import org.roda.rodain.core.sip.SipPreview;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 19/11/2015.
 */
public class CreateSips {
  private static final Logger LOGGER = LoggerFactory.getLogger(CreateSips.class.getName());

  private SipType type;
  private Path outputPath;
  private SimpleSipCreator creator;
  private String prefix;
  private SipNameStrategy sipNameStrategy;

  private int sipsCount;
  private long startedTime;
  private boolean exportAll, exportItems, createReport;

  /**
   * Creates a new object of the SIP exporter
   *
   * @param outputPath
   *          The path of the output folder of the SIP exportation
   * @param type
   *          The format of the SIP output
   */
  public CreateSips(Path outputPath, SipType type, boolean exportAll, boolean exportItems, String prefix,
    SipNameStrategy sipNameStrategy, boolean createReport) {
    this.type = type;
    this.outputPath = outputPath;
    this.exportAll = exportAll;
    this.prefix = prefix;
    this.sipNameStrategy = sipNameStrategy;
    this.exportItems = exportItems;
    this.createReport = createReport;
  }

  /**
   * Starts the exportation process.
   */
  public void start(Map<Sip, List<String>> sips) {
    // Map<DescriptionObject, List<String>> sips;
    // if (exportAll) {
    // sips = RodaInApplication.getAllDescriptionObjects();
    // } else {
    // sips = RodaInApplication.getSelectedDescriptionObjects();
    // }

    if (!exportItems)
      sips = sips.entrySet().stream().filter(entry -> entry.getKey() instanceof SipPreview)
        .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
    startedTime = System.currentTimeMillis();

    sipsCount = sips.size();
    if (type == SipType.BAGIT) {
      creator = new BagitSipCreator(outputPath, sips, createReport, prefix, sipNameStrategy);
      creator.start();
    } else {
      creator = new EarkSipCreator(outputPath, sips, prefix, sipNameStrategy, createReport);
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
