package core;

/**
 * Created by adrapereira on 16-09-2015.
 */

import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

import org.slf4j.LoggerFactory;

import rules.Rule;
import rules.ui.RuleComponent;
import rules.ui.RulesPane;
import schema.ui.SchemaNode;
import schema.ui.SchemaPane;
import source.ui.FileExplorerPane;
import source.ui.items.SourceTreeItem;

import java.io.IOException;
import java.util.HashSet;

public class Main extends Application {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(Main.class.getName());
    private Stage stage;

    private static FileExplorerPane previewExplorer;
    private static RulesPane rulesPane;
    private static SchemaPane schemaPane;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;

        try {
            stage.getIcons().add(new Image(ClassLoader.getSystemResource("roda2-logo.png").openStream()));
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        createFrameStructure();
        stage.show();
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
        previewExplorer = new FileExplorerPane(stage);
        rulesPane = new RulesPane(stage);
        schemaPane = new SchemaPane(stage);

        split.getItems().addAll(previewExplorer, schemaPane, rulesPane);

        //Create Footer
        HBox footer = new Footer(stage);

        BorderPane mainPane = new BorderPane();
        mainPane.setCenter(split);
        mainPane.setBottom(footer);

        // setup and show the window
        stage.setTitle("RODA-In");
        Scene scene = new Scene(mainPane, bounds.getWidth(), bounds.getHeight());
        scene.getStylesheets().add(ClassLoader.getSystemResource("Modena.css").toExternalForm());
        stage.setScene(scene);
    }

    public static SchemaNode getSchemaSelectedItem(){
        return schemaPane.getSelectedItem();
    }
    public static SourceTreeItem getSourceSelectedItem(){
        return previewExplorer.getSelectedItem();
    }
    public static void removeRule(RuleComponent rule){
        rulesPane.removeChild(rule);
    }
    public static HashSet<Rule> getRules(){
        return rulesPane.getRules();
    }
}
