package org.roda.rodain.inspection;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.apache.commons.lang.StringUtils;
import org.roda.rodain.core.Main;
import org.roda.rodain.rules.Rule;
import org.roda.rodain.rules.RuleTypes;
import org.roda.rodain.rules.ui.RuleModalController;
import org.roda.rodain.schema.ui.SchemaNode;
import org.roda.rodain.source.ui.items.SourceTreeDirectory;
import org.roda.rodain.source.ui.items.SourceTreeFile;
import org.roda.rodain.source.ui.items.SourceTreeItem;

import java.util.*;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 16-11-2015.
 */
public class RuleCell extends HBox implements Observer {
  private static Properties properties;
  private Rule rule;
  private SchemaNode schemaNode;

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
        type = properties.getProperty("association.singleSip.title");
        break;
      case SIP_PER_FILE:
        type = properties.getProperty("association.sipPerFile.title");
        break;
      case SIP_PER_FOLDER:
        type = properties.getProperty("association.sipPerFolder.title");
        break;
      case SIP_PER_SELECTION:
        type = properties.getProperty("association.sipSelection.title");
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

    VBox sourceBox = new VBox(5);
    if (!dirs.isEmpty()) {
      Label directories = new Label();
      directories.maxWidthProperty().bind(widthProperty().subtract(20));
      directories.setGraphic(new ImageView(SourceTreeDirectory.folderCollapseImage));
      directories.setWrapText(true);
      String directoriesString = StringUtils.join(dirs, ", ");
      directories.setText(directoriesString);
      sourceBox.getChildren().add(directories);
    }
    if (!fil.isEmpty()) {
      Label files = new Label();
      files.maxWidthProperty().bind(widthProperty().subtract(20));
      files.setGraphic(new ImageView(SourceTreeFile.fileImage));
      files.setWrapText(true);
      String filesString = StringUtils.join(fil, ", ");
      files.setText(filesString);
      sourceBox.getChildren().add(files);
    }
    content.getChildren().addAll(lType, sourceBox);

    return content;
  }

  /**
   * Sets the Properties object of RuleCell.
   *
   * @param prop The new Properties object.
   */
  public static void setProperties(Properties prop) {
    properties = prop;
  }

  /**
   * Updates the created SIPs count label
   *
   * @param o   The Observable object. Should be a Rule.
   * @param arg The arguments of the update.
   */
  @Override
  public void update(Observable o, Object arg) {
    if (o instanceof Rule) {
      Rule rule = (Rule) o;

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
