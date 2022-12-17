package kr.co.smartandwise.eco_epub3_module.Callback;

import android.content.res.Configuration;
import android.os.Build;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import kr.co.smartandwise.eco_epub3_module.Util.UnitUtil;
import kr.co.smartandwise.eco_epub3_module.View.BaseEpubWebView;

public class EpubViewerDefaultCallback extends EpubViewerCallback {

    private BaseEpubWebView caller;
    public EpubViewerDefaultCallback(BaseEpubWebView caller) {
        this.caller = caller;
    }
    @Override
    public void logJavascript(final String message) {

    }

    @Override
    @JavascriptInterface
    public void updateOpenBookData(final String openBookData) {

    }

    @Override
    @JavascriptInterface
    public void onReadyToLoadEpub() {

    }

    @Override
    @JavascriptInterface
    public void onTocLoaded(final String tocJson) {

    }

    @Override
    @JavascriptInterface
    public void onSpineLoaded() {

    }

    @Override
    @JavascriptInterface
    public void onPageMoved(final int pageNumber, final String pageIdref, final String pageCfi) {

    }

    @Override
    @JavascriptInterface
    public void onContentSelected(final String idref, final String cfi, final String selectedElementText) {

    }

    @Override
    @JavascriptInterface
    public void onBookmarkChecked(final boolean isBookmarkExist, final String idref, final String cfi) {

    }

    @Override
    @JavascriptInterface
    public void onPaginationDone(final int totalPage, final String jsonSpineItems) {

    }

    @Override
    @JavascriptInterface
    public void onBookmarkAdded(final String idref, final String cfi, final String firstElementText) {

    }

    @Override
    @JavascriptInterface
    public void onHighlightAdded(final String idref, final String cfi) {

    }

    @Override
    @JavascriptInterface
    public void onHighlightClicked(final String idref, final String cfi, final int highlightId) {

    }

    @Override
    @JavascriptInterface
    public void onShowPrevPageButton() {

    }

    @Override
    @JavascriptInterface
    public void onHidePrevPageButton() {

    }

    @Override
    @JavascriptInterface
    public void onShowNextPageButton() {

    }

    @Override
    @JavascriptInterface
    public void onHideNextPageButton() {

    }
    
    @Override
    @JavascriptInterface
    public void prepareSpine(final String idref) {

    }
    
    @Override
    @JavascriptInterface
    public void updateSearchResult(final String result) {

    }

    @Override
    @JavascriptInterface
    public int getAndroidVersion() {
        return Build.VERSION.SDK_INT;
    }

    @Override
    @JavascriptInterface
    public String getSyntheticSpread() {
        if (UnitUtil.getSmallestWidth(caller.getContext()) >= 600 && caller.getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return "double";
        } else {
            return "single";
        }
    }

    @Override
    @JavascriptInterface
    public void onSearchDone() {
        Toast.makeText(caller.getContext(), "Search Done", Toast.LENGTH_SHORT).show();
    }

    @Override
    @JavascriptInterface
    public void onLeftPageCurl() {

    }

    @Override
    @JavascriptInterface
    public void onRightPageCurl() {

    }
}
