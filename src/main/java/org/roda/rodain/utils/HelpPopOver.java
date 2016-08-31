package org.roda.rodain.utils;

import java.util.HashSet;

import javax.swing.*;

import org.controlsfx.control.PopOver;
import org.roda.rodain.core.AppProperties;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.web.WebView;


/**
 * @author Andre Pereira apereira@keep.pt
 * @since 29-07-2016.
 */
public class HelpPopOver extends PopOver {
  private static int SHOWING_DELAY = 1000;
  private static int HIDING_DELAY = 200;
  private WebView webView = initializeWebView();
  private boolean onPopup = false, onTarget = false;
  private Node target;
  private boolean hasHiddenScheduled = false, hasShowScheduled = false;
  private static HashSet<HelpPopOver> instances = new HashSet<>();

  private HelpPopOver(Node target, String content, PopOver.ArrowLocation arrowLocation, int maxHeight){
    super();
    this.target = target;

    // Create the popover with the help text that will be displayed when the user clicks the button
    setDetachable(false);
    setArrowLocation(arrowLocation);

    HBox popOverContent = new HBox(10);
    popOverContent.getStyleClass().add("helpTokenPopOver");
    popOverContent.setPadding(new Insets(5, 15, 5, 15));
    popOverContent.setAlignment(Pos.CENTER);
    HBox.setHgrow(popOverContent, Priority.ALWAYS);

    getRoot().setOnMouseEntered(event -> {
      onPopup = true;
      updatePopup();
    });
    getRoot().setOnMouseExited(event -> {
      onPopup = false;
      updatePopup();
    });
    target.setOnMouseEntered(event -> {
      onTarget = true;
      updatePopup();
    });
    target.setOnMouseExited(event -> {
      onTarget = false;
      updatePopup();
    });

    setContentNode(popOverContent);

    Platform.runLater(() ->{
      String head = "<html> <head> <meta charset=\"UTF-8\"> <link rel=\"stylesheet\" type=\"text/css\" href=\"webview.css\"> </head><body>";
      webView.getEngine().loadContent(head + content);
      webView.setStyle("-fx-max-height: " + maxHeight);
      popOverContent.getChildren().add(webView);
    });
  }

  /**
   * Instantiates a new HelpPopOver.
   * @param target The target of the PopOver. It's used to trigger the display of the PopOver and its location.
   * @param content The message that will be displayed in the PopOver.
   * @param arrowLocation Defines the location of the arrow that connects the PopOver to the target.
   * @param maxHeight Defines the maximum height of the PopOver.
   */
  public static void create(Node target, String content, PopOver.ArrowLocation arrowLocation, int maxHeight){
    if(Boolean.parseBoolean(AppProperties.getAppConfig("app.helpEnabled"))){
      instances.add(new HelpPopOver(target, content, arrowLocation, maxHeight));
    }
  }

  private void updatePopup(){
    if(onPopup || onTarget) {
      if(!isShowing() && !hasShowScheduled) {
        hasShowScheduled = true;
        Timer timer = new Timer(SHOWING_DELAY, e -> {
          if (onPopup || onTarget) {
            Platform.runLater(() -> show(target));
          }
          hasShowScheduled = false;
        });
        timer.setRepeats(false); // Only execute once
        timer.start();
      }
    }else{
      if(!hasHiddenScheduled) {
        hasHiddenScheduled = true;
        Timer timer = new Timer(HIDING_DELAY, e -> {
          if (!onPopup && !onTarget) {
            Platform.runLater(() -> hide());
          }
          hasHiddenScheduled = false;
        });
        timer.setRepeats(false); // Only execute once
        timer.start();
      }
    }
  }

  private WebView initializeWebView() {
    Platform.runLater(() -> {
      WebView web = new WebView();
      web.getEngine().setUserStyleSheetLocation(ClassLoader.getSystemResource("css/webview.css").toString());
      webView = web;
    });
    return webView;
  }
}
