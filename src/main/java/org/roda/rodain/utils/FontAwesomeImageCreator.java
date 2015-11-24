package org.roda.rodain.utils;

import java.io.InputStream;

import javafx.geometry.VPos;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
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
    // white
    public static final Image im_w_chevron_right = generate(chevron_right, Color.WHITE);
    public static final Image im_w_chevron_left = generate(chevron_left, Color.WHITE);
    public static final Image im_w_times = generate(times, Color.WHITE);
    // black
    public static final Image im_b_chevron_right = generate(chevron_right, Color.BLACK);
    public static final Image im_b_chevron_left = generate(chevron_left, Color.BLACK);
    public static final Image im_b_times = generate(times, Color.BLACK);

    public static final Font font = loadFont();

    private FontAwesomeImageCreator(){

    }

    private static Font loadFont(){
        InputStream fontIS = FontAwesomeImageCreator.class.getResourceAsStream("/fonts/fontawesome-webfont.ttf");
        Font fontLoaded = Font.loadFont(fontIS, 16);
        return fontLoaded;
    }

    public static Image generate(String unicode) {
        return generate(unicode, Color.BLACK);
    }
    /*
    * http://news.kynosarges.org/2014/01/07/javafx-text-icons-as-images-files/
    */
    public static Image generate(String unicode, Paint color){
        final Canvas canvas = new Canvas(SIZE, SIZE);
        final GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(color);
        gc.setFont(font);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(unicode, SIZE / 2, SIZE / 2);

        final SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        return canvas.snapshot(params, null);
    }
}
