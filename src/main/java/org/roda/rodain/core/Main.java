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
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.roda.rodain.creation.ui.CreationModalPreparation;
import org.roda.rodain.creation.ui.CreationModalStage;
import org.roda.rodain.inspection.InspectionPane;
import org.roda.rodain.inspection.RuleCell;
import org.roda.rodain.rules.VisitorStack;
import org.roda.rodain.rules.sip.SipMetadata;
import org.roda.rodain.rules.sip.SipPreview;
import org.roda.rodain.rules.ui.RuleModalPane;
import org.roda.rodain.schema.ui.SchemaPane;
import org.roda.rodain.source.ui.FileExplorerPane;
import org.roda.rodain.source.ui.SourceTreeCell;
import org.roda.rodain.source.ui.items.SourceTreeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class Main extends Application {
  private static final Logger log = LoggerFactory.getLogger(Main.class.getName());
  private static Stage stage;

  private BorderPane mainPane;
  private double initialWidth = 1200, initialHeight = 700;

  private static FileExplorerPane previewExplorer;
  private static InspectionPane inspectionPane;
  private static SchemaPane schemaPane;

  /**
   * The entry point of the application.
   *
   * @param args The arguments passed to the application.
   */
  public static void main(String[] args) {
    // get the java version
    String javaString = Runtime.class.getPackage().getSpecificationVersion();
    double javaVersion = Double.parseDouble(javaString);
    if (javaVersion < 1.8) {
      log.error("Java version is " + javaString + ". Please use at least \"Java 1.8\".");
      return;
    }

    launch(args);
  }

  /**
   * Creates the interface structure and loads resources.
   * <p/>
   * <p>
   * This method sets the application logo, loads fonts, styles and property
   * files. Furthermore, creates the frame structure and the menu. The frame
   * structure is a SplitPane, split in three sections - file explorer,
   * classification scheme, inspection - and a footer.
   * </p>
   *
   * @param primaryStage
   */
  @Override
  public void start(Stage primaryStage) {
    stage = primaryStage;
    stage.setMinWidth(1024);
    stage.setMinHeight(600);

    stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
      @Override
      public void handle(WindowEvent e) {
        VisitorStack.end();
        Platform.exit();
      }
    });

    try {
      stage.getIcons().add(new Image(ClassLoader.getSystemResource("roda2-logo.png").openStream()));
    } catch (IOException e) {
      log.error("Error reading logo file", e);
    }

    // load the custom fonts
    Font.loadFont(ClassLoader.getSystemResource("fonts/Ubuntu-Regular.ttf").toExternalForm(), 10);
    Font.loadFont(ClassLoader.getSystemResource("fonts/Ubuntu-Medium.ttf").toExternalForm(), 10);
    Font.loadFont(ClassLoader.getSystemResource("fonts/Ubuntu-Light.ttf").toExternalForm(), 10);

    loadProperties();

    createFrameStructure();
    createMenu();

    // setup and show the window
    stage.setTitle("RODA-In");
    Scene scene = new Scene(mainPane, initialWidth, initialHeight);

    scene.getStylesheets().add(ClassLoader.getSystemResource("css/mainWindow.css").toExternalForm());
    scene.getStylesheets().add(ClassLoader.getSystemResource("css/shared.css").toExternalForm());
    stage.setScene(scene);

    stage.setMaximized(true);

    stage.show();
  }

  private void createFrameStructure() {
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

    split.setDividerPositions(0.33, 0.66);
    split.getItems().addAll(previewExplorer, schemaPane, inspectionPane);

    // Create Footer
    Footer footer = new Footer();

    mainPane = new BorderPane();
    mainPane.getStyleClass().add("border-pane");
    mainPane.setCenter(split);
    mainPane.setBottom(footer);
  }

  private void createMenu() {
    MenuBar menu = new MenuBar();
    Menu menuFile = new Menu("File");
    Menu menuEdit = new Menu("Edit");
    Menu menuView = new Menu("View");

    // File
    final MenuItem openFolder = new MenuItem("Open folder");
    openFolder.setAccelerator(KeyCombination.keyCombination("Ctrl+O"));
    openFolder.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent t) {
        previewExplorer.chooseNewRoot();
      }
    });

    final MenuItem updateCS = new MenuItem("Load classification scheme");
    updateCS.setAccelerator(KeyCombination.keyCombination("Ctrl+L"));
    updateCS.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent t) {
        schemaPane.loadClassificationSchema();
      }
    });

    final MenuItem createSIPs = new MenuItem("Create SIPs");
    createSIPs.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
    createSIPs.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent t) {
        // force the edits to the metadata text area to be saved
        inspectionPane.saveMetadata();

        CreationModalStage creationStage = new CreationModalStage(stage);
        CreationModalPreparation pane = new CreationModalPreparation(creationStage);
        creationStage.setRoot(pane);
      }
    });

    final MenuItem quit = new MenuItem("Quit");
    quit.setAccelerator(KeyCombination.keyCombination("Ctrl+Q"));
    quit.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent t) {
        VisitorStack.end();
        Platform.exit();
      }
    });

    menuFile.getItems().addAll(openFolder, updateCS, createSIPs, quit);

    // Edit
    final MenuItem ignoreItems = new MenuItem("Ignore item(s)");
    ignoreItems.setAccelerator(KeyCombination.keyCombination("DELETE"));
    ignoreItems.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent t) {
        previewExplorer.ignore();
      }
    });
    menuEdit.getItems().add(ignoreItems);

    // View
    final MenuItem showFiles = new MenuItem("Hide Files");
    showFiles.setAccelerator(KeyCombination.keyCombination("Ctrl+F"));
    showFiles.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent t) {
        previewExplorer.toggleFilesShowing();
        if (FileExplorerPane.isShowFiles())
          showFiles.setText("Hide files");
        else
          showFiles.setText("Show files");
      }
    });
    final MenuItem showIgnored = new MenuItem("Show Ignored");
    showIgnored.setAccelerator(KeyCombination.keyCombination("Ctrl+I"));
    showIgnored.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent t) {
        previewExplorer.toggleIgnoredShowing();
        if (FileExplorerPane.isShowIgnored())
          showIgnored.setText("Hide ignored");
        else
          showIgnored.setText("Show ignored");
      }
    });
    final MenuItem showMapped = new MenuItem("Show Mapped");
    showMapped.setAccelerator(KeyCombination.keyCombination("Ctrl+M"));
    showMapped.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent t) {
        previewExplorer.toggleMappedShowing();
        if (FileExplorerPane.isShowMapped())
          showMapped.setText("Hide mapped");
        else
          showMapped.setText("Show mapped");
      }
    });

    menuView.getItems().addAll(showFiles, showIgnored, showMapped);

    menu.getMenus().addAll(menuFile, menuEdit, menuView);
    mainPane.setTop(menu);
  }

  private void loadProperties() {
    try {
      Properties style = new Properties();
      style.load(ClassLoader.getSystemResource("properties/styles.properties").openStream());
      SourceTreeCell.setStyleProperties(style);

      Properties config = new Properties();
      config.load(ClassLoader.getSystemResource("properties/config.properties").openStream());
      RuleModalPane.setProperties(config);
      RuleCell.setProperties(config);
      SipMetadata.setProperties(config);
    } catch (IOException e) {
      log.error("Error while loading properties", e);
    }
  }

  public static Set<SourceTreeItem> getSourceSelectedItems() {
    return previewExplorer.getSelectedItems();
  }

  public static InspectionPane getInspectionPane() {
    return inspectionPane;
  }


  public static FileExplorerPane getPreviewExplorer() {
    return previewExplorer;
  }

  public static SchemaPane getSchemaPane() {
    return schemaPane;
  }

  public static Map<SipPreview, String> getSipPreviews() {
    return schemaPane.getSipPreviews();
  }
}
