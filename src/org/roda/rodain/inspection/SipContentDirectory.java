package rodain.inspection;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import rodain.source.ui.items.SourceTreeItem;
import rodain.source.ui.items.SourceTreeItemState;

/**
 * Created by adrapereira on 17-09-2015.
 */
public class SipContentDirectory extends TreeItem<Object> implements SourceTreeItem{
    public static final Image folderCollapseImage = new Image(ClassLoader.getSystemResourceAsStream("icons/folder.png"));
    public static final Image folderExpandImage = new Image(ClassLoader.getSystemResourceAsStream("icons/folder-open.png"));
    private static final Comparator comparator = createComparator();
    //this stores the full path to the file or directory
    private String fullPath;

    public SipContentDirectory(Path file) {
        super(file.toString());
        this.fullPath = file.toString();
        this.setGraphic(new ImageView(folderCollapseImage));

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

        this.addEventHandler(TreeItem.branchExpandedEvent(), new EventHandler<TreeModificationEvent<Object>>() {
            @Override
            public void handle(TreeModificationEvent<Object> e) {
                SipContentDirectory source = (SipContentDirectory) e.getSource();
                if (source.isExpanded()) {
                    ImageView iv = (ImageView) source.getGraphic();
                    iv.setImage(folderExpandImage);
                }
            }
        });

        this.addEventHandler(TreeItem.branchCollapsedEvent(), new EventHandler<TreeModificationEvent<Object>>() {
            @Override
            public void handle(TreeModificationEvent<Object> e) {
                SipContentDirectory source = (SipContentDirectory) e.getSource();
                if (!source.isExpanded()) {
                    ImageView iv = (ImageView) source.getGraphic();
                    iv.setImage(folderCollapseImage);
                }
            }
        });
    }

    public void sortChildren(){
        ArrayList<TreeItem<Object>> aux = new ArrayList<>(getChildren());
        Collections.sort(aux, comparator);
        getChildren().setAll(aux);

        for(TreeItem ti: getChildren()){
            if(ti instanceof SipContentDirectory)
                ((SipContentDirectory)ti).sortChildren();
        }
    }

    private static Comparator createComparator(){
        return new Comparator<TreeItem>() {
            @Override
            public int compare(TreeItem o1, TreeItem o2) {
                if(o1.getClass() == o2.getClass()) { //sort items of the same class by value
                    String s1 = (String) o1.getValue();
                    String s2 = (String) o2.getValue();
                    return s1.compareToIgnoreCase(s2);
                }
                //directories must appear first
                if(o1 instanceof SipContentDirectory)
                    return -1;
                return 1;
            }
        };
    }

    @Override
    public String getPath() {
        return this.fullPath;
    }

    @Override
    public SourceTreeItemState getState(){
        return SourceTreeItemState.NORMAL;
    }

    @Override
    public void ignore(){
    }

    @Override
    public void map(String s){
    }

    @Override
    public void unignore(){
    }

    @Override
    public void unmap(String s){

    }
}
