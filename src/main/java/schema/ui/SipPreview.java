package schema.ui;

import javafx.scene.control.TreeItem;
import org.slf4j.LoggerFactory;

/**
 * Created by adrapereira on 01-10-2015.
 */
public class SipPreview extends TreeItem<String> {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(SipPreview.class.getName());
    private String name;

    public SipPreview(String name){
        super(name);
        this.name = name;
    }
}
