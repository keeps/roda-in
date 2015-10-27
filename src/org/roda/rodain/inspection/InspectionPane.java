package rodain.inspection;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import rodain.schema.ui.SchemaNode;
import rodain.schema.ui.SipPreviewNode;
import rodain.utils.Utils;

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

    private SipPreviewNode currentSIP;

    public InspectionPane(Stage stage){
        createTop();
        createMetadata();
        createContent();

        center = new VBox(10);
        center.setPadding(new Insets(10, 10, 10, 10));

        metadata.minHeightProperty().bind(stage.heightProperty().multiply(0.40));
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

        box.getChildren().add(title);

        metaText = new TextArea();
        metaText.setStyle("-fx-background-color:white; -fx-focus-color: transparent; fx-faint-focus-color: transparent;");
        HBox.setHgrow(metaText, Priority.ALWAYS);
        VBox.setVgrow(metaText, Priority.ALWAYS);
        metadata.getChildren().addAll(box, metaText);

        /* We listen to the focused property and not the text property because we only need to update when the text area loses focus
        * Using text property, we would update after every single character modification, making the application slower
        */
        metaText.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observableValue, Boolean aBoolean, Boolean t1) {
                if(!t1) { //lost focus, so update
                    String oldMetadata = currentSIP.getSip().getMetadata();
                    String newMetadata = metaText.getText();
                    if(! oldMetadata.equals(newMetadata)) { //only update if there's been modifications
                        currentSIP.getSip().setMetadata(metaText.getText());
                        currentSIP.setModified();
                        //update ui
                        String value = currentSIP.getValue();
                        currentSIP.setValue("");
                        currentSIP.setValue(value);
                    }
                }
            }
        });
    }

    private void createContent(){
        content = new BorderPane();
        content.setStyle("-fx-border-width: 1; -fx-border-color: lightgray");
        content.setPadding(new Insets(10, 10, 10, 10));
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
        createContentBottom();
    }

    private void createContentBottom(){
        HBox box = new HBox();
        HBox.setHgrow(box, Priority.ALWAYS);

        Button ignore = new Button("Ignore");
        Button flatten = new Button("Flatten directory");
        Button skip = new Button("Skip Directory");

        ignore.minWidthProperty().bind(content.widthProperty().multiply(0.32));
        flatten.minWidthProperty().bind(content.widthProperty().multiply(0.32));
        skip.minWidthProperty().bind(content.widthProperty().multiply(0.32));

        box.getChildren().addAll(ignore, flatten, skip);
        content.setBottom(box);
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
        if(result instanceof SipContentDirectory)
            ((SipContentDirectory) result).sortChildren();
        return result;
    }

    public void update(SipPreviewNode sip){
        currentSIP = sip;
        createContent(sip);
        center.getChildren().clear();
        center.getChildren().addAll(metadata, content);
        setCenter(center);

        String meta = sip.getSip().getMetadata();
        if(sip.isModified() || meta.equals(""))
            metaText.setText(meta);
        else{
            try {
                metaText.setText(Utils.readFile(meta, Charset.defaultCharset()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Label title = new Label(sip.getValue());
        title.setId("title");
        topBox.getChildren().clear();
        topBox.getChildren().addAll(sip.getGraphic(), title);
    }

    public void update(SchemaNode node){
        currentSIP = null;
        center.getChildren().clear();
        center.getChildren().add(metadata);
        setCenter(center);

        Label title = new Label(node.getValue());
        title.setId("title");
        topBox.getChildren().clear();
        topBox.getChildren().addAll(node.getGraphic(), title);
    }
}
