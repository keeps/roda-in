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
public class SipPreviewNode extends TreeItem<String>implements Observer {
  private SipPreview sip;
  private Image icon;

  public SipPreviewNode(SipPreview sip, Image icon) {
    super(sip.getName());
    this.sip = sip;
    this.icon = icon;
    setGraphic(new ImageView(icon));
  }

  @Override
  public String toString() {
    return sip.toString();
  }

  public SipPreview getSip() {
    return sip;
  }

  public Image getIcon() {
    return icon;
  }

  public boolean isMetaModified() {
    return sip.isMetadataModified();
  }

  public boolean isContentModified() {
    return sip.isContentModified();
  }

  @Override
  public void update(Observable o, Object arg) {
    String value = getValue();
    setValue("");
    setValue(value); // this forces a redraw of the item
  }
}
