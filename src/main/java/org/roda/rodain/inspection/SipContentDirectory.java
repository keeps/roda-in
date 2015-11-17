package org.roda.rodain.inspection;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import org.roda.rodain.rules.TreeNode;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 17-09-2015.
 */
public class SipContentDirectory extends TreeItem<Object> implements InspectionTreeItem {
    public static final Image folderCollapseImage = new Image(ClassLoader.getSystemResourceAsStream("icons/folder.png"));
    public static final Image folderExpandImage = new Image(ClassLoader.getSystemResourceAsStream("icons/folder-open.png"));
    private static final Comparator comparator = createComparator();
    //this stores the full path to the file or directory
    private Path fullPath;
    private TreeNode treeNode;
    private TreeItem parent;

    public SipContentDirectory(TreeNode treeNode, TreeItem parent) {
        super(treeNode.getPath());
        this.treeNode = treeNode;
        this.fullPath = treeNode.getPath();
        this.parent = parent;
        this.setGraphic(new ImageView(folderCollapseImage));

        Path name = fullPath.getFileName();
        if (name != null) {
            this.setValue(name.toString());
        } else {
            this.setValue(fullPath.toString());
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

    public TreeNode getTreeNode(){
        return treeNode;
    }

    @Override
    public TreeItem getParentDir() {
        return parent;
    }

    public void skip(){
        SipContentDirectory parent = (SipContentDirectory)getParent();
        TreeNode parentTreeNode = parent.getTreeNode();
        parentTreeNode.remove(treeNode.getPath()); //remove this treeNode from the parent
        parentTreeNode.addAll(treeNode.getAllFiles()); // add this treeNode's children to this parent
    }

    public void flatten(){
        treeNode.flatten();
        getChildren().clear();
        for(String path: treeNode.getKeys()){
            SipContentFile file = new SipContentFile(Paths.get(path), this);
            getChildren().add(file);
        }
        sortChildren();
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
    public Path getPath() {
        return this.fullPath;
    }
}
