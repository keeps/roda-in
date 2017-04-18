package org.roda.rodain.ui.creation.METSHeaderComponents;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.roda.rodain.core.Constants;
import org.roda.rodain.ui.utils.FontAwesomeImageCreator;
import org.roda_project.commons_ip.model.IPHeader;

import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * An AbstractGroup groups together and manages similar AbstractItem objects
 * (eg: a group of AltRecordItem that share the same type, or a group of
 * AgentItem having the same role).
 *
 * @author Bruno Ferreira <bferreira@keep.pt>
 */
public abstract class AbstractGroup extends VBox {
  private final static String ICON_ADD = FontAwesomeImageCreator.PLUS;

  private HBox addMorePanel;
  private Hyperlink addMoreInfo;

  private VBox errorMessagesPanel;

  private String type;
  private String longId;
  private String shortId;
  private Constants.SipType sipType;

  private int minimumItems;
  private int maximumItems;

  // must be a set that preserves insertion order
  private Set<AbstractItem> items = new LinkedHashSet<>();

  IPHeader savedHeader;

  private AbstractGroup() {
  }

  public AbstractGroup(Constants.SipType sipType, String shortId, IPHeader savedHeader) {
    super(5);

    this.savedHeader = savedHeader;
    this.sipType = sipType;
    this.shortId = shortId;
    initBeforeGUI(savedHeader);

    // GUI
    styleAsNormalGroup();

    // create the error messages panel
    errorMessagesPanel = new VBox(5);
    this.getChildren().add(errorMessagesPanel);

    // create button to add more lines to the form
    addMorePanel = new HBox(5);
    addMorePanel.setAlignment(Pos.BASELINE_LEFT);
    addMorePanel.managedProperty().bind(addMorePanel.visibleProperty());
    addMorePanel.setVisible(false);

    addMoreInfo = new Hyperlink();
    addMoreInfo.getStyleClass().add(Constants.CSS_METS_HEADER_ITEM_ADD_MORE_LINK);
    addMoreInfo.setOnAction(event -> createAndAddRow());

    addMorePanel.getChildren().addAll(addMoreInfo);
    this.getChildren().add(addMorePanel);

    updateAddMore();

    if (getSavedItemIterator().hasNext()) {
      while (getSavedItemIterator().hasNext()) {
        createAndAddRow(true);
      }
    } else {
      for (int i = 0; i < getMinimum() && getMinimum() >= 0; i++) {
        createAndAddRow();
      }
    }
  }

  /*
   * Default behaviour, do not override
   * ____________________________________________________________________________________________________________________
   */
  public void styleAsNormalGroup() {
    this.getStyleClass().remove(Constants.CSS_METS_HEADER_GROUP_LAST);
    this.getStyleClass().add(Constants.CSS_METS_HEADER_GROUP);
  }

  public void styleAsLastGroup() {
    this.getStyleClass().remove(Constants.CSS_METS_HEADER_GROUP);
    this.getStyleClass().add(Constants.CSS_METS_HEADER_GROUP_LAST);
  }

  public void createAndAddRow() {
    createAndAddRow(false);
  }

  public void createAndAddRow(boolean usingSavedItems) {
    AbstractItem newItem = internalCreateRow(usingSavedItems);
    if (newItem != null) {
      items.add(newItem);

      // add this item before the "add more" button
      this.getChildren().add(this.getChildren().size() - 1, newItem);
      updateAddMore();
    }
  }

  public int totalItemsAmount() {
    return items.size();
  }

  public void removeItem(AbstractItem item) {
    METSHeaderUtils.tryToFocusNextTextField(this.getParent(), item.getRemoveButton());
    items.remove(item);
    this.getChildren().remove(item);
    updateAddMore();
  }

  public int getMinimum() {
    return this.minimumItems;
  }

  public int getMaximum() {
    return this.maximumItems;
  }

  protected Set<AbstractItem> getItems() {
    return items;
  }

  protected void updateAddMore() {
    boolean ableToRemoveItems = totalItemsAmount() > getMinimum();
    for (AbstractItem item : items) {
      item.setRemovable(ableToRemoveItems);
    }

    if (totalItemsAmount() == getMaximum() && getMaximum() == 1) {
      addMorePanel.setVisible(false);
    } else {
      addMorePanel.setVisible(true);
      if (totalItemsAmount() < getMaximum()) {
        addMorePanel.setDisable(false);
        addMoreInfo.setText(String.format("%s %s", StringUtils.capitalize(getTextFromI18N(Constants.I18N_ADD)),
          getTextFromI18N(getFieldParameterAsString(Constants.CONF_K_METS_HEADER_FIELD_TITLE, ""))));
      } else {
        addMorePanel.setDisable(true);
        addMoreInfo.setText("Maximum reached!");
      }
    }
  }

  public boolean validate() {
    boolean valid = true;

    errorMessagesPanel.getChildren().clear();

    for (AbstractItem item : items) {
      if (!item.isValid()) {
        valid = false;

        for (String errorMessage : item.getValidationFailures()) {
          Label errorMessageLabel = new Label(errorMessage);
          errorMessageLabel.getStyleClass().add(Constants.CSS_ERROR);
          errorMessagesPanel.getChildren().add(errorMessageLabel);
        }
      }
    }
    return valid;
  }

  protected String getFieldParameterAsString(String suffix, String defaultValue) {
    return METSHeaderUtils.getFieldParameterAsString(sipType, shortId, suffix, defaultValue);
  }

  protected String[] getFieldParameterAsStringArray(String suffix, String[] defaultValue) {
    return METSHeaderUtils.getFieldParameterAsStringArray(sipType, shortId, suffix, defaultValue);
  }

  protected Integer getFieldParameterAsInteger(String suffix, Integer defaultValue) {
    return METSHeaderUtils.getFieldParameterAsInteger(sipType, shortId, suffix, defaultValue);
  }

  protected Boolean getFieldParameterAsBoolean(String suffix, Boolean defaultValue) {
    return METSHeaderUtils.getFieldParameterAsBoolean(sipType, shortId, suffix, defaultValue);
  }

  protected String getTextFromI18N(String key) {
    return METSHeaderUtils.getTextFromI18N(key);
  }

  public IPHeader getSavedHeader() {
    return savedHeader;
  }

  /*
   * Methods to override
   * ____________________________________________________________________________________________________________________
   */
  public abstract String getHeaderText();

  protected abstract AbstractItem internalCreateRow(boolean usingSavedItems);

  /**
   * Override this method, calling super.initBeforeGUI() and then adding extra
   * config
   */
  protected void initBeforeGUI(IPHeader savedHeader) {
    // configs
    this.longId = Constants.CONF_K_METS_HEADER_FIELDS_PREFIX + sipType.name()
      + Constants.CONF_K_METS_HEADER_FIELD_SEPARATOR + shortId;

    this.type = METSHeaderUtils.getFieldParameterAsString(sipType, shortId, Constants.CONF_K_METS_HEADER_FIELD_TYPE,
      "");

    this.minimumItems = METSHeaderUtils.getFieldParameterAsInteger(sipType, shortId,
      Constants.CONF_K_METS_HEADER_FIELD_AMOUNT_MIN, 0);

    this.maximumItems = METSHeaderUtils.getFieldParameterAsInteger(sipType, shortId,
      Constants.CONF_K_METS_HEADER_FIELD_AMOUNT_MAX, Integer.MAX_VALUE);
  }

  protected abstract Iterator getSavedItemIterator();
}
