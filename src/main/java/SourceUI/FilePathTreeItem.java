package SourceUI;

import WithHandlers.ExpandedEventHandler;
import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by adrap on 16-09-2015.
 */
public class FilePathTreeItem extends TreeItem<String> {
    public static Image folderCollapseImage = new Image(ClassLoader.getSystemResourceAsStream("folder.png"));
    public static Image folderExpandImage = new Image(ClassLoader.getSystemResourceAsStream("folder-open.png"));
    public static Image fileImage = new Image(ClassLoader.getSystemResourceAsStream("text-x-generic.png"));

    //this stores the full path to the file or directory
    private String fullPath;

    public String getFullPath() {
        return (this.fullPath);
    }

    private boolean isDirectory;

    public boolean isDirectory() {
        return (this.isDirectory);
    }

    public FilePathTreeItem(Path file) {
        super(file.toString());
        this.fullPath = file.toString();

        //test if this is a directory and set the icon
        if (Files.isDirectory(file)) {
            this.isDirectory = true;
            this.setGraphic(new ImageView(folderCollapseImage));
        } else {
            this.isDirectory = false;
            this.setGraphic(new ImageView(fileImage));
        }

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
            public void handle(TreeModificationEvent<Object> e) {
                FilePathTreeItem source = (FilePathTreeItem) e.getSource();
                if (source.isDirectory() && !source.isExpanded()) {
                    ImageView iv = (ImageView) source.getGraphic();
                    iv.setImage(folderCollapseImage);
                }
            }
        });

        this.addEventHandler(TreeItem.branchCollapsedEvent(), new EventHandler<TreeModificationEvent<Object>>() {
            public void handle(TreeModificationEvent<Object> e) {
                FilePathTreeItem source = (FilePathTreeItem) e.getSource();
                if (source.isDirectory() && source.isExpanded()) {
                    ImageView iv = (ImageView) source.getGraphic();
                    iv.setImage(FilePathTreeItem.folderExpandImage);
                }
            }
        });
    }
}
