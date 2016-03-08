package org.roda.rodain.utils;

import javafx.geometry.VPos;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.io.InputStream;

/**
 * @author Andre Pereira apereira@keep.pt
 * @since 22-09-2015.
 */
public class FontAwesomeImageCreator {
  public static final int SIZE = 16;
  public static final String chevron_right = "\uf054";
  public static final String chevron_left = "\uf053";
  public static final String times = "\uf00d";
  public static final String code = "\uf121";
  public static final String list = "\uf022";
  public static final String check = "\uf00c";
  public static final String square = "\uf096";

  public static final Font font = loadFont();

  private FontAwesomeImageCreator() {

  }

  private static Font loadFont() {
    InputStream fontIS = FontAwesomeImageCreator.class.getResourceAsStream("/fonts/fontawesome-webfont.ttf");
    return Font.loadFont(fontIS, 16);
  }

  /**
   * Converts an unicode char to an Image using the FontAwesome font with the
   * color Black.
   *
   * @param unicode
   *          The String with the unicode which will be turned into an image.
   * @return An Image with the unicode char converted to an image.
   * @see #generate(String, Paint)
   */
  public static Image generate(String unicode) {
    return generate(unicode, Color.BLACK);
  }

  /**
   * Converts an unicode char to an Image using the FontAwesome font.
   * <p/>
   * <p>
   * Based on the code presented here: <a href=
   * "http://news.kynosarges.org/2014/01/07/javafx-text-icons-as-images-files/">
   * javafx-text-icons-as-images-files </a>
   * </p>
   *
   * @param unicode
   *          The String with the unicode which will be turned into an image.
   * @param color
   *          The color of the font.
   * @return An Image with the unicode char converted to an image.
   */
  public static Image generate(String unicode, Paint color) {
    return generate(unicode, color, SIZE);
  }

  public static Image generate(String unicode, Paint color, int size) {
    Font newFont;
    if (size == SIZE) {
      newFont = font;
    } else {
      InputStream fontIS = FontAwesomeImageCreator.class.getResourceAsStream("/fonts/fontawesome-webfont.ttf");
      newFont = Font.loadFont(fontIS, size);
    }
    final Canvas canvas = new Canvas(size, size);
    final GraphicsContext gc = canvas.getGraphicsContext2D();
    gc.setFill(color);
    gc.setFont(newFont);
    gc.setTextAlign(TextAlignment.CENTER);
    gc.setTextBaseline(VPos.CENTER);
    gc.fillText(unicode, size / 2, size / 2);

    final SnapshotParameters params = new SnapshotParameters();
    params.setFill(Color.TRANSPARENT);
    return canvas.snapshot(params, null);
  }
}
