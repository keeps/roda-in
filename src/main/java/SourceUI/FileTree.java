package SourceUI; /**
 * Created by adrap on 16-09-2015.
 */

import Source.SourceDirectory;
import Source.SourceItem;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.xml.transform.Source;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileTree extends Application {

    private TreeView<String> treeView;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        //create tree pane
        VBox treeBox=new VBox();
        treeBox.setPadding(new Insets(10, 10, 10, 10));
        treeBox.setSpacing(10);

        Path pRoot = Paths.get("/");
        SourceDirectory root = new SourceDirectory(pRoot);

        TreeItem<String> rootNode=new TreeItem<String>("/",new ImageView(new Image(ClassLoader.getSystemResourceAsStream("computer.png"))));

        //create the tree view
        treeView=new TreeView<String>(rootNode);
        //add everything to the tree pane
        treeBox.getChildren().addAll(new Label("File browser"),treeView);
        VBox.setVgrow(treeView, Priority.ALWAYS);

        findFiles(root, null);

        //setup and show the window
        primaryStage.setTitle("JavaFX File Browse Demo");
        StackPane rootPane =new StackPane();
        rootPane.getChildren().addAll(treeBox);
        primaryStage.setScene(new Scene(rootPane, 400, 300));
        primaryStage.show();

        treeView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 2) {
                    TreeItem<String> item = treeView.getSelectionModel().getSelectedItem();
                    item.setExpanded(!item.isExpanded());
                }
            }
        });
    }

    private void findFiles(SourceDirectory dir, FilePathTreeItem parent) {
        FilePathTreeItem root = new FilePathTreeItem(dir.getPath());
        root.setExpanded(false);
        for(String sourceItem: dir.getChildren().keySet()){
            Path sourcePath = Paths.get(sourceItem);
            if(Files.isDirectory(sourcePath)){
                findFiles(dir.getChildDirectory(sourcePath), root);
            }else root.getChildren().add(new SourceUI.FilePathTreeItem(sourcePath));

        }

        if(parent==null){
            treeView.setRoot(root);
        } else {
            parent.getChildren().add(root);
        }
    }
}
