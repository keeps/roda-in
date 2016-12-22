package org.roda.rodain.core;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 13/09/2016.
 */
public class HelpModal extends BorderPane {
  private static final String WEB_VIEW_HEAD = "<html> <head> <meta charset=\"UTF-8\"> <link rel=\"stylesheet\" type=\"text/css\" href=\"webview.css\"> </head><body>";
  private Stage stage;
  private WebView webView;
  private Hyperlink currentlyVisited;

  public HelpModal(Stage stage) {
    this.stage = stage;
    getStyleClass().add("modal");

    createTop();
    createCenter();
  }

  private void createTop() {
    VBox top = new VBox(5);
    top.setAlignment(Pos.CENTER);
    top.getStyleClass().add("hbox");
    top.setPadding(new Insets(10, 10, 10, 0));

    Label title = new Label(I18n.t("help").toUpperCase());
    title.setId("title");

    top.getChildren().add(title);
    setTop(top);
  }

  private void createCenter() {
    VBox center = new VBox(10);
    center.setPadding(new Insets(10, 10, 10, 10));
    center.setAlignment(Pos.TOP_CENTER);

    webView = new WebView();
    webView.getEngine().setUserStyleSheetLocation(ClassLoader.getSystemResource("css/webview.css").toString());

    HBox bottom = new HBox();
    bottom.setAlignment(Pos.CENTER_RIGHT);
    Button btClose = new Button(I18n.t("close"));
    btClose.setMinWidth(100);
    btClose.setOnAction(event -> closeHelp());

    bottom.getChildren().add(btClose);
    setHelp();
    center.getChildren().addAll(webView, bottom);

    setCenter(center);
  }

  private void setHelp() {
    String help = AppProperties.getHelpFile();
    webView.getEngine().load(AppProperties.getHelpFile());
  }

  private void closeHelp() {
    Platform.runLater(() -> stage.close());
  }

}
