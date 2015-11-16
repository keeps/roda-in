package org.roda.rodain.core;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 16-09-2015.
 */

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.roda.rodain.inspection.InspectionPane;
import org.roda.rodain.inspection.RuleCell;
import org.roda.rodain.rules.Rule;
import org.roda.rodain.rules.VisitorStack;
import org.roda.rodain.rules.sip.SipPreview;
import org.roda.rodain.rules.ui.RuleModalPane;
import org.roda.rodain.schema.ui.SchemaNode;
import org.roda.rodain.schema.ui.SchemaPane;
import org.roda.rodain.schema.ui.SchemaTreeCell;
import org.roda.rodain.source.ui.FileExplorerPane;
import org.roda.rodain.source.ui.SourceTreeCell;
import org.roda.rodain.source.ui.items.SourceTreeItem;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class Main extends Application {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(Main.class.getName());
    private static Stage stage;
    private static double javaVersion;

    private BorderPane mainPane;
    private MenuBar menu;
    private double initialWidth, initialHeight;

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

        // load the custom fonts
        Font.loadFont( ClassLoader.getSystemResource("fonts/Ubuntu-Regular.ttf").toExternalForm(),  10 );
        Font.loadFont( ClassLoader.getSystemResource("fonts/Ubuntu-Medium.ttf").toExternalForm(),  10 );

        loadProperties();

        createFrameStructure();
        createMenu();

        // setup and show the window
        stage.setTitle("RODA-In Alpha 3");
        Scene scene = new Scene(mainPane, initialWidth, initialHeight);
        if(javaVersion < 1.8) {
            scene.getStylesheets().add(ClassLoader.getSystemResource("css/Modena.css").toExternalForm());
        }else //the setMaximized method was added in JavaFX 8
            stage.setMaximized(true);

        scene.getStylesheets().add(ClassLoader.getSystemResource("css/mainwindow.css").toExternalForm());
        scene.getStylesheets().add(ClassLoader.getSystemResource("css/shared.css").toExternalForm());
        stage.setScene(scene);

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

        initialHeight = bounds.getHeight();
        initialWidth = bounds.getWidth();

        // Divide center pane in 3
        SplitPane split = new SplitPane();
        previewExplorer = new FileExplorerPane(stage);
        schemaPane = new SchemaPane(stage);
        inspectionPane = new InspectionPane(stage);

        split.getItems().addAll(previewExplorer, schemaPane, inspectionPane);

        //Create Footer
        HBox footer = new Footer(stage);

        mainPane = new BorderPane();
        mainPane.setCenter(split);
        mainPane.setBottom(footer);
    }

    private void createMenu(){
        menu = new MenuBar();
        Menu menuFile = new Menu("File");
        Menu menuView = new Menu("View");

        //File
        final MenuItem openFolder = new MenuItem("Open folder");
        openFolder.setAccelerator(KeyCombination.keyCombination("Ctrl+O"));
        openFolder.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                DirectoryChooser chooser = new DirectoryChooser();
                chooser.setTitle("Please choose a folder");
                File selectedDirectory = chooser.showDialog(stage);
                if (selectedDirectory == null)
                    return;
                Path path = selectedDirectory.toPath();
                previewExplorer.setFileExplorerRoot(path);
            }
        });

        final MenuItem updateCS = new MenuItem("Update classification schema");
        updateCS.setAccelerator(KeyCombination.keyCombination("Ctrl+U"));
        updateCS.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {

            }
        });
        menuFile.getItems().addAll(openFolder, updateCS);

        // View
        final MenuItem showFiles = new MenuItem("Hide Files");
        showFiles.setAccelerator(KeyCombination.keyCombination("Ctrl+F"));
        showFiles.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                previewExplorer.toggleFilesShowing();
                if(FileExplorerPane.isShowFiles())
                    showFiles.setText("Hide files");
                else showFiles.setText("Show files");
            }
        });
        final MenuItem showIgnored = new MenuItem("Show Ignored");
        showIgnored.setAccelerator(KeyCombination.keyCombination("Ctrl+I"));
        showIgnored.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                previewExplorer.toggleIgnoredShowing();
                if(FileExplorerPane.isShowIgnored())
                    showIgnored.setText("Hide ignored");
                else showIgnored.setText("Show ignored");
            }
        });
        final MenuItem showMapped = new MenuItem("Show Mapped");
        showMapped.setAccelerator(KeyCombination.keyCombination("Ctrl+M"));
        showMapped.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                previewExplorer.toggleMappedShowing();
                if(FileExplorerPane.isShowMapped())
                    showMapped.setText("Hide mapped");
                else showMapped.setText("Show mapped");
            }
        });

        menuView.getItems().addAll(showFiles, showIgnored, showMapped);

        menu.getMenus().addAll(menuFile, menuView);
        mainPane.setTop(menu);
    }

    private void loadProperties(){
        try {
            Properties style = new Properties();
            style.load(ClassLoader.getSystemResource("properties/styles.properties").openStream());
            SchemaTreeCell.setStyleProperties(style);
            SchemaPane.setStyleProperties(style);
            SourceTreeCell.setStyleProperties(style);

            Properties config = new Properties();
            config.load(ClassLoader.getSystemResource("properties/config.properties").openStream());
            RuleModalPane.setProperties(config);
            RuleCell.setProperties(config);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static double getJavaVersion(){
        return javaVersion;
    }

    public static SchemaNode getSchemaSelectedItem(){
        return schemaPane.getSelectedItem();
    }
    public static Set<SourceTreeItem> getSourceSelectedItems(){
        return previewExplorer.getSelectedItems();
    }
    public static void mapSelected(Rule r){
        previewExplorer.map(r);
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
