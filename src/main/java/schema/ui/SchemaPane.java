package schema.ui;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import org.slf4j.LoggerFactory;

import rules.TreeNode;
import schema.ClassificationSchema;
import schema.DescriptionObject;
import core.Footer;

/**
 * Created by adrapereira on 28-09-2015.
 */
public class SchemaPane extends BorderPane {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(SchemaPane.class.getName());
    private TreeView<String> treeView;
    private HBox refresh;
    private GridPane descObjsMetadata, sipMetadata, emptyGrid;
    private SplitPane split;
    //descObjsMetadata
    private Label l_id, l_title, l_parentId, l_level, l_descrpLevel, l_sipsCount, l_sipsSize;
    //sipMetadata
    private Label l_name;
    private TreeView<Object> sipFiles;
    private TreeItem<Object> sipRoot;

    public SchemaPane(Stage stage){
        super();

        createTreeView();
        createTop();
        createMetadata();

        emptyGrid = new GridPane();

        split  = new SplitPane();
        split.setOrientation(Orientation.VERTICAL);
        split.getItems().addAll(treeView, emptyGrid);

        this.setTop(refresh);
        this.setCenter(split);

        this.minWidthProperty().bind(stage.widthProperty().multiply(0.25));
    }

    public void createTop(){
        Button btn = new Button("Update");
        Label title = new Label("Classification Schema");
        title.setFont(Font.font("System", FontWeight.BOLD, 14));

        HBox space = new HBox();
        HBox.setHgrow(space, Priority.ALWAYS);

        refresh = new HBox();
        refresh.setPadding(new Insets(10, 10, 10, 10));
        refresh.setSpacing(10);
        refresh.setAlignment(Pos.TOP_RIGHT);
        refresh.getChildren().addAll(title, space, btn);

        btn.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                Footer.setStatus("Update Classification Schema");
            }
        });
    }

    public void createTreeView(){
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
        treeBox.getChildren().add(treeView);
        treeView.setOnMouseClicked(new SchemaClickedEventHandler(this));
    }

    private void createMetadata(){
        createDescObjsMetadata();
        createSipMetadata();
    }

    public SchemaNode getSelectedItem(){
        int selIndex = treeView.getSelectionModel().getSelectedIndex();
        if(selIndex == -1) return null;
        return (SchemaNode)treeView.getTreeItem(selIndex);
    }
    public void updateMetadata(SchemaNode node){
        split.getItems().remove(emptyGrid);
        split.getItems().remove(sipMetadata);
        if(!split.getItems().contains(descObjsMetadata))
            split.getItems().add(descObjsMetadata);

        DescriptionObject dobject = node.dob;
        l_id.setText(dobject.getId());
        l_descrpLevel.setText(dobject.getDescriptionlevel());
        l_level.setText(dobject.getLevel());
        l_parentId.setText(dobject.getParentId());
        l_title.setText(dobject.getTitle());
        l_sipsCount.setText(node.getSipCount() + " items");
    }

    public void updateMetadata(SipPreviewNode node){
        split.getItems().remove(emptyGrid);
        split.getItems().remove(descObjsMetadata);
        if(!split.getItems().contains(sipMetadata))
            split.getItems().add(sipMetadata);

        l_name.setText(node.getValue());
        sipRoot.getChildren().clear();
        TreeNode startingNode = node.getSip().getFiles();
        TreeItem<Object> startingItem = rec_CreateSipContent(startingNode);
        startingItem.setExpanded(true);
        sipRoot.getChildren().add(startingItem);
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
    
    public void createSipMetadata(){
        sipMetadata = new GridPane();
        sipMetadata.setAlignment(Pos.TOP_LEFT);
        sipMetadata.setHgap(10);
        sipMetadata.setVgap(10);
        sipMetadata.setPadding(new Insets(25, 25, 25, 25));

        Label name = new Label("Name");
        name.setFont(Font.font("System", FontWeight.BOLD, 14));
        sipMetadata.add(name, 0, 1);
        l_name = new Label();
        sipMetadata.add(l_name, 1, 1, 4, 1);

        Label content = new Label("Content");
        content.setFont(Font.font("System", FontWeight.BOLD, 14));
        GridPane.setValignment(content, VPos.BASELINE);
        sipMetadata.add(content, 0, 2);

        //create tree pane
        VBox treeBox=new VBox();
        treeBox.setPadding(new Insets(10, 10, 10, 10));
        treeBox.setSpacing(10);

        sipFiles = new TreeView<Object>();
        // add everything to the tree pane
        treeBox.getChildren().addAll(sipFiles);
        VBox.setVgrow(sipFiles, Priority.ALWAYS);
        HBox.setHgrow(sipFiles, Priority.ALWAYS);

        sipRoot = new TreeItem<Object>();
        sipRoot.setExpanded(true);
        sipFiles.setRoot(sipRoot);
        sipFiles.setShowRoot(false);

        sipMetadata.add(treeBox, 1, 2, 4, 1);
    }

    private void createDescObjsMetadata(){
        descObjsMetadata = new GridPane();
        descObjsMetadata.setAlignment(Pos.TOP_LEFT);
        descObjsMetadata.setHgap(10);
        descObjsMetadata.setVgap(10);
        descObjsMetadata.setPadding(new Insets(25, 25, 25, 25));

        Label id = new Label("ID");
        id.setFont(Font.font("System", FontWeight.BOLD, 14));
        descObjsMetadata.add(id, 0, 1);
        l_id = new Label();
        descObjsMetadata.add(l_id, 1, 1);

        Label title = new Label("Title");
        title.setFont(Font.font("System", FontWeight.BOLD, 14));
        descObjsMetadata.add(title, 0, 2);
        l_title = new Label();
        l_title.setWrapText(true);
        descObjsMetadata.add(l_title, 1, 2);

        Label parID = new Label("Parent ID");
        parID.setFont(Font.font("System", FontWeight.BOLD, 14));
        descObjsMetadata.add(parID, 0, 3);
        l_parentId = new Label();
        descObjsMetadata.add(l_parentId, 1, 3);

        Label level = new Label("Level");
        level.setFont(Font.font("System", FontWeight.BOLD, 14));
        descObjsMetadata.add(level, 0, 4);
        l_level = new Label();
        descObjsMetadata.add(l_level, 1, 4);

        Label descriptionLevel = new Label("Description Level");
        descriptionLevel.setFont(Font.font("System", FontWeight.BOLD, 14));
        descriptionLevel.setMinWidth(100); //don't allow the label to minimize when the pane is shrunk
        descObjsMetadata.add(descriptionLevel, 0, 5);
        l_descrpLevel = new Label();
        l_descrpLevel.setWrapText(true);
        descObjsMetadata.add(l_descrpLevel, 1, 5);

        Label data = new Label("Data");
        data.setFont(Font.font("System", FontWeight.BOLD, 16));
        descObjsMetadata.add(data, 0, 8);

        Label sips = new Label("SIPs");
        sips.setFont(Font.font("System", FontWeight.BOLD, 14));
        descObjsMetadata.add(sips, 0, 9);
        l_sipsCount = new Label();
        descObjsMetadata.add(l_sipsCount, 1, 9);

        Label content = new Label("Content");
        content.setFont(Font.font("System", FontWeight.BOLD, 14));
        descObjsMetadata.add(content, 0, 10);
        l_sipsSize = new Label();
        descObjsMetadata.add(l_sipsSize, 1, 10);
    }

    public TreeView<String> getTreeView() {
        return treeView;
    }
}
