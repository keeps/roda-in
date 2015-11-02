package org.roda.rodain.schema.ui;

import java.io.InputStream;

import javafx.geometry.VPos;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 22-09-2015.
 */
public class DescriptionLevelImageCreator {
    private String unicode;
    public static final double SIZE = 16;

    public DescriptionLevelImageCreator(String unicode) {
        this.unicode = unicode;
    }

    /*
    * http://news.kynosarges.org/2014/01/07/javafx-text-icons-as-images-files/
    */
    public Image generate(){
        InputStream fontIS = getClass().getResourceAsStream("/fontawesome-webfont.ttf");
        Font font = Font.loadFont(fontIS, 16);

        final Canvas canvas = new Canvas(SIZE, SIZE);
        final GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFont(font);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(unicode, SIZE / 2, SIZE / 2);

        final SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        return canvas.snapshot(params, null);
    }
}
