package WithHandlers;

import Source.SourceDirectory;
import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by adrap on 17-09-2015.
 */
public class SourceTreeDirectory extends TreeItem<String> {
    public static Image folderCollapseImage = new Image(ClassLoader.getSystemResourceAsStream("folder.png"));
    public SourceDirectory directory;
    public boolean expanded = false;
    //this stores the full path to the file or directory
    private String fullPath;

    public String getFullPath() {
        return (this.fullPath);
    }

    public SourceTreeDirectory(Path file, SourceDirectory directory) {
        super(file.toString());
        this.directory = directory;
        this.fullPath = file.toString();
        this.setGraphic(new ImageView(folderCollapseImage));

        this.getChildren().add(new SourceTreeLoading());

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

        this.addEventHandler(TreeItem.branchExpandedEvent(), new ExpandedEventHandler());

        this.addEventHandler(TreeItem.branchCollapsedEvent(), new EventHandler<TreeModificationEvent<Object>>() {
            public void handle(TreeItem.TreeModificationEvent<Object> e) {
                SourceTreeDirectory source = (SourceTreeDirectory) e.getSource();
                if (!source.isExpanded()) {
                    ImageView iv = (ImageView) source.getGraphic();
                    iv.setImage(folderCollapseImage);
                    source.expanded = false;
                }
            }
        });
    }

    public void loadMore(){
        int loaded = directory.loadMore();
        System.out.println("Loaded More: " + loaded);

        if(loaded != 0) {
            this.getChildren().clear();
            for (String sourceItem : directory.getChildren().keySet()) {
                Path sourcePath = Paths.get(sourceItem);
                if (Files.isDirectory(sourcePath)) {
                    this.getChildren().add(new SourceTreeDirectory(sourcePath, directory.getChildDirectory(sourcePath)));
                } else this.getChildren().add(new SourceTreeFile(sourcePath));
            }
            // check if there's more files to load
            if(directory.isStreamOpen())
                this.getChildren().add(new SourceTreeLoadMore());
        }
    }

}
