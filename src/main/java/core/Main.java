package core;

/**
 * Created by adrapereira on 16-09-2015.
 */

import javafx.scene.control.*;
import javafx.scene.layout.*;
import rules.ui.RulesPane;
import schema.ui.SchemaPane;
import source.ui.FileExplorerPane;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Main extends Application {
    public Stage stage;

    private static FileExplorerPane previewExplorer;
    private static BorderPane rulesPane;
    private static SchemaPane schemaPane;

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
        Rectangle2D bounds = screen.getVisualBounds();

        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());

        // Divide center pane in 3
        SplitPane split = new SplitPane();
        //StackPane previewExplorer = createPreviewExplorer();
        previewExplorer = new FileExplorerPane(stage);
        rulesPane = new RulesPane(stage);
        schemaPane = new SchemaPane(stage);
        split.getItems().addAll(previewExplorer, rulesPane, schemaPane);

        //Create Footer
        HBox footer = new Footer();

        BorderPane mainPane = new BorderPane();
        mainPane.setCenter(split);
        mainPane.setBottom(footer);

        // setup and show the window
        stage.setTitle("RODA-In");
        stage.setScene(new Scene(mainPane, bounds.getWidth(), bounds.getHeight()));
        stage.show();
    }

    public static String getSchemaSelectedItem(){
        return schemaPane.getSelectedItem();
    }
    public static String getSourceSelectedItem(){
        return previewExplorer.getSelectedItem();
    }
}
