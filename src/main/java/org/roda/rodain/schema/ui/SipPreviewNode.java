package org.roda.rodain.schema.ui;

import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.roda.rodain.rules.sip.SipPreview;

import java.util.Observable;
import java.util.Observer;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 05-10-2015.
 */
public class SipPreviewNode extends TreeItem<String> implements Observer {
  private SipPreview sip;
  private Image icon;

  /**
   * Creates a new SipPreviewNode
   *
   * @param sip  The SipPreview object to be wrapped
   * @param icon The icon to be used in the SipPreviewNode
   */
  public SipPreviewNode(SipPreview sip, Image icon) {
    super(sip.getName());
    this.sip = sip;
    this.icon = icon;
    setGraphic(new ImageView(icon));
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
    return icon;
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
