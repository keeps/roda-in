package schema.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import schema.ClassificationSchema;
import schema.DescriptionObject;

/**
 * Created by adrapereira on 28-09-2015.
 */
public class SchemaPane extends StackPane{
    private TreeView<String> treeView;

    public SchemaPane(Stage stage){
        //create tree pane
        VBox treeBox=new VBox();
        treeBox.setPadding(new Insets(10, 10, 10, 10));
        treeBox.setSpacing(10);

        TreeItem<String> rootNode = new TreeItem<String>();
        rootNode.setExpanded(true);

        // get the classification schema and add all its nodes to the tree
        ClassificationSchema cs = ClassificationSchema.instantiate();
        for(DescriptionObject obj: cs.getDos()){
            SchemaNode sn = new SchemaNode(obj);
            rootNode.getChildren().add(sn);
        }

        // create the tree view
        treeView=new TreeView<String>(rootNode);
        treeView.setShowRoot(false);
        // add everything to the tree pane
        treeBox.getChildren().addAll(new Label("Classification Schema"), treeView);
        VBox.setVgrow(treeView, Priority.ALWAYS);

        this.getChildren().add(treeBox);

        this.minWidthProperty().bind(stage.widthProperty().multiply(0.2));
    }

    public String getSelectedItem(){
        int selIndex = treeView.getSelectionModel().getSelectedIndex();
        if(selIndex == -1) return null;
        SchemaNode item = (SchemaNode)treeView.getTreeItem(selIndex);
        return item.getValue();
    }
}
