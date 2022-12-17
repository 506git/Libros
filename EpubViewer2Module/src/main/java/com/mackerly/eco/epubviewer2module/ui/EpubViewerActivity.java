package com.mackerly.eco.epubviewer2module.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.RequiresApi;
import android.util.Base64;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.lemonsol.toolbox.utils.AppUtils;
import com.lemonsol.toolbox.utils.StringUtils;
import com.mackerly.eco.epubviewer2module.R;
import com.mackerly.eco.epubviewer2module.components.ResourceCallback;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EpubViewerActivity extends BaseActivity {
    public static final String INTENT_BOOK_ID = "INTENT_BOOK_ID";
    public static final String INTENT_BOOK_KEY = "INTENT_BOOK_KEY";
    public static final String INTENT_EPUB_PATH = "INTENT_EPUB_PATH";
    public static final String INTENT_USER_ID = "INTENT_USER_ID";
   // public static final String INTENT_USER_KEY = "INTENT_USER_KEY";
    public static final String INTENT_API_URL = "INTENT_API_URL";
    public static final String INTENT_CHROME_YN = "INTENT_CHROME_YN";

    private static final String WEB_VIEW_CALLBACK_NAME = "App";

    private static final long FINISH_VIEWER_INTERVAL = 3000;

    private WebView mWebView;

    private String mBookId;
    private String chromeYn;
    private long mLastBackPressedTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_epub_viewer);

        initializeViews();

        Intent intent = getIntent();
        mBookId = intent.getStringExtra(INTENT_BOOK_ID);
        String bookKey = intent.getStringExtra(INTENT_BOOK_KEY);
        String epubPath = intent.getStringExtra(INTENT_EPUB_PATH);
        String userId = intent.getStringExtra(INTENT_USER_ID);
        //String userKey = intent.getStringExtra(INTENT_USER_KEY);
        String apiUrl = intent.getStringExtra(INTENT_API_URL);
        chromeYn = intent.getStringExtra(INTENT_CHROME_YN);
        openBook(epubPath, mBookId, bookKey, userId, apiUrl);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mWebView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWebView.onResume();

        // 태블릿이 아닌 휴대폰으로 간주될 경우 세로 고정
        if (getResources().getConfiguration().smallestScreenWidthDp < 600) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWebView.destroy();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initializeViews() {
        mWebView = findViewById(R.id.web_view);

        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);

        mWebView.addJavascriptInterface(new WebViewCallback(), WEB_VIEW_CALLBACK_NAME);
        mWebView.setWebChromeClient(new WebChromeClient());
        mWebView.setWebViewClient(new WebViewClient() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                boolean redirected = super.shouldOverrideUrlLoading(view, request);

                if (!redirected) {
                    redirected = shouldOverrideUrlLoading(request.getUrl().toString());
                }
                return redirected;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                boolean redirected = super.shouldOverrideUrlLoading(view, url);
                if (!redirected) {
                    redirected = shouldOverrideUrlLoading(url);
                }
                return redirected;
            }

            private boolean shouldOverrideUrlLoading(String url) {
                if (url != null && URLUtil.isHttpUrl(url) || URLUtil.isHttpsUrl(url)) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return true;
                } else {
                    return false;
                }
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                InputStream stream = getResourceInputStream(url);

                if (stream != null) {
                    return new WebResourceResponse(null, null, stream);
                } else {
                    return super.shouldInterceptRequest(view, request);
                }
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                InputStream stream = getResourceInputStream(url);

                if (stream != null) {
                    return new WebResourceResponse(null, null, stream);
                } else {
                    return super.shouldInterceptRequest(view, url);
                }
            }

            private InputStream getResourceInputStream(String url) {
                final String ANDROID_ASSET = "file:///android_asset/";

                if (url.startsWith(ANDROID_ASSET)) {
                    url = url.replaceFirst(ANDROID_ASSET, "");
                    try {
                        AssetManager assets = getAssets();
                        Uri uri = Uri.parse(url);
                        return assets.open(uri.getPath(), AssetManager.ACCESS_STREAMING);
                    } catch (IOException e) {
                    }
                } else if (ResourceCallback.getInstance().getHandler() != null) {
                    // TODO; replace hard coded fileType
                    byte[] data = ResourceCallback.getInstance().getHandler().handleResource(mBookId, "EPUB", url);
                    if (data != null) {
                        return new ByteArrayInputStream(data);
                    }
                } else {
                    File file = new File(url);
                    try {
                        return new FileInputStream(file);
                    } catch (FileNotFoundException e) {
                        return null;
                    }
                }
                return null;
            }
        });
    }

    @Override
    public void onBackPressed() {
        long now = System.currentTimeMillis();
        if (now - mLastBackPressedTime > FINISH_VIEWER_INTERVAL) {
            mLastBackPressedTime = now;
            AppUtils.toast(R.string.toast_finish_viewer);
        } else {
            super.onBackPressed();
            //웹 브라우저 실행 시, 두번 선택하면 앱 종료되도록 설정
            if(chromeYn.equalsIgnoreCase("Y")){
                finishAffinity();
                moveTaskToBack(false);
                finish();
                android.os.Process.killProcess(android.os.Process.myPid());
            }else{

            }
        }
    }

    private void openBook(String epubPath, String bookId, String bookKey, String userId, String apiUrl) {
        String viewerUrl = StringUtils.getString(R.string.epub_viewer_host);
        String parameters = StringUtils.format("epub=%s&ebookId=%s&bookKey=%s&apiUrl=%s&userId=%s", epubPath, bookId, bookKey, apiUrl, userId);
        try {
            String key = StringUtils.getString(R.string.viewer_crypto_key); // 128 bit key
            String initVector = StringUtils.getString(R.string.viewer_crypto_iv); // 16 bytes IV

            parameters = encrypt(key, initVector, parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mWebView.loadUrl(StringUtils.format("%s?%s", viewerUrl, parameters));
    }

    public static String encrypt(String key, String initVector, String value) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, iv);

            byte[] encrypted = cipher.doFinal(value.getBytes());
            return Base64.encodeToString(encrypted, Base64.DEFAULT);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    @SuppressWarnings("unused")
    public class WebViewCallback {
        @JavascriptInterface
        public void exit() {
            // 앱 브라우저로 접속한 경우 종료 처리
            if(chromeYn.equalsIgnoreCase("Y")){
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(EpubViewerActivity.this);
                alertDialog.setTitle("알림");
                alertDialog.setMessage("외부 브라우저로 접속하셨습니다.\n앱을 종료하시겠습니까?");

                alertDialog.setPositiveButton("확인", new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        moveTaskToBack(true);
                        finish();
                        android.os.Process.killProcess(android.os.Process.myPid());
                    }
                });
                alertDialog.setNegativeButton("취소", new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alertDialog.create();
                alertDialog.show();
            }else if(chromeYn.equalsIgnoreCase("N")){
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(EpubViewerActivity.this);
                alertDialog.setTitle("알림");
                alertDialog.setMessage("뷰어가 종료됩니다.\n뷰어를 종료하시겠습니까?");

                alertDialog.setPositiveButton("확인", new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });
                alertDialog.setNegativeButton("취소", new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                alertDialog.create();
                alertDialog.show();
            }
        }
    }
}
