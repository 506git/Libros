package kr.co.smartandwise.eco_epub3_module.Model;

import java.util.ArrayList;

public class EpubViewerParam {
    private String contentId = "";
    private String bookRootPath = "";
    private String lastReadPageIdref = "";
    private String lastReadPageCfi = "";
    private String pagingInfo = "";
    private ArrayList<Pagination> paginationCacheList = null;
    private ArrayList<Bookmark> bookmarkList = null;
    private ArrayList<Highlight> highlightList = null;
    private Setting setting = null;

    public EpubViewerParam() {

    }

    public EpubViewerParam(String bookRootPath) {
        this.bookRootPath = bookRootPath;
    }

    public EpubViewerParam(String contentId, String bookRootPath) {
        this.contentId = contentId;
        this.bookRootPath = bookRootPath;
    }

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public String getBookRootPath() {
        return bookRootPath;
    }

    public void setBookRootPath(String bookRootPath) {
        this.bookRootPath = bookRootPath;
    }

    public String getLastReadPageIdref() {
        return lastReadPageIdref;
    }

    public void setLastReadPageIdref(String lastReadPageIdref) {
        this.lastReadPageIdref = lastReadPageIdref;
    }

    public String getLastReadPageCfi() {
        return lastReadPageCfi;
    }

    public void setLastReadPageCfi(String lastReadPageCfi) {
        this.lastReadPageCfi = lastReadPageCfi;
    }

    public String getPagingInfo() {
        return pagingInfo;
    }

    public void setPagingInfo(String pagingInfo) {
        this.pagingInfo = pagingInfo;
    }

    public ArrayList<Pagination> getPaginationCacheList() {
        if (paginationCacheList == null) {
            paginationCacheList = new ArrayList<Pagination>();
        }
        return paginationCacheList;
    }

    public void setPaginationCacheList(ArrayList<Pagination> paginationCacheList) {
        this.paginationCacheList = paginationCacheList;
    }

    public ArrayList<Bookmark> getBookmarkList() {
        if (bookmarkList == null) {
            bookmarkList = new ArrayList<Bookmark>();
        }
        return bookmarkList;
    }

    public void setBookmarkList(ArrayList<Bookmark> bookmarkList) {
        this.bookmarkList = bookmarkList;
    }

    public ArrayList<Highlight> getHighlightList() {
        if (highlightList == null) {
            highlightList = new ArrayList<Highlight>();
        }
        return highlightList;
    }

    public void setHighlightList(ArrayList<Highlight> highlightList) {
        this.highlightList = highlightList;
    }

    public Setting getSetting() {
        if (this.setting == null) {
            this.setting = new Setting("", "", "");
        }
        return setting;
    }

    public void setSetting(Setting setting) {
        this.setting = setting;
    }
}
