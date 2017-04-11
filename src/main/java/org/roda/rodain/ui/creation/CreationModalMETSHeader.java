package org.roda.rodain.ui.creation;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.roda.rodain.core.ConfigurationManager;
import org.roda.rodain.core.Constants;
import org.roda.rodain.core.I18n;
import org.roda.rodain.core.sip.naming.SIPNameBuilder;
import org.roda.rodain.ui.creation.METSHeaderComponents.AbstractGroup;
import org.roda.rodain.ui.creation.METSHeaderComponents.AgentGroup;
import org.roda.rodain.ui.creation.METSHeaderComponents.AltRecordGroup;
import org.roda.rodain.ui.creation.METSHeaderComponents.METSHeaderUtils;
import org.roda.rodain.ui.creation.METSHeaderComponents.Section;
import org.roda.rodain.ui.creation.METSHeaderComponents.StatusGroup;
import org.roda_project.commons_ip.model.IPHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.javafx.scene.control.skin.ScrollPaneSkin;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public class CreationModalMETSHeader extends BorderPane {
  private static final Logger LOGGER = LoggerFactory.getLogger(CreationModalMETSHeader.class.getName());

  private final static int DEFAULT_TEXTFIELD_WIDTH = 240;

  private final SimpleBooleanProperty validationErrorsVisibilityProperty;

  private CreationModalStage stage;
  private CreationModalPreparation previousPanel;
  private final Path outputFolder;
  private final boolean exportAll;
  private final boolean exportItems;
  private Constants.SipType sipType;
  private final SIPNameBuilder sipNameBuilder;
  private final boolean createReport;
  private Button start;
  private ScrollPane scrollPane;

  private List<AbstractGroup> fieldGroups;

  private IPHeader savedHeader;

  /**
   * Creates a modal to input METS Header info.
   * <p/>
   * <p>
   * This class creates a pane with the fieldGroups necessary to fill in METS
   * Header information needed by the Hungarian SIP 4 format.
   * </p>
   *
   * @param stage
   *          The stage of the pane
   */
  public CreationModalMETSHeader(CreationModalStage stage, CreationModalPreparation previousPanel, Path outputFolder,
    boolean exportAll, boolean exportItems, Constants.SipType sipType, SIPNameBuilder sipNameBuilder,
    boolean createReport) {
    this.stage = stage;
    this.previousPanel = previousPanel;
    this.outputFolder = outputFolder;
    this.exportAll = exportAll;
    this.exportItems = exportItems;
    this.sipType = sipType;
    this.sipNameBuilder = sipNameBuilder;
    this.createReport = createReport;
    this.savedHeader = ConfigurationManager
      .deserialize(sipType.name() + Constants.RODAIN_SERIALIZE_FILE_METS_HEADER_SUFFIX);

    this.validationErrorsVisibilityProperty = new SimpleBooleanProperty(false);

    getStyleClass().add(Constants.CSS_SIPCREATOR);

    createTop();
    createCenter();
    createBottom();

    stage.sizeToScene();
    stage.setWidth(stage.getWidth() * 1.2);
    stage.setHeight(stage.getHeight() * 1.4);
  }

  private void createTop() {
    VBox top = new VBox(5);
    top.getStyleClass().add(Constants.CSS_HBOX);
    top.setPadding(new Insets(10, 10, 10, 0));
    top.setAlignment(Pos.CENTER);

    Label title = new Label(I18n.t(Constants.I18N_CREATIONMODALMETSHEADER_HEADER));
    title.setId("title");

    top.getChildren().add(title);
    setTop(top);
  }

  private void createCenter() {
    VBox center = new VBox(5);
    center.setAlignment(Pos.CENTER_LEFT);
    center.setPadding(new Insets(0, 0, 10, 0));

    scrollPane = new ScrollPane();
    // 20170330 bferreira - source: http://stackoverflow.com/a/29376445/1483200
    scrollPane.getStyleClass().add(Constants.CSS_EDGE_TO_EDGE);
    scrollPane.setFitToWidth(true);
    scrollPane.setSkin(new ScrollPaneSkin(scrollPane) {
      @Override
      public void onTraverse(Node n, Bounds b) {
        /*
         * 20170330 bferreira: Workaround to avoid having the scrollPane scroll
         * to the top when a "remove field" button is clicked. Source:
         * http://www.developersite.org/103-2678-java
         */
      }
    });

    Section sectionStatus = new Section(I18n.t(Constants.I18N_CREATIONMODALMETSHEADER_SECTION_STATUS));
    Section sectionAltRecord = new Section(I18n.t(Constants.I18N_CREATIONMODALMETSHEADER_SECTION_ALTRECORDS));
    Section sectionAgent = new Section(I18n.t(Constants.I18N_CREATIONMODALMETSHEADER_SECTION_AGENTS));

    VBox inner = new VBox(0, sectionStatus, sectionAltRecord, sectionAgent);
    // inner.setPadding(new Insets(10, 10, 10, 10));
    inner.setAlignment(Pos.CENTER_LEFT);

    this.fieldGroups = new ArrayList<>();
    String[] shortIds = METSHeaderUtils.getFieldList(sipType);
    AbstractGroup group = null;
    for (String fieldShortId : shortIds) {
      group = METSHeaderUtils.getComponentForField(sipType, fieldShortId, savedHeader);
      if (group instanceof StatusGroup) {
        sectionStatus.addGroup(group);
      } else if (group instanceof AltRecordGroup) {
        sectionAltRecord.addGroup(group);
      } else if (group instanceof AgentGroup) {
        sectionAgent.addGroup(group);
      }
      fieldGroups.add(group);
    }

    if (group != null) {
      group.getStyleClass().add(Constants.CSS_METS_HEADER_GROUP_LAST);
    }

    scrollPane.setContent(inner);

    center.getChildren().add(scrollPane);
    setCenter(center);
  }

  private void createBottom() {
    HBox bottom = new HBox();
    bottom.setPadding(new Insets(0, 10, 10, 10));
    bottom.setAlignment(Pos.CENTER_LEFT);

    Button back = new Button(I18n.t(Constants.I18N_BACK));
    back.setOnAction(actionEvent -> {
      stage.setRoot(previousPanel);
      stage.sizeToScene();
    });

    start = new Button(I18n.t(Constants.I18N_START));
    start.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent actionEvent) {
        IPHeader header = new IPHeader();
        boolean valid = true;

        for (AbstractGroup fieldGroup : fieldGroups) {

          if (!fieldGroup.validate()) {
            valid = false;
          } else {
            if (fieldGroup instanceof StatusGroup) {
              ((StatusGroup) fieldGroup).addStatusToHeader(header);
            } else if (fieldGroup instanceof AltRecordGroup) {
              ((AltRecordGroup) fieldGroup).addAltRecordsToHeader(header);
            } else if (fieldGroup instanceof AgentGroup) {
              ((AgentGroup) fieldGroup).addAgentsToHeader(header);
            }
          }
        }

        if (valid) {
          ConfigurationManager.serialize(header, sipType.name() + Constants.RODAIN_SERIALIZE_FILE_METS_HEADER_SUFFIX);
          stage.startCreation(outputFolder, exportAll, exportItems, sipNameBuilder, createReport, header);
        }
      }
    });

    start.setDisable(false);

    bottom.getChildren().addAll(back, HorizontalSpace.create(), start);
    setBottom(bottom);
  }
}
