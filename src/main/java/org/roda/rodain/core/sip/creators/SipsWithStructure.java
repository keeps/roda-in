package org.roda.rodain.core.sip.creators;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.roda.rodain.core.ConfigurationManager;
import org.roda.rodain.core.Constants;
import org.roda.rodain.core.Constants.MetadataOption;
import org.roda.rodain.core.Constants.PathState;
import org.roda.rodain.core.PathCollection;
import org.roda.rodain.core.rules.TreeNode;
import org.roda.rodain.core.rules.filters.ContentFilter;
import org.roda.rodain.core.schema.DescObjMetadata;
import org.roda.rodain.core.schema.Sip;
import org.roda.rodain.core.sip.PseudoDescriptionObject;
import org.roda.rodain.core.sip.PseudoItem;
import org.roda.rodain.core.sip.PseudoSIP;
import org.roda.rodain.core.sip.SipPreview;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 10-03-2016.
 */
public class SipsWithStructure extends SipPreviewCreator {
  private static final Logger LOGGER = LoggerFactory.getLogger(SipsWithStructure.class.getName());
  private Deque<Folder> folders;
  private Set<PseudoItem> tree;
  private Map<Path, PseudoItem> record;
  private Map<Path, Sip> descriptionObjects;
  private Map<Path, SipPreview> sipPreviewMap;

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
  public SipsWithStructure(String id, Set<ContentFilter> filters, MetadataOption metadataOption, String metadataType,
    Path metadataPath, String templateType, String metadataVersion) {
    super(id, filters, metadataOption, metadataType, metadataPath, templateType, metadataVersion);
    folders = new ArrayDeque<>();
    tree = new HashSet<>();
    record = new HashMap<>();
    descriptionObjects = new HashMap<>();
    sipPreviewMap = new HashMap<>();
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
    if (filter(path) || cancelled)
      return;
    Folder newFolder = new Folder(path);
    folders.add(newFolder);
  }

  @Override
  public void setStartPath(String path) {
    try {
      Files.walkFileTree(Paths.get(path), new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          PathCollection.simpleAddPath(file);
          return isTerminated();
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
          PathCollection.simpleAddPath(dir);
          return isTerminated();
        }
      });
    } catch (AccessDeniedException e) {
      LOGGER.info("Access denied to file", e);
    } catch (IOException e) {
      LOGGER.error("Error walking the file tree", e);
    }
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
    if (filter(path) || cancelled)
      return;
    // pop the node of this directory and add it to its parent (if it exists)
    Folder folder = folders.removeLast();
    if (!folders.isEmpty())
      folders.peekLast().addItem(folder.getPath());

    Set<Path> subFiles, subFolders;
    subFiles = folder.getFiles();
    subFolders = folder.getFolders();

    // some files and no sub-folders -> single SIP with all the files
    if (!subFiles.isEmpty() && subFolders.isEmpty()) {
      TreeNode node = new TreeNode(path);
      for (Path p : subFiles) {
        TreeNode fileNode = new TreeNode(p);
        node.add(fileNode);
      }
      PseudoSIP pseudoSIP = new PseudoSIP(node, getMetadataPath(path));
      record.put(path, pseudoSIP);
      if (folders.isEmpty()) {
        tree.add(pseudoSIP);
      }
    } else {
      // each file will be a SIP
      for (Path p : subFiles) {
        record.put(p, new PseudoSIP(new TreeNode(p), getMetadataPath(p)));
      }

      // make this node a description object
      Sip descriptionObject = new Sip(
        new DescObjMetadata(MetadataOption.TEMPLATE, templateType, metadataType, metadataVersion));
      descriptionObject.setTitle(path.getFileName().toString());
      try {
        String metadataAggregationLevel = ConfigurationManager
          .getMetadataConfig(templateType + Constants.CONF_K_SUFIX_AGGREG_LEVEL);
        descriptionObject.setDescriptionlevel(metadataAggregationLevel);
      } catch (Throwable t) {
        LOGGER.error(t.getMessage(), t);
      }
      descriptionObjects.put(path, descriptionObject);

      // Set this node as a parent of its descriptionObject children (which can
      // only be folders)
      for (Path p : subFolders) {
        Sip dobj = descriptionObjects.get(p);
        if (dobj != null) {
          dobj.setParentId(descriptionObject.getId());
        }
      }

      // construct the tree
      Set<Path> children = new HashSet<>(subFiles);
      children.addAll(subFolders);

      PseudoDescriptionObject pdo = new PseudoDescriptionObject(path);
      for (Path child : children) {
        pdo.addChild(record.get(child));
      }
      record.put(path, pdo);

      if (folders.isEmpty()) {
        tree.add(pdo);
      }
    }
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
    if (filter(path) || cancelled) {
      return;
    }
    if (folders.isEmpty()) {
      PseudoSIP pseudoSIP = new PseudoSIP(new TreeNode(path), getMetadataPath(path));
      record.put(path, pseudoSIP);
      tree.add(pseudoSIP);
    } else {
      folders.peekLast().addItem(path);
    }
  }

  /**
   * Ends the tree visit, creating the SIP with all the files added during the
   * visit and notifying the observers.
   */
  @Override
  public void end() {
    if (cancelled) {
      setChanged();
      notifyObservers(Constants.EVENT_FINISHED);
      return;
    }
    Set<PseudoDescriptionObject> descObjs = new HashSet<>();
    record.forEach((path, pseudoItem) -> {
      if (pseudoItem instanceof PseudoSIP) {
        PseudoSIP pseudoSIP = (PseudoSIP) pseudoItem;
        createSip(pseudoSIP.getNode());
      }
      if (pseudoItem instanceof PseudoDescriptionObject) {
        descObjs.add((PseudoDescriptionObject) pseudoItem);
      }
    });

    // Map the paths of the description objects only AFTER the SIPs are created
    // to avoid unwanted filtering
    descObjs
      .forEach(pseudoDescriptionObject -> PathCollection.addPath(pseudoDescriptionObject.getPath(), PathState.MAPPED));
    setChanged();
    notifyObservers(Constants.EVENT_FINISHED);
  }

  private void createSip(TreeNode node) {
    Path path = node.getPath();
    SipPreview sipPreview = createSip(path, node);
    sipPreviewMap.put(path, sipPreview);
  }

  public Map<Path, PseudoItem> getRecord() {
    return record;
  }

  public Set<PseudoItem> getTree() {
    return tree;
  }

  public Map<Path, Sip> getDescriptionObjects() {
    return descriptionObjects;
  }

  public Map<Path, SipPreview> getSipPreviewMap() {
    return sipPreviewMap;
  }

  private FileVisitResult isTerminated() {
    // terminate if the thread has been interrupted
    if (Thread.interrupted() || cancelled) {
      return FileVisitResult.TERMINATE;
    }
    return FileVisitResult.CONTINUE;
  }

  class Folder {
    private Path path;
    private Set<Path> files, folders;

    public Folder(Path path) {
      this.path = path;
      files = new HashSet<>();
      folders = new HashSet<>();
    }

    public Path getPath() {
      return path;
    }

    public Set<Path> getFiles() {
      return files;
    }

    public Set<Path> getFolders() {
      return folders;
    }

    public void addItem(Path path) {
      if (Files.isDirectory(path)) {
        folders.add(path);
      } else
        files.add(path);
    }
  }
}
