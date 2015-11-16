package org.roda.rodain.utils;

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
public class FontAwesomeImageCreator {
    public static final double SIZE = 16;
    public static final String chevron_right = "\uf054";
    public static final String chevron_left = "\uf053";
    public static final String times = "\uf00d";

    // pre-generate these images because we can't snapshot when in a modal window
    public static final Image im_chevron_right = generate(chevron_right);
    public static final Image im_chevron_left = generate(chevron_left);
    public static final Image im_times = generate(times);

    public static Font font = null;

    private FontAwesomeImageCreator(){

    }

    /*
    * http://news.kynosarges.org/2014/01/07/javafx-text-icons-as-images-files/
    */
    public static Image generate(String unicode){
        // load the font if it hasn't been loaded yet
        if(font == null) {
            InputStream fontIS = FontAwesomeImageCreator.class.getResourceAsStream("/fonts/fontawesome-webfont.ttf");
            font = Font.loadFont(fontIS, 16);
        }

        final Canvas canvas = new Canvas(SIZE, SIZE);
        final GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFont(font);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(unicode, SIZE / 2, SIZE / 2);

        final SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        Image res = canvas.snapshot(params, null);
        return res;
    }
}
