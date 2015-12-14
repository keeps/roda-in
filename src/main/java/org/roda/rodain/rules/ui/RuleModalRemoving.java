package org.roda.rodain.rules.ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.roda.rodain.rules.Rule;

import java.util.Observable;
import java.util.Observer;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 19/11/2015.
 */
public class RuleModalRemoving extends BorderPane implements Observer {
  private Rule rule;
  // top
  private Label sipsRemovedLabel;
  // center
  private ProgressBar progress;
  private int sipCount;

  /**
   * Creates a new RuleModalRemoving object.
   *
   * @param rule The Rule that is being removed.
   */
  public RuleModalRemoving(Rule rule) {
    this.rule = rule;
    sipCount = rule.getSipCount();

    getStyleClass().add("sipcreator");

    createTop();
    createCenter();
  }

  private void createTop() {
    VBox top = new VBox(5);
    top.setAlignment(Pos.CENTER);
    top.setPadding(new Insets(10, 10, 10, 0));
    top.getStyleClass().add("hbox");

    Label title = new Label("Removing SIPs");
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
   * @param o The Observable object, should be a Rule.
   * @param args The arguments of the update.
   */
  public void update(Observable o, Object args) {
    if (o == rule) {
      Platform.runLater(new Runnable() {
        @Override
        public void run() {
          if (args instanceof Integer) {
            int removed = (int) args;
            double prog = (removed * 1.0) / sipCount;
            progress.setProgress(prog);
            sipsRemovedLabel.setText(String.format("Removed %d SIPs (%d%%)", removed, (int) (prog * 100)));
          } else {
            close();
          }
        }
      });
    }

  }

  private void close() {
    RuleModalController.cancel();
  }
}
