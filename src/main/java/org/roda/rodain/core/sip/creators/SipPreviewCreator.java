package org.roda.rodain.core.sip.creators;

import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.roda.rodain.core.ConfigurationManager;
import org.roda.rodain.core.Constants;
import org.roda.rodain.core.Constants.MetadataOption;
import org.roda.rodain.core.Controller;
import org.roda.rodain.core.rules.TreeNode;
import org.roda.rodain.core.rules.filters.ContentFilter;
import org.roda.rodain.core.schema.DescriptiveMetadata;
import org.roda.rodain.core.sip.SipPreview;
import org.roda.rodain.core.sip.SipRepresentation;
import org.roda.rodain.core.utils.TreeVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 20-10-2015.
 */
public class SipPreviewCreator extends Observable implements TreeVisitor {
  private static final Logger LOGGER = LoggerFactory.getLogger(SipPreviewCreator.class.getName());
  private String startPath;
  // This map is returned, in full, to the SipPreviewNode when there's an update
  protected Map<String, SipPreview> sipsMap;
  // This ArrayList is used to keep the SIPs ordered.
  // We need them ordered because we have to keep track of which SIPs have
  // already been loaded
  protected List<SipPreview> sips;
  protected int added = 0, returned = 0;
  protected Deque<TreeNode> nodes;
  protected Set<TreeNode> files;

  private String id;
  private Set<ContentFilter> filters;
  protected String metadataType;
  protected MetadataOption metadataOption;
  protected Path metadataPath;
  protected String templateType, metadataVersion;
  private Map<String, Set<Path>> metadata;

  protected boolean cancelled = false;

  /**
   * Creates a new SipPreviewCreator where there's a new SIP created with all
   * the visited paths.
   *
   * @param id
   *          The id of the SipPreviewCreator.
   * @param filters
   *          The set of content filters
   * @param metadataOption
   *          The type of metadata to be applied to each SIP
   * @param metadataPath
   *          The path of the metadata
   * @param templateType
   *          The type of the metadata template
   */
  public SipPreviewCreator(String id, Set<ContentFilter> filters, MetadataOption metadataOption, String metadataType,
    Path metadataPath, String templateType, String metadataVersion) {
    this.filters = filters;
    sipsMap = new HashMap<>();
    sips = new ArrayList<>();
    nodes = new ArrayDeque<>();
    this.id = id;
    this.metadataOption = metadataOption;
    this.metadataType = metadataType;
    this.metadataPath = metadataPath;
    this.templateType = templateType;
    this.metadataVersion = metadataVersion;
    files = new HashSet<>();
    metadata = new HashMap<>();

    if (metadataPath != null && metadataOption == MetadataOption.DIFF_DIRECTORY) {
      try {
        Files.walkFileTree(metadataPath, new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            String key = FilenameUtils.removeExtension(file.getFileName().toString());
            Set<Path> paths = metadata.get(key);
            if (paths == null)
              paths = new HashSet<>();
            paths.add(file);
            metadata.put(key, paths);
            return FileVisitResult.CONTINUE;
          }
        });
      } catch (AccessDeniedException e) {
        LOGGER.info("Access denied to file", e);
      } catch (IOException e) {
        LOGGER.error("Error walking the file tree", e);
      }
    }
  }

  /**
   * @return The Map with the SIPs created by the SipPreviewCreator.
   */
  public Map<String, SipPreview> getSips() {
    return sipsMap;
  }

  /**
   * @return The count of the SIPs already created.
   */
  public int getCount() {
    return added;
  }

  /**
   * The object keeps a list with the created SIPs and this method returns them
   * one at a time.
   *
   * @return The next SIP in the list.
   */
  public SipPreview getNext() {
    return sips.get(returned++);
  }

  /**
   * @return True if the number of SIPs returned is smaller than the count of
   *         added SIPs.
   */
  public boolean hasNext() {
    return returned < added;
  }

  protected boolean filter(Path path) {
    String pathString = path.toString();
    for (ContentFilter cf : filters) {
      if (cf.filter(pathString))
        return true;
    }
    return false;
  }

  /**
   * Sets the starting path of this TreeVisitor.
   *
   * @param st
   *          The starting path of the TreeVisitor.
   */
  @Override
  public void setStartPath(String st) {
    startPath = st;
  }

  public String getStartPath() {
    return startPath;
  }

  /**
   * Creates a new TreeNode and adds it to the nodes Deque if the path isn't
   * mapped or ignored.
   *
   * @param path
   *          The path of the directory.
   * @param attrs
   *          The attributes of the directory.
   */
  @Override
  public void preVisitDirectory(Path path, BasicFileAttributes attrs) {
  }

  /**
   * Adds the current directory to its parent's node. If the parent doesn't
   * exist, adds a new node to the Deque.
   *
   * @param path
   *          The path of the directory.
   */
  @Override
  public void postVisitDirectory(Path path) {
  }

  /**
   * Adds the visited file to its parent. If the parent doesn't exist, adds a
   * new TreeNode to the Deque.
   *
   * @param path
   *          The path of the visited file
   * @param attrs
   *          The attributes of the visited file
   */
  @Override
  public void visitFile(Path path, BasicFileAttributes attrs) {
  }

  /**
   * This method is empty in this class, but it's defined because of the
   * TreeVisitor interface.
   *
   * @param path
   *          The path of the file.
   */
  @Override
  public void visitFileFailed(Path path) {
  }

  /**
   * Ends the tree visit, creating the SIP with all the files added during the
   * visit and notifying the observers.
   */
  @Override
  public void end() {
    setChanged();
    notifyObservers(Constants.EVENT_FINISHED);
  }

  protected SipPreview createSip(Path path, TreeNode node) {
    Set<Path> metaPath = getMetadataPath(path);

    Set<TreeNode> filesSet = new HashSet<>();
    // start as false otherwise when there's only files they would be jumped
    boolean onlyFiles = false;
    // check if there's a folder with only files inside
    // in that case we will jump the folder and add the files to the root of the
    // representation
    if (Files.isDirectory(node.getPath())) {
      onlyFiles = true;
      for (String pt : node.getKeys()) {
        if (Files.isDirectory(Paths.get(pt))) {
          onlyFiles = false;
          break;
        }
      }
    }
    if (onlyFiles) {
      filesSet.addAll(node.getChildren().values());
    } else {
      filesSet.add(node);
    }
    // create a new Sip
    SipRepresentation rep = new SipRepresentation(Constants.SIP_REP_FIRST);
    rep.setFiles(filesSet);
    Set<SipRepresentation> repSet = new HashSet<>();
    repSet.add(rep);
    SipPreview sipPreview = new SipPreview(path.getFileName().toString(), repSet, null);
    node.addObserver(sipPreview);

    if (metadataOption == MetadataOption.TEMPLATE) {
      sipPreview.getMetadata().add(new DescriptiveMetadata(metadataOption, templateType, metadataType, metadataVersion));
      String level = ConfigurationManager.getMetadataConfig(templateType + Constants.CONF_K_SUFIX_FILE_LEVEL);
      sipPreview.setDescriptionlevel(level);
    } else {
      for (Path m : metaPath) {
        DescriptiveMetadata dom = new DescriptiveMetadata(metadataOption, m, metadataType, metadataVersion, templateType);
        dom = Controller.updateTemplate(dom);
        sipPreview.getMetadata().add(dom);
      }
    }

    String fileLevelItem = ConfigurationManager.getMetadataConfig(templateType + Constants.CONF_K_SUFIX_FILE_LEVEL);
    sipPreview.setDescriptionlevel(fileLevelItem);

    sips.add(sipPreview);
    sipsMap.put(sipPreview.getId(), sipPreview);
    added++;

    return sipPreview;
  }

  protected Set<Path> getMetadataPath(Path sipPath) {
    Set<Path> result = new HashSet<>();
    switch (metadataOption) {
      case SINGLE_FILE:
        result.add(metadataPath);
        break;
      case DIFF_DIRECTORY:
        result = getFileFromDir(sipPath);
        break;
      case SAME_DIRECTORY:
        result.addAll(searchMetadata(sipPath));
        break;
      default:
        return null;
    }
    return result;
  }

  private Set<Path> searchMetadata(Path sipPath) {
    File dir = sipPath.toFile();
    if (!dir.isDirectory())
      dir = sipPath.getParent().toFile();

    PathMatcher matcher = FileSystems.getDefault().getPathMatcher(Constants.MISC_GLOB + templateType);
    File[] foundFiles = dir.listFiles((dir1, name) -> matcher.matches(Paths.get(name)));

    Set<Path> result = new HashSet<>();
    if (foundFiles != null && foundFiles.length > 0) {
      for (File foundFile : foundFiles) {
        result.add(foundFile.toPath());
      }
    }
    return result;
  }

  private Set<Path> getFileFromDir(Path path) {
    String fileNameWithExtension = path.getFileName().toString();
    String fileName = FilenameUtils.removeExtension(fileNameWithExtension);

    Set<Path> paths = metadata.get(fileName);
    Set<Path> result = new HashSet<>();
    if (paths != null) {
      for (Path p : paths) {
        if (!p.getFileName().toString().equals(fileNameWithExtension))
          result.add(p);

      }
    }
    return result;
  }

  /**
   * @return The id of the visitor.
   */
  @Override
  public String getId() {
    return id;
  }

  /**
   * Cancels the execution of the SipPreviewCreator
   */
  public void cancel() {
    cancelled = true;
  }
}
