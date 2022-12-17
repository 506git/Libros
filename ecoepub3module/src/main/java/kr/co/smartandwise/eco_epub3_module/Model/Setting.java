package kr.co.smartandwise.eco_epub3_module.Model;

public class Setting {
    private String theme;
    private String fontFace;
    private String fontSize;

    public Setting(String theme, String fontFace, String fontSize) {
        if ("".equals(theme)) {
            theme = "default-theme";
        }
        if ("".equals(fontFace)) {
            fontFace = "normal";
        }
        if ("".equals(fontSize)) {
            fontSize = "100";
        }
        this.theme = theme;
        this.fontFace = fontFace;
        this.fontSize = fontSize;
    }

    public void setTheme(String theme) {
        if ("".equals(theme)) {
            theme = "default-theme";
        }
        this.theme = theme;
    }
    public void setFontFace(String fontFace) {
        if ("".equals(fontFace)) {
            fontFace = "normal";
        }
        this.fontFace = fontFace;
    }
    public void setFontSize(String fontSize) {
        if ("".equals(fontSize)) {
            fontSize = "100";
        }
        this.fontSize = fontSize;
    }

    public String getTheme() {
        if ("".equals(this.theme)) {
            this.theme = "default-theme";
        }
        return this.theme;
    }

    public String getFontFace() {
        if ("".equals(this.fontFace)) {
            this.fontFace = "normal";
        }
        return fontFace;
    }
    public String getFontSize() {
        if ("". equals(this.fontSize)) {
            this.fontSize = "100";
        }
        return this.fontSize;
    }

    @Override
    public String toString() {
        return String.format("{'theme':'%s', 'fontFace':'%s', 'fontSize':'%s'}", this.theme, this.fontFace, this.fontSize);
    }
}