package org.roda.rodain.rules;

import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import org.roda.rodain.core.PathCollection;
import org.roda.rodain.rules.filters.ContentFilter;
import org.roda.rodain.rules.sip.*;
import org.roda.rodain.schema.ui.SipPreviewNode;
import org.roda.rodain.source.ui.items.SourceTreeDirectory;
import org.roda.rodain.source.ui.items.SourceTreeItem;
import org.roda.rodain.source.ui.items.SourceTreeItemState;
import org.roda.rodain.utils.FontAwesomeImageCreator;
import org.roda.rodain.utils.TreeVisitor;

import java.nio.file.Path;
import java.util.*;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 29-09-2015.
 */
public class Rule extends Observable implements Observer, Comparable {
  // this ruleCount is used to determine the ID of each rule
  private static int ruleCount = 0;

  private Set<SourceTreeItem> source;
  private TemplateType templateType;
  private Path metadataPath;
  private RuleTypes assocType;
  private MetadataTypes metaType;
  private Set<ContentFilter> filters;

  // map of SipPreview id -> SipPreview
  private Map<String, SipPreview> sips;
  // map of SipPreview id -> SipPreviewNode
  private HashMap<String, SipPreviewNode> sipNodes = new HashMap<>();
  private Image icon;
  private int added = 0;
  private int level;
  private Integer id;

  /**
   * @param source       The set of items to be transformed into SIPs
   * @param assocType    The association type of the rule.
   * @param level        The maximum level of the directory. Used in the SIP_PER_FOLDER
   *                     type only.
   * @param metadataPath The path to the metadata file(s)
   * @param template     The type of the chosen template
   * @param metaType     The type of metadata to be applied to the SIPs.
   */
  public Rule(Set<SourceTreeItem> source, RuleTypes assocType, int level, Path metadataPath, TemplateType template,
              MetadataTypes metaType) {
    ruleCount++;
    this.source = source;
    this.assocType = assocType;
    this.level = level;
    this.templateType = template;
    this.metadataPath = metadataPath;
    this.metaType = metaType;
    filters = new HashSet<>();
    id = ruleCount;

    createIcon();
    createFilters();
  }

  private void createIcon() {
    ResourceBundle hierarchyConfig = ResourceBundle.getBundle("properties/roda-description-levels-hierarchy");
    String category = hierarchyConfig.getString("category.item");
    String unicode = hierarchyConfig.getString("icon." + category);

    icon = FontAwesomeImageCreator.generate(unicode);
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
   * SIP_PER_FOLDER, the created TreeVisitor is a SipPerFolderVisitor, that
   * receives the maximum level as a parameter.
   * </p>
   * <p/>
   * <p>
   * The method also adds the rule as an observer of the TreeVisitor, to be
   * notified of any changes.
   * </p>
   *
   * @return The TreeVisitor created using the options defined in the rule.
   * @see TreeVisitor
   * @see SipPerFileVisitor
   * @see SipPerFolderVisitor
   * @see SipPerSelection
   * @see SipSingle
   */
  public TreeVisitor apply() {
    added = 0;
    sips = new HashMap<>();
    sipNodes = new HashMap<>();

    TreeVisitor visitor;
    switch (assocType) {
      case SIP_PER_FOLDER:
        SipPerFolderVisitor visitorFolder = new SipPerFolderVisitor(id.toString(), level, filters, metaType,
            metadataPath, templateType);
        visitorFolder.addObserver(this);
        visitor = visitorFolder;
        break;
      case SIP_PER_SELECTION:
        // create a set with the paths of the selected items
        Set<String> selection = new HashSet<>();
        for (SourceTreeItem sti : source) {
          selection.add(sti.getPath());
        }
        SipPerSelection visitorSelection = new SipPerSelection(id.toString(), selection, filters, metaType,
            metadataPath, templateType);
        visitorSelection.addObserver(this);
        visitor = visitorSelection;
        break;
      case SIP_PER_FILE:
        SipPerFileVisitor visitorFile = new SipPerFileVisitor(id.toString(), filters, metaType, metadataPath,
            templateType);
        visitorFile.addObserver(this);
        visitor = visitorFile;
        break;
      default:
      case SINGLE_SIP:
        SipSingle visitorSingle = new SipSingle(id.toString(), filters, metaType, metadataPath, templateType);
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
      sips = visit.getSips();
      while (visit.hasNext() && added < 100) {
        added++;
        SipPreview sipPreview = visit.getNext();
        SipPreviewNode sipNode = new SipPreviewNode(sipPreview, icon);
        sipPreview.addObserver(sipNode);
        sipPreview.addObserver(this);
        sipNodes.put(sipPreview.getId(), sipNode);
      }
      setChanged();
      notifyObservers();
    } else if (o instanceof SipPreview) {
      SipPreview sip = (SipPreview) o;
      if (sip.isRemoved()) {
        sipNodes.remove(sip.getId());
        sips.remove(sip.getId());
        for (TreeNode tn : sip.getFiles()) {
          PathCollection.addPaths(tn.getFullTreePaths(), SourceTreeItemState.NORMAL);
        }
        setChanged();
        notifyObservers("Removed SIP");
      }
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
        for (SipPreview sip : sips.values()) {
          sip.setRemoved();
        }

        int removedSips = 0;
        sipNodes.clear();
        for (SipPreview sip : sips.values()) {
          for (TreeNode tn : sip.getFiles()) {
            PathCollection.addPaths(tn.getFullTreePaths(), SourceTreeItemState.NORMAL);
          }
          removedSips++;
          setChanged();
          notifyObservers(removedSips);
        }
        sips.clear();
        return null;
      }
    };

    // After everything is loaded, we add all the items to the TreeView at once.
    task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
      @Override
      public void handle(WorkerStateEvent workerStateEvent) {
        setChanged();
        notifyObservers("Removed SIP");
      }
    });

    new Thread(task).start();
  }

  /**
   * Compares two rules, by their id.
   *
   * @param o The rule to be compared
   * @return the value 0 if this Rule's id is equal to the argument Rule's id; a
   * value less than 0 if this Rule's id is numerically less than the
   * argument Rule's id; and a value greater than 0 if this Rule's id is
   * numerically greater than the argument Rule's id (signed
   * comparison).
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
   * @param o The rule to be compared
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
