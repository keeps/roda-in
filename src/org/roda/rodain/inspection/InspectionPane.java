package rodain.inspection;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javafx.util.Callback;
import rodain.rules.TreeNode;
import rodain.schema.ui.*;

/**
 * Created by adrapereira on 26-10-2015.
 */
public class InspectionPane extends BorderPane {
    private HBox topBox;
    private VBox center;

    private VBox metadata;
    private TextArea metaText;

    private BorderPane content;
    private TreeView sipFiles;
    private TreeItem sipRoot;

    public InspectionPane(Stage stage){
        createTop();
        createMetadata();
        createContent();

        center = new VBox(10);
        center.setPadding(new Insets(10, 10, 10, 10));

        this.minWidthProperty().bind(stage.widthProperty().multiply(0.33));
    }

    private void createTop(){
        Label top = new Label(" ");
        topBox = new HBox(5);
        topBox.getChildren().add(top);
        topBox.setPadding(new Insets(15, 10, 5, 10));
        topBox.setAlignment(Pos.CENTER_LEFT);
        setTop(topBox);
    }

    private void createMetadata(){
        metadata = new VBox();
        metadata.setStyle("-fx-border-width: 1; -fx-border-color: lightgray");

        HBox box = new HBox();
        box.setPadding(new Insets(5, 10, 5, 10));
        box.setAlignment(Pos.CENTER_LEFT);
        box.setStyle("-fx-background-color: lightgray");

        Label title = new Label("Metadata");
        title.setStyle("-fx-font-size: 14pt");
        Button load = new Button("Load from file");
        HBox space = new HBox();
        HBox.setHgrow(space, Priority.ALWAYS);

        box.getChildren().addAll(title, space, load);

        metaText = new TextArea();
        metaText.setStyle("-fx-background-color:white; -fx-focus-color: transparent; fx-faint-focus-color: transparent;");
        HBox.setHgrow(metaText, Priority.ALWAYS);
        metadata.getChildren().addAll(box, metaText);
    }

    private void createContent(){
        content = new BorderPane();
        content.setStyle("-fx-border-width: 1; -fx-border-color: lightgray");
        VBox.setVgrow(content, Priority.ALWAYS);

        HBox top = new HBox();
        top.setStyle("-fx-background-color: lightgray");
        top.setPadding(new Insets(5, 10, 5, 10));
        Label title = new Label("Content");
        title.setStyle("-fx-font-size: 14pt");
        top.getChildren().add(title);
        content.setTop(top);

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

        sipFiles.setCellFactory(new Callback<TreeView<String>, TreeCell<String>>() {
            @Override
            public TreeCell<String> call(TreeView<String> p) {
                InspectionTreeCell cell = new InspectionTreeCell();
                return cell;
            }
        });

        sipRoot = new TreeItem<>();
        sipRoot.setExpanded(true);
        sipFiles.setRoot(sipRoot);
        sipFiles.setShowRoot(false);
        content.setCenter(sipFiles);
    }

    private void createContent(SipPreviewNode node){
        sipRoot.getChildren().clear();
        Set<TreeNode> files = node.getSip().getFiles();
        for(TreeNode treeNode: files) {
            TreeItem<Object> startingItem = recCreateSipContent(treeNode);
            startingItem.setExpanded(true);
            sipRoot.getChildren().add(startingItem);
        }
    }

    private TreeItem<Object> recCreateSipContent(TreeNode node){
        TreeItem<Object> result;
        Path path = Paths.get(node.getPath());
        if(Files.isDirectory(path))
            result = new SipContentDirectory(path);
        else return new SipContentFile(path);

        for(String key: node.getKeys()){
            TreeItem<Object> temp = recCreateSipContent(node.get(key));
            result.getChildren().add(temp);
        }
        return result;
    }

    public void update(SipPreviewNode sip){
        createContent(sip);
        center.getChildren().clear();
        center.getChildren().addAll(metadata, content);
        setCenter(center);

        metaText.setText(sip.getSip().getMetadata());

        Label title = new Label(sip.getValue());
        title.setId("title");
        topBox.getChildren().clear();
        topBox.getChildren().addAll(sip.getGraphic(), title);
    }

    public void update(SchemaNode node){
        center.getChildren().clear();
        center.getChildren().add(metadata);
        setCenter(center);

        Label title = new Label(node.getValue());
        title.setId("title");
        topBox.getChildren().clear();
        topBox.getChildren().addAll(node.getGraphic(), title);
    }
}
