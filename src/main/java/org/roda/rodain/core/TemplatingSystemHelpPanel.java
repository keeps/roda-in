package org.roda.rodain.core;

import javafx.scene.control.Alert;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 03-08-2016.
 */
public class TemplatingSystemHelpPanel extends Alert{
  private WebView webView;

  public TemplatingSystemHelpPanel(Stage primaryStage){
    super(AlertType.INFORMATION);

    String head = "<html> <head> <meta charset=\"UTF-8\"> <link rel=\"stylesheet\" type=\"text/css\" href=\"templating.css\"> </head><body>";
    String content = head + I18n.help("templatingSystem");
    webView = new WebView();
    webView.getEngine().setUserStyleSheetLocation(ClassLoader.getSystemResource("css/templating.css").toString());
    webView.getEngine().loadContent(content);

    initStyle(StageStyle.UNDECORATED);
    setHeaderText(I18n.t("templatingSystemHelp.header"));
    getDialogPane().setContent(webView);
    initModality(Modality.APPLICATION_MODAL);
    initOwner(primaryStage);
    show();
  }
}
