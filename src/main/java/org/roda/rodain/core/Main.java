package org.roda.rodain.core;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 16-09-2015.
 */

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
import org.roda.rodain.inspection.InspectionPane;
import org.roda.rodain.rules.VisitorStack;
import org.roda.rodain.rules.sip.SipPreview;
import org.roda.rodain.schema.ui.SchemaNode;
import org.roda.rodain.schema.ui.SchemaPane;
import org.roda.rodain.schema.ui.SchemaTreeCell;
import org.roda.rodain.source.ui.FileExplorerPane;
import org.roda.rodain.source.ui.SourceTreeCell;
import org.roda.rodain.source.ui.items.SourceTreeItem;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class Main extends Application {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(Main.class.getName());
    private static Stage stage;
    private double javaVersion;

    private static FileExplorerPane previewExplorer;
    private static InspectionPane inspectionPane;
    private static SchemaPane schemaPane;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;
        stage.setMinWidth(1024);
        stage.setMinHeight(512);

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent e) {
                VisitorStack.end();
                Platform.exit();
            }
        });

        //get the java version
        String javaString =  Runtime.class.getPackage().getSpecificationVersion();
        javaVersion = Double.parseDouble(javaString);

        try {
            stage.getIcons().add(new Image(ClassLoader.getSystemResource("roda2-logo.png").openStream()));
        } catch (IOException e) {
            log.error("Error reading logo file", e);
        }

        loadProperties();

        createFrameStructure();

        //the setMaximized method was added in JavaFX 8
        if(javaVersion >= 1.8)
            stage.setMaximized(true);

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
        if(javaVersion < 1.8) {
            scene.getStylesheets().add(ClassLoader.getSystemResource("Modena.css").toExternalForm());
        }
        scene.getStylesheets().add(ClassLoader.getSystemResource("mainWindow.css").toExternalForm());
        stage.setScene(scene);
    }

    private void loadProperties(){
        try {
            Properties style = new Properties();
            style.load(ClassLoader.getSystemResource("properties/styles.properties").openStream());

            SchemaTreeCell.setStyleProperties(style);
            SchemaPane.setStyleProperties(style);
            SourceTreeCell.setStyleProperties(style);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
