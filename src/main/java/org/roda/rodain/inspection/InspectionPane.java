package org.roda.rodain.inspection;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import org.roda.rodain.rules.TreeNode;
import org.roda.rodain.rules.sip.SipPreview;
import org.roda.rodain.schema.ui.SchemaNode;
import org.roda.rodain.schema.ui.SipPreviewNode;
import org.slf4j.LoggerFactory;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 26-10-2015.
 */
public class InspectionPane extends BorderPane {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(InspectionPane.class.getName());
    private HBox topBox;
    private VBox center;

    private VBox metadata;
    private TextArea metaText;

    private BorderPane content;
    private TreeView sipFiles;
    private TreeItem sipRoot;

    private Button remove;

    private SipPreview currentSIP;

    public InspectionPane(Stage stage){
        createTop();
        createMetadata();
        createContent();
        createBottom();

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
        metaText.setWrapText(true);
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
                    String oldMetadata = currentSIP.getMetadataContent();
                    String newMetadata = metaText.getText();
                    if(! oldMetadata.equals(newMetadata)) { //only update if there's been modifications
                        currentSIP.updateMetadata(metaText.getText());
                    }
                }
            }
        });
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
                return new InspectionTreeCell();
            }
        });

        sipFiles.setOnMouseClicked(
                new EventHandler<MouseEvent>() {
                   @Override
                   public void handle(MouseEvent mouseEvent) {
                       if (mouseEvent.getClickCount() == 2) {
                           Object source = sipFiles.getSelectionModel().getSelectedItem();
                           if(source instanceof SipContentFile) {
                               SipContentFile sipFile = (SipContentFile) source;
                               try {
                                   Desktop.getDesktop().open(new File(sipFile.getPath().toString()));
                               } catch (IOException e) {
                                   log.info("Error opening file from SIP content", e);
                               }
                           }
                       }
                   }
               }
        );

        sipRoot = new TreeItem<>();
        sipRoot.setExpanded(true);
        sipFiles.setRoot(sipRoot);
        sipFiles.setShowRoot(false);
        content.setCenter(sipFiles);
        createContentBottom();
    }

    private void createContentBottom() {
        HBox box = new HBox();
        box.setPadding(new Insets(10,10,10,10));
        HBox.setHgrow(box, Priority.ALWAYS);

        Button ignore = new Button("Ignore");
        Button flatten = new Button("Flatten directory");
        flatten.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                Object selected = sipFiles.getSelectionModel().getSelectedItem();
                if(selected instanceof SipContentDirectory){
                    SipContentDirectory dir = (SipContentDirectory) selected;
                    dir.flatten();
                }
            }
        });
        Button skip = new Button("Skip Directory");
        skip.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                Object selected = sipFiles.getSelectionModel().getSelectedItem();
                if(selected instanceof SipContentDirectory){
                    SipContentDirectory dir = (SipContentDirectory) selected;
                    dir.skip();
                    // clear the parent and recreate the children based on the updated tree nodes
                    SipContentDirectory parent = (SipContentDirectory)dir.getParent();
                    TreeItem newParent = recCreateSipContent(parent.getTreeNode());
                    parent.getChildren().clear();
                    parent.getChildren().addAll(newParent.getChildren());
                    parent.sortChildren();
                }
            }
        });

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
        SipContentDirectory result;
        Path path = node.getPath();
        if(Files.isDirectory(path))
            result = new SipContentDirectory(node);
        else return new SipContentFile(path);

        for(String key: node.getKeys()){
            TreeItem<Object> temp = recCreateSipContent(node.get(key));
            result.getChildren().add(temp);
        }
        result.sortChildren();
        return result;
    }

    public void createBottom(){
        HBox bottom = new HBox();
        bottom.setPadding(new Insets(10,10,10,10));
        bottom.setAlignment(Pos.CENTER_LEFT);

        remove = new Button("Remove");
        bottom.getChildren().add(remove);

        setBottom(bottom);
    }

    public void update(SipPreviewNode sip){
        currentSIP = sip.getSip();
        createContent(sip);
        center.getChildren().clear();
        center.getChildren().addAll(metadata, content);
        setCenter(center);

        String meta = sip.getSip().getMetadataContent();
        metaText.setText(meta);

        Label title = new Label(sip.getValue());
        title.setId("title");
        topBox.getChildren().clear();
        topBox.getChildren().addAll(sip.getGraphic(), title);

        remove.setDisable(false);
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

        remove.setDisable(true);
    }
}
