package org.roda.rodain.creation;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import org.roda.rodain.core.Main;
import org.roda.rodain.rules.sip.SipPreview;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 19/11/2015.
 */
public class CreateSips {
  private SipTypes type;
  private Path outputPath;
  private SimpleSipCreator creator;

  private Instant startTime;
  private int sipsCount;
  private float currentSipProgress;
  /**
   * Creates a new object of the SIP exporter
   *
   * @param outputPath The path of the output folder of the SIP exportation
   * @param type       The format of the SIP output
   */
  public CreateSips(Path outputPath, SipTypes type) {
    this.type = type;
    this.outputPath = outputPath;
  }

  /**
   * Starts the exportation process.
   */
  public void start() {
    Map<SipPreview, String> sips = Main.getSipPreviews();
    sipsCount = sips.size();
    if (type == SipTypes.BAGIT) {
      creator = new BagitSipCreator(outputPath, sips);
      creator.start();
    } else {
      creator = new EarkSipCreator(outputPath, sips);
      creator.start();
    }
    startTime = Instant.now();
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
   * created by the total number of SIPs.
   */
  public double getProgress() {
    return (creator.getCreatedSipsCount() / (sipsCount * 1.0)) + currentSipProgress;
  }

  /**
   * Estimates the remaining time needed to finish exporting the SIPs.
   *
   * <p>
   * The estimate time is the sum of the data copy time and the other processes
   * time. To estimate the data copy time, we first find the average copy speed
   * and then divide the remaining data size by that speed.
   * </p>
   *
   * <p>
   * The other processes (metadata copy and finalizing) are estimated together,
   * and can be obtained by subtracting the data copy time from the elapsed
   * time. By dividing that result by the number of already exported SIPs, we
   * get the average time these processes took.
   * </p>
   * 
   * @return The estimate in milliseconds
   */
  public double getTimeRemainingEstimate() {
    // prevent divide by zero
    if (creator.transferedTime == 0)
      return -1;
    // estimate the remaining data copy time for the current SIP
    float allSpeed = creator.transferedSize / creator.transferedTime;
    long allSizeLeft = creator.allSipsSize - creator.transferedSize;
    long sizeLeft = creator.sipSize - creator.sipTransferedSize;
    float sipRemaining = sizeLeft / allSpeed;

    // 80% is the progress of the data copy of current SIP
    // the other 20% are for the SIP finalization
    // divide the result by the number of SIPs because this should be the
    // progress of 1 SIP
    currentSipProgress = (creator.sipTransferedSize / (float) creator.sipSize) * 0.8f;
    currentSipProgress /= sipsCount;
    System.out.println(currentSipProgress);

    // estimate the time remaining for the other SIPs, except the data copy time
    long timeSinceStart = Duration.between(startTime, Instant.now()).toMillis();
    long allOtherTime = timeSinceStart - creator.transferedTime;
    int createdSips = getCreatedSipsCount();
    float eachOtherTime;
    if (createdSips != 0) {
      eachOtherTime = allOtherTime / createdSips;
    } else { // if the finishing time is very small, set it to 70% of the
             // estimated time
      eachOtherTime = (creator.sipSize / allSpeed) * 0.7f;
    }

    // time = data copy estimate + other SIP's estimate (without copy time)
    int remaining = sipsCount - createdSips;
    float dataTime = sipRemaining + (allSizeLeft / allSpeed);
    long sipTime = Duration.between(creator.sipStartInstant, Instant.now()).toMillis();
    float sipOtherTime = sipTime - creator.sipTransferedTime;
    float otherTime = (eachOtherTime * remaining) - sipOtherTime;

    return dataTime + otherTime;
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

}
