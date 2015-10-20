package schema.ui;

import core.Main;
import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import rules.TreeNode;

/**
 * Created by adrapereira on 21-09-2015.
 */
public class SchemaClickedEventHandler implements EventHandler<MouseEvent> {
    private TreeView<String> treeView;
    private SchemaPane spane;

    public SchemaClickedEventHandler(SchemaPane pane){
        this.treeView = pane.getTreeView();
        spane = pane;
    }

    @Override
    public void handle(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 1) {
            /*
            * Used to select the schema node.
            * TODO
            */
            TreeItem source = treeView.getSelectionModel().getSelectedItem();
            if(source instanceof SipPreviewNode){
                Main.getRulePane().updateMetadata((SipPreviewNode)source);
            }
        }
    }
}
