/**
 * Created by adrap on 16-09-2015.
 */

import SourceRepresentation.SourceDirectory;
import SourceUI.ClickedEventHandler;
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

public class Main extends Application {
    private Stage stage;
    private Rectangle2D bounds;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;

        createFrameStructure();
    }

    private void createFrameStructure(){
        // Maximize window
        Screen screen = Screen.getPrimary();
        bounds = screen.getVisualBounds();

        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());

        // Divide Pane in 3
        SplitPane split = new SplitPane();

        StackPane previewExplorer = createPreviewExplorer();
        StackPane rulesPane = createRulesPane();
        StackPane sipExplorer = createSIPExplorer();

        split.getItems().addAll(previewExplorer, rulesPane, sipExplorer);

        // setup and show the window
        stage.setTitle("RODA-In");
        stage.setScene(new Scene(split, bounds.getWidth(), bounds.getHeight()));
        stage.show();
    }

    private StackPane createPreviewExplorer(){
        //create tree pane
        final VBox treeBox=new VBox();
        treeBox.setPadding(new Insets(10, 10, 10, 10));
        treeBox.setSpacing(10);

        Path rootPath = Paths.get("/");
        SourceTreeDirectory rootNode = new SourceTreeDirectory(rootPath, new SourceDirectory(rootPath));

        // create the tree view
        TreeView<String> treeView=new TreeView<String>(rootNode);
        // add everything to the tree pane
        treeBox.getChildren().addAll(new Label("Preview File Browser"), treeView);
        VBox.setVgrow(treeView, Priority.ALWAYS);

        StackPane previewExplorer = new StackPane();
        previewExplorer.getChildren().add(treeBox);

        treeView.setOnMouseClicked(new ClickedEventHandler(treeView));
        return previewExplorer;
    }

    private StackPane createRulesPane(){
        double splitWidth = bounds.getWidth()/3;

        StackPane rulesPane = new StackPane();
        rulesPane.setMinWidth(splitWidth);

        return rulesPane;
    }

    public StackPane createSIPExplorer(){
        StackPane sipExplorer = new StackPane();
        return sipExplorer;
    }
}
