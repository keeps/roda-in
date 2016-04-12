package org.roda.rodain.schema.ui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;

import org.roda.rodain.rules.TreeNode;
import org.roda.rodain.rules.sip.SipPreview;
import org.roda.rodain.rules.sip.SipRepresentation;

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

  public String create(SipPreviewNode sipPreviewNode) {
    SipPreview sip = sipPreviewNode.getSip();
    representations = sip.getRepresentations().size();
    Set<Path> paths = new HashSet<>();
    for (SipRepresentation rep : sip.getRepresentations()) {
      for (TreeNode tn : rep.getFiles()) {
        paths.add(tn.getPath());
        paths.addAll(tn.getFullTreePathsAsPaths());
      }
    }
    files = 0;
    folders = 0;
    size = 0;
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
}
