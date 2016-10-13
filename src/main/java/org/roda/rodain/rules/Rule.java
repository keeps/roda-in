package org.roda.rodain.rules;

import javafx.concurrent.Task;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import org.roda.rodain.core.AppProperties;
import org.roda.rodain.core.PathCollection;
import org.roda.rodain.rules.filters.ContentFilter;
import org.roda.rodain.sip.*;
import org.roda.rodain.schema.DescriptionObject;
import org.roda.rodain.schema.ui.SchemaNode;
import org.roda.rodain.schema.ui.SipPreviewNode;
import org.roda.rodain.sip.creators.*;
import org.roda.rodain.source.ui.items.SourceTreeDirectory;
import org.roda.rodain.source.ui.items.SourceTreeItem;
import org.roda.rodain.source.ui.items.SourceTreeItemState;
import org.roda.rodain.utils.FontAwesomeImageCreator;
import org.roda.rodain.utils.TreeVisitor;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 29-09-2015.
 */
public class Rule extends Observable implements Observer, Comparable {
  // this ruleCount is used to determine the ID of each rule
  private static int ruleCount = 0;

  private Set<SourceTreeItem> source;
  private String templateType, metadataVersion, parentID;
  private Path metadataPath;
  private RuleTypes assocType;
  private MetadataOptions metadataOption;
  private String metadataType;
  private Set<ContentFilter> filters;

  // map of SipPreview id -> SipPreview
  private Map<String, SipPreview> sips;
  // map of SipPreview id -> SipPreviewNode
  private Map<String, SipPreviewNode> sipNodes = new ConcurrentHashMap<>();
  private Set<SchemaNode> schemaNodes = Collections.synchronizedSet(new HashSet<>());
  private Image itemIconBlack, itemIconWhite, dObjIconBlack, dObjIconWhite, fileIconBlack, fileIconWhite;
  private Integer id;

  /**
   * @param source
   *          The set of items to be transformed into SIPs
   * @param assocType
   *          The association type of the rule.
   * @param metadataPath
   *          The path to the metadata file(s)
   * @param template
   *          The type of the chosen template
   * @param metadataOption
   *          The type of metadata to be applied to the SIPs.
   */
  public Rule(Set<SourceTreeItem> source, RuleTypes assocType, Path metadataPath, String template,
    MetadataOptions metadataOption, String metadataType, String metadataVersion, String parentID) {
    ruleCount++;
    this.source = source;
    this.assocType = assocType;
    this.templateType = template;
    this.metadataVersion = metadataVersion;
    this.metadataPath = metadataPath;
    this.metadataOption = metadataOption;
    this.metadataType = metadataType;
    this.parentID = parentID;
    filters = new HashSet<>();
    id = ruleCount;

    createIcon();
    createFilters();
  }

  private void createIcon() {
    itemIconBlack = FontAwesomeImageCreator.generate(AppProperties.getConfig("levels.icon.internal.itemLevel"));
    itemIconWhite = FontAwesomeImageCreator.generate(AppProperties.getConfig("levels.icon.internal.itemLevel"), Color.WHITE);

    fileIconBlack = FontAwesomeImageCreator.generate(AppProperties.getConfig("levels.icon.internal.fileLevel"));
    fileIconWhite = FontAwesomeImageCreator.generate(AppProperties.getConfig("levels.icon.internal.fileLevel"), Color.WHITE);

    dObjIconBlack = FontAwesomeImageCreator.generate(AppProperties.getConfig("levels.icon.internal.otherLevel"));
    dObjIconWhite = FontAwesomeImageCreator.generate(AppProperties.getConfig("levels.icon.internal.otherLevel"), Color.WHITE);
  }

  private void createFilters() {
    ContentFilter filter = new ContentFilter();
    for (SourceTreeItem sti : source) {
      // add this item to the filter if it's ignored or mapped
      if (sti.getState() == SourceTreeItemState.IGNORED)
        filter.addIgnored(sti.getPath());
      else if (sti.getState() == SourceTreeItemState.MAPPED)
        filter.addMapped(sti.getPath());
      // if it's a directory, get all its mapped and ignored children and add to
      // the filters
      if (sti instanceof SourceTreeDirectory) {
        Set<String> filterIgnored = ((SourceTreeDirectory) sti).getIgnored();
        filter.addAllIgnored(filterIgnored);
        Set<String> filterMapped = ((SourceTreeDirectory) sti).getMapped();
        filter.addAllMapped(filterMapped);
      }
    }
    filters.add(filter);
  }

  /**
   * @return The set of items used as source.
   */
  public Set<SourceTreeItem> getSource() {
    return source;
  }

  /**
   * @return The id of the rule.
   */
  public int getId() {
    return id;
  }

  /**
   * @return The SIP's created by the rule.
   */
  public Collection<SipPreview> getSips() {
    return sips.values();
  }

  /**
   * @return The count of the created SIPs
   */
  public int getSipCount() {
    return sips.size();
  }

  /**
   * @return The set of SipPreviewNodes
   */
  public Collection<SipPreviewNode> getSipNodes() {
    return sipNodes.values();
  }

  public Set<SchemaNode> getSchemaNodes() {
    return schemaNodes;
  }

  /**
   * @return The association type.
   */
  public RuleTypes getAssocType() {
    return assocType;
  }

  /**
   * Creates a TreeVisitor with the options defined in the rule.
   * <p/>
   * <p>
   * For each different type of association, creates a different TreeVisitor
   * with the specific options. For example, when the type of the association is
   * SIP_PER_SELECTION, the created TreeVisitor is a SipPerSelection, that
   * receives the selected paths as a parameter.
   * </p>
   * <p/>
   * <p>
   * The method also adds the rule as an observer of the TreeVisitor, to be
   * notified of any changes.
   * </p>
   *
   * @return The TreeVisitor created using the options defined in the rule.
   * @see TreeVisitor
   * @see SipPerFile
   * @see SipPerSelection
   * @see SipSingle
   */
  public TreeVisitor apply() {
    sips = new ConcurrentHashMap<>();
    sipNodes = new ConcurrentHashMap<>();
    schemaNodes = Collections.synchronizedSet(new HashSet<>());

    TreeVisitor visitor;
    switch (assocType) {
      case SIP_PER_SELECTION:
        // create a set with the paths of the selected items
        Set<String> selection = new HashSet<>();
        for (SourceTreeItem sti : source) {
          selection.add(sti.getPath());
        }
        SipPerSelection visitorSelection = new SipPerSelection(id.toString(), selection, filters, metadataOption,
          metadataType, metadataPath, templateType, metadataVersion);
        visitorSelection.addObserver(this);
        visitor = visitorSelection;
        break;
      case SIP_PER_FILE:
        SipPerFile visitorFile = new SipPerFile(id.toString(), filters, metadataOption, metadataType, metadataPath,
          templateType, metadataVersion);
        visitorFile.addObserver(this);
        visitor = visitorFile;
        break;
      case SIP_WITH_STRUCTURE:
        SipsWithStructure visitorStructure = new SipsWithStructure(id.toString(), filters, metadataOption, metadataType,
          metadataPath, templateType, metadataVersion);
        visitorStructure.addObserver(this);
        visitor = visitorStructure;
        break;
      default:
      case SINGLE_SIP:
        SipSingle visitorSingle = new SipSingle(id.toString(), filters, metadataOption, metadataType, metadataPath,
          templateType, metadataVersion);
        visitorSingle.addObserver(this);
        visitor = visitorSingle;
        break;
    }
    return visitor;
  }

  /**
   * Updates the rule when notified by a SipPreviewCreator or a SipPreview.
   * <p>
   * When the notification is from a SipPreviewCreator, the method creates new
   * SipPreviewNodes until a maximum of 100 items. Finally, notifies the rule's
   * observers of changes.
   * </p>
   * <p/>
   * <p>
   * When the notification is from a SipPreview, the method only does work if
   * the item has been removed, in which case, it sets all the paths of the
   * SIP's content with the NORMAL state and removes the SipPreview from the
   * list of SIPs.
   * </p>
   *
   * @param o
   * @param arg
   */
  @Override
  public void update(Observable o, Object arg) {
    if (o instanceof SipPreviewCreator) {
      SipPreviewCreator visit = (SipPreviewCreator) o;
      if (visit instanceof SipsWithStructure) {
        updateSipsWithStructure((SipsWithStructure) visit);
      } else {
        sips = visit.getSips();
        while (visit.hasNext()) {
          SipPreview sipPreview = visit.getNext();
          sipPreview.setParentId(parentID);
          SipPreviewNode sipNode;
          if ("internal.itemLevel".equals(sipPreview.getDescriptionlevel()))
            sipNode = new SipPreviewNode(sipPreview, itemIconBlack, itemIconWhite);
          else
            sipNode = new SipPreviewNode(sipPreview, fileIconBlack, fileIconWhite);
          sipPreview.addObserver(sipNode);
          sipPreview.addObserver(this);
          sipNodes.put(sipPreview.getId(), sipNode);
        }
      }
      setChanged();
      notifyObservers(arg);
    } else if (o instanceof SipPreview) {
      SipPreview sip = (SipPreview) o;
      if (sip.isRemoved()) {
        sipNodes.remove(sip.getId());
        sips.remove(sip.getId());
        setChanged();

        schemaNodes.forEach(schemaNode -> {
          if (schemaNode.isRemoved())
            schemaNodes.remove(schemaNode);
        });
        if (sips.isEmpty() && schemaNodes.isEmpty()) {
          notifyObservers("Removed rule");
        } else
          notifyObservers("Removed SIP");
      } else {
        // Remove the SIP from this rule
        if (arg instanceof String && "Remove from rule".equals(arg)) {
          String sipId = sip.getId();
          sips.remove(sipId);
          sipNodes.remove(sipId);
        }
      }
    }
  }

  private void updateSipsWithStructure(SipsWithStructure visitor) {
    Set<PseudoItem> tree = visitor.getTree();
    Map<Path, DescriptionObject> descriptionObjectMap = visitor.getDescriptionObjects();
    Map<Path, SipPreview> sipPreviewMap = visitor.getSipPreviewMap();

    for (PseudoItem item : tree) {
      TreeItem<String> treeItem = rec_createNode(item, sipPreviewMap, descriptionObjectMap);
      if (treeItem instanceof SipPreviewNode) {
        SipPreviewNode sipPreviewNode = (SipPreviewNode) treeItem;
        sipNodes.put(sipPreviewNode.getSip().getId(), sipPreviewNode);
      }
      if (treeItem instanceof SchemaNode) {
        SchemaNode schemaNode = (SchemaNode) treeItem;
        schemaNodes.add(schemaNode);
      }
    }
  }

  private TreeItem<String> rec_createNode(PseudoItem pseudoItem, Map<Path, SipPreview> sipPreviewMap,
    Map<Path, DescriptionObject> descriptionObjectMap) {
    if (pseudoItem instanceof PseudoSIP) {
      PseudoSIP pseudoSIP = (PseudoSIP) pseudoItem;
      SipPreview sipPreview = sipPreviewMap.get(pseudoSIP.getNode().getPath());
      sipPreview.setParentId(parentID);
      sips.put(sipPreview.getId(), sipPreview);
      SipPreviewNode sipNode;
      if ("internal.itemLevel".equals(sipPreview.getDescriptionlevel()))
        sipNode = new SipPreviewNode(sipPreview, itemIconBlack, itemIconWhite);
      else
        sipNode = new SipPreviewNode(sipPreview, fileIconBlack, fileIconWhite);
      sipPreview.addObserver(sipNode);
      sipPreview.addObserver(this);
      return sipNode;
    } else {
      PseudoDescriptionObject pdo = (PseudoDescriptionObject) pseudoItem;
      DescriptionObject dobj = descriptionObjectMap.get(pdo.getPath());
      SchemaNode schemaNode = new SchemaNode(dobj, dObjIconBlack, dObjIconWhite);
      for (PseudoItem pi : pdo.getChildren()) {
        TreeItem<String> child = rec_createNode(pi, sipPreviewMap, descriptionObjectMap);
        schemaNode.addChild(id.toString(), child);
        schemaNode.getChildren().add(child);
      }
      schemaNode.sortChildren();
      return schemaNode;
    }
  }

  /**
   * Sets all the SIPs from the rule as removed and removes all the
   * SipPreviewNodes.
   */
  public void remove() {
    Task<Void> task = new Task<Void>() {
      @Override
      protected Void call() throws Exception {
        int paths = 0, removedPaths = 0;
        for (SipPreview sip : sips.values()) {
          sip.setRemoved();
          for (SipRepresentation sr : sip.getRepresentations()) {
            for (TreeNode tn : sr.getFiles()) {
              paths += tn.getFullTreePaths().size();
            }
          }
        }

        sipNodes.clear();
        for (SipPreview sip : sips.values()) {
          for (SipRepresentation sr : sip.getRepresentations()) {
            for (TreeNode tn : sr.getFiles()) {
              for (String path : tn.getFullTreePaths()) {
                PathCollection.addPath(path, SourceTreeItemState.NORMAL);
                removedPaths++;
                float result = (float) removedPaths / paths;
                setChanged();
                notifyObservers(result);
              }
            }
          }
        }
        sips.clear();
        schemaNodes.clear();
        return null;
      }
    };

    // After everything is loaded, we add all the items to the TreeView at once.
    task.setOnSucceeded(event -> {
      setChanged();
      notifyObservers("Removed Rule");
    });

    new Thread(task).start();
  }

  /**
   * Compares two rules, by their id.
   *
   * @param o
   *          The rule to be compared
   * @return the value 0 if this Rule's id is equal to the argument Rule's id; a
   *         value less than 0 if this Rule's id is numerically less than the
   *         argument Rule's id; and a value greater than 0 if this Rule's id is
   *         numerically greater than the argument Rule's id (signed
   *         comparison).
   */
  @Override
  public int compareTo(Object o) {
    int result = 0;
    if (o instanceof Rule) {
      Rule rule = (Rule) o;
      Integer idInt = id;
      result = idInt.compareTo(rule.getId());
    }
    return result;
  }

  /**
   * @param o
   *          The rule to be compared
   * @return True if the ids of the rules match, false otherwise
   */
  @Override
  public boolean equals(Object o) {
    boolean result = false;
    if (o instanceof Rule) {
      Rule rule = (Rule) o;
      result = rule.getId() == id;
    }
    return result;
  }

  /**
   * @return The rule's id hashcode
   */
  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
