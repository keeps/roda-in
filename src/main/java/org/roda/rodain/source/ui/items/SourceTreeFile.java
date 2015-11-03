package org.roda.rodain.source.ui.items;

import java.io.File;
import java.nio.file.Path;

import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 17-09-2015.
 */
public class SourceTreeFile extends TreeItem<String> implements SourceTreeItem{
    public static final Image fileImage = new Image(ClassLoader.getSystemResourceAsStream("icons/file.png"));
    //this stores the full path to the file
    private String fullPath;
    private SourceTreeItemState state;
    private String mappingRuleId;

    public SourceTreeFile(Path file, SourceTreeItemState st){
        this(file);
        state = st;
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
        if(state == SourceTreeItemState.NORMAL)
            state = SourceTreeItemState.IGNORED;
    }

    @Override
    public void addMapping(String ruleId){
        if(state == SourceTreeItemState.NORMAL)
            state = SourceTreeItemState.MAPPED;
        mappingRuleId = ruleId;
    }

    @Override
    public void removeIgnore(){
        if(state == SourceTreeItemState.IGNORED)
            state = SourceTreeItemState.NORMAL;
    }

    @Override
    public void removeMapping(String ruleId){
        if(state == SourceTreeItemState.MAPPED && mappingRuleId.equals(ruleId))
            state = SourceTreeItemState.NORMAL;
        mappingRuleId = "";
    }
}
