package org.roda.rodain.core;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
  private Label centerLabel;
  private Hyperlink currentlyVisited;

  public HelpModal(Stage stage){
    this.stage = stage;
    getStyleClass().add("modal");

    createTop();
    createCenter();
    createMenu();
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
    center.setPadding(new Insets(10,10,10,10));
    center.setAlignment(Pos.TOP_CENTER);

    centerLabel = new Label();
    centerLabel.setId("sub-title");

    webView = new WebView();
    webView.getEngine().setUserStyleSheetLocation(ClassLoader.getSystemResource("css/webview.css").toString());

    HBox bottom = new HBox();
    bottom.setAlignment(Pos.CENTER_RIGHT);
    Button btClose = new Button(I18n.t("close"));
    btClose.setMinWidth(100);
    btClose.setOnAction(event -> closeHelp());

    bottom.getChildren().add(btClose);

    center.getChildren().addAll(centerLabel, webView, bottom);
    setCenter(center);
  }

  private void setHelp(String key){
    webView.getEngine().loadContent(WEB_VIEW_HEAD + I18n.help(key));
    centerLabel.setText(I18n.help("link." + key));
  }

  private void createMenu() {
    VBox menu = new VBox();
    menu.setPadding(new Insets(10, 10, 10, 10));
    menu.setAlignment(Pos.TOP_LEFT);
    menu.getStyleClass().add("help-menu");

    Label menuLabel = new Label(I18n.t("menu"));
    menuLabel.setId("sub-title");
    menuLabel.setPadding(new Insets(0,0,20,0));

    List<Node> children = new ArrayList<>();
    children.add(menuLabel);

    children.add(createLink("associate"));
    children.add(createLink("associationMethod"));
    children.add(createLink("schemaPane"));
    children.add(createLink("firstStep"));
    children.add(createLink("secondStep"));
    children.add(createLink("export"));
    children.add(createLink("fileExplorer"));
    children.add(createLink("inspectionPanel"));
    children.add(createLink("inspectionPanel.associations"));
    children.add(createLink("inspectionPanel.data"));
    children.add(createLink("inspectionPanel.metadata"));
    children.add(createLink("metadataMethod"));
    children.add(createLink("templatingSystem"));

    Hyperlink first = (Hyperlink) children.get(1);
    if(first != null) {
      first.fire();
    }

    menu.getChildren().addAll(children);
    setLeft(menu);
  }

  private void closeHelp(){
    Platform.runLater(() -> stage.close());
  }


  private Hyperlink createLink(String key){
    Hyperlink link = new Hyperlink(I18n.help("link." + key));
    link.setPadding(new Insets(5,0,5,0));
    link.setOnAction(event ->{
      setHelp(key);
      if(currentlyVisited != null && currentlyVisited != link){
        currentlyVisited.setVisited(false);
      }
      currentlyVisited = link;
    });
    return link;
  }
}
