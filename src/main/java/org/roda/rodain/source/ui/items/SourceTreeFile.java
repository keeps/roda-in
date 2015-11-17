package org.roda.rodain.source.ui.items;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.roda.rodain.core.PathCollection;
import org.roda.rodain.rules.Rule;

import java.io.File;
import java.nio.file.Path;
import java.util.Observable;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 17-09-2015.
 */
public class SourceTreeFile extends SourceTreeItem{
    public static final Image fileImage = new Image(ClassLoader.getSystemResourceAsStream("icons/file.png"));
    //this stores the full path to the file
    private String fullPath;
    private SourceTreeDirectory parent;

    private Rule rule;

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
        PathCollection.addPath(fullPath, state);
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
            PathCollection.addPath(fullPath, state);
            parent.verifyState();
        }
    }

    @Override
    public void addMapping(Rule r){
        rule = r;
        if(state == SourceTreeItemState.NORMAL) {
            state = SourceTreeItemState.MAPPED;
            PathCollection.addPath(fullPath, state);
            if(parent != null)
                parent.verifyState();
        }
    }

    @Override
    public void removeIgnore(){
        if(state == SourceTreeItemState.IGNORED) {
            state = SourceTreeItemState.NORMAL;
            PathCollection.addPath(fullPath, state);
            if(parent != null)
                parent.verifyState();
        }
    }

    @Override
    public void removeMapping(Rule r){
        if(rule == null || r == rule){
            if (!r.isMapped(fullPath)) {
                state = SourceTreeItemState.NORMAL;
                PathCollection.addPath(fullPath, state);
            }
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
