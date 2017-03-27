package org.roda.rodain.core.creation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.roda.rodain.core.Constants;
import org.roda.rodain.core.Constants.SipNameStrategy;
import org.roda.rodain.core.Controller;
import org.roda.rodain.core.I18n;
import org.roda.rodain.core.Pair;
import org.roda.rodain.core.rules.TreeNode;
import org.roda.rodain.core.schema.Sip;
import org.roda.rodain.core.sip.SipPreview;
import org.roda.rodain.core.sip.SipRepresentation;
import org.roda.rodain.ui.creation.CreationModalProcessing;
import org.roda_project.commons_ip.model.IPContentType;
import org.roda_project.commons_ip.model.IPRepresentation;
import org.roda_project.commons_ip.model.SIP;
import org.roda_project.commons_ip.model.SIPObserver;
import org.roda_project.commons_ip.model.impl.bagit.BagitSIP;
import org.roda_project.commons_ip.model.impl.bagit.BagitUtils;
import org.roda_project.commons_ip.utils.IPEnums.IPStatus;
import org.roda_project.commons_ip.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 19/11/2015.
 */
public class BagitSipCreator extends SimpleSipCreator implements SIPObserver {
  private static final Logger LOGGER = LoggerFactory.getLogger(BagitSipCreator.class.getName());
  private int countFilesOfZip;
  private int currentSIPsize = 0;
  private int currentSIPadded = 0;
  private int repProcessingSize;

  private Instant startTime;

  private String prefix;
  private SipNameStrategy sipNameStrategy;

  /**
   * Creates a new BagIt exporter.
   *
   * @param outputPath
   *          The path to the output folder of the SIP exportation
   * @param previews
   *          The map with the SIPs that will be exported
   */
  public BagitSipCreator(Path outputPath, Map<Sip, List<String>> previews, boolean createReport, String prefix,
    SipNameStrategy sipNameStrategy) {
    super(outputPath, previews, createReport);
    this.prefix = prefix;
    this.sipNameStrategy = sipNameStrategy;
    for (Sip obj : previews.keySet()) {
      if (obj instanceof SipPreview) {
        SipPreview sip = (SipPreview) obj;
        for (SipRepresentation sr : sip.getRepresentations()) {
          for (TreeNode tn : sr.getFiles()) {
            try {
              allSipsSize += nodeSize(tn);
            } catch (IOException e) {
              LOGGER.error("Can't access file '{}'", tn.getPath(), e);
            }
          }
        }
      }
    }
  }

  /**
   * Attempts to create a BagIt SIP of each SipPreview
   */
  @Override
  public void run() {

    startTime = Instant.now();
    Map<Path, Object> sips = new HashMap<>();
    for (Sip preview : previews.keySet()) {
      if (canceled) {
        break;
      }
      Pair pathBag = createBagit(preview);
      if (pathBag != null) {
        sips.put((Path) pathBag.getKey(), (SIP) pathBag.getValue());
      }
    }
    if (createReport) {
      createReport(sips);
    }

    currentAction = I18n.t(Constants.I18N_DONE);
  }

  private Pair createBagit(Sip descriptionObject) {
    Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
    // we add a timestamp to the beginning of the SIP name to avoid same name
    // conflicts
    currentSipName = descriptionObject.getTitle();
    currentAction = actionCreatingFolders;

    IPContentType contentType = descriptionObject instanceof SipPreview
      ? ((SipPreview) descriptionObject).getContentType() : IPContentType.getMIXED();

    SIP bagit = new BagitSIP(Controller.urlEncode(descriptionObject.getId()), contentType, agentName);
    bagit.addObserver(this);
    bagit.setStatus(IPStatus.NEW);

    String sipName = createSipName(descriptionObject, prefix, sipNameStrategy);

    Path namePath;
    if (StringUtils.isNotBlank(sipName)) {
      namePath = outputPath.resolve(sipName);
    } else {
      namePath = outputPath.resolve(descriptionObject.getId());
    }

    Path data = namePath.resolve(Constants.BAGIT_DATA_FOLDER);
    new File(data.toString()).mkdirs();

    try {
      currentAction = actionCopyingData;
      if (descriptionObject instanceof SipPreview) {
        SipPreview sip = (SipPreview) descriptionObject;
        for (SipRepresentation sr : sip.getRepresentations()) {
          IPRepresentation rep = new IPRepresentation(sr.getName());
          rep.setContentType(sr.getType());

          Set<TreeNode> files = sr.getFiles();
          currentSIPadded = 0;
          currentSIPsize = 0;

          // count files
          for (TreeNode tn : files) {
            currentSIPsize += tn.getFullTreePaths().size();
          }

          for (TreeNode tn : files) {
            addFileToRepresentation(tn, new ArrayList<>(), rep);
            createFiles(tn, data);
          }

          bagit.addRepresentation(rep);
        }
      }

      Map<String, String> metadataList = new HashMap<>();
      metadataList.put(Constants.BAGIT_ID, descriptionObject.getId());
      metadataList.put(Constants.BAGIT_PARENT, descriptionObject.getParentId());
      metadataList.put(Constants.BAGIT_TITLE, descriptionObject.getTitle());
      metadataList.put(Constants.BAGIT_LEVEL, Constants.BAGIT_ITEM_LEVEL);

      Map<String, String> list = descriptionObject.getMetadataWithReplaces();
      if (!list.isEmpty()) {
        list.forEach((id, content) -> metadataList.put(Constants.CONF_K_PREFIX_METADATA + id, content));
      }

      Path metadataPath = tempDir.resolve(Utils.generateRandomAndPrefixedUUID());
      bagit.addDescriptiveMetadata(BagitUtils.createBagitMetadata(metadataList, metadataPath));
      Path name = bagit.build(outputPath, sipName);

      currentAction = actionFinalizingSip;
      createdSipsCount++;
      return new Pair(name, bagit);
    } catch (Exception e) {
      LOGGER.error("Error creating SIP", e);
      unsuccessful.add(descriptionObject);
      CreationModalProcessing.showError(descriptionObject, e);
      return null;
    }
  }

  private void addFileToRepresentation(TreeNode tn, List<String> relativePath, IPRepresentation rep) {
    if (Files.isDirectory(tn.getPath())) {
      // add this directory to the path list
      List<String> newRelativePath = new ArrayList<>(relativePath);
      newRelativePath.add(tn.getPath().getFileName().toString());
      // recursive call to all the node's children
      for (TreeNode node : tn.getChildren().values()) {
        addFileToRepresentation(node, newRelativePath, rep);
      }
    } else {
      // if it's a file, add it to the representation
      rep.addFile(tn.getPath(), relativePath);
      currentSIPadded++;
      currentAction = String.format("%s (%d/%d)", actionCopyingData, currentSIPadded, currentSIPsize);
    }
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
  @Override
  public double getTimeRemainingEstimate() {
    // prevent divide by zero
    if (transferedTime == 0)
      return -1;
    // estimate the remaining data copy time for the current SIP
    float allSpeed = transferedSize / transferedTime;
    long allSizeLeft = allSipsSize - transferedSize;
    long sizeLeft = sipSize - sipTransferedSize;
    float sipRemaining = sizeLeft / allSpeed;

    // 80% is the progress of the data copy of current SIP
    // the other 20% are for the SIP finalization
    // divide the result by the number of SIPs because this should be the
    // progress of 1 SIP
    currentSipProgress = (sipTransferedSize / (float) sipSize) * 0.8f;
    currentSipProgress /= sipPreviewCount;

    // estimate the time remaining for the other SIPs, except the data copy time
    long timeSinceStart = Duration.between(startTime, Instant.now()).toMillis();
    long allOtherTime = timeSinceStart - transferedTime;
    int createdSips = getCreatedSipsCount();
    float eachOtherTime;
    if (createdSips != 0) {
      eachOtherTime = allOtherTime / createdSips;
    } else { // if the finishing time is very small, set it to 70% of the
      // estimated time
      eachOtherTime = (sipSize / allSpeed) * 0.7f;
    }

    // time = data copy estimate + other SIP's estimate (without copy time)
    int remaining = sipPreviewCount - createdSips;
    float dataTime = sipRemaining + (allSizeLeft / allSpeed);
    long sipTime = Duration.between(sipStartInstant, Instant.now()).toMillis();
    float sipOtherTime = sipTime - sipTransferedTime;
    float otherTime = (eachOtherTime * remaining) - sipOtherTime;

    return dataTime + otherTime;
  }

  private long nodeSize(TreeNode node) throws IOException {
    Path nodePath = node.getPath();
    long result = 0;
    if (Files.isDirectory(nodePath)) {
      for (TreeNode tn : node.getChildren().values()) {
        result += nodeSize(tn);
      }
    } else {
      result += Files.size(nodePath);
    }
    return result;
  }

  private void createFiles(TreeNode node, Path dest) throws IOException {
    sipSize = nodeSize(node);
    sipTransferedSize = 0;
    sipTransferedTime = 0;
    sipStartInstant = Instant.now();
    recCreateFiles(node, dest);
  }

  private void recCreateFiles(TreeNode node, Path dest) throws IOException {
    Path nodePath = node.getPath();
    if (Files.isDirectory(nodePath)) {
      Path directory = dest.resolve(nodePath.getFileName().toString());
      new File(directory.toString()).mkdir();
      for (TreeNode tn : node.getChildren().values()) {
        recCreateFiles(tn, directory);
      }
    } else {
      Path destination = dest.resolve(nodePath.getFileName().toString());
      copyFile(nodePath, destination);
    }
  }

  private void copyFile(Path path, Path dest) {
    final int progressCheckpoint = 1000;
    long bytesCopied = 0;
    long previousLength = 0;
    File destFile = dest.toFile();

    try {
      long totalBytes = Files.size(path);
      try (InputStream in = new FileInputStream(path.toFile()); OutputStream out = new FileOutputStream(destFile)) {
        byte[] buf = new byte[1024];
        int counter = 0;
        int len;
        lastInstant = Instant.now();

        while ((len = in.read(buf)) > 0) {
          out.write(buf, 0, len);
          counter += len;
          bytesCopied += (destFile.length() - previousLength);
          previousLength = destFile.length();
          if (counter > progressCheckpoint || bytesCopied == totalBytes) {
            sipTransferedSize += counter;
            transferedSize += counter;
            Instant now = Instant.now();
            Duration dur = Duration.between(lastInstant, now);
            transferedTime += dur.toMillis();
            sipTransferedTime += dur.toMillis();
            lastInstant = now;
            counter = 0;
          }
        }
      }
    } catch (IOException e) {
      LOGGER.error("Error writing(copying) file. Source: {}; Destination: {}", path, dest, e);
    }
  }

  @Override
  public void sipBuildRepresentationsProcessingStarted(int current) {
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
