package org.roda.rodain.rules.sip;

import org.roda.rodain.rules.TreeNode;
import org.roda.rodain.utils.RandomIdGenerator;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 01-10-2015.
 */
public class SipPreview {
    private String name;
    private String path;
    private String metadata;
    private Set<TreeNode> files;
    private boolean metaModified = false;
    private boolean contentModified = false;

    public SipPreview(String path, Set<TreeNode> files, String metadata){
        Path pa = Paths.get(path);
        name = "SIP " + pa.getFileName().toString();
        this.path = path;
        this.files = files;
        this.metadata = metadata;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public Set<TreeNode> getFiles() {
        return files;
    }

    public String getMetadata(){
        return metadata;
    }

    public void setMetadata(String meta){
        metadata = meta;
    }

    public boolean isMetaModified() {
        return metaModified;
    }

    public void setMetaModified(){
        metaModified = true;
    }

    public boolean isContentModified() {
        return contentModified;
    }

    public void setContentModified() {
        this.contentModified = true;
    }

    @Override
    public String toString() {
        return "SipPreview{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", files=" + files +
                '}';
    }
}
