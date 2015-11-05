package org.roda.rodain.rules.sip;

import org.roda.rodain.rules.TreeNode;
import org.roda.rodain.utils.Utils;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.UUID;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 01-10-2015.
 */
public class SipPreview extends Observable implements Observer {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(SipPreview.class.getName());
    private String id;
    private String name;
    private String metadataContent;
    private Path metadataPath;
    private Set<TreeNode> files;
    private boolean metadataLoaded = false, metadataModified = false;
    private boolean contentModified = false;
    private boolean removed = false;

    public SipPreview(String name, Set<TreeNode> files, Path metadataPath, String metadataContent){
        this.name = name;
        this.files = files;
        this.metadataPath = metadataPath;
        this.metadataContent = metadataContent;
        id = UUID.randomUUID().toString();
    }

    private void loadMetadata(){
        if(metadataPath != null){
            try {
                metadataContent = Utils.readFile(metadataPath.toString(), Charset.defaultCharset());
                metadataLoaded = true;
            } catch (IOException e) {
                log.error("Error reading metadata file", e);
            }
        }
    }

    public String getName() {
        return name;
    }

    public Set<TreeNode> getFiles() {
        return files;
    }

    public String getMetadataContent(){
        if(! metadataLoaded){
            loadMetadata();
        }
        return metadataContent;
    }

    public Path getMetadataPath(){
        return metadataPath;
    }

    public void updateMetadata(String meta){
        metadataModified = true;
        metadataContent = meta;
        setChanged();
        notifyObservers();
    }

    public boolean isMetadataModified() {
        return metadataModified;
    }

    public boolean isContentModified() {
        return contentModified;
    }

    public boolean isRemoved() {
        return removed;
    }

    public String getId() {
        return id;
    }

    public void remove(){
        removed = true;
        setChanged();
        notifyObservers();
    }

    @Override
    public void update(Observable o, Object arg) {
        if(o instanceof TreeNode){
            contentModified = true;
            setChanged();
            notifyObservers();
        }
    }

    @Override
    public String toString() {
        return "SipPreview{" +
                "name='" + name + '\'' +
                ", files=" + files +
                '}';
    }
}
