package kr.co.smartandwise.eco_epub3_module.Model;

public class Book {

    private String contentId = "";
    private String bookName = "";
    private String rootPath = "";
    private String bookData = "";

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public String getBookData() {
        return bookData;
    }

    public void setBookData(String bookData) {
        this.bookData = bookData;
    }
}
