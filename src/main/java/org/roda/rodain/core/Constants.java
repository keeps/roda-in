package org.roda.rodain.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.roda.rodain.core.creation.BagitSipCreator;
import org.roda.rodain.core.creation.EarkSip2Creator;
import org.roda.rodain.core.creation.EarkSipCreator;
import org.roda.rodain.core.creation.HungarianSipCreator;
import org.roda.rodain.core.schema.IPContentType;
import org.roda.rodain.core.schema.RepresentationContentType;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Class containing constants to be used application wide ("no string should be
 * left alone in the wild" (i.e. in another class besides this one))
 * 
 * @since 2017-03-07
 * 
 * @author Hélder Silva <hsilva@keep.pt>
 */
public final class Constants {

  // misc
  public static final String RODAIN_HOME_ENV_VARIABLE = "RODAIN_HOME";
  public static final String RODAIN_ENV_VARIABLE = "RODAIN_ENV";
  public static final String RODAIN_ENV_TESTING = "testing";
  public static final String RODAIN_CONFIG_FOLDER = "roda-in";
  public static final String RODAIN_SERIALIZE_FILE_PREFIX = "serial_";
  public static final String RODAIN_SERIALIZE_FILE_METS_HEADER_SUFFIX = "_metsheader.bin";
  public static final String RODAIN_GITHUB_LATEST_VERSION_LINK = "https://github.com/keeps/roda-in/releases";
  public static final String RODAIN_GITHUB_LATEST_VERSION_API_LINK = "https://api.github.com/repos/keeps/roda-in/releases/latest";
  public static final String RODAIN_GUI_TITLE = "RODA-In";
  public static final String RODAIN_DEFAULT_ENCODING = "UTF-8";
  public static final String RODAIN_DEFAULT_CONFIG_ENCODING = RODAIN_DEFAULT_ENCODING;
  public static final String ENCODING_BASE64 = "Base64";
  public static final String MISC_TRUE = "true";
  public static final String MISC_FALSE = "false";
  public static final String MISC_GLOB = "glob:";
  public static final String MISC_DEFAULT_ID_PREFIX = "uuid-";
  public static final String MISC_XML_EXTENSION = ".xml";
  public static final String MISC_DOT = ".";
  public static final String MISC_COLON = ":";
  public static final String MISC_COMMA = ",";
  public static final String MISC_DOUBLE_QUOTE = "\"";
  public static final String MISC_DOUBLE_QUOTE_W_SPACE = " \"";
  public static final String MISC_OR_OP = "||";
  public static final String MISC_AND_OP = "&&";
  public static final String MISC_FWD_SLASH = "/";
  public static final String MISC_METADATA_SEP = "!###!";
  public static final String MISC_DEFAULT_HUNGARIAN_SIP_SERIAL = "001";

  // langs
  public static final String LANG_PT_BR = "pt-br";
  public static final String LANG_PT_PT = "pt-pt";
  public static final String LANG_PT = "pt";
  public static final String LANG_ES_CL = "es-cl";
  public static final String LANG_ES = "es";
  public static final String LANG_EN = "en";
  public static final String LANG_HU = "hu";
  public static final String LANG_HR = "hr";
  public static final String LANG_DEFAULT = LANG_EN;

  // sip related
  public static final String SIP_DEFAULT_PACKAGE_TYPE = "UNKNOWN";
  public static final String SIP_DEFAULT_CONTENT_TYPE = "MIXED";
  public static final String SIP_CONTENT_TYPE_OTHER = "OTHER";
  public static final String SIP_DEFAULT_REPRESENTATION_CONTENT_TYPE = SIP_DEFAULT_CONTENT_TYPE;
  public static final String SIP_REP_FIRST = "rep1";
  public static final String SIP_REP_PREFIX = "rep";
  public static final String SIP_DEFAULT_AGENT_NAME = "RODA-in";
  public static final String SIP_AGENT_ROLE_OTHER = "OTHER";
  public static final String SIP_AGENT_TYPE_INDIVIDUAL = "INDIVIDUAL";
  public static final String SIP_AGENT_OTHERROLE_SUBMITTER = "SUBMITTER";
  public static final String SIP_AGENT_NOTETYPE_IDENTIFICATIONCODE = "IDENTIFICATIONCODE";
  public static final String SIP_AGENT_VERSION_UNKNOWN = "UNKNOWN";
  public static final String SIP_AGENT_NAME_FORMAT = "RODA-in %s";
  public static final String SIP_NAME_STRATEGY_SERIAL_FORMAT_NUMBER = "%03d";

  // folders
  public static final String FOLDER_SCHEMAS = "schemas";
  public static final String FOLDER_TEMPLATES = "templates";
  public static final String FOLDER_LOG = "log";
  public static final String FOLDER_METADATA = "metadata";
  public static final String FOLDER_HELP = "help";

  // configs keys prefixes & sufixes
  public static final String CONF_K_PREFIX_METADATA = "metadata.";
  public static final String CONF_K_SUFFIX_SCHEMA = ".schema";
  public static final String CONF_K_SUFFIX_TEMPLATE = ".template";
  public static final String CONF_K_SUFFIX_TITLE = ".title";
  public static final String CONF_K_SUFFIX_TYPE = ".type";
  public static final String CONF_K_SUFFIX_VERSION = ".version";
  public static final String CONF_K_SUFFIX_AGGREG_LEVEL = ".aggregationLevel";
  public static final String CONF_K_SUFFIX_TOP_LEVEL = ".topLevel";
  public static final String CONF_K_SUFFIX_ITEM_LEVEL = ".itemLevel";
  public static final String CONF_K_SUFFIX_FILE_LEVEL = ".fileLevel";
  public static final String CONF_K_SUFFIX_TAGS = ".tags";
  public static final String CONF_K_SUFFIX_SYNCHRONIZED = ".synchronized";
  public static final String CONF_K_SUFFIX_LEVELS_ICON = "levels.icon.";
  // configs keys
  public static final String CONF_K_ACTIVE_SIP_TYPES = "activeSipTypes";
  public static final String CONF_K_DEFAULT_LANGUAGE = "defaultLanguage";
  public static final String CONF_K_LAST_SIP_TYPE = "lastSipType";
  public static final String CONF_K_DEFAULT_SIP_TYPE = "creationModalPreparation.defaultSipType";
  public static final String CONF_K_IGNORED_FILES = "app.ignoredFiles";
  public static final String CONF_K_METADATA_TEMPLATES = "metadata.templates";
  public static final String CONF_K_METADATA_TYPES = "metadata.types";
  public static final String CONF_K_LEVELS_ICON_DEFAULT = "levels.icon.internal.default";
  public static final String CONF_K_LEVELS_ICON_ITEM = "levels.icon.internal.itemLevel";
  public static final String CONF_K_LEVELS_ICON_FILE = "levels.icon.internal.fileLevel";
  public static final String CONF_K_LEVELS_ICON_AGGREGATION = "levels.icon.internal.aggregationLevel";
  public static final String CONF_K_EXPORT_LAST_PREFIX = "export.lastPrefix";
  public static final String CONF_K_EXPORT_LAST_TRANSFERRING = "export.lastTransferring";
  public static final String CONF_K_EXPORT_LAST_SERIAL = "export.lastSerial";
  public static final String CONF_K_EXPORT_LAST_ITEM_EXPORT_SWITCH = "export.lastItemExportSwitch";
  public static final String CONF_K_EXPORT_LAST_REPORT_CREATION_SWITCH = "export.lastReportCreationSwitch";
  public static final String CONF_K_EXPORT_LAST_SIP_OUTPUT_FOLDER = "export.lastSipOutputFolder";
  public static final String CONF_K_ID_PREFIX = "idPrefix";
  public static final String CONF_K_SIP_CREATION_ALWAYS_JUMP_FOLDER = "sipPreviewCreator.createSip.alwaysJumpFolder";
  // METS Header fields
  public static final String CONF_K_METS_HEADER_FIELDS_PREFIX = "metsheader.";
  public static final String CONF_K_METS_HEADER_FIELDS_SUFFIX = ".fields";
  public static final String CONF_K_METS_HEADER_FIELD_SEPARATOR = ".field.";
  public static final String CONF_K_METS_HEADER_TYPES_SEPARATOR = ".type.";
  public static final String CONF_K_METS_HEADER_TYPE_RECORD_STATUS = "recordstatus";
  public static final String CONF_K_METS_HEADER_TYPE_ALTRECORD_ID = "altrecordid";
  public static final String CONF_K_METS_HEADER_TYPE_AGENT = "agent";
  public static final String CONF_K_METS_HEADER_FIELD_TITLE = ".title";
  public static final String CONF_K_METS_HEADER_FIELD_TYPE = ".type";
  public static final String CONF_K_METS_HEADER_FIELD_AMOUNT_MAX = ".amount.max";
  public static final String CONF_K_METS_HEADER_FIELD_AMOUNT_MIN = ".amount.min";
  public static final String CONF_K_METS_HEADER_FIELD_LABEL = ".label";
  public static final String CONF_K_METS_HEADER_FIELD_DESCRIPTION = ".description";
  public static final String CONF_K_METS_HEADER_FIELD_COMBO_VALUES = ".combo";
  public static final String CONF_K_METS_HEADER_FIELD_ATTRIBUTE_OTHERTYPE_VALUE = ".attr.othertype.value";
  public static final String CONF_K_METS_HEADER_FIELD_ATTRIBUTE_OTHERTYPE_LABEL = ".attr.othertype.label";
  public static final String CONF_K_METS_HEADER_FIELD_ATTRIBUTE_OTHERTYPE_DESCRIPTION = ".attr.othertype.description";
  public static final String CONF_K_METS_HEADER_FIELD_ATTRIBUTE_ROLE_VALUE = ".attr.role.value";
  public static final String CONF_K_METS_HEADER_FIELD_ATTRIBUTE_ROLE_LABEL = ".attr.role.label";
  public static final String CONF_K_METS_HEADER_FIELD_ATTRIBUTE_ROLE_DESCRIPTION = ".attr.role.description";
  public static final String CONF_K_METS_HEADER_FIELD_ATTRIBUTE_TYPE_VALUE = ".attr.type.value";
  public static final String CONF_K_METS_HEADER_FIELD_ATTRIBUTE_TYPE_LABEL = ".attr.type.label";
  public static final String CONF_K_METS_HEADER_FIELD_ATTRIBUTE_TYPE_DESCRIPTION = ".attr.type.description";
  public static final String CONF_K_METS_HEADER_FIELD_ATTRIBUTE_NAME_LABEL = ".attr.name.label";
  public static final String CONF_K_METS_HEADER_FIELD_ATTRIBUTE_NAME_DESCRIPTION = ".attr.name.description";
  public static final String CONF_K_METS_HEADER_FIELD_ATTRIBUTE_NOTE_LABEL = ".attr.note.label";
  public static final String CONF_K_METS_HEADER_FIELD_ATTRIBUTE_NOTE_DESCRIPTION = ".attr.note.description";
  public static final String CONF_K_METS_HEADER_FIELD_ATTRIBUTE_NOTE_MANDATORY = ".attr.note.mandatory";
  public static final String CONF_K_METS_HEADER_FIELD_MULTI_FIELD_COMBO_VALUES_NAME = ".multifieldcombo.attr.name";
  public static final String CONF_K_METS_HEADER_FIELD_MULTI_FIELD_COMBO_VALUES_NOTE = ".multifieldcombo.attr.note";
  public static final String CONF_V_METS_HEADER_FIELD_TYPE_AGENT = "agent";
  public static final String CONF_V_METS_HEADER_FIELD_TYPE_ALTRECORDID = "altrecordid";
  public static final String CONF_V_METS_HEADER_FIELD_TYPE_RECORDSTATUS = "recordstatus";
  public static final String CONF_V_METS_HEADER_FIELD_AMOUNT_MAX_INFINITE = "N";

  // app configs keys
  public static final String CONF_K_APP_LAST_CLASS_SCHEME = "lastClassificationScheme";
  public static final String CONF_K_APP_HELP_ENABLED = "app.helpEnabled";
  public static final String CONF_K_APP_LANGUAGE = "app.language";
  public static final String CONF_K_APP_MULTIPLE_EDIT_MAX = "app.multipleEdit.max";
  // configs files
  public static final String CONFIG_FILE = "config.properties";
  public static final String APP_CONFIG_FILE = ".app.properties";
  // configs values
  public static final String CONF_V_TRUE = MISC_TRUE;
  public static final String CONF_V_FALSE = MISC_FALSE;

  // events
  public static final String EVENT_REMOVED_RULE = "Removed rule";
  public static final String EVENT_REMOVE_FROM_RULE = "Remove from rule";
  public static final String EVENT_REMOVED_SIP = "Removed SIP";
  public static final String EVENT_FINISHED = "Finished";

  // date related formats
  public static final String DATE_FORMAT_1 = "yyyy.MM.dd HH.mm.ss.SSS";
  public static final String DATE_FORMAT_2 = "dd.MM.yyyy '@' HH:mm:ss z";
  public static final String DATE_FORMAT_3 = "yyyy-MM-dd'T'HH:mm:ss'Z'";
  public static final String DATE_FORMAT_4 = "yyyy-MM-dd";
  public static final String DATE_FORMAT_5 = "yyyyMMdd";

  // resources
  public static final String RSC_SPLASH_SCREEN_IMAGE = "roda-in-splash.png";
  public static final String RSC_RODA_LOGO = "RODA-in.png";
  public static final String RSC_LOADING_GIF = "loading.GIF";
  public static final String RSC_ICON_FOLDER = "icons/folder.png";
  public static final String RSC_ICON_FOLDER_OPEN = "icons/folder-open.png";
  public static final String RSC_ICON_FILE = "icons/file.png";
  public static final String RSC_ICON_FOLDER_COLAPSE = "icons/folder.png";
  public static final String RSC_ICON_FOLDER_EXPAND = "icons/folder-open.png";
  public static final String RSC_ICON_LIST_ADD = "icons/list-add.png";
  public static final String RSC_CSS_SHARED = "css/shared.css";
  public static final String RSC_CSS_MODAL = "css/modal.css";

  // CSS
  public static final String CSS_BOLDTEXT = "boldText";
  public static final String CSS_BORDER_PANE = "border-pane";
  public static final String CSS_CELL = "cell";
  public static final String CSS_CELLTEXT = "cellText";
  public static final String CSS_DARK_BUTTON = "dark-button";
  public static final String CSS_DESCRIPTION = "description";
  public static final String CSS_EDGE_TO_EDGE = "edge-to-edge";
  public static final String CSS_ERROR = "error";
  public static final String CSS_EXPORT_BUTTON = "export-button";
  public static final String CSS_FOOTER = "footer";
  public static final String CSS_FORMLABEL = "formLabel";
  public static final String CSS_FORMSEPARATOR = "formSeparator";
  public static final String CSS_FORM_TEXT_AREA = "form-text-area";
  public static final String CSS_HBOX = "hbox";
  public static final String CSS_HELPBUTTON = "helpButton";
  public static final String CSS_HELPTITLE = "helpTitle";
  public static final String CSS_INDEXED_CELL = "indexed-cell";
  public static final String CSS_INSPECTIONPART = "inspectionPart";
  public static final String CSS_MAIN_TREE = "main-tree";
  public static final String CSS_METS_HEADER_ITEM_ADD_MORE_LINK = "mets-header-item-add-more-link";
  public static final String CSS_METS_HEADER_GROUP_LAST = "mets-header-group-last";
  public static final String CSS_METS_HEADER_GROUP = "mets-header-group";
  public static final String CSS_METS_HEADER_ITEM_BUTTON_REMOVE = "mets-item-button-remove";
  public static final String CSS_METS_HEADER_ITEM_WITHOUT_SIBLINGS = "mets-item-without-siblings";
  public static final String CSS_METS_HEADER_ITEM_WITH_SIBLINGS = "mets-item-with-siblings";
  public static final String CSS_METS_HEADER_ITEM_WITH_MULTIPLE_FIELDS = "mets-item-multiple-fields";
  public static final String CSS_METS_HEADER_SECTION_TITLE = "mets-header-section-title";
  public static final String CSS_METS_HEADER_SECTION = "mets-header-section";
  public static final String CSS_MODAL = "modal";
  public static final String CSS_PREPARECREATIONSUBTITLE = "prepareCreationSubtitle";
  public static final String CSS_RULECELL = "ruleCell";
  public static final String CSS_SCHEMANODE = "schemaNode";
  public static final String CSS_SCHEMANODEEMPTY = "schemaNodeEmpty";
  public static final String CSS_SCHEMANODEHOVERED = "schemaNodeHovered";
  public static final String CSS_SIPCREATOR = "sipcreator";
  public static final String CSS_TITLE = "title";
  public static final String CSS_TITLE_BOX = "title-box";
  public static final String CSS_TOP = "top";
  public static final String CSS_TOP_SUBTITLE = "top-subtitle";
  public static final String CSS_TREE_CELL = "tree-cell";
  public static final String CSS_BACKGROUNDWHITE = "backgroundWhite";
  public static final String CSS_FX_BACKGROUND_COLOR_TRANSPARENT = "-fx-background-color: transparent";
  public static final String CSS_FX_FONT_SIZE_16PX = "-fx-font-size: 16px";
  public static final String CSS_FX_TEXT_FILL_BLACK = "-fx-text-fill: black";

  // I18n
  public static final String I18N_NEW_VERSION_CONTENT = "Main.newVersion.content";
  public static final String I18N_ASSOCIATION_SINGLE_SIP_DESCRIPTION = "association.singleSip.description";
  public static final String I18N_ASSOCIATION_SINGLE_SIP_TITLE = "association.singleSip.title";
  public static final String I18N_ASSOCIATION_SIP_PER_FILE_DESCRIPTION = "association.sipPerFile.description";
  public static final String I18N_ASSOCIATION_SIP_PER_FILE_TITLE = "association.sipPerFile.title";
  public static final String I18N_ASSOCIATION_SIP_SELECTION_DESCRIPTION = "association.sipSelection.description";
  public static final String I18N_ASSOCIATION_SIP_SELECTION_TITLE = "association.sipSelection.title";
  public static final String I18N_ASSOCIATION_SIP_WITH_STRUCTURE_DESCRIPTION = "association.sipWithStructure.description";
  public static final String I18N_ASSOCIATION_SIP_WITH_STRUCTURE_TITLE = "association.sipWithStructure.title";
  public static final String I18N_CREATIONMODALMETSHEADER_HEADER = "CreationModalMETSHeader.METSHeader";
  public static final String I18N_CREATIONMODALMETSHEADER_SECTION_STATUS = "CreationModalMETSHeader.section.status";
  public static final String I18N_CREATIONMODALMETSHEADER_SECTION_AGENTS = "CreationModalMETSHeader.section.agents";
  public static final String I18N_CREATIONMODALMETSHEADER_SECTION_ALTRECORDS = "CreationModalMETSHeader.section.altrecords";
  public static final String I18N_CREATIONMODALMETSHEADER_ERROR_MINIMUM = "CreationModalMETSHeader.error.minimum";
  public static final String I18N_CREATIONMODALMETSHEADER_ERROR_MAXIMUM = "CreationModalMETSHeader.error.maximum";
  public static final String I18N_CREATIONMODALMETSHEADER_ERROR_ONE_OF = "CreationModalMETSHeader.error.oneOf";
  public static final String I18N_CREATIONMODALMETSHEADER_ERROR_MANDATORY_CAN_NOT_BE_BLANK = "CreationModalMETSHeader.error.mandatoryCanNotBeBlank";
  public static final String I18N_CREATIONMODALMETSHEADER_ERROR_MANDATORY_WHEN = "CreationModalMETSHeader.error.mandatoryWhen";
  public static final String I18N_CREATIONMODALMETSHEADER_ERROR_MANDATORY = "CreationModalMETSHeader.error.mandatory";
  public static final String I18N_CREATIONMODALMETSHEADER_WARNING_MAXIMUM = "CreationModalMETSHeader.warning.maximum";
  public static final String I18N_CREATIONMODALMETSHEADER_HELPER_BLANK = "CreationModalMETSHeader.helper.blank";
  public static final String I18N_CREATIONMODALPREPARATION_CHOOSE = "CreationModalPreparation.choose";
  public static final String I18N_CREATIONMODALPREPARATION_CREATE_REPORT = "CreationModalPreparation.createReport";
  public static final String I18N_CREATIONMODALPREPARATION_CREATING_SIPS = "CreationModalPreparation.creatingSips";
  public static final String I18N_CREATIONMODALPREPARATION_EXPORT_ALL = "CreationModalPreparation.exportAll";
  public static final String I18N_CREATIONMODALPREPARATION_INCLUDE_HIERARCHY = "CreationModalPreparation.includeHierarchy";
  public static final String I18N_CREATIONMODALPREPARATION_OUTPUT_DIRECTORY = "CreationModalPreparation.outputDirectory";
  public static final String I18N_CREATIONMODALPREPARATION_PREFIX = "CreationModalPreparation.prefix";
  public static final String I18N_CREATIONMODALPREPARATION_AGENT_NAME = "CreationModalPreparation.submitterAgentName";
  public static final String I18N_CREATIONMODALPREPARATION_AGENT_ID = "CreationModalPreparation.submitterAgentID";
  public static final String I18N_CREATIONMODALPREPARATION_TRANSFERRING = "CreationModalPreparation.transferring";
  public static final String I18N_CREATIONMODALPREPARATION_SERIAL = "CreationModalPreparation.serial";
  public static final String I18N_CREATIONMODALPREPARATION_SIP_FORMAT = "CreationModalPreparation.sipFormat";
  public static final String I18N_CREATIONMODALPROCESSING_ACTION = "CreationModalProcessing.action";
  public static final String I18N_CREATIONMODALPROCESSING_ALERT_HEADER = "CreationModalProcessing.alert.header";
  public static final String I18N_CREATIONMODALPROCESSING_ALERT_STACK_TRACE = "CreationModalProcessing.alert.stacktrace";
  public static final String I18N_CREATIONMODALPROCESSING_ALERT_TITLE = "CreationModalProcessing.alert.title";
  public static final String I18N_CREATIONMODALPROCESSING_CAUSE = "CreationModalProcessing.cause";
  public static final String I18N_CREATIONMODALPROCESSING_CURRENT_SIP = "CreationModalProcessing.currentSip";
  public static final String I18N_CREATIONMODALPROCESSING_EARK_PROGRESS = "CreationModalProcessing.eark.progress";
  public static final String I18N_CREATIONMODALPROCESSING_ELAPSED = "CreationModalProcessing.elapsed";
  public static final String I18N_CREATIONMODALPROCESSING_ERROR_MESSAGES_STOPPED_CONTENT = "CreationModalProcessing.errorMessagesStopped.content";
  public static final String I18N_CREATIONMODALPROCESSING_ERROR_MESSAGES_STOPPED_HEADER = "CreationModalProcessing.errorMessagesStopped.header";
  public static final String I18N_CREATIONMODALPROCESSING_ERRORS = "CreationModalProcessing.errors";
  public static final String I18N_CREATIONMODALPROCESSING_FINISHED = "CreationModalProcessing.finished";
  public static final String I18N_CREATIONMODALPROCESSING_HOUR = "CreationModalProcessing.hour";
  public static final String I18N_CREATIONMODALPROCESSING_HOURS = "CreationModalProcessing.hours";
  public static final String I18N_CREATIONMODALPROCESSING_IMPOSSIBLE_ESTIMATE = "CreationModalProcessing.impossibleEstimate";
  public static final String I18N_CREATIONMODALPROCESSING_LESS_MINUTE = "CreationModalProcessing.lessMinute";
  public static final String I18N_CREATIONMODALPROCESSING_LESS_SECONDS = "CreationModalProcessing.lessSeconds";
  public static final String I18N_CREATIONMODALPROCESSING_MINUTE = "CreationModalProcessing.minute";
  public static final String I18N_CREATIONMODALPROCESSING_MINUTES = "CreationModalProcessing.minutes";
  public static final String I18N_CREATIONMODALPROCESSING_OPEN_FOLDER = "CreationModalProcessing.openfolder";
  public static final String I18N_CREATIONMODALPROCESSING_REMAINING = "CreationModalProcessing.remaining";
  public static final String I18N_CREATIONMODALPROCESSING_SUBTITLE = "CreationModalProcessing.subtitle";
  public static final String I18N_DIRECTORY_CHOOSER_TITLE = "directorychooser.title";
  public static final String I18N_EXPORT_BOX_TITLE = "ExportBox.title";
  public static final String I18N_FILE_CHOOSER_TITLE = "filechooser.title";
  public static final String I18N_FILE_EXPLORER_PANE_ALERT_ADD_FOLDER_CONTENT = "FileExplorerPane.alertAddFolder.content";
  public static final String I18N_FILE_EXPLORER_PANE_ALERT_ADD_FOLDER_HEADER = "FileExplorerPane.alertAddFolder.header";
  public static final String I18N_FILE_EXPLORER_PANE_ALERT_ADD_FOLDER_TITLE = "FileExplorerPane.alertAddFolder.title";
  public static final String I18N_FILE_EXPLORER_PANE_CHOOSE_DIR = "FileExplorerPane.chooseDir";
  public static final String I18N_FILE_EXPLORER_PANE_HELP_TITLE = "FileExplorerPane.help.title";
  public static final String I18N_FILE_EXPLORER_PANE_REMOVE_FOLDER = "FileExplorerPane.removeFolder";
  public static final String I18N_FILE_EXPLORER_PANE_TITLE = "FileExplorerPane.title";
  public static final String I18N_FOOTER_MEMORY = "Footer.memory";
  public static final String I18N_GENERIC_ERROR_CONTENT = "genericError.content";
  public static final String I18N_GENERIC_ERROR_TITLE = "genericError.title";
  public static final String I18N_INSPECTIONPANE_ADDMETADATA = "InspectionPane.addMetadata";
  public static final String I18N_INSPECTIONPANE_ADD_METADATA_ERROR_CONTENT = "InspectionPane.addMetadataError.content";
  public static final String I18N_INSPECTIONPANE_ADD_METADATA_ERROR_HEADER = "InspectionPane.addMetadataError.header";
  public static final String I18N_INSPECTIONPANE_ADD_METADATA_ERROR_TITLE = "InspectionPane.addMetadataError.title";
  public static final String I18N_INSPECTIONPANE_ADD_REPRESENTATION = "InspectionPane.addRepresentation";
  public static final String I18N_INSPECTIONPANE_CHANGE_TEMPLATE_CONTENT = "InspectionPane.changeTemplate.content";
  public static final String I18N_INSPECTIONPANE_CHANGE_TEMPLATE_HEADER = "InspectionPane.changeTemplate.header";
  public static final String I18N_INSPECTIONPANE_CHANGE_TEMPLATE_TITLE = "InspectionPane.changeTemplate.title";
  public static final String I18N_INSPECTIONPANE_DOCS_HELP_TITLE = "InspectionPane.docsHelp.title";
  public static final String I18N_INSPECTIONPANE_FORM = "InspectionPane.form";
  public static final String I18N_INSPECTIONPANE_HELP_RULE_LIST = "InspectionPane.help.ruleList";
  public static final String I18N_INSPECTIONPANE_HELP_TITLE = "InspectionPane.help.title";
  public static final String I18N_INSPECTIONPANE_METADATA = "InspectionPane.metadata";
  public static final String I18N_INSPECTIONPANE_MULTIPLE_SELECTED_APPLIED_MESSAGE = "InspectionPane.multipleSelected.appliedMessage";
  public static final String I18N_INSPECTIONPANE_MULTIPLE_SELECTED_CONFIRM = "InspectionPane.multipleSelected.confirm";
  public static final String I18N_INSPECTIONPANE_MULTIPLE_SELECTED_HELP = "InspectionPane.multipleSelected.help";
  public static final String I18N_INSPECTIONPANE_MULTIPLE_SELECTED_HELP_TITLE = "InspectionPane.multipleSelected.helpTitle";
  public static final String I18N_INSPECTIONPANE_ON_DROP = "InspectionPane.onDrop";
  public static final String I18N_INSPECTIONPANE_ON_DROP_DOCS = "InspectionPane.onDropDocs";
  public static final String I18N_INSPECTIONPANE_REMOVE_METADATA = "InspectionPane.removeMetadata";
  public static final String I18N_INSPECTIONPANE_REMOVE_METADATA_CONTENT = "InspectionPane.removeMetadata.content";
  public static final String I18N_INSPECTIONPANE_REMOVE_METADATA_HEADER = "InspectionPane.removeMetadata.header";
  public static final String I18N_INSPECTIONPANE_REMOVE_METADATA_TITLE = "InspectionPane.removeMetadata.title";
  public static final String I18N_INSPECTIONPANE_REPRESENTATION_TYPE_TOOLTIP = "InspectionPane.representationTypeTooltip";
  public static final String I18N_INSPECTIONPANE_RULES = "InspectionPane.rules";
  public static final String I18N_INSPECTIONPANE_SIP_TYPE_TOOLTIP = "InspectionPane.sipTypeTooltip";
  public static final String I18N_INSPECTIONPANE_TEXTCONTENT = "InspectionPane.textContent";
  public static final String I18N_INSPECTIONPANE_TITLE = "InspectionPane.title";
  public static final String I18N_INSPECTIONPANE_VALIDATE = "InspectionPane.validate";
  public static final String I18N_INSPECTIONPANE_IP_CONTENT_TYPE_PREFIX = "IPContentType.";
  public static final String I18N_INSPECTIONPANE_REPRESENTATION_CONTENT_TYPE_PREFIX = "RepresentationContentType.";
  public static final String I18N_MAIN_ADD_FOLDER = "Main.addFolder";
  public static final String I18N_MAIN_CHECK_VERSION = "Main.checkVersion";
  public static final String I18N_MAIN_CLASS_SCHEME = "Main.classScheme";
  public static final String I18N_MAIN_CONFIRM_RESET_CONTENT = "Main.confirmReset.content";
  public static final String I18N_MAIN_CONFIRM_RESET_HEADER = "Main.confirmReset.header";
  public static final String I18N_MAIN_CREATE_CS = "Main.createCS";
  public static final String I18N_MAIN_EDIT = "Main.edit";
  public static final String I18N_MAIN_EXPORT_CS = "Main.exportCS";
  public static final String I18N_MAIN_EXPORT_SIPS = "Main.exportSips";
  public static final String I18N_MAIN_FILE = "Main.file";
  public static final String I18N_MAIN_HELP = "Main.help";
  public static final String I18N_MAIN_HELP_PAGE = "Main.helpPage";
  public static final String I18N_MAIN_HIDE_FILES = "Main.hideFiles";
  public static final String I18N_MAIN_HIDE_HELP = "Main.hideHelp";
  public static final String I18N_MAIN_HIDE_IGNORED = "Main.hideIgnored";
  public static final String I18N_MAIN_HIDE_MAPPED = "Main.hideMapped";
  public static final String I18N_MAIN_IGNORE_ITEMS = "Main.ignoreItems";
  public static final String I18N_MAIN_LANGUAGE = "Main.language";
  public static final String I18N_MAIN_LOADCS = "Main.loadCS";
  public static final String I18N_MAIN_NEW_VERSION_HEADER = "Main.newVersion.header";
  public static final String I18N_MAIN_NO_UPDATES_CONTENT = "Main.noUpdates.content";
  public static final String I18N_MAIN_NO_UPDATES_HEADER = "Main.noUpdates.header";
  public static final String I18N_MAIN_OPEN_CONFIGURATION_FOLDER = "Main.openConfigurationFolder";
  public static final String I18N_MAIN_QUIT = "Main.quit";
  public static final String I18N_MAIN_RESET = "Main.reset";
  public static final String I18N_MAIN_SHOW_FILES = "Main.showFiles";
  public static final String I18N_MAIN_SHOW_HELP = "Main.showHelp";
  public static final String I18N_MAIN_SHOW_IGNORED = "Main.showIgnored";
  public static final String I18N_MAIN_SHOW_MAPPED = "Main.showMapped";
  public static final String I18N_MAIN_UPDATE_LANG_CONTENT = "Main.updateLang.content";
  public static final String I18N_MAIN_UPDATE_LANG_HEADER = "Main.updateLang.header";
  public static final String I18N_MAIN_UPDATE_LANG_TITLE = "Main.updateLang.title";
  public static final String I18N_MAIN_USE_JAVA8 = "Main.useJava8";
  public static final String I18N_MAIN_VIEW = "Main.view";
  public static final String I18N_METADATA_DIFF_FOLDER_DESCRIPTION = "metadata.diffFolder.description";
  public static final String I18N_METADATA_DIFF_FOLDER_TITLE = "metadata.diffFolder.title";
  public static final String I18N_METADATA_EMPTY_FILE_DESCRIPTION = "metadata.emptyFile.description";
  public static final String I18N_METADATA_EMPTY_FILE_TITLE = "metadata.emptyFile.title";
  public static final String I18N_METADATA_SAME_FOLDER_DESCRIPTION = "metadata.sameFolder.description";
  public static final String I18N_METADATA_SAME_FOLDER_TITLE = "metadata.sameFolder.title";
  public static final String I18N_METADATA_SINGLE_FILE_DESCRIPTION = "metadata.singleFile.description";
  public static final String I18N_METADATA_SINGLE_FILE_TITLE = "metadata.singleFile.title";
  public static final String I18N_METADATA_TEMPLATE_DESCRIPTION = "metadata.template.description";
  public static final String I18N_METADATA_TEMPLATE_TITLE = "metadata.template.title";
  public static final String I18N_RULECELL_CREATED_ITEM = "RuleCell.createdItem";
  public static final String I18N_RULEMODALPANE_ASSOCIATION_METHOD = "RuleModalPane.associationMethod";
  public static final String I18N_RULEMODALPANE_CHOOSE_DIRECTORY = "RuleModalPane.chooseDirectory";
  public static final String I18N_RULEMODALPANE_CHOOSE_FILE = "RuleModalPane.chooseFile";
  public static final String I18N_RULEMODALPANE_METADATA_METHOD = "RuleModalPane.metadataMethod";
  public static final String I18N_RULEMODALPANE_METADATA_PATTERN = "RuleModalPane.metadataPattern";
  public static final String I18N_RULEMODALPROCESSING_CREATED_PREVIEWS = "RuleModalProcessing.createdPreviews";
  public static final String I18N_RULEMODALPROCESSING_CREATING_PREVIEW = "RuleModalProcessing.creatingPreview";
  public static final String I18N_RULEMODALPROCESSING_PROCESSED_DIRS_FILES = "RuleModalProcessing.processedDirsFiles";
  public static final String I18N_RULEMODALREMOVING_REMOVED_FORMAT = "RuleModalRemoving.removedFormat";
  public static final String I18N_RULEMODALREMOVING_TITLE = "RuleModalRemoving.title";
  public static final String I18N_SCHEMAPANE_ADD = "SchemaPane.add";
  public static final String I18N_SCHEMAPANE_CONFIRM_NEW_SCHEME_CONTENT = "SchemaPane.confirmNewScheme.content";
  public static final String I18N_SCHEMAPANE_CONFIRM_NEW_SCHEME_HEADER = "SchemaPane.confirmNewScheme.header";
  public static final String I18N_SCHEMAPANE_CONFIRM_NEW_SCHEME_TITLE = "SchemaPane.confirmNewScheme.title";
  public static final String I18N_SCHEMAPANE_CONFIRM_REMOVE_CONTENT = "SchemaPane.confirmRemove.content";
  public static final String I18N_SCHEMAPANE_CONFIRM_REMOVE_HEADER = "SchemaPane.confirmRemove.header";
  public static final String I18N_SCHEMAPANE_CONFIRM_REMOVE_TITLE = "SchemaPane.confirmRemove.title";
  public static final String I18N_SCHEMAPANE_CREATE = "SchemaPane.create";
  public static final String I18N_SCHEMAPANE_DRAG_HELP = "SchemaPane.dragHelp";
  public static final String I18N_SCHEMAPANE_HELP_TITLE = "SchemaPane.help.title";
  public static final String I18N_SCHEMAPANE_NEW_NODE = "SchemaPane.newNode";
  public static final String I18N_SCHEMAPANE_OR = "SchemaPane.or";
  public static final String I18N_SCHEMAPANE_REMOVE = "SchemaPane.remove";
  public static final String I18N_SCHEMAPANE_TITLE = "SchemaPane.title";
  public static final String I18N_SCHEMAPANE_TOO_MANY_SELECTED_CONTENT = "SchemaPane.tooManySelected.content";
  public static final String I18N_SCHEMAPANE_TOO_MANY_SELECTED_HEADER = "SchemaPane.tooManySelected.header";
  public static final String I18N_SCHEMAPANE_TOO_MANY_SELECTED_TITLE = "SchemaPane.tooManySelected.title";
  public static final String I18N_SIMPLE_SIP_CREATOR_COPYING_DATA = "SimpleSipCreator.copyingData";
  public static final String I18N_SIMPLE_SIP_CREATOR_DOCUMENTATION = "SimpleSipCreator.documentation";
  public static final String I18N_SIMPLE_SIP_CREATOR_INIT_ZIP = "SimpleSipCreator.initZIP";
  public static final String I18N_SIMPLE_SIP_CREATOR_CREATING_STRUCTURE = "SimpleSipCreator.creatingStructure";
  public static final String I18N_SIMPLE_SIP_CREATOR_COPYING_METADATA = "SimpleSipCreator.copyingMetadata";
  public static final String I18N_SIMPLE_SIP_CREATOR_FINALIZING_SIP = "SimpleSipCreator.finalizingSip";
  public static final String I18N_SOURCE_TREE_CELL_REMOVE = "SourceTreeCell.remove";
  public static final String I18N_SOURCE_TREE_LOADING_TITLE = "SourceTreeLoading.title";
  public static final String I18N_SOURCE_TREE_LOAD_MORE_TITLE = "SourceTreeLoadMore.title";
  public static final String I18N_ADD = "add";
  public static final String I18N_AND = "and";
  public static final String I18N_APPLY = "apply";
  public static final String I18N_ASSOCIATE = "associate";
  public static final String I18N_BACK = "back";
  public static final String I18N_CANCEL = "cancel";
  public static final String I18N_CLEAR = "clear";
  public static final String I18N_CLOSE = "close";
  public static final String I18N_COLLAPSE = "collapse";
  public static final String I18N_CONFIRM = "confirm";
  public static final String I18N_CONTINUE = "continue";
  public static final String I18N_CREATIONMODALPROCESSING_REPRESENTATION = "CreationModalProcessing.representation";
  public static final String I18N_DATA = "data";
  public static final String I18N_DIRECTORIES = "directories";
  public static final String I18N_DIRECTORY = "directory";
  public static final String I18N_DOCUMENTATION = "documentation";
  public static final String I18N_DONE = "done";
  public static final String I18N_ERROR_VALIDATING_METADATA = "errorValidatingMetadata";
  public static final String I18N_EXPAND = "expand";
  public static final String I18N_EXPORT = "export";
  public static final String I18N_FILE = "file";
  public static final String I18N_FILES = "files";
  public static final String I18N_FOLDERS = "folders";
  public static final String I18N_HELP = "help";
  public static final String I18N_IGNORE = "ignore";
  public static final String I18N_INVALID_METADATA = "invalidMetadata";
  public static final String I18N_IP_CONTENT_TYPE = "IPContentType.";
  public static final String I18N_ITEMS = "items";
  public static final String I18N_LOAD = "load";
  public static final String I18N_LOADINGPANE_CREATE_ASSOCIATION = "LoadingPane.createAssociation";
  public static final String I18N_NAME = "name";
  public static final String I18N_REMOVE = "remove";
  public static final String I18N_REPRESENTATION_CONTENT_TYPE = "RepresentationContentType.";
  public static final String I18N_RESTART = "restart";
  public static final String I18N_ROOT = "root";
  public static final String I18N_SELECTED = "selected";
  public static final String I18N_SIP_NAME_STRATEGY = "sipNameStrategy.";
  public static final String I18N_START = "start";
  public static final String I18N_TYPE = "type";
  public static final String I18N_VALID_METADATA = "validMetadata";
  public static final String I18N_VERSION = "version";
  public static final String I18N_RENAME_REPRESENTATION = "RenameModalProcessing.renameRepresentation";
  public static final String I18N_RENAME = "rename";
  public static final String I18N_RENAME_REPRESENTATION_ALREADY_EXISTS = "RenameModalProcessing.renameAlreadyExists";

  /*
   * ENUMs
   */
  // sip type
  public enum SipType {
    EARK(EarkSipCreator.getText(), EarkSipCreator.requiresMETSHeaderInfo(), EarkSipCreator.ipSpecificContentTypes(),
      EarkSipCreator.representationSpecificContentTypes(), SipNameStrategy.ID, SipNameStrategy.TITLE_ID,
      SipNameStrategy.TITLE_DATE),
    EARK2(EarkSip2Creator.getText(), EarkSip2Creator.requiresMETSHeaderInfo(), EarkSip2Creator.ipSpecificContentTypes(),
      EarkSip2Creator.representationSpecificContentTypes(), SipNameStrategy.ID, SipNameStrategy.TITLE_ID,
      SipNameStrategy.TITLE_DATE),
    BAGIT(BagitSipCreator.getText(), BagitSipCreator.requiresMETSHeaderInfo(), BagitSipCreator.ipSpecificContentTypes(),
      BagitSipCreator.representationSpecificContentTypes(), SipNameStrategy.ID, SipNameStrategy.TITLE_ID,
      SipNameStrategy.TITLE_DATE),
    HUNGARIAN(HungarianSipCreator.getText(), HungarianSipCreator.requiresMETSHeaderInfo(),
      HungarianSipCreator.ipSpecificContentTypes(), HungarianSipCreator.representationSpecificContentTypes(),
      SipNameStrategy.DATE_TRANSFERRING_SERIALNUMBER);

    private final String text;
    private Set<SipNameStrategy> sipNameStrategies;
    private boolean requiresMETSHeaderInfo;
    private List<IPContentType> ipContentTypes;
    private List<RepresentationContentType> representationContentTypes;

    private static SipType[] filteredValues = null;
    private static List<Pair<SipType, List<IPContentType>>> filteredIpContentTypes = null;
    private static List<Pair<SipType, List<RepresentationContentType>>> filteredRepresentationContentTypes = null;

    private SipType(final String text, boolean requiresMETSHeaderInfo, List<IPContentType> ipContentTypes,
      List<RepresentationContentType> representationContentTypes, SipNameStrategy... sipNameStrategies) {
      this.text = text;
      this.requiresMETSHeaderInfo = requiresMETSHeaderInfo;
      this.ipContentTypes = ipContentTypes;
      this.representationContentTypes = representationContentTypes;
      this.sipNameStrategies = new LinkedHashSet<>();
      for (SipNameStrategy sipNameStrategy : sipNameStrategies) {
        this.sipNameStrategies.add(sipNameStrategy);
      }
    }

    /**
     * Used instead of SipType#values to get the values of this enum that are
     * enabled (according to the configuration).
     *
     * @return a SipType list filtered according to configuration.
     */
    public static SipType[] getFilteredValues() {
      if (filteredValues == null) {
        List<SipType> list = Arrays.asList(SipType.values());
        List<String> allowedValues = Arrays
          .asList(ConfigurationManager.getConfigAsStringArray(CONF_K_ACTIVE_SIP_TYPES));
        filteredValues = list.stream().filter(m -> allowedValues.contains(m.name())).collect(Collectors.toList())
          .toArray(new SipType[] {});
      }
      return filteredValues;
    }

    public static List<Pair<SipType, List<IPContentType>>> getFilteredIpContentTypes() {
      if (filteredIpContentTypes == null) {
        filteredIpContentTypes = new ArrayList<Pair<SipType, List<IPContentType>>>();
        for (SipType sipType : getFilteredValues()) {
          filteredIpContentTypes.add(new Pair<>(sipType, sipType.getIpContentTypes()));
        }
      }
      return filteredIpContentTypes;
    }

    public static List<Pair<SipType, List<RepresentationContentType>>> getFilteredRepresentationContentTypes() {
      if (filteredRepresentationContentTypes == null) {
        filteredRepresentationContentTypes = new ArrayList<>();
        for (SipType sipType : getFilteredValues()) {
          filteredRepresentationContentTypes.add(new Pair<>(sipType, sipType.getRepresentationContentTypes()));
        }
      }
      return filteredRepresentationContentTypes;
    }

    public List<IPContentType> getIpContentTypes() {
      return ipContentTypes;
    }

    public List<RepresentationContentType> getRepresentationContentTypes() {
      return representationContentTypes;
    }

    public boolean requiresMETSHeaderInfo() {
      return this.requiresMETSHeaderInfo;
    }

    public Set<SipNameStrategy> getSipNameStrategies() {
      return this.sipNameStrategies;
    }

    @Override
    public String toString() {
      return text;
    }
  }

  // sip name strategy
  public enum SipNameStrategy {
    ID, TITLE_ID, TITLE_DATE, DATE_TRANSFERRING_SERIALNUMBER
  }

  // path state
  public enum PathState {
    NORMAL, IGNORED, MAPPED
  }

  public enum RuleType {
    SINGLE_SIP, SIP_PER_FILE, SIP_WITH_STRUCTURE, SIP_PER_SELECTION
  }

  public enum MetadataOption {
    SINGLE_FILE, SAME_DIRECTORY, DIFF_DIRECTORY, TEMPLATE, NEW_FILE;

    /**
     * Translates the value that was serialized to the Enum.
     * 
     * @param value
     *          The serialized value.
     * @return The translated value.
     */
    @JsonCreator
    public static MetadataOption getEnumFromValue(String value) {
      if ("".equals(value))
        return SINGLE_FILE;
      for (MetadataOption testEnum : values()) {
        if (testEnum.toString().equals(value)) {
          return testEnum;
        }
      }
      throw new IllegalArgumentException();
    }
  }

  public enum VisitorState {
    VISITOR_DONE, VISITOR_QUEUED, VISITOR_NOTSUBMITTED, VISITOR_RUNNING, VISITOR_CANCELLED
  }

  private Constants() {
    // do nothing
  }
}
