package SourceUI;

import SourceUI.Items.SourceTreeDirectory;
import SourceUI.Items.SourceTreeLoadMore;
import SourceUI.Items.SourceTreeLoading;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;

/**
 * Created by adrap on 21-09-2015.
 */
public class ClickedEventHandler implements EventHandler<MouseEvent> {

    private TreeView<String> treeView;

    public ClickedEventHandler(TreeView<String> treeView){
        this.treeView = treeView;
    }

    public void handle(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 1) {
            TreeItem<String> item = treeView.getSelectionModel().getSelectedItem();
            if(item instanceof SourceTreeLoadMore) {
                final SourceTreeDirectory parent = (SourceTreeDirectory)item.getParent();
                parent.getChildren().remove(item);
                final SourceTreeLoading loading = new SourceTreeLoading();
                parent.getChildren().add(loading);

                Platform.runLater(new Runnable() {
                    public void run() {
                        parent.loadMore();
                    }
                });

            }
        }
    }
}
