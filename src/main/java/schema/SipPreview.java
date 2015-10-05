package schema;

import org.slf4j.LoggerFactory;

/**
 * Created by adrapereira on 01-10-2015.
 */
public class SipPreview {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(SipPreview.class.getName());
    private String name;
    private String path;

    public SipPreview(String name, String path){
        this.name = name;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }
}
