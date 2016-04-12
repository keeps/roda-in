package org.roda.rodain.schema.ui;

import javafx.scene.control.TreeItem;
import org.roda.rodain.core.I18n;
import org.roda.rodain.rules.TreeNode;
import org.roda.rodain.rules.sip.SipPreview;
import org.roda.rodain.rules.sip.SipRepresentation;
import org.roda.rodain.utils.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 12-04-2016.
 */
public class SchemeItemToString {
  private static SchemeItemToString instance = null;
  private int representations, files, folders;
  private long size;
  private StringBuilder sb;

  private SchemeItemToString() {

  }

  public static SchemeItemToString getInstance() {
    if (instance == null) {
      instance = new SchemeItemToString();
    }
    return instance;
  }

  public String create(TreeItem<String> item) {
    files = 0;
    folders = 0;
    representations = 0;
    size = 0;
    sb = new StringBuilder();
    sb.append(item.getValue()).append(": ");
    if (item instanceof SipPreviewNode) {
      create((SipPreviewNode) item);
      sb.append(representations).append(" ").append("reps");
      sb.append(", ");
      sb.append(folders).append(" ").append(I18n.t("folders"));
      sb.append(", ");
      sb.append(files).append(" ").append(I18n.t("files"));
      sb.append(", ");
      sb.append(Utils.formatSize(size));
    } else if (item instanceof SchemaNode) {
      create((SchemaNode) item);
    }
    return sb.toString();
  }

  private void create(SipPreviewNode sipPreviewNode) {
    SipPreview sip = sipPreviewNode.getSip();
    representations += sip.getRepresentations().size();
    Set<Path> paths = new HashSet<>();
    for (SipRepresentation rep : sip.getRepresentations()) {
      for (TreeNode tn : rep.getFiles()) {
        paths.add(tn.getPath());
        paths.addAll(tn.getFullTreePathsAsPaths());
      }
    }
    try {
      for (Path p : paths) {
        if (Files.isDirectory(p)) {
          folders++;
        } else {
          files++;
          size += Files.readAttributes(p, BasicFileAttributes.class).size();
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void create(SchemaNode schemaNode) {

  }
}
