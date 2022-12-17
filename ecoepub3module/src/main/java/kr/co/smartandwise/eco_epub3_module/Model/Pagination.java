package kr.co.smartandwise.eco_epub3_module.Model;

public class Pagination {

    private String fontSize = "";
    private int totalPage = 0;
    private String jsonSpineItems = "";
    private boolean isPortrait = true;

    public String getFontSize() {
        return fontSize;
    }

    public void setFontSize(String fontSize) {
        this.fontSize = fontSize;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public String getJsonSpineItems() {
        return jsonSpineItems;
    }

    public void setJsonSpineItems(String jsonSpineItems) {
        this.jsonSpineItems = jsonSpineItems;
    }

    public boolean isPortrait() {
        return isPortrait;
    }

    public void setIsPortrait(boolean isPortrait) {
        this.isPortrait = isPortrait;
    }
}
