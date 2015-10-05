package schema.ui;

import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;

import org.slf4j.LoggerFactory;

/**
 * Created by adrapereira on 21-09-2015.
 */
public class SchemaClickedEventHandler implements EventHandler<MouseEvent> {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(SchemaClickedEventHandler.class.getName());
    private TreeView<String> treeView;
    private SchemaPane spane;

    public SchemaClickedEventHandler(SchemaPane pane){
        this.treeView = pane.getTreeView();
        spane = pane;
    }

    public void handle(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 1) {
            TreeItem<String> item = treeView.getSelectionModel().getSelectedItem();
            if(item instanceof SchemaNode) {
                SchemaNode node = (SchemaNode) item;
                if (node != null)
                    spane.updateMetadata(node);
            }else if(item instanceof SipPreviewNode){
                SipPreviewNode node = (SipPreviewNode)item;
                spane.updateMetadata(node);
            }
        }
    }
}
