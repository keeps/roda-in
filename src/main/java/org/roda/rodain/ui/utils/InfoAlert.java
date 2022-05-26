package org.roda.rodain.ui.utils;

import org.roda.rodain.core.ConfigurationManager;
import org.roda.rodain.core.Constants;
import org.roda.rodain.core.I18n;

import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * {@author João Gomes <jgomes@keep.pt>}.
 */
public class InfoAlert {

  public static final Image infoIcon = new Image(ClassLoader.getSystemResourceAsStream(Constants.RSC_ICON_INFO_CIRCLE));

  /**
   * Creates {@link ImageView} with the info icon and the click event with an info
   * alert.
   * 
   * @param stage
   *          the {@link Stage}
   * @param i18nHeaderIdentifier
   *          the identifier for i18n header text
   * @param i18nContentIdentifier
   *          the identifier for i18n header text
   * @return the {@link ImageView}.
   */
  public static ImageView getInfoAlert(Stage stage, String i18nHeaderIdentifier, String i18nContentIdentifier) {
    ImageView infoImage = new ImageView(infoIcon);
    infoImage.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent mouseEvent) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initStyle(StageStyle.DECORATED);
        alert.initOwner(stage);
        alert.setTitle(I18n.t(Constants.I18N_HELP));
        alert.setHeaderText(I18n.t(i18nHeaderIdentifier));
        alert.setContentText(I18n.t(i18nContentIdentifier));
        alert.getDialogPane().setStyle(ConfigurationManager.getStyle("export.alert"));
        alert.getDialogPane().setStyle(ConfigurationManager.getStyle("creationmodalprocessing.blackmagic"));
        alert.show();
      }
    });
    return infoImage;
  }
}
