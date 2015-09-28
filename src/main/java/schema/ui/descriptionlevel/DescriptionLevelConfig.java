package schema.ui.descriptionlevel;

/**
 * Created by adrapereira on 22-09-2015.
 */
public class DescriptionLevelConfig {
    private String title;
    private String abbreviation;
    private int fontSize;
    private String fontColor;
    private String backgroundColor;
    private int width;

    public DescriptionLevelConfig() {
    }

    public DescriptionLevelConfig(String title, String abbreviation, int fontSize, String fontColor, String backgroundColor, int width) {
        this.title = title;
        this.abbreviation = abbreviation;
        this.fontSize = fontSize;
        this.fontColor = fontColor;
        this.backgroundColor = backgroundColor;
        this.width = width;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public String getFontColor() {
        return fontColor;
    }

    public void setFontColor(String fontColor) {
        this.fontColor = fontColor;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }
}
