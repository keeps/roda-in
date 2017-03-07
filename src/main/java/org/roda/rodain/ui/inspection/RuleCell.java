package org.roda.rodain.ui.inspection;

import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.TreeSet;

import org.roda.rodain.core.Constants;
import org.roda.rodain.core.Constants.RuleType;
import org.roda.rodain.core.I18n;
import org.roda.rodain.ui.RodaInApplication;
import org.roda.rodain.ui.rules.Rule;
import org.roda.rodain.ui.rules.ui.RuleModalController;
import org.roda.rodain.ui.schema.ui.SchemaNode;
import org.roda.rodain.ui.source.items.SourceTreeDirectory;
import org.roda.rodain.ui.source.items.SourceTreeFile;
import org.roda.rodain.ui.source.items.SourceTreeItem;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 16-11-2015.
 */
public class RuleCell extends HBox implements Observer {
  private static final int ANIMATION_DURATION = 150;
  private static final int ITEM_HEIGHT = 21;
  private Rule rule;
  private SchemaNode schemaNode;
  private VBox sourceBox;
  private VBox sourceBoxWrapper;

  private HBox toggleBox;
  private Hyperlink toggleLink;
  private boolean expanded = false, expanding = false;
  private int expandedHeight = 300, collapsedHeight = 115;

  private String titleFormat = I18n.t(Constants.I18N_RULECELL_CREATED_ITEM);
  private Label lCreated;

  /**
   * Creates a new RuleCell, associating it to a Rule.
   *
   * @param node
   *          The SchemaNode being inspected
   * @param rule
   *          The rule to be associated to the cell
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

    Button remove = new Button(I18n.t(Constants.I18N_REMOVE));
    remove.setId("removeRule" + rule.getId());
    remove.setAlignment(Pos.CENTER);

    remove.setOnAction(event -> {
      RuleModalController.removeRule(rule);
      schemaNode.removeRule(rule);
      RodaInApplication.getInspectionPane().notifyChange();
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
    RuleType ruleType = rule.getAssocType();
    String type;
    switch (ruleType) {
      case SINGLE_SIP:
        type = I18n.t(Constants.I18N_ASSOCIATION_SINGLE_SIP_TITLE);
        break;
      case SIP_PER_FILE:
        type = I18n.t(Constants.I18N_ASSOCIATION_SIP_PER_FILE_TITLE);
        break;
      case SIP_WITH_STRUCTURE:
        type = I18n.t(Constants.I18N_ASSOCIATION_SIP_WITH_STRUCTURE_TITLE);
        break;
      case SIP_PER_SELECTION:
        type = I18n.t(Constants.I18N_ASSOCIATION_SIP_SELECTION_TITLE);
        break;
      default:
        type = "Unknown association type";
        break;
    }
    Label lType = new Label(type);
    lType.maxWidthProperty().bind(widthProperty());
    lType.setWrapText(true);

    // source items
    Set<SourceTreeItem> source = rule.getSource();
    Set<String> dirs = new TreeSet<>();
    Set<String> fil = new TreeSet<>();
    for (SourceTreeItem it : source) {
      if (it instanceof SourceTreeDirectory)
        dirs.add(it.getValue());
      else
        fil.add(it.getValue());
    }

    HBox contentSummary = buildContentSummary(dirs, fil);

    toggleBox = new HBox();
    toggleLink = new Hyperlink(I18n.t(Constants.I18N_EXPAND));
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
    sourceBoxWrapper = new VBox();
    content.getChildren().addAll(lType, contentSummary, sourceBoxWrapper, toggleBox);

    expandedHeight = collapsedHeight + dirs.size() * ITEM_HEIGHT + fil.size() * ITEM_HEIGHT;

    return content;
  }

  private void expand() {
    expanding = true;
    sourceBoxWrapper.getChildren().add(sourceBox);
    Timeline animation = new Timeline();
    animation.getKeyFrames()
      .add(new KeyFrame(Duration.millis(ANIMATION_DURATION), new KeyValue(minHeightProperty(), expandedHeight),
        new KeyValue(maxHeightProperty(), expandedHeight), new KeyValue(prefHeightProperty(), expandedHeight)));
    animation.setOnFinished(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        expanded = true;
        expanding = false;
        toggleLink.setText(I18n.t(Constants.I18N_COLLAPSE));
      }
    });
    animation.play();
  }

  private void collapse() {
    expanding = true;
    Timeline animation = new Timeline();
    animation.getKeyFrames()
      .add(new KeyFrame(Duration.millis(ANIMATION_DURATION), new KeyValue(minHeightProperty(), collapsedHeight),
        new KeyValue(maxHeightProperty(), collapsedHeight), new KeyValue(prefHeightProperty(), collapsedHeight)));
    animation.setOnFinished(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        expanded = false;
        expanding = false;
        sourceBoxWrapper.getChildren().remove(sourceBox);
        toggleLink.setText(I18n.t(Constants.I18N_EXPAND));
      }
    });
    animation.play();
  }

  private HBox buildContentSummary(Set<String> dirs, Set<String> fil) {
    HBox result = new HBox();
    StringBuilder sb = new StringBuilder("Input: ");
    if (!dirs.isEmpty()) {
      sb.append(dirs.size());
      if (dirs.size() == 1) {
        sb.append(I18n.t(Constants.I18N_DIRECTORY));
      } else {
        sb.append(I18n.t(Constants.I18N_DIRECTORIES));
      }
      if (!fil.isEmpty()) {
        sb.append(", ");
      }
    }
    if (!fil.isEmpty()) {
      sb.append(fil.size());
      if (fil.size() == 1) {
        sb.append(I18n.t(Constants.I18N_FILE));
      } else {
        sb.append(I18n.t(Constants.I18N_FILES));
      }
    }
    Label filDirLabel = new Label(sb.toString());
    result.getChildren().add(filDirLabel);
    return result;
  }

  /**
   * Updates the created SIPs count label
   *
   * @param o
   *          The Observable object. Should be a Rule.
   * @param arg
   *          The arguments of the update.
   */
  @Override
  public void update(Observable o, Object arg) {
    if (o == rule) {
      Platform.runLater(() -> {
        if (arg instanceof String && Constants.EVENT_REMOVED_RULE.equals(arg)) {
          RuleModalController.removeRule(rule);
          schemaNode.removeRule(rule);
          RodaInApplication.getInspectionPane().notifyChange();
        } else {
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
