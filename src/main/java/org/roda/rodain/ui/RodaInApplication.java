package org.roda.rodain.ui;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationException;
import org.roda.rodain.core.ConfigurationManager;
import org.roda.rodain.core.Constants;
import org.roda.rodain.core.Controller;
import org.roda.rodain.core.I18n;
import org.roda.rodain.core.PathCollection;
import org.roda.rodain.core.schema.Sip;
import org.roda.rodain.core.utils.OpenPathInExplorer;
import org.roda.rodain.ui.creation.CreationModalPreparation;
import org.roda.rodain.ui.creation.CreationModalStage;
import org.roda.rodain.ui.inspection.InspectionPane;
import org.roda.rodain.ui.rules.VisitorStack;
import org.roda.rodain.ui.schema.ui.SchemaPane;
import org.roda.rodain.ui.source.FileExplorerPane;
import org.roda.rodain.ui.source.items.SourceTreeItem;
import org.roda.rodain.ui.utils.FontAwesomeImageCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.CacheHint;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 16-09-2015.
 * @since 2017-03-10 (renamed to )
 */
public class RodaInApplication extends Application {
  private static final Logger LOGGER = LoggerFactory.getLogger(RodaInApplication.class.getName());
  private static Stage stage;

  private BorderPane mainPane;
  private double initialWidth = 1200, initialHeight = 700;

  // Splash
  private Pane splashPane;
  private Stage splashStage;

  private static FileExplorerPane fileExplorer;
  private static InspectionPane inspectionPane;
  private static SchemaPane schemePane;

  // Languages
  private RadioMenuItem langEN, langPT, langHU, langES_CL;

  private static long lastMessage = System.currentTimeMillis();

  /**
   * The entry point of the application.
   *
   * @param args
   *          The arguments passed to the application.
   */
  public static void start(String[] args) {
    launch(args);
  }

  @Override
  public void init() {
    ImageView splash;
    try {
      splash = new ImageView(new Image(ClassLoader.getSystemResource(Constants.RSC_SPLASH_SCREEN_IMAGE).openStream()));
    } catch (IOException e) {
      LOGGER.error("Error reading logo file", e);
      splash = new ImageView();
    }

    splashPane = new Pane();
    splashPane.setStyle(Constants.CSS_FX_BACKGROUND_COLOR_TRANSPARENT);
    splashPane.getChildren().add(splash);
    splashPane.setCache(true);
    splashPane.setCacheHint(CacheHint.SPEED);
    splashPane.setEffect(new DropShadow());
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
    Thread.setDefaultUncaughtExceptionHandler(RodaInApplication::showError);
    if (splashPane != null) {
      splashStage = new Stage();
      Scene splashScene = new Scene(splashPane);
      splashScene.setFill(Color.TRANSPARENT);
      splashStage.setScene(splashScene);
      splashStage.initOwner(stage);
      splashStage.initStyle(StageStyle.TRANSPARENT);
      splashStage.show();
      splashStage.centerOnScreen();
    }

    stage = primaryStage;
    stage.setMinWidth(1024);
    stage.setMinHeight(600);

    try {
      stage.getIcons().add(new Image(ClassLoader.getSystemResource(Constants.RSC_RODA_LOGO).openStream()));
    } catch (IOException e) {
      LOGGER.error("Error reading logo file", e);
    }

    Task<Void> initTask = new Task<Void>() {
      @Override
      protected Void call() throws Exception {
        stage.setOnCloseRequest(event -> closeApp());

        ConfigurationManager.initialize();
        LOGGER.info("Done initializing RODA-in folders & properties");

        // load the custom fonts
        Font.loadFont(ClassLoader.getSystemResource("fonts/Ubuntu-Regular.ttf").toExternalForm(), 10);
        Font.loadFont(ClassLoader.getSystemResource("fonts/Ubuntu-Medium.ttf").toExternalForm(), 10);
        Font.loadFont(ClassLoader.getSystemResource("fonts/Ubuntu-Light.ttf").toExternalForm(), 10);

        createFrameStructure();
        createMenu();

        // setup and show the window
        stage.setTitle(Constants.RODAIN_GUI_TITLE);
        return null;
      }
    };

    initTask.setOnSucceeded(event -> {
      Scene scene = new Scene(mainPane, initialWidth, initialHeight);

      scene.getStylesheets().add(ClassLoader.getSystemResource("css/mainWindow.css").toExternalForm());
      scene.getStylesheets().add(ClassLoader.getSystemResource("css/shared.css").toExternalForm());
      scene.getStylesheets().add(ClassLoader.getSystemResource("css/xml-highlighting.css").toExternalForm());
      stage.setScene(scene);

      stage.show();
      stage.centerOnScreen();
      stage.setMaximized(true);
      if (splashStage != null)
        splashStage.close();

      // Add the bindings after stage.show(), otherwise they'll start as 0
      Footer.addBindings(fileExplorer);

      checkForUpdates(true);
    });

    initTask.exceptionProperty().addListener((observable, oldValue, newValue) -> {
      if (newValue != null) {
        LOGGER.error("Error initializing application", newValue);
      }
    });

    initTask.setOnFailed(event -> {
      LOGGER.error("Failed application initialization");
      if (splashStage != null)
        splashStage.close();
    });

    new Thread(initTask).start();
  }

  private static void showError(Thread t, Throwable e) {
    if (Platform.isFxApplicationThread()) {
      LOGGER.error("Unexpected error", e);
      showErrorDialog(e);
    } else {
      LOGGER.error("An unexpected error occurred in {}", t, e);
    }
  }

  private static void showErrorDialog(Throwable e) {
    if (System.currentTimeMillis() - lastMessage > 500) {
      lastMessage = System.currentTimeMillis();
      Stage dialog = new Stage();
      dialog.initModality(Modality.APPLICATION_MODAL);
      Parent root = new HBox();
      dialog.setScene(new Scene(root, 400, 550));
      dialog.show();
      dialog.centerOnScreen();
      dialog.close();

      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.initStyle(StageStyle.DECORATED);
      alert.initOwner(dialog);
      alert.setTitle(I18n.t(Constants.I18N_GENERIC_ERROR_TITLE));
      alert.setHeaderText(I18n.t(Constants.I18N_GENERIC_ERROR_TITLE));
      StringBuilder content = new StringBuilder(I18n.t(Constants.I18N_GENERIC_ERROR_CONTENT));
      content.append("\n\n");
      content.append(e.toString());
      alert.setContentText(content.toString());
      alert.getDialogPane().setStyle(ConfigurationManager.getStyle("export.alert"));

      // Create expandable Exception.
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      pw.println(e.getMessage());
      for (StackTraceElement ste : e.getStackTrace()) {
        pw.println("\t" + ste);
      }
      String exceptionText = sw.toString();

      Label label = new Label(I18n.t(Constants.I18N_CREATIONMODALPROCESSING_ALERT_STACK_TRACE));

      TextArea textArea = new TextArea(exceptionText);
      textArea.setWrapText(true);
      textArea.setEditable(false);
      textArea.minWidthProperty().bind(alert.getDialogPane().widthProperty().subtract(20));
      textArea.maxWidthProperty().bind(alert.getDialogPane().widthProperty().subtract(20));

      GridPane expContent = new GridPane();
      expContent.setMaxWidth(Double.MAX_VALUE);
      expContent.add(label, 0, 0);
      expContent.add(textArea, 0, 1);

      textArea.minHeightProperty().bind(expContent.heightProperty().subtract(50));
      // Set expandable Exception into the dialog pane.
      alert.getDialogPane().setExpandableContent(expContent);
      alert.getDialogPane().minHeightProperty().bindBidirectional(dialog.minHeightProperty());
      alert.getDialogPane().setMinWidth(500);
      alert.getDialogPane().setMinHeight(275);

      // Without this setStyle the pane won't resize correctly. Black magic...
      alert.getDialogPane().setStyle(ConfigurationManager.getStyle("creationmodalprocessing.blackmagic"));

      alert.show();
    }
  }

  private void createFrameStructure() {
    mainPane = new BorderPane();
    mainPane.getStyleClass().add(Constants.CSS_BORDER_PANE);
    mainPane.setCenter(createSplitPane());

    // Create Footer after the center because Footer needs to bind to some
    // properties of panes in the center
    Platform.runLater(() -> {
      Footer footer = Footer.getInstance();
      mainPane.setBottom(footer);
    });
  }

  private SplitPane createSplitPane() {
    // Divide center pane in 3
    SplitPane split = new SplitPane();
    // schemePane must be created before fileExplorer because of some variable
    // bindings
    schemePane = new SchemaPane(stage);
    fileExplorer = new FileExplorerPane(stage);
    inspectionPane = new InspectionPane(stage);

    split.setDividerPositions(0.33, 0.67);
    split.getItems().addAll(fileExplorer, schemePane, inspectionPane);

    return split;
  }

  private void createMenu() {
    MenuBar menu = new MenuBar();
    Menu menuFile = new Menu(I18n.t(Constants.I18N_MAIN_FILE));

    Menu menuEdit = new Menu(I18n.t(Constants.I18N_MAIN_EDIT));
    Menu menuClassScheme = new Menu(I18n.t(Constants.I18N_MAIN_CLASS_SCHEME));
    Menu menuView = new Menu(I18n.t(Constants.I18N_MAIN_VIEW));
    Menu menuHelp = new Menu(I18n.t(Constants.I18N_MAIN_HELP));

    Menu language = new Menu(I18n.t(Constants.I18N_MAIN_LANGUAGE));

    Platform.runLater(
      () -> language.setGraphic(new ImageView(FontAwesomeImageCreator.generate(FontAwesomeImageCreator.GLOBE))));

    // File
    final ToggleGroup languageGroup = new ToggleGroup();
    langPT = new RadioMenuItem("Português");
    langPT.setUserData(Constants.LANG_PT);
    langPT.setToggleGroup(languageGroup);
    langEN = new RadioMenuItem("English");
    langEN.setUserData(Constants.LANG_EN);
    langEN.setToggleGroup(languageGroup);
    langHU = new RadioMenuItem("Magyar");
    langHU.setUserData(Constants.LANG_HU);
    langHU.setToggleGroup(languageGroup);
    langES_CL = new RadioMenuItem("Español (Chile)");
    langES_CL.setUserData("es_CL");
    langES_CL.setToggleGroup(languageGroup);
    language.getItems().addAll(langEN, langPT, langHU, langES_CL);

    updateSelectedLanguageMenu();

    languageGroup.selectedToggleProperty().addListener(observable -> {
      if (languageGroup.getSelectedToggle() != null) {
        String lang = (String) languageGroup.getSelectedToggle().getUserData();
        if (!lang.equals(ConfigurationManager.getLocale().getLanguage())) {
          Alert dlg = new Alert(Alert.AlertType.CONFIRMATION);
          dlg.getButtonTypes().clear();
          dlg.getButtonTypes().addAll(
            new ButtonType(I18n.t(Constants.I18N_CANCEL, lang), ButtonBar.ButtonData.CANCEL_CLOSE),
            new ButtonType(I18n.t(Constants.I18N_RESTART, lang), ButtonBar.ButtonData.OK_DONE));
          dlg.initStyle(StageStyle.UNDECORATED);
          dlg.setHeaderText(I18n.t(Constants.I18N_MAIN_UPDATE_LANG_HEADER, lang));
          dlg.setTitle(I18n.t(Constants.I18N_MAIN_UPDATE_LANG_TITLE, lang));
          dlg.setContentText(I18n.t(Constants.I18N_MAIN_UPDATE_LANG_CONTENT, lang));
          dlg.initModality(Modality.APPLICATION_MODAL);
          dlg.initOwner(stage);
          dlg.show();
          dlg.resultProperty().addListener(o -> confirmLanguageChange(lang, dlg.getResult()));
        }
      }
    });

    final MenuItem openConfigurationFolder = new MenuItem(I18n.t(Constants.I18N_MAIN_OPEN_CONFIGURATION_FOLDER));
    openConfigurationFolder.setAccelerator(KeyCombination.keyCombination("Ctrl+G"));
    openConfigurationFolder.setOnAction(event -> OpenPathInExplorer.open(ConfigurationManager.getRodainPath()));

    final MenuItem openFolder = new MenuItem(I18n.t(Constants.I18N_MAIN_ADD_FOLDER));
    openFolder.setAccelerator(KeyCombination.keyCombination("Ctrl+O"));
    openFolder.setOnAction(event -> fileExplorer.chooseNewRoot());

    final MenuItem createSIPs = new MenuItem(I18n.t(Constants.I18N_MAIN_EXPORT_SIPS));
    createSIPs.setAccelerator(KeyCombination.keyCombination("Ctrl+X"));
    createSIPs.setOnAction(event -> exportSIPs());

    final MenuItem quit = new MenuItem(I18n.t(Constants.I18N_MAIN_QUIT));
    quit.setAccelerator(KeyCombination.keyCombination("Ctrl+Q"));
    quit.setOnAction(event -> closeApp());
    final MenuItem reset = new MenuItem(I18n.t(Constants.I18N_MAIN_RESET));
    reset.setAccelerator(KeyCombination.keyCombination("Ctrl+N"));
    reset.setOnAction(event -> {
      Alert dlg = new Alert(Alert.AlertType.CONFIRMATION);
      dlg.initStyle(StageStyle.UNDECORATED);
      dlg.setHeaderText(I18n.t(Constants.I18N_MAIN_CONFIRM_RESET_HEADER));
      dlg.setTitle(I18n.t(Constants.I18N_MAIN_RESET));
      dlg.setContentText(I18n.t(Constants.I18N_MAIN_CONFIRM_RESET_CONTENT));
      dlg.initModality(Modality.APPLICATION_MODAL);
      dlg.initOwner(stage);
      dlg.showAndWait();

      if (dlg.getResult().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
        PathCollection.reset();
        inspectionPane = new InspectionPane(stage);
        fileExplorer = new FileExplorerPane(stage);
        schemePane = new SchemaPane(stage);
        mainPane.setCenter(createSplitPane());
        schemePane.showHelp();
      }
    });

    menuFile.getItems().addAll(reset, openFolder, createSIPs, openConfigurationFolder, quit);

    // Classification scheme
    final MenuItem createCS = new MenuItem(I18n.t(Constants.I18N_MAIN_CREATE_CS));
    createCS.setAccelerator(KeyCombination.keyCombination("Ctrl+R"));
    createCS.setOnAction(event -> schemePane.createClassificationScheme());

    final MenuItem updateCS = new MenuItem(I18n.t(Constants.I18N_MAIN_LOADCS));
    updateCS.setAccelerator(KeyCombination.keyCombination("Ctrl+L"));
    updateCS.setOnAction(event -> schemePane.loadClassificationSchema());

    final MenuItem exportCS = new MenuItem(I18n.t(Constants.I18N_MAIN_EXPORT_CS));
    exportCS.setAccelerator(KeyCombination.keyCombination("Ctrl+E"));
    exportCS.setOnAction(event -> {
      FileChooser chooser = new FileChooser();
      chooser.setTitle(I18n.t(Constants.I18N_FILE_CHOOSER_TITLE));
      File selectedFile = chooser.showSaveDialog(stage);
      if (selectedFile == null)
        return;
      Controller.exportClassificationScheme(schemePane.getSchemaNodes(), selectedFile.toPath().toString());
    });

    menuClassScheme.getItems().addAll(createCS, updateCS, exportCS);

    // Edit
    final MenuItem ignoreItems = new MenuItem(I18n.t(Constants.I18N_MAIN_IGNORE_ITEMS));
    ignoreItems.setAccelerator(KeyCombination.keyCombination("DELETE"));
    ignoreItems.setOnAction(event -> fileExplorer.ignore());

    menuEdit.getItems().addAll(ignoreItems);

    // View
    final MenuItem showFiles = new MenuItem(I18n.t(Constants.I18N_MAIN_HIDE_FILES));
    showFiles.setAccelerator(KeyCombination.keyCombination("Ctrl+F"));
    showFiles.setOnAction(event -> {
      fileExplorer.toggleFilesShowing();
      if (FileExplorerPane.isShowFiles())
        showFiles.setText(I18n.t(Constants.I18N_MAIN_HIDE_FILES));
      else
        showFiles.setText(I18n.t(Constants.I18N_MAIN_SHOW_FILES));
    });
    final MenuItem showIgnored = new MenuItem(I18n.t(Constants.I18N_MAIN_SHOW_IGNORED));
    showIgnored.setAccelerator(KeyCombination.keyCombination("Ctrl+I"));
    showIgnored.setOnAction(event -> {
      fileExplorer.toggleIgnoredShowing();
      if (FileExplorerPane.isShowIgnored())
        showIgnored.setText(I18n.t(Constants.I18N_MAIN_HIDE_IGNORED));
      else
        showIgnored.setText(I18n.t(Constants.I18N_MAIN_SHOW_IGNORED));
    });
    final MenuItem showMapped = new MenuItem(I18n.t(Constants.I18N_MAIN_SHOW_MAPPED));
    showMapped.setAccelerator(KeyCombination.keyCombination("Ctrl+M"));
    showMapped.setOnAction(event -> {
      fileExplorer.toggleMappedShowing();
      if (FileExplorerPane.isShowMapped())
        showMapped.setText(I18n.t(Constants.I18N_MAIN_HIDE_MAPPED));
      else
        showMapped.setText(I18n.t(Constants.I18N_MAIN_SHOW_MAPPED));
    });

    menuView.getItems().addAll(showFiles, showIgnored, showMapped);

    // Help
    final MenuItem checkVersion = new MenuItem(I18n.t(Constants.I18N_MAIN_CHECK_VERSION));
    checkVersion.setAccelerator(KeyCombination.keyCombination("Ctrl+U"));
    checkVersion.setOnAction(event -> {
      if (!checkForUpdates(false)) {
        try {
          Alert dlg = new Alert(Alert.AlertType.INFORMATION);
          dlg.initStyle(StageStyle.UNDECORATED);
          dlg.setHeaderText(
            String.format(I18n.t(Constants.I18N_MAIN_NO_UPDATES_HEADER), Controller.getCurrentVersion()));
          dlg.setContentText(I18n.t(Constants.I18N_MAIN_NO_UPDATES_CONTENT));
          dlg.initModality(Modality.APPLICATION_MODAL);
          dlg.initOwner(stage);

          dlg.getDialogPane().setMinWidth(300);
          dlg.show();
        } catch (ConfigurationException e) {
          LOGGER.debug("Unable to get version.", e);
        }
      }
    });

    String startingValue = ConfigurationManager.getAppConfig(Constants.CONF_K_APP_HELP_ENABLED);
    String showHelpText;
    if (Boolean.parseBoolean(startingValue)) {
      showHelpText = I18n.t(Constants.I18N_MAIN_HIDE_HELP);
    } else {
      showHelpText = I18n.t(Constants.I18N_MAIN_SHOW_HELP);
    }
    final MenuItem showHelp = new MenuItem(showHelpText);
    showHelp.setAccelerator(KeyCombination.keyCombination("Ctrl+H"));
    showHelp.setOnAction(event -> {
      String currentValue = ConfigurationManager.getAppConfig(Constants.CONF_K_APP_HELP_ENABLED);
      if (Boolean.parseBoolean(currentValue)) {
        showHelp.setText(I18n.t(Constants.I18N_MAIN_SHOW_HELP));
        ConfigurationManager.setAppConfig(Constants.CONF_K_APP_HELP_ENABLED, Constants.CONF_V_FALSE, true);
        currentValue = I18n.t(Constants.I18N_MAIN_HIDE_HELP);
      } else {
        showHelp.setText(I18n.t(Constants.I18N_MAIN_HIDE_HELP));
        ConfigurationManager.setAppConfig(Constants.CONF_K_APP_HELP_ENABLED, Constants.CONF_V_TRUE, true);
        currentValue = I18n.t(Constants.I18N_MAIN_SHOW_HELP);
      }
      Alert dlg = new Alert(Alert.AlertType.CONFIRMATION);
      dlg.getButtonTypes().clear();
      dlg.getButtonTypes().addAll(new ButtonType(I18n.t(Constants.I18N_CANCEL), ButtonBar.ButtonData.CANCEL_CLOSE),
        new ButtonType(I18n.t(Constants.I18N_RESTART), ButtonBar.ButtonData.OK_DONE));
      dlg.initStyle(StageStyle.UNDECORATED);
      dlg.setHeaderText(currentValue);
      dlg.setContentText(I18n.t(Constants.I18N_MAIN_UPDATE_LANG_CONTENT));
      dlg.initModality(Modality.APPLICATION_MODAL);
      dlg.initOwner(stage);
      dlg.show();
      dlg.resultProperty().addListener(o -> confirmRestart(dlg.getResult()));
    });

    final MenuItem helpPage = new MenuItem(I18n.t(Constants.I18N_MAIN_HELP_PAGE));
    helpPage.setOnAction(event -> {
      ModalStage modalStage = new ModalStage(stage);
      modalStage.setRoot(new HelpModal(modalStage), false);
    });

    menuHelp.getItems().addAll(language, checkVersion, showHelp, helpPage);

    menu.getMenus().addAll(menuFile, menuEdit, menuClassScheme, menuView, menuHelp);
    mainPane.setTop(menu);
  }

  private void updateSelectedLanguageMenu() {
    switch (ConfigurationManager.getLocale().toLanguageTag().toLowerCase()) {
      case Constants.LANG_EN:
        langEN.setSelected(true);
        break;
      case Constants.LANG_PT_PT:
      case Constants.LANG_PT_BR:
      case Constants.LANG_PT:
        langPT.setSelected(true);
        break;
      case Constants.LANG_HU:
        langHU.setSelected(true);
        break;
      case Constants.LANG_ES:
      case Constants.LANG_ES_CL:
        langES_CL.setSelected(true);
        break;
      default:
        langEN.setSelected(true);
        break;
    }
  }

  private void confirmRestart(ButtonType result) {
    if (result.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
      restartApplication();
    }
  }

  private void confirmLanguageChange(String lang, ButtonType result) {
    if (result.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
      ConfigurationManager.setAppConfig(Constants.CONF_K_APP_LANGUAGE, lang, true);
      restartApplication();
    } else {
      updateSelectedLanguageMenu();
    }
  }

  private void restartApplication() {
    try {
      final File currentExecutable = new File(
        RodaInApplication.class.getProtectionDomain().getCodeSource().getLocation().toURI());

      /* is it a jar or exe file? */
      if (currentExecutable.getName().endsWith(".jar")) {
        /* Build command: java -jar application.jar */
        final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        final ArrayList<String> command = new ArrayList<>();
        command.add(javaBin);
        command.add("-jar");
        command.add(currentExecutable.getPath());

        final ProcessBuilder builder = new ProcessBuilder(command);
        builder.start();
        Platform.exit();
      } else if (currentExecutable.getName().endsWith(".exe")) {
        OpenPathInExplorer.open(currentExecutable.toPath());
        Platform.exit();
      }
    } catch (URISyntaxException e) {
      LOGGER.error("Error creating URI when restarting the application", e);
    } catch (IOException e) {
      LOGGER.error("Error creating the process to restart the application", e);
    }
  }

  private static void closeApp() {
    Controller.exportClassificationScheme(schemePane.getSchemaNodes(),
      ConfigurationManager.getRodainPath().resolve(".plan.temp").toString());
    // 20170308 hsilva: disabled watchservice
    // fileExplorer.closeWatcher();
    VisitorStack.end();
    Footer.getInstance().cancelMemoryAutoUpdater();
    Platform.exit();
  }

  /**
   * @param checkForEnvVariable
   *          if true, the method will consult RODA-in env. variable to see if
   *          its running in a special mode (e.g. testing), in order to avoid
   *          checking for version update
   */
  private static boolean checkForUpdates(boolean checkForEnvVariable) {
    Optional<String> updateMessage = Controller.checkForUpdates(checkForEnvVariable);
    if (updateMessage.isPresent()) {
      Alert dlg = new Alert(Alert.AlertType.CONFIRMATION);
      dlg.initStyle(StageStyle.UNDECORATED);
      dlg.setHeaderText(I18n.t(Constants.I18N_MAIN_NEW_VERSION_HEADER));
      dlg.setTitle("");
      dlg.setContentText(updateMessage.get());
      dlg.initModality(Modality.APPLICATION_MODAL);
      dlg.initOwner(stage);

      dlg.getDialogPane().setMinWidth(300);
      dlg.showAndWait();

      if (dlg.getResult().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
        OpenPathInExplorer.open(Constants.RODAIN_GITHUB_LATEST_VERSION_LINK);
      }
      return true;
    } else {
      return false;
    }
  }

  /**
   * @return The selected items in the file explorer
   */
  public static Set<SourceTreeItem> getSourceSelectedItems() {
    return fileExplorer.getSelectedItems();
  }

  /**
   * @return The inspection pane object.
   */
  public static InspectionPane getInspectionPane() {
    return inspectionPane;
  }

  /**
   * @return The file explorer object.
   */
  public static FileExplorerPane getFileExplorer() {
    return fileExplorer;
  }

  /**
   * @return The scheme pane object.
   */
  public static SchemaPane getSchemePane() {
    return schemePane;
  }

  /**
   * @return The Map with the selected SIPs of all the SchemaNodes in the scheme
   *         pane
   */
  public static Map<Sip, List<String>> getSelectedDescriptionObjects() {
    return schemePane.getSelectedDescriptionObjects();
  }

  /**
   * @return The Map with all the SIPs of all the SchemaNodes in the scheme pane
   */
  public static Map<Sip, List<String>> getAllDescriptionObjects() {
    return schemePane.getAllDescriptionObjects();
  }

  /**
   * Shows a pane to start the export process of the created SIPs.
   */
  public static void exportSIPs() {
    // force the edits to the metadata text area to be saved
    inspectionPane.saveMetadata();

    CreationModalStage creationStage = new CreationModalStage(stage);
    CreationModalPreparation pane = new CreationModalPreparation(creationStage);
    creationStage.setRoot(pane);
  }
}
