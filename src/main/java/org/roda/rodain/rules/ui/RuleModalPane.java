package org.roda.rodain.rules.ui;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.roda.rodain.rules.MetadataTypes;
import org.roda.rodain.rules.RuleTypes;
import org.roda.rodain.schema.ui.SchemaNode;
import org.roda.rodain.source.ui.items.SourceTreeDirectory;
import org.roda.rodain.source.ui.items.SourceTreeItem;
import org.roda.rodain.utils.FontAwesomeImageCreator;
import org.roda.rodain.utils.Utils;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 28-09-2015.
 */
public class RuleModalPane extends BorderPane {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(RuleModalPane.class.getName());
    private enum States {
        ASSOCIATION,
        METADATA
    }
    private static Properties properties;

    private Stage stage;
    private SchemaNode schema;
    private Set<SourceTreeItem> sourceSet;
    //Association
    private VBox boxAssociation;
    private ListView<HBoxCell> assocList;
    private ComboBox<Integer> level;
    //Metadata
    private VBox gridMetadata;
    private ToggleGroup groupMetadata;
    private RadioButton diffFolder, sameFolder;
    private Button chooseDir, chooseFile;

    private Button btContinue, btCancel, btBack;
    private HBox space, buttons;
    private States currentState;
    private String fromFile, diffDir, sameDir;

    private int folderCount, fileCount;


    public RuleModalPane(Stage stage, Set<SourceTreeItem> sourceSet, SchemaNode schemaNode){
        super();
        schema = schemaNode;
        this.sourceSet = sourceSet;
        this.stage = stage;
        getStyleClass().add("modal");

        createTop();
        createCenter();
        createBottom();

        currentState = States.ASSOCIATION;
    }

    public static void setProperties(Properties prop){
        properties = prop;
    }

    private void createTop(){
        StackPane pane = new StackPane();
        pane.setPadding(new Insets(0, 0, 10, 0));

        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("hbox");
        box.setPadding(new Insets(5, 5, 5, 5));
        pane.getChildren().add(box);

        Label title = new Label("Create association to " + schema.getDob().getTitle());
        title.setId("title");

        ArrayList<String> dirs = new ArrayList<>();
        ArrayList<String> fil = new ArrayList<>();
        for(SourceTreeItem it: sourceSet) {
            if(it instanceof SourceTreeDirectory)
                dirs.add(it.getValue());
            else fil.add(it.getValue());
        }
        folderCount = dirs.size();
        fileCount = fil.size();

        box.getChildren().add(title);

        setTop(pane);
    }

    private void createCenter(){
        createCenterAssociation();
        gridMetadata = createCenterMetadata();

        setCenter(boxAssociation);
    }

    private void createCenterAssociation(){
        boxAssociation = new VBox();
        boxAssociation.setPadding(new Insets(0, 10, 0, 10));

        assocList = new ListView<>();

        assocList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<HBoxCell>() {
                    public void changed(ObservableValue<? extends HBoxCell> observable, final HBoxCell oldValue, HBoxCell newValue) {
                        if(newValue != null && newValue.isDisabled()){
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    if(oldValue != null)
                                        assocList.getSelectionModel().select(oldValue);
                                    else assocList.getSelectionModel().clearSelection();
                                }
                            });
                        }
                    }
                });

        String icon = properties.getProperty("association.singleSip.icon");
        String title = properties.getProperty("association.singleSip.title");
        String description = properties.getProperty("association.singleSip.description");
        HBoxCell cellSingleSip = new HBoxCell(icon, title, description, new HBox());
        cellSingleSip.setUserData(RuleTypes.SINGLESIP);

        icon = properties.getProperty("association.sipSelection.icon");
        title = properties.getProperty("association.sipSelection.title");
        description = properties.getProperty("association.sipSelection.description");
        HBoxCell cellSelected = new HBoxCell(icon, title, description, new HBox());
        cellSelected.setUserData(RuleTypes.SIPPERSELECTION);

        icon = properties.getProperty("association.sipPerFile.icon");
        title = properties.getProperty("association.sipPerFile.title");
        description = properties.getProperty("association.sipPerFile.description");
        HBoxCell cellSipPerFile = new HBoxCell(icon, title, description, new HBox());
        cellSipPerFile.setUserData(RuleTypes.SIPPERFILE);

        icon = properties.getProperty("association.sipPerFolder.icon");
        title = properties.getProperty("association.sipPerFolder.title");
        description = properties.getProperty("association.sipPerFolder.description");
        HBox options = createPerFolderOptions();
        HBoxCell cellSipPerFolder = new HBoxCell(icon, title, description, options);
        cellSipPerFolder.setUserData(RuleTypes.SIPPERFOLDER);

        ObservableList<HBoxCell> hboxList = FXCollections.observableArrayList();
        hboxList.addAll(cellSingleSip, cellSelected, cellSipPerFile, cellSipPerFolder);
        assocList.setItems(hboxList);

        if(folderCount == 0 || level.getItems().size() == 0){
            cellSipPerFolder.setDisable(true);
        }

        boxAssociation.getChildren().add(assocList);
    }

    private HBox createPerFolderOptions(){
        HBox resultBox = new HBox(10);
        resultBox.setAlignment(Pos.CENTER_LEFT);

        int depth = 0;
        if(folderCount == sourceSet.size()) { //check if the selected items are all directories
            //we only need to compute the depth if we'll be going to activate the radio button
            for (SourceTreeItem std : sourceSet) {
                Path startPath = Paths.get(std.getPath());
                int depthAux = Utils.getRelativeMaxDepth(startPath);
                if (depthAux > depth)
                    depth = depthAux;
            }
        }

        ArrayList<Integer> levels = new ArrayList<>();
        for (int i = 1; i <= depth; i++)
            levels.add(i);

        ObservableList<Integer> options = FXCollections.observableArrayList(levels);
        level = new ComboBox<>(options);
        level.setValue((int)Math.ceil(depth/2.0));

        Label lLevel = new Label("Max depth");
        resultBox.getChildren().addAll(lLevel, level);

        return resultBox;
    }

    private VBox createCenterMetadata(){
        VBox gridCenter = new VBox(10);
        gridCenter.setPadding(new Insets(0, 10, 0, 10));

        Label metaTitle = new Label("Apply metadata from:");
        metaTitle.setId("title");

        VBox center = new VBox(5);
        groupMetadata = new ToggleGroup();

        RadioButton newFile = new RadioButton("An empty text area");
        newFile.setUserData(MetadataTypes.NEWTEXT);
        newFile.setToggleGroup(groupMetadata);

        rbSameFolder();
        HBox singleFileBox = rbSingleFile();
        HBox diffFolderBox = rbDiffFolder();
        center.getChildren().addAll(singleFileBox, sameFolder, diffFolderBox, newFile);

        gridCenter.getChildren().addAll(metaTitle, center);

        return gridCenter;
    }

    private HBox rbSingleFile(){
        HBox box = new HBox();
        box.setAlignment(Pos.CENTER_LEFT);

        final RadioButton singleFile = new RadioButton("A single file");
        singleFile.setToggleGroup(groupMetadata);
        singleFile.setUserData(MetadataTypes.SINGLEFILE);
        singleFile.setSelected(true);

        chooseFile = new Button("Choose File...");
        chooseFile.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                singleFile.setSelected(true);
                FileChooser chooser = new FileChooser();
                chooser.setTitle("Please choose a file");
                chooser.setInitialDirectory(new File(sameDir));
                File selectedFile = chooser.showOpenDialog(stage);
                if (selectedFile == null)
                    return;
                fromFile = selectedFile.toPath().toString();
                chooseFile.setText(selectedFile.toPath().getFileName().toString());
                chooseFile.setUserData(fromFile);
            }
        });

        HBox spaceSingleFile = new HBox();
        HBox.setHgrow(spaceSingleFile, Priority.ALWAYS);

        box.getChildren().addAll(singleFile, spaceSingleFile, chooseFile);
        return box;
    }

    private void rbSameFolder(){
        //fill list with the directories in the source set
        List<String> directories = new ArrayList<>();
        for(SourceTreeItem sti: sourceSet){
            if(sti instanceof SourceTreeDirectory)
                directories.add(sti.getPath());
            else{ //if the item isn't a directory, get its parent
                Path path = Paths.get(sti.getPath());
                directories.add(path.getParent().toString());
            }
        }
        //get the common prefix of the directories
        sameDir = Utils.longestCommonPrefix(directories);

        sameFolder = new RadioButton("The directory \"" + sameDir + "\"");
        sameFolder.setToggleGroup(groupMetadata);
        sameFolder.setUserData(MetadataTypes.SAMEDIRECTORY);
    }

    private HBox rbDiffFolder(){
        HBox box = new HBox();
        box.setAlignment(Pos.CENTER_LEFT);

        diffFolder = new RadioButton("Another directory");
        diffFolder.setUserData(MetadataTypes.DIFFDIRECTORY);
        diffFolder.setToggleGroup(groupMetadata);

        chooseDir = new Button("Choose Directory...");
        chooseDir.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                diffFolder.setSelected(true);
                DirectoryChooser chooser = new DirectoryChooser();
                chooser.setTitle("Please choose a folder");
                chooser.setInitialDirectory(new File(sameDir));
                File selectedDirectory = chooser.showDialog(stage);
                if (selectedDirectory == null)
                    return;
                diffDir = selectedDirectory.toPath().toString();
                chooseDir.setText(selectedDirectory.toPath().getFileName().toString());
                chooseDir.setUserData(diffDir);
            }
        });

        HBox spaceDiffFolder = new HBox();
        HBox.setHgrow(spaceDiffFolder, Priority.ALWAYS);

        box.getChildren().addAll(diffFolder, spaceDiffFolder, chooseDir);
        return box;
    }

    private void createBottom(){
        createContinueButton();
        createBackButton();
        createCancelButton();

        space = new HBox();
        HBox.setHgrow(space, Priority.ALWAYS);

        buttons = new HBox(10);
        buttons.setPadding(new Insets(10, 10, 10, 10));
        buttons.setAlignment(Pos.CENTER);
        buttons.getStyleClass().add("hbox");
        buttons.getChildren().addAll(btCancel, space, btContinue);

        setBottom(buttons);
    }

    private void createContinueButton(){
        btContinue = new Button("Continue");
        btContinue.setMaxWidth(100);
        btContinue.setMinWidth(100);
        btContinue.setGraphic(new ImageView(FontAwesomeImageCreator.im_chevron_right));
        btContinue.setGraphicTextGap(10);
        btContinue.setContentDisplay(ContentDisplay.RIGHT);

        btContinue.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                if(currentState == States.ASSOCIATION) {
                    if(assocList.getSelectionModel().getSelectedIndex() != -1) {
                        setCenter(gridMetadata);
                        currentState = States.METADATA;
                        enableMetaRadioButtons();
                        buttons.getChildren().clear();
                        buttons.getChildren().addAll(btCancel, space, btBack, btContinue);
                        btContinue.setText("Confirm");
                        btContinue.setGraphicTextGap(16);
                    }
                }else
                    if(currentState == States.METADATA && metadataCheckContinue())
                        RuleModalController.confirm();
            }
        });
    }

    private void createCancelButton(){
        btCancel = new Button("Cancel");
        btCancel.setMaxWidth(100);
        btCancel.setMinWidth(100);
        btCancel.setGraphic(new ImageView(FontAwesomeImageCreator.im_times));
        btCancel.setGraphicTextGap(20);
        btCancel.setContentDisplay(ContentDisplay.RIGHT);

        btCancel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                RuleModalController.cancel();
            }
        });
    }

    private void createBackButton(){
        btBack = new Button("Back");
        btBack.setMaxWidth(100);
        btBack.setMinWidth(100);
        btBack.setGraphic(new ImageView(FontAwesomeImageCreator.im_chevron_left));
        btBack.setGraphicTextGap(30);

        btBack.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if(currentState == States.ASSOCIATION) {
                    setCenter(gridMetadata);
                    currentState = States.METADATA;
                    enableMetaRadioButtons();
                }else if(currentState == States.METADATA) {
                    setCenter(boxAssociation);
                    currentState = States.ASSOCIATION;
                    buttons.getChildren().clear();
                    buttons.getChildren().addAll(btCancel, space, btContinue);
                    btContinue.setText("Continue");
                    btContinue.setGraphicTextGap(10);
                }
            }
        });
    }

    private boolean metadataCheckContinue(){
        try {
            MetadataTypes metaType = getMetadataType();
            if(metaType == MetadataTypes.SINGLEFILE)
                return chooseFile.getUserData() != null;
            if(metaType == MetadataTypes.DIFFDIRECTORY)
                return chooseDir.getUserData() != null;
        } catch (Exception e) {
            log.error("Error getting metadata type", e);
        }
        return true;
    }

    private void enableMetaRadioButtons(){
        try {
            RuleTypes assocType = getAssociationType();
            if(assocType == RuleTypes.SIPPERFILE){
                sameFolder.setDisable(false);
                diffFolder.setDisable(false);
                chooseDir.setDisable(false);
            }else {
                sameFolder.setDisable(true);
                diffFolder.setDisable(true);
                chooseDir.setDisable(true);
            }
        } catch (Exception e) {
            log.error("Error getting association type", e);
        }
    }

    public RuleTypes getAssociationType() throws UnexpectedDataTypeException{
        HBoxCell cell = assocList.getSelectionModel().getSelectedItem();
        if(cell.getUserData() instanceof RuleTypes)
            return (RuleTypes)cell.getUserData();
        else throw new UnexpectedDataTypeException();
    }

    public int getLevel(){
        return level.getValue();
    }

    public MetadataTypes getMetadataType() throws UnexpectedDataTypeException{
        Toggle selected = groupMetadata.getSelectedToggle();
        if(selected.getUserData() instanceof MetadataTypes)
            return (MetadataTypes)selected.getUserData();
        else throw new UnexpectedDataTypeException();
    }

    public Path getFromFile() {
        return Paths.get(fromFile);
    }

    public Path getDiffDir() {
        return Paths.get(diffDir);
    }

    public Path getSameDir() {
        return Paths.get(sameDir);
    }
}
