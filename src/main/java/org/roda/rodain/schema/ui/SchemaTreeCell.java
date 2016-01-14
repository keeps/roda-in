package org.roda.rodain.schema.ui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 12-10-2015.
 */
public class SchemaTreeCell extends TextFieldTreeCell<String> {
  private static final String pattern = "(.+)(  )\\((\\d+ ite[^)]*)\\)";
  /**
   * Creates a new SchemaTreeCell
   */
  public SchemaTreeCell() {
    super(new StringConverter<String>() {
      @Override
      public String toString(String object) {
        if (object == null || object.length() == 0)
          return null;
        Pattern pat = Pattern.compile(pattern);
        Matcher mat = pat.matcher(object);
        if (mat.find()) {
          return mat.group(1);
        }
        return object;
      }

      @Override
      public String fromString(String string) {
        return string;
      }
    });
  }

  @Override
  public void commitEdit(String newValue) {
    super.commitEdit(newValue);
    TreeItem<String> treeItem = getTreeItem();
    if (treeItem != null && treeItem instanceof SchemaNode) {
      SchemaNode node = (SchemaNode) treeItem;
      node.getDob().setTitle(newValue);
    }

  }

  @Override
  public void cancelEdit() {
    String item = super.getItem();
    super.cancelEdit();
    updateItem(item, false);
  }

  @Override
  public void updateItem(String item, boolean empty) {
    super.updateItem(item, empty);
    if (getStyleClass().contains("schemaNodeEmpty"))
      getStyleClass().remove("schemaNodeEmpty");

    if (empty || item == null) {
      setText(null);
      setGraphic(null);
      // To hide the hover color in the empty nodes
      if (getStyleClass().contains("schemaNode"))
        getStyleClass().remove("schemaNode");
      if (getStyleClass().contains("tree-cell"))
        getStyleClass().remove("tree-cell");
    } else {
      if (!getStyleClass().contains("tree-cell"))
        getStyleClass().add("tree-cell");
      if (!getStyleClass().contains("schemaNode"))
        getStyleClass().add("schemaNode");

      HBox hbox = new HBox();
      hbox.setAlignment(Pos.BOTTOM_LEFT);
      Label lab = new Label(item);
      lab.getStyleClass().add("cellText");
      Image icon = null;

      // Get the correct item
      TreeItem<String> treeItem = getTreeItem();
      if (treeItem == null)
        return;

      boolean addHbox = false;
      if (treeItem instanceof SchemaNode) {
        setEditable(true);
        SchemaNode itemNode = (SchemaNode) treeItem;
        icon = itemNode.getImage();
        Pattern pat = Pattern.compile(pattern);
        Matcher mat = pat.matcher(item);
        boolean schemaEmpty = true;
        if (mat.find()) {
          lab = new Label(mat.group(1));
          setText(mat.group(3)); // group 2 is the spaces " "
          addHbox = true;
          schemaEmpty = false;
        } else {
          int sipCount = itemNode.getSipCount();
          if (sipCount > 0) {
            schemaEmpty = false;
            itemNode.setValue(item + "  (" + sipCount + " items)");
          }
        }
        if (schemaEmpty) {
          if (getStyleClass().contains("schemaNode")) {
            getStyleClass().remove("schemaNode");
          }
          getStyleClass().add("schemaNodeEmpty");
        }
      } else {
        if (treeItem instanceof SipPreviewNode) {
          setEditable(false);
          addHbox = true;
          SipPreviewNode sipNode = (SipPreviewNode) treeItem;
          icon = sipNode.getIcon();
          if (sipNode.isMetaModified() || sipNode.isContentModified()) {
            setText("*");
          } else
            setText("");
        }
      }
      if (addHbox) {
        hbox.getChildren().addAll(new ImageView(icon), lab);
        setGraphic(hbox);
      }
    }
  }
}
