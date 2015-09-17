package WithHandlers; /**
 * Created by adrap on 16-09-2015.
 */

import Source.SourceDirectory;
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

import java.io.IOException;
import java.nio.file.*;

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

        Path rootPath = Paths.get("/");
        SourceTreeDirectory rootNode = new SourceTreeDirectory(rootPath, new SourceDirectory(rootPath));

        //create the tree view
        treeView=new TreeView<String>(rootNode);
        //add everything to the tree pane
        treeBox.getChildren().addAll(new Label("File browser"),treeView);
        VBox.setVgrow(treeView, Priority.ALWAYS);

        //setup and show the window
        primaryStage.setTitle("JavaFX File Browse Demo");
        StackPane root=new StackPane();
        root.getChildren().addAll(treeBox);
        primaryStage.setScene(new Scene(root, 400, 300));
        primaryStage.show();

        treeView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 1) {
                    TreeItem<String> item = treeView.getSelectionModel().getSelectedItem();
                    if(item instanceof SourceTreeLoadMore) {
                        SourceTreeDirectory parent = (SourceTreeDirectory)item.getParent();
                        parent.loadMore();
                    }
                }
            }
        });
    }
}
