package rodain.core;

/**
 * Created by adrapereira on 16-09-2015.
 */

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

import javafx.stage.WindowEvent;
import org.slf4j.LoggerFactory;

import rodain.inspection.InspectionPane;
import rodain.rules.sip.SipPreview;
import rodain.schema.ui.SchemaNode;
import rodain.schema.ui.SchemaPane;
import rodain.source.ui.FileExplorerPane;
import rodain.source.ui.items.SourceTreeItem;

public class Main extends Application {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(Main.class.getName());
    private static Stage stage;

    private static FileExplorerPane previewExplorer;
    private static InspectionPane inspectionPane;
    private static SchemaPane schemaPane;

    public static void main(String[] args) {
        launch(args);
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent e) {
                Platform.exit();
                System.exit(0);
            }
        });
    }

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        stage.setMinWidth(1024);
        stage.setMinHeight(512);

        try {
            stage.getIcons().add(new Image(ClassLoader.getSystemResource("roda2-logo.png").openStream()));
        } catch (IOException e) {
            log.error("" + e);
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
        schemaPane = new SchemaPane(stage);
        inspectionPane = new InspectionPane(stage);

        split.getItems().addAll(previewExplorer, schemaPane, inspectionPane);

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
    public static Set<SourceTreeItem> getSourceSelectedItems(){
        return previewExplorer.getSelectedItems();
    }
    public static void mapSelected(String ruleId){
        previewExplorer.map(ruleId);
    }
    public static void ignore(Set<SourceTreeItem> items){
        previewExplorer.ignore(items);
    }
    public static InspectionPane getInspectionPane(){
        return inspectionPane;
    }
    public static Map<SipPreview, String> getSipPreviews(){
        return schemaPane.getSipPreviews();
    }
}
