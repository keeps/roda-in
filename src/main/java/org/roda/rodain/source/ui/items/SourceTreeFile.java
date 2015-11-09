package org.roda.rodain.source.ui.items;

import java.io.File;
import java.nio.file.Path;
import java.util.Observable;
import java.util.Set;

import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.roda.rodain.rules.Rule;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 17-09-2015.
 */
public class SourceTreeFile extends SourceTreeItem{
    public static final Image fileImage = new Image(ClassLoader.getSystemResourceAsStream("icons/file.png"));
    //this stores the full path to the file
    private String fullPath;
    private SourceTreeItemState state;
    private SourceTreeDirectory parent;

    public SourceTreeFile(Path file, SourceTreeItemState st, SourceTreeDirectory parent){
        this(file);
        state = st;
        this.parent = parent;
    }

    public SourceTreeFile(Path file) {
        super(file.toString());
        this.fullPath = file.toString();
        this.setGraphic(new ImageView(fileImage));

        //set the value
        if (!fullPath.endsWith(File.separator)) {
            //set the value (which is what is displayed in the tree)
            String value = file.toString();
            int indexOf = value.lastIndexOf(File.separator);
            if (indexOf > 0) {
                this.setValue(value.substring(indexOf + 1));
            } else {
                this.setValue(value);
            }
        }

        state = SourceTreeItemState.NORMAL;
    }

    @Override
    public String getPath() {
        return this.fullPath;
    }

    @Override
    public SourceTreeItemState getState(){
        return state;
    }

    @Override
    public void addIgnore(){
        if(state == SourceTreeItemState.NORMAL) {
            state = SourceTreeItemState.IGNORED;
            parent.verifyState();
        }
    }

    @Override
    public void addMapping(Rule r){
        if(state == SourceTreeItemState.NORMAL) {
            state = SourceTreeItemState.MAPPED;
            if(parent != null)
                parent.verifyState();
        }
    }

    @Override
    public void removeIgnore(){
        if(state == SourceTreeItemState.IGNORED) {
            state = SourceTreeItemState.NORMAL;
            if(parent != null)
                parent.verifyState();
        }
    }

    @Override
    public void removeMapping(Rule r){
        Set<String> removed = r.getRemoved();
        if(state == SourceTreeItemState.MAPPED && removed.contains(fullPath)) {
            state = SourceTreeItemState.NORMAL;
            if(parent != null)
                parent.verifyState();
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        if(o instanceof Rule){
            Rule rule = (Rule) o;
            removeMapping(rule);
        }
    }
}
