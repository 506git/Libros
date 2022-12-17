package kr.co.smartandwise.eco_epub3_module.View;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import kr.co.smartandwise.eco_epub3_module.Callback.EpubViewerCallback;
import kr.co.smartandwise.eco_epub3_module.Callback.EpubViewerDefaultCallback;
import kr.co.smartandwise.eco_epub3_module.Model.Setting;

public abstract class BaseEpubWebView extends WebView {
    protected final Context context;
    protected boolean isPaginationNeeded = false;
    protected String bookRootUrl = null;
    protected String openBookData = null;

    protected ViewerState viewerState = ViewerState.NONE;
    protected PaginationState paginationState = PaginationState.NONE;

    protected BaseEpubWebView currentWebView = null;

    public enum ViewerState { NONE, OPENING_BOOK, OPENED_BOOK, LOADED_PAGE, LOADING_PAGE }
    public enum PaginationState { NONE, DOING, DONE, STOP }

    protected EpubViewerCallback epubViewerCallback;

    // constructors
    protected BaseEpubWebView(Context context) {
        super(context);
        this.context = context;
        this.init();
    }
    protected BaseEpubWebView(Context context, AttributeSet attr) {
        super(context, attr);
        this.context = context;
        this.init();
    }
    protected BaseEpubWebView(Context context, AttributeSet attr, int defStyle) {
        super(context, attr, defStyle);
        this.context = context;
        this.init();
    }

    protected void init() {
//        // TODO: for test
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            this.setWebContentsDebuggingEnabled(true);
//        }
        this.getSettings().setJavaScriptEnabled(true);
        this.getSettings().setAllowFileAccess(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            this.getSettings().setAllowFileAccessFromFileURLs(true);
            this.getSettings().setAllowContentAccess(true);
            this.getSettings().setAllowUniversalAccessFromFileURLs(true);
        }
        this.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        this.getSettings().setPluginState(WebSettings.PluginState.ON);

        this.setWebChromeClient(new WebChromeClient());
        this.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url != null && (url.startsWith("http://") || url.startsWith("https://") || url.startsWith("mailto:"))) {
                    view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return true;
                } else {
                    return false;
                }
            }
        });
        this.addJavascriptInterface(new EpubViewerDefaultCallback(this), "AndroidCallback");

        this.currentWebView = this;
    }

    public void destroy() {
        getSettings().setJavaScriptEnabled(false);
        stopLoading();
        clearCache(true);
        clearHistory();
        clearAnimation();
        clearFormData();

        currentWebView = null;
    }

    public abstract void startPagination();

    public void callJavascript(final String function) {
        this.post(new Runnable() {
            public void run() {
            	if (currentWebView != null) {
            		currentWebView.loadUrl("javascript:{ var result=" + function + " }");
            	}
            }
        });
    }

    public void openBook(String bookRootUrl) {
        if ("".equals(bookRootUrl) || bookRootUrl == null) {
            Log.d("error", "bookRootUrl must not be empty");
            return;
        }

        this.bookRootUrl = bookRootUrl;
        this.isPaginationNeeded = true;
        
        this.loadUrl("file:///android_asset/index.html?epub=" + this.bookRootUrl);
    }

    public void loadBook(String idref, String cfi) {
        this.callJavascript("ReadiumSDK.reader.trigger(ReadiumSDK.Events.LOAD_EPUB, '"+idref+"', '"+cfi+"')");
    }

    public void updateSpineItems(String jSpineItems) {
        if (this.viewerState != ViewerState.LOADING_PAGE) {
            this.callJavascript("ReadiumSDK.reader.trigger(ReadiumSDK.Native.Events.UPDATE_PAGINATION_CACHES, '" + jSpineItems + "')");
        }
    }
    
    public void loadSpine() {
    	this.callJavascript("ReadiumSDK.reader.trigger(ReadiumSDK.Native.Events.LOAD_SPINE)");
    }

    public void setViewerStyle(Setting setting) {
        if ("".equals(setting.getTheme()) || setting.getTheme() == null) {
            setting.setTheme("default-theme");
        }

        if ("".equals(setting.getFontFace()) || setting.getFontFace() == null) {
            setting.setFontFace("normal");
        }

        if ("".equals(setting.getFontSize()) || setting.getFontSize() == null) {
            setting.setFontSize("100");
        }

        this.callJavascript("ReadiumSDK.reader.updateSettings(" + setting.toString() + ")");
    }

    // public getters and setters
    public boolean isPaginationNeeded() {
        return isPaginationNeeded;
    }

    public void setPaginationNeeded(boolean isPaginationNeeded) {
        this.isPaginationNeeded = isPaginationNeeded;
    }

    public void setOpenBookData(String openBookData) {
        this.openBookData = openBookData;
    }

    public ViewerState getViewerState() {
        return viewerState;
    }

    public void setViewerState(ViewerState viewerState) {
        this.viewerState = viewerState;
    }

    public PaginationState getPaginationState() {
        return paginationState;
    }

    public void setPaginationState(PaginationState paginationState) {
        this.paginationState = paginationState;
    }

    public void setEpubViewerCallback(EpubViewerCallback AndroidCallback) {
        if (AndroidCallback != null) {
            this.epubViewerCallback = AndroidCallback;
            this.addJavascriptInterface(this.epubViewerCallback, "AndroidCallback");
        }
    }

    public EpubViewerCallback getEpubViewerCallback() {
        return epubViewerCallback;
    }
}
