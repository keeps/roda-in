package org.roda.rodain.core;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 16-09-2015.
 */

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.*;

import org.roda.rodain.creation.ui.CreationModalPreparation;
import org.roda.rodain.creation.ui.CreationModalStage;
import org.roda.rodain.inspection.InspectionPane;
import org.roda.rodain.rules.VisitorStack;
import org.roda.rodain.rules.filters.IgnoredFilter;
import org.roda.rodain.rules.sip.SipPreview;
import org.roda.rodain.schema.ClassificationSchema;
import org.roda.rodain.schema.DescriptionObject;
import org.roda.rodain.schema.ui.SchemaNode;
import org.roda.rodain.schema.ui.SchemaPane;
import org.roda.rodain.source.ui.FileExplorerPane;
import org.roda.rodain.source.ui.items.SourceTreeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RodaIn extends Application {
  private static final Logger log = LoggerFactory.getLogger(RodaIn.class.getName());
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
  public static void startApp(String[] args) {
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

    AppProperties.initialize();
    String ignoredRaw = AppProperties.getConfig("app.ignoredFiles");
    String[] ignored = ignoredRaw.split(",");
    for (String s : ignored) {
      IgnoredFilter.addIgnoreRule(s);
    }

    // load the custom fonts
    Font.loadFont(ClassLoader.getSystemResource("fonts/Ubuntu-Regular.ttf").toExternalForm(), 10);
    Font.loadFont(ClassLoader.getSystemResource("fonts/Ubuntu-Medium.ttf").toExternalForm(), 10);
    Font.loadFont(ClassLoader.getSystemResource("fonts/Ubuntu-Light.ttf").toExternalForm(), 10);

    createFrameStructure();
    createMenu();

    // setup and show the window
    stage.setTitle("RODA-In");
    Scene scene = new Scene(mainPane, initialWidth, initialHeight);

    scene.getStylesheets().add(ClassLoader.getSystemResource("css/mainWindow.css").toExternalForm());
    scene.getStylesheets().add(ClassLoader.getSystemResource("css/shared.css").toExternalForm());
    scene.getStylesheets().add(ClassLoader.getSystemResource("css/xml-highlighting.css").toExternalForm());
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

    // Create Footer
    Footer footer = new Footer();

    mainPane = new BorderPane();
    mainPane.getStyleClass().add("border-pane");
    mainPane.setCenter(createSplitPane());
    mainPane.setBottom(footer);
  }

  private SplitPane createSplitPane() {
    // Divide center pane in 3
    SplitPane split = new SplitPane();
    previewExplorer = new FileExplorerPane(stage);
    schemaPane = new SchemaPane(stage);
    inspectionPane = new InspectionPane(stage);

    split.setDividerPositions(0.33, 0.67);
    split.getItems().addAll(previewExplorer, schemaPane, inspectionPane);

    return split;
  }

  private void createMenu() {
    MenuBar menu = new MenuBar();
    Menu menuFile = new Menu(AppProperties.getLocalizedString("Main.file"));
    Menu menuEdit = new Menu(AppProperties.getLocalizedString("Main.edit"));
    Menu menuClassScheme = new Menu(AppProperties.getLocalizedString("Main.classScheme"));
    Menu menuView = new Menu(AppProperties.getLocalizedString("Main.view"));

    // File
    Menu language = new Menu(AppProperties.getLocalizedString("Main.language"));
    final ToggleGroup languageGroup = new ToggleGroup();
    RadioMenuItem langPT = new RadioMenuItem("Português");
    langPT.setUserData("pt");
    langPT.setToggleGroup(languageGroup);
    RadioMenuItem langEN = new RadioMenuItem("English");
    langEN.setUserData("en");
    langEN.setToggleGroup(languageGroup);
    language.getItems().addAll(langEN, langPT);

    switch (AppProperties.locale.getLanguage()) {
      case "en":
        langEN.setSelected(true);
        break;
      case "pt":
        langPT.setSelected(true);
        break;
      default:
        langEN.setSelected(true);
        break;
    }

    languageGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
      public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {
        if (languageGroup.getSelectedToggle() != null) {
          String lang = (String) languageGroup.getSelectedToggle().getUserData();
          AppProperties.setConfig("app.language", lang);
          AppProperties.saveConfig();
          Alert dlg = new Alert(Alert.AlertType.INFORMATION);
          dlg.setHeaderText(AppProperties.getLocalizedString("Main.updateLang.header"));
          dlg.setTitle(AppProperties.getLocalizedString("Main.updateLang.title"));
          dlg.setContentText(AppProperties.getLocalizedString("Main.updateLang.content"));
          dlg.initModality(Modality.APPLICATION_MODAL);
          dlg.initOwner(stage);
          dlg.show();
        }
      }
    });

    final MenuItem openFolder = new MenuItem(AppProperties.getLocalizedString("Main.openFolder"));
    openFolder.setAccelerator(KeyCombination.keyCombination("Ctrl+O"));
    openFolder.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent t) {
        previewExplorer.chooseNewRoot();
      }
    });

    final MenuItem createSIPs = new MenuItem(AppProperties.getLocalizedString("Main.exportSips"));
    createSIPs.setAccelerator(KeyCombination.keyCombination("Ctrl+X"));
    createSIPs.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent t) {
        exportSIPs();
      }
    });

    final MenuItem quit = new MenuItem(AppProperties.getLocalizedString("Main.quit"));
    quit.setAccelerator(KeyCombination.keyCombination("Ctrl+Q"));
    quit.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent t) {
        VisitorStack.end();
        Platform.exit();
      }
    });
    final MenuItem reset = new MenuItem(AppProperties.getLocalizedString("Main.reset"));
    reset.setAccelerator(KeyCombination.keyCombination("Ctrl+N"));
    reset.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent t) {
        Alert dlg = new Alert(Alert.AlertType.CONFIRMATION);
        dlg.setHeaderText(AppProperties.getLocalizedString("Main.confirmReset.header"));
        dlg.setTitle(AppProperties.getLocalizedString("Main.reset"));
        dlg.setContentText(AppProperties.getLocalizedString("Main.confirmReset.content"));
        dlg.initModality(Modality.APPLICATION_MODAL);
        dlg.initOwner(stage);
        dlg.showAndWait();

        if (dlg.getResult().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
          PathCollection.reset();
          inspectionPane = new InspectionPane(stage);
          previewExplorer = new FileExplorerPane(stage);
          schemaPane = new SchemaPane(stage);
          mainPane.setCenter(createSplitPane());
        }
      }
    });

    menuFile.getItems().addAll(reset, openFolder, createSIPs, language, quit);

    // Classification scheme
    final MenuItem createCS = new MenuItem(AppProperties.getLocalizedString("Main.createCS"));
    createCS.setAccelerator(KeyCombination.keyCombination("Ctrl+R"));
    createCS.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent t) {
        schemaPane.createClassificationScheme();
      }
    });

    final MenuItem updateCS = new MenuItem(AppProperties.getLocalizedString("Main.loadCS"));
    updateCS.setAccelerator(KeyCombination.keyCombination("Ctrl+L"));
    updateCS.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent t) {
        schemaPane.loadClassificationSchema();
      }
    });

    final MenuItem exportCS = new MenuItem(AppProperties.getLocalizedString("Main.exportCS"));
    exportCS.setAccelerator(KeyCombination.keyCombination("Ctrl+E"));
    exportCS.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent t) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(AppProperties.getLocalizedString("filechooser.title"));
        File selectedFile = chooser.showSaveDialog(stage);
        if (selectedFile == null)
          return;
        String outputFile = selectedFile.toPath().toString();

        Set<SchemaNode> nodes = schemaPane.getSchemaNodes();
        List<DescriptionObject> dobjs = new ArrayList<>();
        for (SchemaNode sn : nodes) {
          dobjs.add(sn.getDob());
        }
        ClassificationSchema cs = new ClassificationSchema();
        cs.setDos(dobjs);
        cs.export(outputFile);
        AppProperties.setConfig("lastClassificationScheme", outputFile);
        AppProperties.saveConfig();
      }
    });

    menuClassScheme.getItems().addAll(createCS, updateCS, exportCS);

    // Edit
    final MenuItem ignoreItems = new MenuItem(AppProperties.getLocalizedString("Main.ignoreItems"));
    ignoreItems.setAccelerator(KeyCombination.keyCombination("DELETE"));
    ignoreItems.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent t) {
        previewExplorer.ignore();
      }
    });

    menuEdit.getItems().addAll(ignoreItems);

    // View
    final MenuItem showFiles = new MenuItem(AppProperties.getLocalizedString("Main.hideFiles"));
    showFiles.setAccelerator(KeyCombination.keyCombination("Ctrl+F"));
    showFiles.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent t) {
        previewExplorer.toggleFilesShowing();
        if (FileExplorerPane.isShowFiles())
          showFiles.setText(AppProperties.getLocalizedString("Main.hideFiles"));
        else
          showFiles.setText(AppProperties.getLocalizedString("Main.showFiles"));
      }
    });
    final MenuItem showIgnored = new MenuItem(AppProperties.getLocalizedString("Main.showIgnored"));
    showIgnored.setAccelerator(KeyCombination.keyCombination("Ctrl+I"));
    showIgnored.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent t) {
        previewExplorer.toggleIgnoredShowing();
        if (FileExplorerPane.isShowIgnored())
          showIgnored.setText(AppProperties.getLocalizedString("Main.hideIgnored"));
        else
          showIgnored.setText(AppProperties.getLocalizedString("Main.showIgnored"));
      }
    });
    final MenuItem showMapped = new MenuItem(AppProperties.getLocalizedString("Main.showMapped"));
    showMapped.setAccelerator(KeyCombination.keyCombination("Ctrl+M"));
    showMapped.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent t) {
        previewExplorer.toggleMappedShowing();
        if (FileExplorerPane.isShowMapped())
          showMapped.setText(AppProperties.getLocalizedString("Main.hideMapped"));
        else
          showMapped.setText(AppProperties.getLocalizedString("Main.showMapped"));
      }
    });

    menuView.getItems().addAll(showFiles, showIgnored, showMapped);

    menu.getMenus().addAll(menuFile, menuEdit, menuClassScheme, menuView);
    mainPane.setTop(menu);
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

  public static void exportSIPs() {
    // force the edits to the metadata text area to be saved
    inspectionPane.saveMetadata();

    CreationModalStage creationStage = new CreationModalStage(stage);
    CreationModalPreparation pane = new CreationModalPreparation(creationStage);
    creationStage.setRoot(pane);
  }
}
