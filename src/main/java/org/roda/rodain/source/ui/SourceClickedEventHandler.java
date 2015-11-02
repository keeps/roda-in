package org.roda.rodain.source.ui;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;

import org.roda.rodain.source.ui.items.SourceTreeFile;
import org.roda.rodain.source.ui.items.SourceTreeLoadMore;
import org.roda.rodain.source.ui.items.SourceTreeLoading;
import org.roda.rodain.source.ui.items.SourceTreeDirectory;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 21-09-2015.
 */
public class SourceClickedEventHandler implements EventHandler<MouseEvent> {
    private TreeView<String> treeView;
    private FileExplorerPane fep;

    public SourceClickedEventHandler(FileExplorerPane pane){
        this.treeView = pane.getTreeView();
        fep = pane;
    }

    @Override
    public void handle(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 1) {
            TreeItem<String> item = treeView.getSelectionModel().getSelectedItem();
            if(item instanceof SourceTreeLoadMore) {
                final SourceTreeDirectory parent = (SourceTreeDirectory)item.getParent();
                parent.getChildren().remove(item);
                SourceTreeLoading loading = new SourceTreeLoading();
                parent.getChildren().add(loading);

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        parent.loadMore();
                    }
                });
            }else if(item instanceof SourceTreeDirectory){
                SourceTreeDirectory directory = (SourceTreeDirectory)item;
                fep.updateMetadata(directory.getPath());
            }else if(item instanceof SourceTreeFile) {
                SourceTreeFile directory = (SourceTreeFile) item;
                fep.updateMetadata(directory.getPath());
            }
        }
    }
}
