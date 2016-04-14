package org.roda.rodain.schema.ui;

import javafx.concurrent.Task;
import javafx.scene.control.TreeItem;
import org.roda.rodain.core.Footer;
import org.roda.rodain.core.I18n;
import org.roda.rodain.rules.TreeNode;
import org.roda.rodain.rules.sip.SipPreview;
import org.roda.rodain.rules.sip.SipRepresentation;
import org.roda.rodain.utils.Utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 12-04-2016.
 */
public class SchemeItemToString {
  private int representations, files, folders;
  private long size;
  private StringBuilder sb;
  private List<TreeItem<String>> input;

  protected SchemeItemToString(List<TreeItem<String>> items) {
    files = 0;
    folders = 0;
    representations = 0;
    size = 0;
    sb = new StringBuilder();
    input = new ArrayList<>(items);
  }

  protected void createAndUpdateFooter() {
    Task computeThread = new Task() {
      @Override
      protected Object call() throws Exception {
        Set<SipPreview> sipPreviews = new HashSet<>();
        for (TreeItem<String> item : input) {
          if (item instanceof SipPreviewNode) {
            sipPreviews.add(((SipPreviewNode) item).getSip());
          } else if (item instanceof SchemaNode) {
            sipPreviews.addAll(((SchemaNode) item).getSipPreviews().keySet());
          }
        }
        compute(sipPreviews);
        return null;
      }
    };

    computeThread.setOnSucceeded(event -> {
      if (input.size() == 1) {
        TreeItem item = input.get(0);
        sb.append(item.getValue()).append(": ");
        if (item instanceof SipPreviewNode) {
          sb.append(representations).append(" ").append("reps");
          sb.append(", ");
        }
      } else {
        sb.append(input.size()).append(" ").append(I18n.t("items")).append(": ");
      }
      sb.append(folders).append(" ").append(I18n.t("folders"));
      sb.append(", ");
      sb.append(files).append(" ").append(I18n.t("files"));
      sb.append(", ");
      sb.append(Utils.formatSize(size));
      Footer.setClassPlanStatus(sb.toString());
    });
    new Thread(computeThread).start();
  }

  private void compute(Set<SipPreview> sipPreviews) {
    for (SipPreview sip : sipPreviews) {
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
  }
}
