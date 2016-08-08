package org.roda.rodain.utils;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import org.controlsfx.control.PopOver;


/**
 * @author Andre Pereira apereira@keep.pt
 * @since 29-07-2016.
 */
public class HelpToken extends PopOver {
  private WebView webView = initializeWebView();

  public HelpToken(String content, PopOver.ArrowLocation arrowLocation, int maxHeight){
    super();
    // Create the popover with the help text that will be displayed when the user clicks the button
    setDetachable(false);
    setArrowLocation(arrowLocation);

    HBox popOverContent = new HBox(10);
    popOverContent.getStyleClass().add("helpTokenPopOver");
    popOverContent.setPadding(new Insets(5, 15, 5, 15));
    popOverContent.setAlignment(Pos.CENTER);
    HBox.setHgrow(popOverContent, Priority.ALWAYS);


    setContentNode(popOverContent);

    Platform.runLater(() ->{
      String head = "<html> <head> <meta charset=\"UTF-8\"> <link rel=\"stylesheet\" type=\"text/css\" href=\"webview.css\"> </head><body>";
      webView.getEngine().loadContent(head + content);
      webView.setStyle("-fx-max-height: " + maxHeight);
      popOverContent.getChildren().add(webView);
    });
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
