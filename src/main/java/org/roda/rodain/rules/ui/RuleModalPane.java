package org.roda.rodain.rules.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.lang.StringUtils;
import org.roda.rodain.rules.MetadataTypes;
import org.roda.rodain.rules.RuleTypes;
import org.roda.rodain.schema.ui.SchemaNode;
import org.roda.rodain.source.ui.items.SourceTreeDirectory;
import org.roda.rodain.source.ui.items.SourceTreeFile;
import org.roda.rodain.source.ui.items.SourceTreeItem;
import org.roda.rodain.utils.Utils;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
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
    private Stage stage;
    private SchemaNode schema;
    private Set<SourceTreeItem> sourceSet;
    //Association
    private GridPane gridAssociation;
    private ToggleGroup groupAssoc;
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

    private void createTop(){
        StackPane pane = new StackPane();
        pane.setPadding(new Insets(0, 0, 10, 0));

        VBox box = new VBox(5);
        box.getStyleClass().add("hbox");
        box.setPadding(new Insets(5, 5, 5, 5));
        pane.getChildren().add(box);

        Label title = new Label("Association options");
        title.setId("title");

        ArrayList<String> dirs = new ArrayList<>();
        ArrayList<String> fil = new ArrayList<>();
        for(SourceTreeItem it: sourceSet) {
            if(it instanceof SourceTreeDirectory)
                dirs.add(it.getValue());
            else fil.add(it.getValue());
        }

        BorderPane middle = new BorderPane();
        HBox.setHgrow(middle, Priority.ALWAYS);

        // source
        VBox sourceBox = new VBox(5);
        if(dirs.size() > 0) {
            Label directories = new Label();
            directories.setGraphic(new ImageView(SourceTreeDirectory.folderCollapseImage));
            directories.setWrapText(true);
            directories.setMaxWidth(600);
            String directoriesString = StringUtils.join(dirs, ", ");
            directories.setText(directoriesString);
            sourceBox.getChildren().add(directories);
        }
        if(fil.size() > 0) {
            Label files = new Label();
            files.setGraphic(new ImageView(SourceTreeFile.fileImage));
            files.setWrapText(true);
            files.setMaxWidth(600);
            String filesString = StringUtils.join(fil, ", ");
            files.setText(filesString);
            sourceBox.getChildren().add(files);
        }

        // destination
        VBox destBox = new VBox(5);

        Label descObj = new Label(schema.getDob().getTitle());
        descObj.setGraphic(new ImageView(schema.getImage()));
        descObj.setTextAlignment(TextAlignment.LEFT);

        destBox.getChildren().addAll(descObj);

        middle.setLeft(sourceBox);
        middle.setRight(destBox);

        box.getChildren().addAll(title, middle);

        setTop(pane);
    }

    private void createCenter(){
        gridAssociation = createCenterAssociation();
        gridMetadata = createCenterMetadata();

        setCenter(gridAssociation);
    }

    private GridPane createCenterAssociation(){
        GridPane gridCenter = new GridPane();
        gridCenter.setPadding(new Insets(0, 10, 0, 10));
        gridCenter.setVgap(5);
        gridCenter.setHgap(10);

        groupAssoc = new ToggleGroup();

        RadioButton singleSip = new RadioButton("Create a single SIP");
        singleSip.setToggleGroup(groupAssoc);
        singleSip.setUserData(RuleTypes.SINGLESIP);
        singleSip.setSelected(true);

        RadioButton perFile = new RadioButton("Create one SIP per file");
        perFile.setToggleGroup(groupAssoc);
        perFile.setUserData(RuleTypes.SIPPERFILE);

        //count number of folders selected in the source
        int dirCount = 0;
        for(SourceTreeItem sti: sourceSet)
            if(sti instanceof SourceTreeDirectory)
                dirCount++;

        RadioButton byFolder = new RadioButton("Create one SIP per folder until level");
        byFolder.setUserData(RuleTypes.SIPPERFOLDER);
        byFolder.setToggleGroup(groupAssoc);

        int depth = 0;
        if(dirCount == sourceSet.size()) { //check if the selected items are all directories
            //we only need to compute the depth if we'll be going to activate the radio button
            for (SourceTreeItem std : sourceSet) {
                Path startPath = Paths.get(std.getPath());
                int depthAux = Utils.getRelativeMaxDepth(startPath);
                if (depthAux > depth)
                    depth = depthAux;
            }
        }else byFolder.setDisable(true); //disable this option because it doesn't make sense if all the top level items aren't directories

        ArrayList<Integer> levels = new ArrayList<>();
        for (int i = 1; i <= depth; i++)
            levels.add(i);

        ObservableList<Integer> options = FXCollections.observableArrayList(levels);
        level = new ComboBox<>(options);
        level.setValue((int)Math.ceil(depth/2.0));

        if(dirCount != sourceSet.size()) //disable the level comboBox too if this option doesn't make sense
            level.setDisable(true);

        gridCenter.add(singleSip, 0, 1);
        gridCenter.add(perFile, 0, 2);
        gridCenter.add(byFolder, 0, 3);
        gridCenter.add(level, 1, 3);

        return gridCenter;
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
        btCancel = new Button("Cancel");
        btContinue = new Button("Continue");
        btBack = new Button("Back");

        space = new HBox();
        HBox.setHgrow(space, Priority.ALWAYS);

        buttons = new HBox(5);
        buttons.setPadding(new Insets(10, 10, 10, 10));
        buttons.setSpacing(10);
        buttons.setAlignment(Pos.CENTER);
        buttons.getChildren().addAll(btCancel, space, btContinue);

        btBack.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if(currentState == States.ASSOCIATION) {
                    setCenter(gridMetadata);
                    currentState = States.METADATA;
                    enableMetaRadioButtons();
                }else if(currentState == States.METADATA) {
                    setCenter(gridAssociation);
                    currentState = States.ASSOCIATION;
                    buttons.getChildren().clear();
                    buttons.getChildren().addAll(btCancel, space, btContinue);
                    btContinue.setText("Continue");
                }
            }
        });

        btContinue.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                if(currentState == States.ASSOCIATION) {
                    setCenter(gridMetadata);
                    currentState = States.METADATA;
                    enableMetaRadioButtons();
                    buttons.getChildren().clear();
                    buttons.getChildren().addAll(btCancel, btBack, space, btContinue);
                    btContinue.setText("Confirm");
                }else
                    if(currentState == States.METADATA && metadataCheckContinue())
                        RuleModalController.confirm();
            }
        });

        btCancel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                RuleModalController.cancel();
            }
        });

        setBottom(buttons);
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
        Toggle selected = groupAssoc.getSelectedToggle();
        if(selected.getUserData() instanceof RuleTypes)
            return (RuleTypes)selected.getUserData();
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
