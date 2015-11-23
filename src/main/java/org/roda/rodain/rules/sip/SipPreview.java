package org.roda.rodain.rules.sip;

import org.roda.rodain.core.PathCollection;
import org.roda.rodain.rules.MetadataTypes;
import org.roda.rodain.rules.TreeNode;
import org.roda.rodain.source.ui.items.SourceTreeItemState;
import org.roda.rodain.utils.Utils;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.*;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 01-10-2015.
 */
public class SipPreview extends Observable implements Observer {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(SipPreview.class.getName());
    private String id;
    private String name;
    private SipMetadata metadata;
    private Set<TreeNode> files;
    private boolean contentModified = false;
    private boolean removed = false;

    public SipPreview(String name, Set<TreeNode> files, MetadataTypes metaType, Path metadataPath,
                      String metadataResource){
        this.name = name;
        this.files = files;
        metadata = new SipMetadata(metaType, metadataPath, metadataResource);
        id = UUID.randomUUID().toString();

        setPathsAsMapped();
    }

    private void setPathsAsMapped(){
        for(TreeNode tn: files){
            Set<String> paths = tn.getFullTreePaths();
            for(String path: paths){
                PathCollection.addPath(path, SourceTreeItemState.MAPPED);
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
        return metadata.getMetadataContent();
    }

    public void updateMetadata(String meta){
        metadata.update(meta);
        setChanged();
        notifyObservers();
    }

    public void ignoreContent(Set<Path> paths){
        Set<String> ignored = new HashSet<>();
        Set<TreeNode> toRemove = new HashSet<>();
        for(TreeNode tn: files){
            ignored.addAll(tn.ignoreContent(paths));
            if(paths.contains(tn.getPath()))
                toRemove.add(tn);
        }
        files.removeAll(toRemove);
        PathCollection.addPaths(ignored, SourceTreeItemState.NORMAL);
    }

    public boolean isMetadataModified() {
        return metadata.isModified();
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

    public void setRemoved(){
        removed = true;
    }

    public void changedAndNotify(){
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
