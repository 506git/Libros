package kr.co.smartandwise.eco_epub3_module.Callback;

import android.webkit.JavascriptInterface;

import kr.co.smartandwise.eco_epub3_module.View.BaseEpubWebView;

public abstract class EpubViewerCallback {

    protected BaseEpubWebView caller;

    protected EpubViewerCallback() {

    }

    protected  EpubViewerCallback(BaseEpubWebView caller) {
        this.caller = caller;
    }

    @JavascriptInterface
    public abstract void logJavascript(String message);

    @JavascriptInterface
    public abstract void updateOpenBookData(String openBookData);

    @JavascriptInterface
    public abstract void onReadyToLoadEpub();

    @JavascriptInterface
    public abstract void onTocLoaded(String tocJson);

    @JavascriptInterface
    public abstract void onSpineLoaded();

    @JavascriptInterface
    public abstract void onPageMoved(final int pageNumber, final String pageIdref, final String pageCfi);

    @JavascriptInterface
    public abstract void onPaginationDone(final int totalPage, final String jsonSpineItems);

    @JavascriptInterface
    public abstract void onContentSelected(final String idref, final String cfi, final String selectedElementText);

    @JavascriptInterface
    public abstract void onBookmarkChecked(final boolean isBookmarkExist, final String idref, final String cfi);

    @JavascriptInterface
    public abstract void onBookmarkAdded(final String idref, final String cfi, final String firstElementText);

    @JavascriptInterface
    public abstract void onHighlightAdded(final String idref, final String cfi);

    @JavascriptInterface
    public abstract void onHighlightClicked(final String idref, final String cfi, final int highlightId);

    @JavascriptInterface
    public abstract void onShowPrevPageButton();

    @JavascriptInterface
    public abstract void onHidePrevPageButton();

    @JavascriptInterface
    public abstract void onShowNextPageButton();

    @JavascriptInterface
    public abstract void onHideNextPageButton();
    
    @JavascriptInterface
    public abstract void prepareSpine(final String idref);
    
    @JavascriptInterface
    public abstract void updateSearchResult(final String result);

    @JavascriptInterface
    public abstract int getAndroidVersion();

    @JavascriptInterface
    public abstract String getSyntheticSpread();

    @JavascriptInterface
    public abstract void onSearchDone();

    @JavascriptInterface
    public abstract void onLeftPageCurl();

    @JavascriptInterface
    public abstract void onRightPageCurl();
}