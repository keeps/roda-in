package schema.ui;

import java.util.Set;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Callback;

import rules.VisitorStack;
import rules.ui.RuleModalController;
import schema.ClassificationSchema;
import schema.DescriptionObject;
import source.ui.items.SourceTreeDirectory;
import source.ui.items.SourceTreeFile;
import source.ui.items.SourceTreeItem;
import core.Footer;
import core.Main;

/**
 * Created by adrapereira on 28-09-2015.
 */
public class SchemaPane extends BorderPane {
    private TreeView<String> treeView;
    private HBox refresh;
    private HBox bottom;
    private VisitorStack visitors = new VisitorStack();
    private Stage primaryStage;


    public SchemaPane(Stage stage){
        super();

        primaryStage = stage;

        createTreeView();
        createTop();
        createBottom();

        this.setTop(refresh);
        this.setCenter(treeView);
        this.setBottom(bottom);

        this.minWidthProperty().bind(stage.widthProperty().multiply(0.33));
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
            @Override
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

        TreeItem<String> rootNode = new TreeItem<>();
        rootNode.setExpanded(true);

        // get the classification schema and add all its nodes to the tree
        ClassificationSchema cs = ClassificationSchema.instantiate();
        for(DescriptionObject obj: cs.getDos()){
            SchemaNode sn = new SchemaNode(obj);
            rootNode.getChildren().add(sn);
        }

        // create the tree view
        treeView=new TreeView<String>(rootNode);
        treeView.setStyle("-fx-background-color:white;");
        treeView.setShowRoot(false);
        treeView.setCellFactory(new Callback<TreeView<String>, TreeCell<String>>() {
            @Override
            public TreeCell<String> call(TreeView<String> p) {
                SchemaTreeCell cell = new SchemaTreeCell();
                setDropEvent(cell);
                return cell;
            }
        });

        // add everything to the tree pane
        treeBox.getChildren().add(treeView);
        treeView.setOnMouseClicked(new SchemaClickedEventHandler(this));
    }

    public SchemaNode getSelectedItem(){
        int selIndex = treeView.getSelectionModel().getSelectedIndex();
        if(selIndex == -1)
            return null;
        return (SchemaNode)treeView.getTreeItem(selIndex);
    }

    public void createBottom(){
        bottom = new HBox();
        bottom.setPadding(new Insets(10,10,10,10));

        Button associate = new Button("Associate");
        associate.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                /*Set<SourceTreeItem> set = Main.getSourceSelectedItems();
                SchemaNode descObj = Main.getSchemaSelectedItem();
                if (source != null && descObj != null) { //both trees need to have 1 element selected
                    if (source instanceof SourceTreeDirectory) { //the source needs to be a directory
                        RuleComponent ruleC = new RuleComponent((SourceTreeDirectory) source, descObj, visitors);
                        RulesPane.addChild(ruleC);
                    }
                }
                Footer.setStatus("Carregou no \"Create Rule\": " + source + " <-> " + descObj);*/
            }
        });

        bottom.getChildren().add(associate);
    }


    private void setDropEvent(SchemaTreeCell cell) {
        setOnDragOver(cell);
        setOnDragEntered(cell);
        setOnDragExited(cell);
        setOnDragDropped(cell);
        setOnDragDone(cell);
    }

    private void setOnDragOver(final SchemaTreeCell cell){
        // on a Target
        cell.setOnDragOver(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                TreeItem<String> treeItem = cell.getTreeItem();
                if (treeItem instanceof SchemaNode) {
                    SchemaNode item = (SchemaNode) cell.getTreeItem();
                    if ((item != null /*&& !item.isLeaf()*/) &&
                            event.getGestureSource() != cell &&
                            event.getDragboard().hasString()) {
                        event.acceptTransferModes(TransferMode.COPY);
                    }
                }
                event.consume();
            }
        });
    }

    private void setOnDragEntered(final SchemaTreeCell cell){
        // on a Target
        cell.setOnDragEntered(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                TreeItem<String> treeItem = cell.getTreeItem();
                if (treeItem instanceof SchemaNode) {
                    SchemaNode item = (SchemaNode) cell.getTreeItem();
                    if ((item != null /* && !item.isLeaf()*/) &&
                            event.getGestureSource() != cell &&
                            event.getDragboard().hasString()) {
                        cell.setStyle("-fx-background-color: powderblue;");
                    }
                }
                event.consume();
            }
        });
    }

    private void setOnDragExited(final SchemaTreeCell cell){
        // on a Target
        cell.setOnDragExited(new EventHandler<DragEvent>() {
                                 @Override
                                 public void handle(DragEvent event) {
                                     cell.setStyle("-fx-background-color: white");
                                     event.consume();
                                 }
                             }
        );
    }

    private void setOnDragDropped(final SchemaTreeCell cell){
        // on a Target
        cell.setOnDragDropped(
                new EventHandler<DragEvent>() {
                    @Override
                    public void handle(DragEvent event) {
                        Dragboard db = event.getDragboard();
                        boolean success = false;
                        if (db.hasString()) {
                            success = true;
                            Set<SourceTreeItem> sourceSet = Main.getSourceSelectedItems();
                            SchemaNode descObj = (SchemaNode)cell.getTreeItem();
                            boolean valid = true;
                            if (sourceSet != null && sourceSet.size() > 0 && descObj != null) { //both trees need to have 1 element selected
                                for(SourceTreeItem source: sourceSet)
                                    if (!(source instanceof SourceTreeDirectory || source instanceof SourceTreeFile)) {
                                        valid = false;
                                        break;
                                    }
                            }else valid = false;
                            if(valid)
                                RuleModalController.newAssociation(primaryStage, sourceSet, descObj);
                        }
                        event.setDropCompleted(success);
                        event.consume();
                    }
                }
        );
    }

    private void setOnDragDone(final SchemaTreeCell cell){
        // on a Source
        cell.setOnDragDone(new EventHandler<DragEvent>() {
                               @Override
                               public void handle(DragEvent event) {
                               }
                           }
        );
    }

    public TreeView<String> getTreeView() {
        return treeView;
    }
}
