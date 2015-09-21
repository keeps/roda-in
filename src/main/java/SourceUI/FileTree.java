package SourceUI; /**
 * Created by adrap on 16-09-2015.
 */

import SourceRepresentation.SourceDirectory;
import SourceUI.Items.SourceTreeDirectory;
import SourceUI.Items.SourceTreeLoadMore;
import SourceUI.Items.SourceTreeLoading;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.nio.file.*;

public class FileTree extends Application {

    private TreeView<String> treeView;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        //create tree pane
        final VBox treeBox=new VBox();
        treeBox.setPadding(new Insets(10, 10, 10, 10));
        treeBox.setSpacing(10);

        Path rootPath = Paths.get("/");
        SourceTreeDirectory rootNode = new SourceTreeDirectory(rootPath, new SourceDirectory(rootPath));

        // create the tree view
        treeView=new TreeView<String>(rootNode);
        // add everything to the tree pane
        treeBox.getChildren().addAll(new Label("Preview File Browser"),treeView);
        VBox.setVgrow(treeView, Priority.ALWAYS);

        // Maximize window
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();

        primaryStage.setX(bounds.getMinX());
        primaryStage.setY(bounds.getMinY());
        primaryStage.setWidth(bounds.getWidth());
        primaryStage.setHeight(bounds.getHeight());

        // Create sections in window
        double splitWidth = bounds.getWidth()/3;
        // Explorer preview view
        SplitPane split = new SplitPane();
        StackPane previewExplorer = new StackPane();
        previewExplorer.getChildren().add(treeBox);

        StackPane child2 = new StackPane();
        child2.setMinWidth(splitWidth);

        StackPane child3 = new StackPane();
        split.getItems().addAll(previewExplorer, child2, child3);

        // setup and show the window
        primaryStage.setTitle("RODA-In");
        primaryStage.setScene(new Scene(split, bounds.getWidth(), bounds.getHeight()));
        primaryStage.show();

        treeView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getClickCount() == 1) {
                    TreeItem<String> item = treeView.getSelectionModel().getSelectedItem();
                    if(item instanceof SourceTreeLoadMore) {
                        final SourceTreeDirectory parent = (SourceTreeDirectory)item.getParent();
                        parent.getChildren().remove(item);
                        final SourceTreeLoading loading = new SourceTreeLoading();
                        parent.getChildren().add(loading);

                        Platform.runLater(new Runnable() {
                            public void run() {
                                parent.loadMore();
                            }
                        });

                    }
                }
            }
        });
    }
}
