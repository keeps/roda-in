package org.roda.rodain.creation.ui;

import java.io.File;
import java.nio.file.Path;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;

import org.roda.rodain.creation.SipTypes;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 19/11/2015.
 */
public class CreationModalPreparation extends BorderPane {
  private CreationModalStage stage;

  private Path outputFolder;
  private ComboBox<String> sipTypes;

  public CreationModalPreparation(CreationModalStage stage) {
    this.stage = stage;

    getStyleClass().add("sipcreator");

    createTop();
    createCenter();
    createBottom();
  }

  private void createTop() {
    VBox top = new VBox(5);
    top.getStyleClass().add("hbox");
    top.setPadding(new Insets(10, 10, 10, 0));
    top.setAlignment(Pos.CENTER);

    Label title = new Label("Creating SIPs");
    title.setId("title");

    top.getChildren().add(title);
    setTop(top);
  }

  private void createCenter() {
    VBox center = new VBox(5);
    center.setAlignment(Pos.CENTER_LEFT);
    center.setPadding(new Insets(0, 10, 10, 10));

    HBox outputFolderBox = createOutputFolder();
    HBox sipTypesBox = createSipTypes();

    center.getChildren().addAll(outputFolderBox, sipTypesBox);
    setCenter(center);
  }

  private HBox createOutputFolder() {
    HBox outputFolderBox = new HBox(5);
    outputFolderBox.setAlignment(Pos.CENTER_LEFT);
    HBox space = new HBox();
    HBox.setHgrow(space, Priority.ALWAYS);

    Label outputFolderLabel = new Label("Output directory");
    Button chooseFile = new Button("Choose...");
    chooseFile.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent e) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Please choose a directory");
        File selectedFile = chooser.showDialog(stage);
        if (selectedFile == null)
          return;
        outputFolder = selectedFile.toPath();
        chooseFile.setText(selectedFile.toPath().getFileName().toString());
      }
    });

    outputFolderBox.getChildren().addAll(outputFolderLabel, space, chooseFile);
    return outputFolderBox;
  }

  private HBox createSipTypes() {
    HBox sipTypesBox = new HBox(5);
    sipTypesBox.setAlignment(Pos.CENTER_LEFT);
    HBox space = new HBox();
    HBox.setHgrow(space, Priority.ALWAYS);

    Label sipTypesLabel = new Label("SIP type");

    sipTypes = new ComboBox<>();
    sipTypes.getItems().addAll("BagIt", "EARK");
    sipTypes.getSelectionModel().selectFirst();

    sipTypesBox.getChildren().addAll(sipTypesLabel, space, sipTypes);
    return sipTypesBox;
  }

  private void createBottom() {
    HBox bottom = new HBox();
    bottom.setPadding(new Insets(0, 10, 10, 10));
    bottom.setAlignment(Pos.CENTER_LEFT);

    HBox space = new HBox();
    HBox.setHgrow(space, Priority.ALWAYS);

    Button cancel = new Button("Cancel");
    cancel.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        stage.close();
      }
    });

    Button start = new Button("Start");
    start.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        String selectedType = sipTypes.getSelectionModel().getSelectedItem();
        SipTypes type;
        if (selectedType.equals("BagIt"))
          type = SipTypes.BAGIT;
        else
          type = SipTypes.EARK;

        stage.startCreation(outputFolder, type);
      }
    });

    bottom.getChildren().addAll(cancel, space, start);
    setBottom(bottom);
  }
}