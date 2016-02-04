package org.roda.rodain.schema.ui;

import java.util.Observable;
import java.util.Observer;

import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import org.roda.rodain.rules.sip.SipPreview;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 05-10-2015.
 */
public class SipPreviewNode extends TreeItem<String> implements Observer {
  private SipPreview sip;
  private Image iconBlack, iconWhite;

  private boolean blackIconSelected = true;

  /**
   * Creates a new SipPreviewNode
   *
   * @param sip
   *          The SipPreview object to be wrapped
   * @param iconBlack
   *          The icon to be used in the SipPreviewNode, with the color black
   * @param iconWhite
   *          The icon to be used in the SipPreviewNode, with the color white
   */
  public SipPreviewNode(SipPreview sip, Image iconBlack, Image iconWhite) {
    super(sip.getName());
    this.sip = sip;
    this.iconBlack = iconBlack;
    this.iconWhite = iconWhite;
    setGraphic(new ImageView(iconBlack));
  }

  /**
   * @return The SipPreview object that the SipPreviewNode is wrapping
   */
  public SipPreview getSip() {
    return sip;
  }

  /**
   * @return The SipPreviewNode's icon
   */
  public Image getIcon() {
    if (blackIconSelected) {
      return iconBlack;
    } else
      return iconWhite;
  }

  public void toggleIcon() {
    if (blackIconSelected) {
      setGraphic(new ImageView(iconWhite));
      blackIconSelected = false;
    } else {
      setGraphic(new ImageView(iconBlack));
      blackIconSelected = true;
    }
  }

  /**
   * @return True if the SipPreview's metadata has been modified, false
   * otherwise
   * @see SipPreview#isMetadataModified()
   */
  public boolean isMetaModified() {
    return sip.isMetadataModified();
  }

  /**
   * @return True if the SipPreview's content has been modified, false otherwise
   * @see SipPreview#isContentModified()
   */
  public boolean isContentModified() {
    return sip.isContentModified();
  }

  /**
   * Forces the redraw of the item
   *
   * @param o
   * @param arg
   */
  @Override
  public void update(Observable o, Object arg) {
    String value = getValue();
    setValue("");
    setValue(value); // this forces a redraw of the item
  }
}
