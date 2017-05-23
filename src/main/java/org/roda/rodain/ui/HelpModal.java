package org.roda.rodain.ui;

import org.roda.rodain.core.ConfigurationManager;
import org.roda.rodain.core.Constants;
import org.roda.rodain.core.I18n;

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
  private Stage stage;
  private WebView webView;
  private Hyperlink currentlyVisited;

  public HelpModal(Stage stage) {
    this.stage = stage;
    getStyleClass().add(Constants.CSS_MODAL);

    createTop();
    createCenter();
  }

  private void createTop() {
    VBox top = new VBox(5);
    top.setAlignment(Pos.CENTER);
    top.getStyleClass().add(Constants.CSS_HBOX);
    top.setPadding(new Insets(10, 10, 10, 0));

    Label title = new Label(I18n.t(Constants.I18N_HELP).toUpperCase());
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
    Button btClose = new Button(I18n.t(Constants.I18N_CLOSE));
    btClose.setMinWidth(100);
    btClose.setOnAction(event -> closeHelp());

    bottom.getChildren().add(btClose);
    setHelp();
    center.getChildren().addAll(webView, bottom);

    setCenter(center);
  }

  private void setHelp() {
    webView.getEngine().load(ConfigurationManager.getHelpFile());
  }

  private void closeHelp() {
    Platform.runLater(() -> stage.close());
  }

}
