package org.roda.rodain.schema.ui;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import org.roda.rodain.rules.Rule;
import org.roda.rodain.rules.sip.SipPreview;
import org.roda.rodain.rules.ui.RuleModalController;
import org.roda.rodain.schema.DescriptionObject;
import org.roda.rodain.utils.FontAwesomeImageCreator;

import java.util.*;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 17-09-2015.
 */
public class SchemaNode extends TreeItem<String> implements Observer {
  private DescriptionObject dob;
  private Map<String, Integer> rules;
  private Map<String, Rule> ruleObjects;
  private Map<String, Set<SipPreviewNode>> sips;
  private Map<String, Set<SchemaNode>> ruleNodes;
  private Image iconBlack, iconWhite;
  private boolean blackIconSelected = true;
  private boolean removed = false;

  private Set<SchemaNode> schemaNodes;

  /**
   * Creates a new SchemaNode
   *
   * @param dobject
   *          The DescriptionObject that defines the SchemaNode
   */
  public SchemaNode(DescriptionObject dobject) {
    super(dobject.getTitle());
    dob = dobject;
    rules = new HashMap<>();
    sips = new HashMap<>();
    ruleObjects = new HashMap<>();
    schemaNodes = new HashSet<>();
    ruleNodes = new HashMap<>();

    if (dob.getDescriptionlevel() != null)
      updateDescLevel(dob.getDescriptionlevel());
  }

  public SchemaNode(DescriptionObject dobject, Image iconBlack, Image iconWhite) {
    super(dobject.getTitle());
    dob = dobject;
    rules = new HashMap<>();
    sips = new HashMap<>();
    ruleObjects = new HashMap<>();
    schemaNodes = new HashSet<>();
    ruleNodes = new HashMap<>();

    this.iconBlack = iconBlack;
    this.iconWhite = iconWhite;
  }

  /**
   * Updates the node when a Rule has been modified.
   *
   * @param o
   *          The observable object
   * @param arg
   *          The arguments sent by the object
   */
  @Override
  public void update(final Observable o, Object arg) {
    if (o instanceof Rule && arg instanceof String) {
      final Rule rule = (Rule) o;
      final Integer idInt = rule.getId();
      final String id = idInt.toString();

      // set the title with the sip count
      int count = rule.getSipCount();
      rules.put(id, count);

      Platform.runLater(() -> {
        // replace the SIPs
        if (sips.get(id) != null) {
          getChildren().removeAll(sips.get(id));
        }
        if (ruleNodes.get(id) != null) {
          getChildren().removeAll(ruleNodes.get(id));
        }

        // we don't need to add the nodes and SIPs if the rule has been removed
        if (arg.equals("Removed rule")) {
          return;
        }
        Set<SipPreviewNode> nodes = new HashSet<>(rule.getSipNodes());
        Set<SchemaNode> schemas = new HashSet<>(rule.getSchemaNodes());

        // We need to set the parents of the top nodes here, except when this
        // node is the hidden root
        if (getParent() != null) { // not the root?
          for (SchemaNode sc : schemas) {
            if (sc.getDob().getParentId() == null) {
              sc.getDob().setParentId(dob.getId());
            }
          }
        }
        sips.put(id, nodes);
        ruleNodes.put(id, schemas);
        getChildren().addAll(nodes);
        getChildren().addAll(schemas);

        sortChildren();
      });
    }
  }

  /**
   * Adds a new Rule to the SchemaNode.
   *
   * @param r
   *          The Rule to be added
   */
  public void addRule(Rule r) {
    int count = r.getSipCount();
    Integer idInt = r.getId();
    String id = idInt.toString();
    // add the rule the maps
    rules.put(id, count);
    ruleObjects.put(id, r);
    setExpanded(true);
  }

  /**
   * Removes a rule from the SchemaNode.
   *
   * @param r
   */
  public void removeRule(Rule r) {
    Integer idInt = r.getId();
    String id = idInt.toString();
    if (sips.get(id) != null) {
      getChildren().removeAll(sips.get(id));
    }
    // remove the rules from the maps
    rules.remove(id);
    ruleObjects.remove(id);
    sips.remove(id);
    r.remove();
  }

  /**
   * Adds a new node to the children nodes list.
   *
   * @param node
   *          The node to be added to the list.
   */
  public void addChildrenNode(SchemaNode node) {
    schemaNodes.add(node);
  }

  public void addChild(String ruleID, TreeItem<String> item) {
    if (item instanceof SipPreviewNode) {
      Set<SipPreviewNode> set = sips.get(ruleID);
      if (set == null) {
        set = new HashSet<>();
      }
      set.add((SipPreviewNode) item);
      sips.put(ruleID, set);
    }
    if (item instanceof SchemaNode) {
      Set<SchemaNode> set = ruleNodes.get(ruleID);
      if (set == null) {
        set = new HashSet<>();
      }
      set.add((SchemaNode) item);
      ruleNodes.put(ruleID, set);
    }
  }

  public void removeChild(TreeItem<String> item) {
    getChildren().remove(item);
    for (Set<SipPreviewNode> set : sips.values()) {
      if (set.contains(item)) {
        set.remove(item);
        return;
      }
    }
    if (schemaNodes.contains(item)) {
      schemaNodes.remove(item);
      return;
    }
    for (Set<SchemaNode> set : ruleNodes.values()) {
      if (set.contains(item)) {
        set.remove(item);
        return;
      }
    }
  }

  public boolean isRemoved() {
    return removed;
  }

  /**
   * Sorts the children of the SchemaNode
   *
   * @see SchemaComparator
   */
  public void sortChildren() {
    ArrayList<TreeItem<String>> aux = new ArrayList<>(getChildren());
    Collections.sort(aux, new SchemaComparator());
    getChildren().setAll(aux);
  }

  public void updateDescLevel(String descLevel) {
    dob.setDescriptionlevel(descLevel);
    ResourceBundle hierarchyConfig = ResourceBundle.getBundle("properties/roda-description-levels-hierarchy");
    String category = hierarchyConfig.getString("category." + dob.getDescriptionlevel());
    String unicode = hierarchyConfig.getString("icon." + category);

    Platform.runLater(() -> {
      iconBlack = FontAwesomeImageCreator.generate(unicode);
      iconWhite = FontAwesomeImageCreator.generate(unicode, Color.WHITE);
      this.setGraphic(new ImageView(iconBlack));
    });
  }

  private Set<Rule> getAllRules() {
    Set<Rule> result = new HashSet<>(ruleObjects.values());
    for (SchemaNode sn : schemaNodes) {
      result.addAll(sn.getAllRules());
    }
    ruleNodes.forEach((s, schNodes) -> schNodes.forEach(schemaNode -> result.addAll(schemaNode.getAllRules())));
    return result;
  }

  public void remove() {
    Map<SipPreview, String> allSips = getSipPreviews();
    // first start the modal to give feedback to the user
    for (SipPreview sipPreview : allSips.keySet()) {
      RuleModalController.removeSipPreview(sipPreview);
    }

    // then we start the remove process
    Task<Void> sipRemoveTask = new Task<Void>() {
      @Override
      protected Void call() throws Exception {
        for (SipPreview sipPreview : allSips.keySet()) {
          sipPreview.removeSIP();
        }
        return null;
      }
    };
    new Thread(sipRemoveTask).start();

    Set<Rule> allRules = getAllRules();
    // first start the modal to give feedback to the user
    for (Rule r : allRules) {
      RuleModalController.removeRule(r);
    }

    // then we start the remove process
    for (Rule r : allRules) {
      removeRule(r);
    }
    sips.clear();
    schemaNodes.clear();
    ruleNodes.clear();
    ruleObjects.clear();
    removed = true;
  }

  /**
   * @return The icon of the SchemaNode
   */
  public Image getImage() {
    if (blackIconSelected) {
      return iconBlack;
    } else
      return iconWhite;
  }

  public Image getIconWhite() {
    return iconWhite;
  }

  public void setBlackIconSelected(boolean value) {
    blackIconSelected = value;
  }

  /**
   * @return The DescriptionObject that defines the SchemaNode
   */
  public DescriptionObject getDob() {
    return dob;
  }

  /**
   * @return The Set of rules associated to the SchemaNode, ordered by Rule id
   */
  public Set<Rule> getRules() {
    return new TreeSet<>(ruleObjects.values());
  }

  /**
   * @return A map of all the SIPs in the SchemaNode and in the SchemaNode's
   *         children
   */
  public Map<SipPreview, String> getSipPreviews() {
    Map<SipPreview, String> result = new HashMap<>();
    // this node's sips
    for (Rule r : ruleObjects.values())
      for (SipPreview sp : r.getSips())
        result.put(sp, dob.getId());

    sips.forEach((id, sipPreviewNodes) -> sipPreviewNodes
      .forEach(sipPreviewNode -> result.put(sipPreviewNode.getSip(), dob.getId())));

    ruleNodes.forEach((s, schNodes) -> schNodes.forEach(schemaNode -> result.putAll(schemaNode.getSipPreviews())));

    // children sips
    for (SchemaNode sn : schemaNodes)
      result.putAll(sn.getSipPreviews());
    return result;
  }
}
