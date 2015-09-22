/**
 * Created by adrap on 16-09-2015.
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import schema.ClassificationSchema;
import source.representation.SourceDirectory;
import source.ui.ClickedEventHandler;
import source.ui.items.SourceTreeDirectory;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;
import sun.misc.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;

public class Main extends Application {
    private Stage stage;
    private Rectangle2D bounds;

    public static void main(String[] args) {
        //read json file data to String
        try {
            InputStream input = ClassLoader.getSystemResourceAsStream("test.json");

            //create ObjectMapper instance
            ObjectMapper objectMapper = new ObjectMapper();

            //convert json string to object
            ClassificationSchema emp = objectMapper.readValue(input, ClassificationSchema.class);

            System.out.println("Object\n"+emp);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //launch(args);
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
