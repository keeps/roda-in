package schema.ui;

import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import schema.SipPreview;

/**
 * Created by adrapereira on 05-10-2015.
 */
public class SipPreviewNode extends TreeItem<String> {
    private SipPreview sip;
    private Image icon;

    public SipPreviewNode(SipPreview sip, Image icon){
        super(sip.getName());
        this.sip = sip;
        this.icon = icon;
        setGraphic(new ImageView(icon));
    }
    public String toString(){return sip.toString();}

    public SipPreview getSip() {
        return sip;
    }

    public Image getIcon() {
        return icon;
    }
}
