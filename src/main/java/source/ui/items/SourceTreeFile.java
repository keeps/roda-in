package source.ui.items;

import java.io.File;
import java.nio.file.Path;
import java.util.logging.Logger;

import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Created by adrapereira on 17-09-2015.
 */
public class SourceTreeFile extends TreeItem<Object> implements SourceTreeItem{
    private static final Logger log = Logger.getLogger(SourceTreeFile.class.getName());
    public static Image fileImage = new Image(ClassLoader.getSystemResourceAsStream("icons/file.png"));

    //this stores the full path to the file
    private String fullPath;

    public String getPath() {
        return (this.fullPath);
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
    }
}
