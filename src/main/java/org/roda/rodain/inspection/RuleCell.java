package org.roda.rodain.inspection;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import org.roda.rodain.core.AppProperties;
import org.roda.rodain.core.Main;
import org.roda.rodain.rules.Rule;
import org.roda.rodain.rules.RuleTypes;
import org.roda.rodain.rules.ui.RuleModalController;
import org.roda.rodain.schema.ui.SchemaNode;
import org.roda.rodain.source.ui.items.SourceTreeDirectory;
import org.roda.rodain.source.ui.items.SourceTreeFile;
import org.roda.rodain.source.ui.items.SourceTreeItem;

import java.sql.Time;
import java.util.*;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 16-11-2015.
 */
public class RuleCell extends HBox implements Observer {
  private static final int ANIMATION_DURATION = 150;
  private Rule rule;
  private SchemaNode schemaNode;
  private VBox sourceBox;
  private Separator separator;
  private VBox sourceBoxWrapper;

  private HBox toggleBox;
  private Hyperlink toggleLink;
  private boolean expanded = false, expanding = false;
  private int expandedHeight = 300, collapsedHeight = 115;

  private String titleFormat = "Created %d item";
  private Label lCreated;

  /**
   * Creates a new RuleCell, associating it to a Rule.
   *
   * @param node The SchemaNode being inspected
   * @param rule The rule to be associated to the cell
   */
  public RuleCell(SchemaNode node, Rule rule) {
    this.rule = rule;
    this.schemaNode = node;
    this.getStyleClass().add("ruleCell");

    VBox root = new VBox(5);
    HBox.setHgrow(root, Priority.ALWAYS);

    HBox top = createTop();
    VBox center = createCenter();

    root.getChildren().addAll(top, center);
    getChildren().add(root);

    setMinHeight(collapsedHeight);
    setMaxHeight(collapsedHeight);
    setPrefHeight(collapsedHeight);
  }

  private HBox createTop() {
    HBox top = new HBox(10);
    top.setPadding(new Insets(5, 5, 5, 5));
    top.setAlignment(Pos.CENTER_LEFT);
    HBox.setHgrow(top, Priority.ALWAYS);
    top.getStyleClass().add("top");

    HBox spaceLeft = new HBox();
    HBox spaceRight = new HBox();
    HBox.setHgrow(spaceLeft, Priority.ALWAYS);
    HBox.setHgrow(spaceRight, Priority.ALWAYS);

    Label id = new Label("#" + rule.getId());
    id.getStyleClass().add("title");

    Button remove = new Button("Remove");
    remove.setId("removeRule" + rule.getId());
    remove.setAlignment(Pos.CENTER);

    remove.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent e) {
        RuleModalController.removeRule(rule);
        schemaNode.removeRule(rule);
        Main.getInspectionPane().notifyChange();
      }
    });

    int sipCount = rule.getSipCount();
    String format = titleFormat;
    if (sipCount != 1) {
      format += "s";
    }
    String created = String.format(format, rule.getSipCount());
    lCreated = new Label(created);

    top.getChildren().addAll(id, spaceLeft, lCreated, spaceRight, remove);
    return top;
  }

  private VBox createCenter() {
    VBox content = new VBox(5);
    content.setPadding(new Insets(5, 5, 5, 5));

    // rule type
    RuleTypes ruleType = rule.getAssocType();
    String type;
    switch (ruleType) {
      case SINGLE_SIP:
        type = AppProperties.getConfig("association.singleSip.title");
        break;
      case SIP_PER_FILE:
        type = AppProperties.getConfig("association.sipPerFile.title");
        break;
      case SIP_PER_FOLDER:
        type = AppProperties.getConfig("association.sipPerFolder.title");
        break;
      case SIP_PER_SELECTION:
        type = AppProperties.getConfig("association.sipSelection.title");
        break;
      default:
        type = "Unknown association type";
        break;
    }
    Label lType = new Label(type);

    // source items
    Set<SourceTreeItem> source = rule.getSource();
    ArrayList<String> dirs = new ArrayList<>();
    ArrayList<String> fil = new ArrayList<>();
    for (SourceTreeItem it : source) {
      if (it instanceof SourceTreeDirectory)
        dirs.add(it.getValue());
      else
        fil.add(it.getValue());
    }

    HBox contentSummary = buildContentSummary(dirs, fil);

    toggleBox = new HBox();
    toggleLink = new Hyperlink("Expand");
    toggleLink.setTextAlignment(TextAlignment.CENTER);
    toggleBox.getChildren().add(toggleLink);
    HBox.setHgrow(toggleBox, Priority.ALWAYS);
    toggleBox.setAlignment(Pos.CENTER);
    toggleLink.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        if (!expanding) {
          if (expanded) {
            collapse();
          } else {
            expand();
          }
        }
      }
    });

    sourceBox = new VBox(5);
    for (String s : dirs) {
      Label directory = new Label();
      directory.maxWidthProperty().bind(widthProperty().subtract(20));
      directory.setGraphic(new ImageView(SourceTreeDirectory.folderCollapseImage));
      directory.setWrapText(true);
      directory.setText(s);
      sourceBox.getChildren().add(directory);
    }
    for (String s : fil) {
      Label file = new Label();
      file.maxWidthProperty().bind(widthProperty().subtract(20));
      file.setGraphic(new ImageView(SourceTreeFile.fileImage));
      file.setWrapText(true);
      file.setText(s);
      sourceBox.getChildren().add(file);
    }

    separator = new Separator();
    sourceBoxWrapper = new VBox();
    content.getChildren().addAll(lType, contentSummary, sourceBoxWrapper, toggleBox);

    expandedHeight = collapsedHeight + dirs.size() * 20 + fil.size() * 20;

    return content;
  }

  private void expand() {
    expanding = true;
    sourceBoxWrapper.getChildren().addAll(separator, sourceBox);
    Timeline animation = new Timeline();
    animation.getKeyFrames().add(new KeyFrame(Duration.millis(ANIMATION_DURATION),
        new KeyValue(minHeightProperty(), expandedHeight),
        new KeyValue(maxHeightProperty(), expandedHeight),
        new KeyValue(prefHeightProperty(), expandedHeight)));
    animation.setOnFinished(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        expanded = true;
        expanding = false;
        toggleLink.setText("Collapse");
      }
    });
    animation.play();
  }

  private void collapse() {
    expanding = true;
    Timeline animation = new Timeline();
    animation.getKeyFrames().add(new KeyFrame(Duration.millis(ANIMATION_DURATION),
        new KeyValue(minHeightProperty(), collapsedHeight),
        new KeyValue(maxHeightProperty(), collapsedHeight),
        new KeyValue(prefHeightProperty(), collapsedHeight)));
    animation.setOnFinished(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        expanded = false;
        expanding = false;
        sourceBoxWrapper.getChildren().removeAll(separator, sourceBox);
        toggleLink.setText("Expand");
      }
    });
    animation.play();
  }

  private HBox buildContentSummary(ArrayList<String> dirs, ArrayList<String> fil) {
    HBox result = new HBox();
    StringBuilder sb = new StringBuilder();
    if (!dirs.isEmpty()) {
      sb.append(dirs.size());
      if (dirs.size() == 1)
        sb.append(" directory");
      else sb.append(" directories");
      sb.append(", ");
      Label dirsLabel = new Label(sb.toString());
      dirsLabel.setGraphic(new ImageView(SourceTreeDirectory.folderCollapseImage));
      result.getChildren().add(dirsLabel);
    }
    sb = new StringBuilder();
    if (!fil.isEmpty()) {
      sb.append(fil.size());
      if (fil.size() == 1)
        sb.append(" file");
      else sb.append(" files");
      Label filLabel = new Label(sb.toString());
      filLabel.setGraphic(new ImageView(SourceTreeFile.fileImage));
      result.getChildren().add(filLabel);
    }
    return result;
  }

  /**
   * Updates the created SIPs count label
   *
   * @param o   The Observable object. Should be a Rule.
   * @param arg The arguments of the update.
   */
  @Override
  public void update(Observable o, Object arg) {
    if (o == rule) {
      Platform.runLater(new Runnable() {
        @Override
        public void run() {
          int sipCount = rule.getSipCount();
          String format = titleFormat;
          if (sipCount != 1) {
            format += "s";
          }
          String created = String.format(format, rule.getSipCount());
          lCreated.setText(created);
        }
      });
    }
  }
}
