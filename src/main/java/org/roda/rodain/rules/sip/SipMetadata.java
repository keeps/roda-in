package org.roda.rodain.rules.sip;

import org.roda.rodain.rules.MetadataTypes;
import org.roda.rodain.utils.Utils;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Path;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 23/11/2015.
 */
public class SipMetadata {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(SipMetadata.class.getName());
    private MetadataTypes type;
    private boolean loaded = false, modified = false;
    private String content;
    private Path path;
    private String resource;

    public SipMetadata(MetadataTypes type, Path path, String resource){
        this.type = type;
        this.path = path;
        this.resource = resource;
    }

    public boolean isModified(){
        return modified;
    }

    private void loadMetadata(){
        try {
            if(type == MetadataTypes.NEWTEXT){
                if(resource != null){
                    URI resourceURI = ClassLoader.getSystemResource(resource).toURI();
                    content = Utils.readFile(resourceURI.toString(), Charset.defaultCharset());
                }
            } else {
                if (path != null) {
                    content = Utils.readFile(path.toString(), Charset.defaultCharset());
                    loaded = true;
                }
            }
        } catch (URISyntaxException e){
            log.error("Error accessing resource", e);
        } catch (IOException e) {
            log.error("Error reading metadata file", e);
        }
    }

    public String getMetadataContent(){
        if(! loaded){
            loadMetadata();
        }
        return content;
    }

    public void update(String meta){
        modified = true;
        content = meta;
    }

}
