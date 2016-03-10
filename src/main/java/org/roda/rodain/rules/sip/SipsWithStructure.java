package org.roda.rodain.rules.sip;

import org.roda.rodain.core.PathCollection;
import org.roda.rodain.rules.MetadataTypes;
import org.roda.rodain.rules.TreeNode;
import org.roda.rodain.rules.filters.ContentFilter;
import org.roda.rodain.schema.DescObjMetadata;
import org.roda.rodain.schema.DescriptionObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 10-03-2016.
 */
public class SipsWithStructure extends SipPreviewCreator {
  private static final Logger log = LoggerFactory.getLogger(SipsWithStructure.class.getName());
  private Deque<Folder> folders;
  private Map<Path, DescriptionObject> record;
  private Map<Path, Set<Path>> structure;

  /**
   * Creates a new SipPreviewCreator where there's a new SIP created with all
   * the visited paths.
   *
   * @param id
   *          The id of the SipPreviewCreator.
   * @param filters
   *          The set of content filters
   * @param metaType
   *          The type of metadata to be applied to each SIP
   * @param metadataPath
   *          The path of the metadata
   * @param templateType
   *          The type of the metadata template
   */
  public SipsWithStructure(String id, Set<ContentFilter> filters, MetadataTypes metaType, Path metadataPath,
    String templateType, String templateVersion) {
    super(id, filters, metaType, metadataPath, templateType, templateVersion);
    folders = new ArrayDeque<>();
    record = new HashMap<>();
    structure = new HashMap<>();
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
          PathCollection.simpleAddPath(file.toString());
          return isTerminated();
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
          PathCollection.simpleAddPath(dir.toString());
          return isTerminated();
        }
      });
    } catch (AccessDeniedException e) {
      log.info("Access denied to file", e);
    } catch (IOException e) {
      log.error("Error walking the file tree", e);
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
      createSip(path, node);
    } else {
      // each file will be a SIP
      for (Path p : subFiles) {
        createSip(p, new TreeNode(p));
      }

      // make this node a description object
      DescriptionObject descriptionObject = new DescriptionObject();
      descriptionObject.setTitle(path.getFileName().toString());
      descriptionObject.setDescriptionlevel("series");
      record.put(path, descriptionObject);

      // Set this node as a parent of its descriptionObject children (which can
      // only be folders)
      for (Path p : subFolders) {
        DescriptionObject dobj = record.get(p);
        if (dobj != null) {
          dobj.setParentId(descriptionObject.getId());
        }
      }

      // add the node's children to the structure
      Set<Path> children = structure.get(path);
      if (children == null)
        children = new HashSet<>();
      children.addAll(subFiles);
      children.addAll(subFolders);
      structure.put(path, children);
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
      createSip(path, new TreeNode(path));
    } else {
      folders.peekLast().addItem(path);
    }
  }

  private void createSip(Path path, TreeNode node) {
    Path metaPath = getMetadataPath(path);
    // create a new Sip
    Set<TreeNode> files = new HashSet<>();
    files.add(node);

    DescObjMetadata metadata;
    if (metaType == MetadataTypes.TEMPLATE)
      metadata = new DescObjMetadata(metaType, templateType, templateVersion);
    else
      metadata = new DescObjMetadata(metaType, metaPath);

    SipRepresentation rep = new SipRepresentation("rep1");
    rep.setFiles(files);
    Set<SipRepresentation> repSet = new HashSet<>();
    repSet.add(rep);
    SipPreview sipPreview = new SipPreview(path.getFileName().toString(), repSet, metadata);
    node.addObserver(sipPreview);

    sips.add(sipPreview);
    sipsMap.put(sipPreview.getId(), sipPreview);
    record.put(path, sipPreview);
    added++;
  }

  private Path getMetadataPath(Path sipPath) {
    Path result = null;
    if (metaType == MetadataTypes.SINGLE_FILE) {
      result = metadataPath;
    } else if (metaType == MetadataTypes.SAME_DIRECTORY) {
      result = searchMetadata(sipPath);
    }
    return result;
  }

  private Path searchMetadata(Path sipPath) {
    File dir = sipPath.toFile();
    if (!dir.isDirectory())
      dir = sipPath.getParent().toFile();

    PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + templateType);
    File[] foundFiles = dir.listFiles((dir1, name) -> {
      return matcher.matches(Paths.get(name));
    });

    if (foundFiles != null && foundFiles.length > 0) {
      return foundFiles[0].toPath();
    }
    return null;
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
