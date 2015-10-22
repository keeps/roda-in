package rodain.rules.ui;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import rodain.rules.TreeNode;
import rodain.schema.ui.SipContentDirectory;
import rodain.schema.ui.SipContentFile;
import rodain.schema.ui.SipPreviewNode;

/**
 * Created by adrapereira on 24-09-2015.
 */
public class RulesPane extends BorderPane {
    private HBox createRule;
    private TreeView sipFiles;
    private TreeItem sipRoot;

    public RulesPane(Stage stage){
        createCreateRule();

        //create tree pane
        VBox treeBox=new VBox();
        treeBox.setPadding(new Insets(10, 10, 10, 10));
        treeBox.setSpacing(10);

        sipFiles = new TreeView<>();
        sipFiles.setStyle("-fx-background-color:white;");
        // add everything to the tree pane
        treeBox.getChildren().addAll(sipFiles);
        VBox.setVgrow(sipFiles, Priority.ALWAYS);
        HBox.setHgrow(sipFiles, Priority.ALWAYS);

        sipRoot = new TreeItem<>();
        sipRoot.setExpanded(true);
        sipFiles.setRoot(sipRoot);
        sipFiles.setShowRoot(false);

        this.setTop(createRule);
        this.setCenter(sipFiles);
        this.minWidthProperty().bind(stage.widthProperty().multiply(0.33));
    }

    private void createCreateRule(){
        Label title = new Label("Mapping Rules");
        title.setFont(Font.font("System", FontWeight.BOLD, 14));

        createRule = new HBox();
        createRule.setPadding(new Insets(10, 10, 10, 10));
        createRule.setSpacing(10);
        createRule.setAlignment(Pos.TOP_LEFT);
        createRule.getChildren().add(title);

    }

    public void updateMetadata(SipPreviewNode node){
        sipRoot.getChildren().clear();
        Set<TreeNode> files = node.getSip().getFiles();
        for(TreeNode treeNode: files) {
            TreeItem<Object> startingItem = rec_CreateSipContent(treeNode);
            startingItem.setExpanded(true);
            sipRoot.getChildren().add(startingItem);
        }
    }

    private TreeItem<Object> rec_CreateSipContent(TreeNode node){
        TreeItem<Object> result;
        Path path = Paths.get(node.getPath());
        if(Files.isDirectory(path))
            result = new SipContentDirectory(path);
        else return new SipContentFile(path);

        for(String key: node.getKeys()){
            TreeItem<Object> temp = rec_CreateSipContent(node.get(key));
            result.getChildren().add(temp);
        }
        return result;
    }
}
