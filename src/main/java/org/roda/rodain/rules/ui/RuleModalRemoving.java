package org.roda.rodain.rules.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import org.roda.rodain.core.AppProperties;
import org.roda.rodain.rules.Rule;
import org.roda.rodain.rules.sip.SipPreview;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 19/11/2015.
 */
public class RuleModalRemoving extends BorderPane implements Observer {
  // rule ID -> progress
  private Map<Integer, Float> rules;
  // top
  private Label sipsRemovedLabel;
  // center
  private ProgressBar progress;

  /**
   * Creates a new RuleModalRemoving object.
   */
  public RuleModalRemoving() {
    rules = new HashMap<>();

    getStyleClass().add("sipcreator");

    createTop();
    createCenter();
  }

  private void createTop() {
    VBox top = new VBox(5);
    top.setAlignment(Pos.CENTER);
    top.setPadding(new Insets(10, 10, 10, 0));
    top.getStyleClass().add("hbox");

    Label title = new Label(AppProperties.getLocalizedString("RuleModalRemoving.title"));
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

  /**
   * Updates the progress of the rule removal.
   *
   * @param o    The Observable object, should be a Rule.
   * @param args The arguments of the update.
   */
  public void update(Observable o, Object args) {
    if (!(args instanceof Float)) {
      if (o instanceof Rule) {
        Rule r = (Rule) o;
        rules.remove(r.getId());
      }
      if (rules.isEmpty()) {
        updateProgress(1f);
        close();
      }
      return;
    }
    float progressValue = 0;
    if (o instanceof Rule) {
      Rule rule = (Rule) o;
      rules.put(rule.getId(), (float) args);
      for (float f : rules.values()) {
        progressValue += f;
      }
      progressValue /= rules.size();
    }
    if (o instanceof SipPreview) {
      progressValue = (float) args;
    }
    updateProgress(progressValue);
  }

  /**
   * Adds a rule to the removal task.
   * 
   * @param r
   */
  public void addRule(Rule r) {
    rules.put(r.getId(), 0f);
  }

  private void updateProgress(float progressValue) {
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        progress.setProgress(progressValue);
        sipsRemovedLabel.setText(String.format(AppProperties.getLocalizedString("RuleModalRemoving.removedFormat"),
          (int) (progressValue * 100)));
      }
    });
  }

  private void close() {
    RuleModalController.cancel();
  }
}
