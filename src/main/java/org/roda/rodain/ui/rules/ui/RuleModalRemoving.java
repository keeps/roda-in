package org.roda.rodain.ui.rules.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;

import org.roda.rodain.core.Constants;
import org.roda.rodain.core.I18n;
import org.roda.rodain.core.sip.SipPreview;
import org.roda.rodain.ui.rules.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 19/11/2015.
 */
public class RuleModalRemoving extends BorderPane implements Observer {
  private static final Logger LOGGER = LoggerFactory.getLogger(RuleModalRemoving.class.getName());

  // rule ID -> progress
  private Map<Integer, Float> rules;
  private Map<Integer, Rule> ruleObjects;
  private Map<String, Float> sips;
  private Map<String, SipPreview> sipObjects;
  // top
  private Label sipsRemovedLabel;
  // center
  private ProgressBar progress;
  private int removedSIPs = 0;
  private Timer timer;

  /**
   * Creates a new RuleModalRemoving object.
   */
  public RuleModalRemoving() {
    rules = new HashMap<>();
    ruleObjects = new HashMap<>();
    sips = new HashMap<>();
    sipObjects = new HashMap<>();

    getStyleClass().add(Constants.CSS_SIPCREATOR);

    createTop();
    createCenter();

    createUpdateTask();
  }

  private void createTop() {
    VBox top = new VBox(5);
    top.setAlignment(Pos.CENTER);
    top.setPadding(new Insets(10, 10, 10, 0));
    top.getStyleClass().add(Constants.CSS_HBOX);

    Label title = new Label(I18n.t(Constants.I18N_RULEMODALREMOVING_TITLE).toUpperCase());
    title.setId("title");

    top.getChildren().add(title);
    setTop(top);
  }

  private void createCenter() {
    VBox center = new VBox(5);
    center.setPadding(new Insets(0, 10, 10, 10));
    center.setAlignment(Pos.CENTER_LEFT);

    progress = new ProgressBar(0);
    progress.setPadding(new Insets(5, 0, 10, 0));
    progress.setPrefSize(380, 25);

    sipsRemovedLabel = new Label();

    center.getChildren().addAll(progress, sipsRemovedLabel);
    setCenter(center);
  }

  private void createUpdateTask() {
    TimerTask updater = new TimerTask() {
      @Override
      public void run() {
        for (Map.Entry<String, SipPreview> entry : sipObjects.entrySet()) {
          SipPreview sp = entry.getValue();
          sp.removeFromRule();
        }
        sipObjects.clear();

        for (Map.Entry<Integer, Rule> entry : ruleObjects.entrySet()) {
          Rule r = entry.getValue();
          if (r.getSipCount() == 0)
            r.remove();
        }
        ruleObjects.clear();

        Platform.runLater(() -> {
          if (sipObjects.isEmpty() && ruleObjects.isEmpty()) {
            close();
          }
        });
      }
    };

    timer = new Timer();
    timer.schedule(updater, 5000, 1000);
  }

  /**
   * Updates the progress of the rule removal.
   *
   * @param o
   *          The Observable object, should be a Rule.
   * @param args
   *          The arguments of the update.
   */
  @Override
  public void update(Observable o, Object args) {
    if (!(args instanceof Float)) {
      if (o instanceof Rule && args instanceof String) {
        Rule r = (Rule) o;
        if (rules.containsKey(r.getId()) && Constants.EVENT_REMOVED_RULE.equals((String) args)) {
          rules.remove(r.getId());
        }
      }
      if (o instanceof SipPreview) {
        SipPreview s = (SipPreview) o;
        sips.remove(s.getId());
        removedSIPs++;
      }
      if (rules.isEmpty() && sips.isEmpty()) {
        updateProgress(1f);
        close();
      }
      updateProgress(computeProgress());
      return;
    }
    if (o instanceof SipPreview) {
      SipPreview sip = (SipPreview) o;
      sips.put(sip.getId(), (float) args);
    }

    updateProgress(computeProgress());
  }

  private float computeProgress() {
    float rulesPartial = 0f, sipsPartial = 0f;
    for (float f : rules.values()) {
      rulesPartial += f;
    }

    if (!rules.isEmpty())
      rulesPartial /= rules.size();

    for (float f : sips.values()) {
      sipsPartial += f;
    }
    sipsPartial += removedSIPs;
    if (sips.size() + removedSIPs > 0)
      sipsPartial /= sips.size() + removedSIPs;

    float result = rulesPartial + sipsPartial;
    if (rulesPartial > 0 && sipsPartial > 0)
      result /= 2;
    return result;
  }

  /**
   * Adds a rule to the removal task.
   *
   * @param r
   */
  public void addRule(Rule r) {
    rules.put(r.getId(), 0f);
  }

  /**
   * Adds a SIP to the removal task.
   *
   * @param s
   */
  public void addSIP(SipPreview s) {
    sips.put(s.getId(), 0f);
  }

  private void updateProgress(float progressValue) {
    Platform.runLater(() -> {
      progress.setProgress(progressValue);
      sipsRemovedLabel.setText(String.format(I18n.t(Constants.I18N_RULEMODALREMOVING_REMOVED_FORMAT), (int) (progressValue * 100)));
      if (Math.round(progressValue) == 1) {
        close();
      }
    });
  }

  private void close() {
    timer.cancel();
    RuleModalController.cancel();
  }
}
