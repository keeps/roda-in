package org.roda.rodain.schema.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;

import org.roda.rodain.core.AppProperties;
import org.roda.rodain.rules.Rule;
import org.roda.rodain.rules.ui.RuleModalController;
import org.roda.rodain.schema.DescriptionObject;
import org.roda.rodain.sip.SipPreview;
import org.roda.rodain.utils.FontAwesomeImageCreator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

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

  private static final Logger LOGGER = LoggerFactory.getLogger(SchemaNode.class.getName());

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
      updateDescriptionLevel(dob.getDescriptionlevel());
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
        if ("Removed rule".equals(arg)) {
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

  public void updateDescriptionLevel(String descLevel) {
    dob.setDescriptionlevel(descLevel);
    try {
    	String unicode = AppProperties.getConfig("levels.icon."+descLevel);
      if (unicode!=null) {
        Platform.runLater(() -> {
          iconBlack = FontAwesomeImageCreator.generate(unicode);
          iconWhite = FontAwesomeImageCreator.generate(unicode, Color.WHITE);
          this.setGraphic(new ImageView(getIcon()));
        });
      }else{
        String unicodeDefault = AppProperties.getConfig("levels.internal.icon.default");
        if (unicodeDefault!=null) {
          Platform.runLater(() -> {
            iconBlack = FontAwesomeImageCreator.generate(unicodeDefault);
            iconWhite = FontAwesomeImageCreator.generate(unicodeDefault, Color.WHITE);
            this.setGraphic(new ImageView(getIcon()));
          });
        }
      }
    } catch (Exception e) {
      // We don't need to process this exception, since it's expected that there
      // will be a lot of them thrown. It could happen because the user still
      // hasn't finished writing the new description level title
      return;
    }
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
    Map<SipPreview, List<String>> allSips = getSipPreviews();
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
  public Image getIcon() {
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
  public Map<SipPreview, List<String>> getSipPreviews() {
    Map<SipPreview, List<String>> result = new HashMap<>();
    List<String> ancestors = computeAncestorsOfSips();

    // this node's sips
    for (Rule r : ruleObjects.values())
      for (SipPreview sp : r.getSips()) {
        result.put(sp, ancestors);
      }

    sips.forEach((id, sipPreviewNodes) -> sipPreviewNodes
      .forEach(sipPreviewNode -> result.put(sipPreviewNode.getSip(), ancestors)));

    ruleNodes.forEach((s, schNodes) -> schNodes.forEach(schemaNode -> result.putAll(schemaNode.getSipPreviews())));

    // children sips
    for (SchemaNode sn : schemaNodes)
      result.putAll(sn.getSipPreviews());
    return result;
  }

  /**
   * Get Descriptions Objects, non-SIPs. Includes self.
   * @return
     */
  public Map<DescriptionObject, List<String>> getDescriptionObjects(){
    Map<DescriptionObject, List<String>> result = new HashMap<>();

    result.put(getDob(), computeAncestors());

    for (SchemaNode sn : schemaNodes) {
      result.putAll(sn.getSipPreviews());
      result.put(sn.getDob(), computeAncestorsOfSips());
      result.putAll(sn.getDescriptionObjects());
    }

    ruleNodes.forEach((s, schNodes) -> schNodes.forEach(schemaNode ->result.putAll(schemaNode.getDescriptionObjects())));
    return result;
  }

  public List<String> computeAncestors(){
    List<String> ancestors = new ArrayList<>();

    if(getDob().getId() == null){
      return ancestors;
    }

    SchemaNode currentNode = (SchemaNode) getParent();
    while(currentNode != null && currentNode.getParent() != null){
      // Stop when the top of the tree is reached
      if(currentNode.getDob().getId() != null) {
        ancestors.add(currentNode.getDob().getId());
        TreeItem parentItem = currentNode.getParent();
        if(parentItem instanceof SchemaNode)
          currentNode = (SchemaNode) parentItem;
        else currentNode = null;
      }
      else currentNode = null;
    }
    return ancestors;
  }

  public List<String> computeAncestorsOfSips(){
    List<String> ancestors = new ArrayList<>();
    // Add current node
    ancestors.add(getDob().getId());
    // Add ancestors of current node
    ancestors.addAll(computeAncestors());
    return ancestors;
  }
}
