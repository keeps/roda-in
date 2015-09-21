package SourceUI.Items;

import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.nio.file.Path;

/**
 * Created by adrap on 17-09-2015.
 */
public class SourceTreeFile extends TreeItem<String> {
    public static Image fileImage = new Image(ClassLoader.getSystemResourceAsStream("text-x-generic.png"));

    //this stores the full path to the file
    private String fullPath;

    public String getFullPath() {
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
