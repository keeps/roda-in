package org.roda.rodain.ui.source;

import org.roda.rodain.core.ConfigurationManager;
import org.roda.rodain.core.Constants;
import org.roda.rodain.core.Constants.PathState;
import org.roda.rodain.core.I18n;
import org.roda.rodain.ui.source.items.SourceTreeDirectory;
import org.roda.rodain.ui.source.items.SourceTreeFile;
import org.roda.rodain.ui.source.items.SourceTreeItem;
import org.roda.rodain.ui.source.items.SourceTreeLoadMore;
import org.roda.rodain.ui.source.items.SourceTreeLoading;

import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 12-10-2015.
 */
public class SourceTreeCell extends TreeCell<String> {
  private ContextMenu menu = new ContextMenu();

  /**
   * Instantiates a new SourceTreeCell object.
   */
  public SourceTreeCell() {
    MenuItem removeIgnore = new MenuItem(I18n.t(Constants.I18N_SOURCE_TREE_CELL_REMOVE));
    removeIgnore.setId("removeIgnore");
    menu.getItems().add(removeIgnore);
    removeIgnore.setOnAction(event -> {
      TreeItem<String> treeItem = getTreeItem();
      SourceTreeItem sti = (SourceTreeItem) treeItem;
      sti.removeIgnore();
      // force update
      String value = treeItem.getValue();
      treeItem.setValue("");
      treeItem.setValue(value);
    });
  }

  @Override
  public void updateItem(String item, boolean empty) {
    super.updateItem(item, empty);

    if (!empty) {
      HBox hbox = new HBox(5);
      hbox.setAlignment(Pos.CENTER_LEFT);
      Label lab = new Label(item);
      Label optionalLabel = null;
      lab.getStyleClass().add(Constants.CSS_CELLTEXT);
      lab.setId("");
      Image icon = null;

      // Remove the context menu
      setContextMenu(null);

      // Get the correct item
      TreeItem<String> treeItem = getTreeItem();
      SourceTreeItem sti = (SourceTreeItem) treeItem;

      // if the item is a file and we're not showing files, clear the cell and
      // return
      if (sti instanceof SourceTreeFile && !FileExplorerPane.isShowFiles()) {
        empty();
        return;
      }

      if (sti == null) {
        return;
      }

      if (sti.getState() == PathState.IGNORED) {
        // the context menu only makes sense if the item is ignored
        setContextMenu(menu);
        lab.setId("ignored");
      }
      if (sti.getState() == PathState.MAPPED) {
        lab.setStyle(ConfigurationManager.getStyle("source.cell.mapped"));
      }

      if (treeItem instanceof SourceTreeDirectory) {
        if (treeItem.isExpanded())
          icon = SourceTreeDirectory.folderExpandImage;
        else
          icon = SourceTreeDirectory.folderCollapseImage;
        if (((SourceTreeDirectory) treeItem).getParentDir() == null) {
          optionalLabel = new Label(((SourceTreeDirectory) treeItem).getPath());
          optionalLabel.setStyle(ConfigurationManager.getStyle("source.cell.mapped"));
        }
      } else {
        if (treeItem instanceof SourceTreeFile)
          icon = SourceTreeFile.fileImage;
        else if (treeItem instanceof SourceTreeLoadMore)
          icon = SourceTreeLoadMore.fileImage;
      }

      if (treeItem instanceof SourceTreeLoadMore) {
        lab.setText(I18n.t(Constants.I18N_SOURCE_TREE_LOAD_MORE_TITLE));
      }
      if (treeItem instanceof SourceTreeLoading) {
        lab.setText(I18n.t(Constants.I18N_SOURCE_TREE_LOADING_TITLE));
      }

      hbox.getChildren().addAll(new ImageView(icon), lab);
      if (optionalLabel != null)
        hbox.getChildren().add(optionalLabel);
      setGraphic(hbox);
    } else
      empty();
  }

  private void empty() {
    setText(null);
    setGraphic(null);
  }
}
