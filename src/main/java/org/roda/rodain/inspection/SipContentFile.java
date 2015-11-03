package org.roda.rodain.inspection;

import java.nio.file.Path;

import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 17-09-2015.
 */
public class SipContentFile extends TreeItem<Object> implements InspectionTreeItem {
    public static final Image fileImage = new Image(ClassLoader.getSystemResourceAsStream("icons/file.png"));

    //this stores the full path to the file
    private Path fullPath;

    public SipContentFile(Path file) {
        super(file.toString());
        this.fullPath = file;
        this.setGraphic(new ImageView(fileImage));

        Path name = fullPath.getFileName();
        if (name != null) {
            this.setValue(name.toString());
        } else {
            this.setValue(fullPath.toString());
        }
    }

    @Override
    public Path getPath() {
        return this.fullPath;
    }
}
