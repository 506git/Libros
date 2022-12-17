package kr.co.smartandwise.eco_epub3_module.Activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import kr.co.smartandwise.eco_epub3_module.Callback.EpubViewerCallback;
import kr.co.smartandwise.eco_epub3_module.Drm.markany.FileManager;
import kr.co.smartandwise.eco_epub3_module.Model.Bookmark;
import kr.co.smartandwise.eco_epub3_module.Model.EpubViewerParam;
import kr.co.smartandwise.eco_epub3_module.Model.Highlight;
import kr.co.smartandwise.eco_epub3_module.Model.Pagination;
import kr.co.smartandwise.eco_epub3_module.Model.SearchResult;
import kr.co.smartandwise.eco_epub3_module.Model.TocItem;
import kr.co.smartandwise.eco_epub3_module.R;
import kr.co.smartandwise.eco_epub3_module.Util.EBookFileUtil;
import kr.co.smartandwise.eco_epub3_module.Util.EpubViewerParamUtil;
import kr.co.smartandwise.eco_epub3_module.Util.UnitUtil;
import kr.co.smartandwise.eco_epub3_module.View.BaseEpubWebView;
import kr.co.smartandwise.eco_epub3_module.View.EpubWebView;
import kr.co.smartandwise.eco_epub3_module.View.PaginationWebView;

/*import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;*/

public class EpubViewerActivity extends AppCompatActivity {
    private static final String STATE_BOOK_DATA = "STATE_BOOK_DATA";

    private static final int MAX_BRIGHTNESS_SCALE = 8;
    private static final int BRIGHTNESS_THRESHOLD = 80;

    private boolean isAutoHide = true;
    private static final boolean AUTO_HIDE = false;
    private static final int AUTO_HIDE_DELAY_MILLIS = 5000;
    private static final double TOUCH_MOVE_LENGTH = 400;
    private static final int TOUCH_DOWN_TERM = 500;

    private double mTouchLength;
    private double mTouchMoveLength;

    private Toast mToast;

/*    private SensorManager mSensorManager;
    private Sensor mLightSensor;
    private SensorEventListener mSensorListener;*/
    private int mBrightnessScale;

    private float TOUCH_DOWN_POS_X;
    private float TOUCH_DOWN_POS_Y;
    private float TOUCH_MOVE_POS_X;
    private float TOUCH_MOVE_POS_Y;
    private float TOUCH_UP_POS_X;
    private float TOUCH_UP_POS_Y;

    private int mWindowHeight = 0;

    String mCurrentPageIdref = "";
    String mCurrentPageCfi = "";
    int mCurrentPageNumber = 1;
    int mTotalPage = 1;

    int mControlsHeight;
    int mShortAnimTime;
    int minFontSize;
    int maxFontSize;
    boolean IS_VISIBLE = false;
    boolean isActionModeStarted = false;

    long mLastBackPressed = Long.MIN_VALUE;

    Handler mPageCurlHandler = new Handler();

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            setActionVisibility(false);
        }
    };

    Handler mMoveHandler = new Handler();
    Runnable mMoveRunnable = new Runnable() {
        @Override
        public void run() {
            if (mEpubWebView != null) {
                mEpubWebView.openPageCfi(mDeferedIdref, mDeferedCfi);
                isDefered = true;
            }
        }
    };

    Handler mActionBarHandler = new Handler();
    Runnable mActionBarRunnable = new Runnable() {
        @Override
        public void run() {
            mActionBarContainer.setVisibility(View.GONE);
            mActionBarContainer.setAlpha(1);
        }
    };

    Handler mHighlightHandler = new Handler();
    Runnable mHighlightRunnable = new Runnable() {
        @Override
        public void run() {
            if (mEpubViewerParam != null) {
                for (Highlight highlight : mEpubViewerParam.getHighlightList()) {
                    if (mEpubWebView != null) {
                        mEpubWebView.removeHighlight(highlight.getId());
                        mEpubWebView.addHighlight(highlight.getIdref(), highlight.getCfi(), highlight.getId(),
                                highlight.getColor());
                    }
                }
            }
        }
    };

    MenuItem mMenuItemBookmark = null;
    RelativeLayout mSettingView = null;
    FrameLayout mSettingOutLayout = null;
    boolean isHighlightViewShowInBottom = false;
    LinearLayout mHighlightView = null;
    ActionBar actionBar = null;
    View mActionBarContainer = null;
    LinearLayout mLayoutPageBar = null;
    SeekBar mSeekBarPager = null;
    TextView mTextViewPager = null;
    PaginationWebView mPaginationWebView = null;
    protected EpubWebView mEpubWebView = null;
    RelativeLayout mLayoutOpenPagePrev;
    RelativeLayout mLayoutOpenPageNext;
    //SeekBar mSeekBarBrightness = null;
    Spinner mSpinnerTheme;
    Spinner mSpinnerFontFace;
    Spinner mSpinnerFontSize;
    Button mButtonFontSizeUp = null;
    Button mButtonFontSizeDown = null;
    Button mButtonApply = null;
    LinearLayout mSearchContainer = null;
    EditText mEditTextSearchBox = null;
    ImageView mImageViewSearch = null;
    ListView mListViewSearchResult = null;

    boolean isDefered = true;
    boolean isTocLoadedFinish = false;
    boolean isBookmarkLoadedFinish = false;
    boolean isBookmarkProcessFinish = false;
    boolean isCurrentPageBookmarked = false;

    protected EpubViewerParam mEpubViewerParam = null;
    protected ArrayList<TocItem> tocItemList = null;

    String mDeferedIdref = "";
    String mDeferedCfi = "";
    String mBookmarkIdref = "";
    String mBookmarkCfi = "";

    int mHighlightId = -1;
    String mHighlightIdref = "";
    String mHighlightCfi = "";
    String mHighlightContent = "";
    String mHighlightColor = "";
    String mHighlightMemo = "";

    String mHighlightColorGreen = "";
    String mHighlightColorBlue = "";
    String mHighlightColorRed = "";
    String mHighlightColorPurple = "";
    String mHighlightColorYellow = "";

    EditText mEditTextMemo;
    ImageButton mImageButtonRemove;
    ImageButton mImageButtonSave;

    ImageButton mImageButtonGreen;
    ImageButton mImageButtonBlue;
    ImageButton mImageButtonRed;
    ImageButton mImageButtonPurple;
    ImageButton mImageButtonYellow;

    ImageView mImageViewLeftPageCurl;
    ImageView mImageViewRightPageCurl;

    ArrayAdapter<CharSequence> tsAdapter;
    ArrayAdapter<CharSequence> ffAdapter;
    ArrayAdapter<CharSequence> fsAdapter;
    SearchResultAdapter mSearchResultAdapter;

    String mCurrentKeyword = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_epub_viewer_3module);
        try {
            if (savedInstanceState != null) {
                String newBookData = savedInstanceState.getString(STATE_BOOK_DATA);
                if (newBookData == null) {
                    String bookData = getIntent().getExtras().getString("EPUB_BOOK_DATA");
                    mEpubViewerParam = EpubViewerParamUtil.createNewObjectFromBase64(bookData);
                } else {
                    mEpubViewerParam = EpubViewerParamUtil.createNewObjectFromBase64(newBookData);
                }
            } else {
                String bookData = getIntent().getExtras().getString("EPUB_BOOK_DATA");
                mEpubViewerParam = EpubViewerParamUtil.createNewObjectFromBase64(bookData);
            }
            mWindowHeight = getWindowManager().getDefaultDisplay().getHeight();

            mHighlightColorGreen = getResources().getString(R.string.highlight_green);
            mHighlightColorBlue = getResources().getString(R.string.highlight_blue);
            mHighlightColorRed = getResources().getString(R.string.highlight_red);
            mHighlightColorPurple = getResources().getString(R.string.highlight_purple);
            mHighlightColorYellow = getResources().getString(R.string.highlight_yellow);

            minFontSize = Integer.parseInt(getResources().getString(R.string.minimum_font_size));
            maxFontSize = Integer.parseInt(getResources().getString(R.string.maximum_font_size));

            actionBar = getSupportActionBar();
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.blue_theme)));
            actionBar.setTitle("");
            actionBar.hide();
            mActionBarContainer = findViewById(R.id.action_bar_container);

            mPaginationWebView = (PaginationWebView) findViewById(R.id.background_viewer);
            mPaginationWebView.setEpubViewerCallback(new ViewerCallback(mPaginationWebView));

            mEpubWebView = (EpubWebView) findViewById(R.id.viewer);
            mEpubWebView.setEpubViewerCallback(new ViewerCallback(mEpubWebView));
            mEpubWebView.setStartActionModeCallback(mStartActionModeCallback);
            mEpubWebView.setOnTouchListener(wvOnTouchListener);

            mSettingView = (RelativeLayout) findViewById(R.id.viewer_setting_layout);
            mSettingView.setOnTouchListener(otOnTouchListener);

            mSettingOutLayout = (FrameLayout) findViewById(R.id.setting_out_layout);
            mSettingOutLayout.setOnClickListener(soOnClickListener);

            mHighlightView = (LinearLayout) findViewById(R.id.viewer_highlight_layout);
            mHighlightView.setOnTouchListener(hvOnTouchListener);

            mImageButtonRemove = (ImageButton) findViewById(R.id.remove_highlight);
            mImageButtonRemove.setOnTouchListener(hvOnTouchListener);

            mImageButtonSave = (ImageButton) findViewById(R.id.save_highlight);
            mImageButtonSave.setOnTouchListener(hvOnTouchListener);

            mImageButtonGreen = (ImageButton) findViewById(R.id.color_green);
            mImageButtonGreen.setOnTouchListener(hvOnTouchListener);

            mImageButtonBlue = (ImageButton) findViewById(R.id.color_blue);
            mImageButtonBlue.setOnTouchListener(hvOnTouchListener);

            mImageButtonRed = (ImageButton) findViewById(R.id.color_red);
            mImageButtonRed.setOnTouchListener(hvOnTouchListener);

            mImageButtonPurple = (ImageButton) findViewById(R.id.color_purple);
            mImageButtonPurple.setOnTouchListener(hvOnTouchListener);

            mImageButtonYellow = (ImageButton) findViewById(R.id.color_yellow);
            mImageButtonYellow.setOnTouchListener(hvOnTouchListener);

            mEditTextMemo = (EditText) findViewById(R.id.highlight_memo);
            mEditTextMemo.setOnTouchListener(hvOnTouchListener);
            mEditTextMemo.setOnFocusChangeListener(hmOnFocusChangeListener);

            mLayoutPageBar = (LinearLayout) findViewById(R.id.view_pagebar);
            mLayoutPageBar.setBackgroundColor(getResources().getColor(R.color.blue_theme_background));
            mLayoutPageBar.setOnTouchListener(otOnTouchListener);

            mSeekBarPager = (SeekBar) findViewById(R.id.page_bar);
            mSeekBarPager.setOnSeekBarChangeListener(pgOnSeekBarChangeListener);
            mSeekBarPager.setEnabled(false);

            mTextViewPager = (TextView) findViewById(R.id.page_text);

            /*mSeekBarBrightness = (SeekBar) findViewById(R.id.brightness_bar);
            mSeekBarBrightness.setOnSeekBarChangeListener(brOnSeekBarChangeListener);
            int brightness = android.provider.Settings.System.getInt(getContentResolver(),
                    android.provider.Settings.System.SCREEN_BRIGHTNESS);
            brightness = Math.max(10, brightness);
            mSeekBarBrightness.setProgress(brightness);*/

            tsAdapter = ArrayAdapter.createFromResource(this, R.array.viewer_theme,
                    android.R.layout.simple_spinner_item);
            tsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            mSpinnerTheme = (Spinner) findViewById(R.id.viewer_theme);
            mSpinnerTheme.setAdapter(tsAdapter);
            mSpinnerTheme
                    .setSelection(tsAdapter.getPosition(getStringOfTheme(mEpubViewerParam.getSetting().getTheme())));

            ffAdapter = ArrayAdapter.createFromResource(this, R.array.font_family,
                    android.R.layout.simple_spinner_item);
            ffAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            mSpinnerFontFace = (Spinner) findViewById(R.id.viewer_font_face);
            mSpinnerFontFace.setAdapter(ffAdapter);
            mSpinnerFontFace.setSelection(
                    ffAdapter.getPosition(getStringOfFontFace(mEpubViewerParam.getSetting().getFontFace())));

            fsAdapter = ArrayAdapter.createFromResource(this, R.array.font_size, android.R.layout.simple_spinner_item);
            fsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            mSpinnerFontSize = (Spinner) findViewById(R.id.viewer_font_size);
            mSpinnerFontSize.setAdapter(fsAdapter);
            mSpinnerFontSize.setSelection(fsAdapter.getPosition(mEpubViewerParam.getSetting().getFontSize()));

            mButtonFontSizeUp = (Button) findViewById(R.id.font_size_up);
            mButtonFontSizeUp.setOnClickListener(fsUpOnClickListener);

            mButtonFontSizeDown = (Button) findViewById(R.id.font_size_down);
            mButtonFontSizeDown.setOnClickListener(fsDownOnClickListener);

            mButtonApply = (Button) findViewById(R.id.setting_apply);
            mButtonApply.setOnClickListener(saOnClickListener);

            mLayoutOpenPagePrev = (RelativeLayout) findViewById(R.id.page_prev);
            // mLayoutOpenPagePrev.setOnClickListener(oppOnClickListener);
            mLayoutOpenPagePrev.setOnTouchListener(opOnTouchListener);

            mLayoutOpenPageNext = (RelativeLayout) findViewById(R.id.page_next);
            // mLayoutOpenPageNext.setOnClickListener(opnOnClickListener);
            mLayoutOpenPageNext.setOnTouchListener(opOnTouchListener);

            mSearchContainer = (LinearLayout) findViewById(R.id.search_container);

            mEditTextSearchBox = (EditText) findViewById(R.id.search_box);
            mEditTextSearchBox.setOnKeyListener(searchBoxOnKeyListener);

            mImageViewSearch = (ImageView) findViewById(R.id.search);
            mImageViewSearch.setOnClickListener(searchOnClickListener);

            mListViewSearchResult = (ListView) findViewById(R.id.search_result);
            mListViewSearchResult.setOnItemClickListener(searchResultOnItemClickListener);

            mImageViewLeftPageCurl = (ImageView) findViewById(R.id.left_page_curl);
            mImageViewRightPageCurl = (ImageView) findViewById(R.id.right_page_curl);

            // Brightness
            /*mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

            mSensorListener = new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    if (!UnitUtil.isAutoBrightness(EpubViewerActivity.this)) {
                        return;
                    }

                    float lightQuantity = event.values[0];  // 0 ~ 60,000 (Galaxy S4 Active)

                    // scale sampling (0 ~ 400)
                    mBrightnessScale = (int) (lightQuantity / BRIGHTNESS_THRESHOLD);
//                    Log.d("Brightness", String.format("LightQuantity: %f, Scale: %d", lightQuantity, mBrightnessScale));
                    // brightness max scale is 8
                    mBrightnessScale = Math.min(mBrightnessScale, MAX_BRIGHTNESS_SCALE);
                    try {
                        int progress = mSeekBarBrightness.getProgress();
                        float brightness = progress * (((float) mBrightnessScale / MAX_BRIGHTNESS_SCALE) * 0.7f + 0.3f);

                        WindowManager.LayoutParams params = getWindow().getAttributes();
                        brightness = Math.max(brightness, 1);

                        params.screenBrightness = brightness / 255;
                        getWindow().setAttributes(params);
                        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, (int) brightness);
                    } catch (Exception e) {
                        onErrorActivityFinish(Log.getStackTraceString(e));
                    }
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }
            };*/

            if (mEpubWebView != null) {
                mEpubWebView.openBook(mEpubViewerParam.getBookRootPath());
            }
            if (mPaginationWebView != null) {
                mPaginationWebView.openBook(mEpubViewerParam.getBookRootPath());
            }
        } catch (Exception e) {
            onErrorActivityFinish(Log.getStackTraceString(e));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Runtime.getRuntime().gc();

        File fileDir = new File(EBookFileUtil.getEBookStoragePath(this), mEpubViewerParam.getContentId() + ".epub/" + mEpubViewerParam.getContentId());
        if (fileDir != null && fileDir.exists() == true) {
            FileManager.deleteFolder(fileDir);
        }
    }

    @Override
    protected void onStart() {
        Runtime.getRuntime().gc();
        super.onStart();
    }

    @Override
    protected void onStop() {
        Runtime.getRuntime().gc();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //mSensorManager.registerListener(mSensorListener, mLightSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();

        //mSensorManager.unregisterListener(mSensorListener);
    }

    @Override
    public void onBackPressed() {
        if (mSearchContainer.getVisibility() == View.VISIBLE) {
            mSearchContainer.setVisibility(View.GONE);
        } else if (mLastBackPressed > System.currentTimeMillis() - 3000) {
            super.onBackPressed();
        } else {
            mLastBackPressed = System.currentTimeMillis();
            showToast(getString(R.string.before_exit_viewer), Toast.LENGTH_SHORT);
        }
    }

    @Override
    public void onActionModeStarted(ActionMode mode) {
        Log.d("ActionBar", "onActionModeStarted");
        super.onActionModeStarted(mode);
        mActionBarContainer.setAlpha(1);
    }

    ActionMode.Callback mStartActionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            Log.d("ActionBar", "onCreateActionMode");
            isActionModeStarted = true;
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mHighlightView.getLayoutParams();

            if (TOUCH_DOWN_POS_Y > mWindowHeight / 2) {
                isHighlightViewShowInBottom = false;
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
            } else {
                isHighlightViewShowInBottom = true;
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            }

            mHighlightView.setLayoutParams(params);

            initHighlightView(-1, "", "", "", mHighlightColorGreen, "", View.VISIBLE);

            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            Log.d("ActionBar", "onPrepareActionMode");
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            Log.d("ActionBar", "onDestroyActionMode");
            isActionModeStarted = false;
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_epub_viewer, menu);
        mMenuItemBookmark = menu.findItem(R.id.action_bookmark);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
            int id = item.getItemId();
            if (id == R.id.action_bookmark) {
                if (isBookmarkLoadedFinish) {
                    if (isBookmarkProcessFinish) {
                        isBookmarkProcessFinish = false;
                        if (isCurrentPageBookmarked) {
                            int bookmarkId = -1;
                            for (int i = 0; i < mEpubViewerParam.getBookmarkList().size(); i++) {
                                Bookmark bookmark = mEpubViewerParam.getBookmarkList().get(i);
                                if (bookmark.getIdref().equals(mBookmarkIdref)
                                        && bookmark.getCfi().equals(mBookmarkCfi)) {
                                    bookmarkId = bookmark.getId();
                                    mEpubViewerParam.getBookmarkList().remove(i);
                                }
                            }

                            showToast(getResources().getString(R.string.toast_cancel_bookmark), Toast.LENGTH_SHORT);
                            setCurrentPageBookmarked(false);

                            onDataUpdate(mEpubViewerParam.getContentId(),
                                    EpubViewerParamUtil.createNewBase64FromObject(mEpubViewerParam));
                            onObserver_DeleteBookMark(getApplicationContext(), bookmarkId);
                        } else {
                            if (mEpubWebView != null) {
                                mEpubWebView.bookmarkCurrentPage();
                            }
                        }
                    }
                } else {
                    showToast(getResources().getString(R.string.toast_load_bookmark), Toast.LENGTH_SHORT);
                }

                return true;
            } else if (id == R.id.action_search) {
                if (mSearchContainer.getVisibility() == View.GONE) {
                    mActionBarHandler.post(mActionBarRunnable);
                    mSearchContainer.setVisibility(View.VISIBLE);
                } else {
                    mSearchContainer.setVisibility(View.GONE);
                }
            } else if (id == R.id.action_list) {
                if (isTocLoadedFinish) {
                    setActionVisibility(false);

                    Intent intent = new Intent(getBaseContext(), EpubContentListActivity.class);
                    intent.putParcelableArrayListExtra("tocItemList", this.tocItemList);
                    intent.putParcelableArrayListExtra("bookmarkList", mEpubViewerParam.getBookmarkList());
                    intent.putParcelableArrayListExtra("highlightList", mEpubViewerParam.getHighlightList());
                    startActivityForResult(intent, 1);
                } else {
                    showToast(getResources().getString(R.string.toast_load_toc), Toast.LENGTH_SHORT);
                }

                return true;
            } else if (id == R.id.action_settings) {
                if (mSettingView.getVisibility() != View.VISIBLE) {
                    mSettingView.setVisibility(View.VISIBLE);
                    mSpinnerTheme.setSelection(
                            tsAdapter.getPosition(getStringOfTheme(mEpubViewerParam.getSetting().getTheme())));
                    mSpinnerFontFace.setSelection(
                            ffAdapter.getPosition(getStringOfFontFace(mEpubViewerParam.getSetting().getFontFace())));
                    mSpinnerFontSize.setSelection(fsAdapter.getPosition(mEpubViewerParam.getSetting().getFontSize()));

                    isAutoHide = false;
                } else {
                    mSettingView.setVisibility(View.INVISIBLE);
                    isAutoHide = true;
                }

                return true;
            }

            delayedActionHide(AUTO_HIDE_DELAY_MILLIS);
        } catch (Exception e) {
            onErrorActivityFinish(Log.getStackTraceString(e));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mSettingView.getVisibility() == View.VISIBLE) {
                mSettingView.setVisibility(View.INVISIBLE);
                return false;
            }

            onExitBackPressed();
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        try {
            outState.putString(STATE_BOOK_DATA, EpubViewerParamUtil.createNewBase64FromObject(mEpubViewerParam));
        } catch (Exception e) {
            // ignore
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        String bookData = savedInstanceState.getString(STATE_BOOK_DATA);

        if (bookData != null) {
            try {
                mEpubViewerParam = EpubViewerParamUtil.createNewObjectFromBase64(bookData);
            } catch (Exception e) {
                mEpubViewerParam = null;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);
            if (resultCode == EpubContentListActivity.TOC_RESULT_CODE) {
                String tocHref = data.getStringExtra("tocHref");
                mEpubWebView.openPageToc(tocHref);
            } else if (resultCode == EpubContentListActivity.BOOKMARK_RESULT_CODE) {
                mDeferedIdref = data.getStringExtra("bookmarkIdref");
                mDeferedCfi = data.getStringExtra("bookmarkCfi");

                mEpubWebView.openPageCfi(mDeferedIdref, mDeferedCfi);
                isDefered = false;
            } else if (resultCode == EpubContentListActivity.HIGHLIGHT_RESULT_CODE) {
                mDeferedIdref = data.getStringExtra("highlightIdref");
                mDeferedCfi = data.getStringExtra("highlightCfi");

                mEpubWebView.openPageCfi(mDeferedIdref, mDeferedCfi);
                isDefered = false;
            }
        } catch (Exception e) {
            onErrorActivityFinish(Log.getStackTraceString(e));
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                TOUCH_DOWN_POS_X = ev.getX();
                TOUCH_DOWN_POS_Y = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                TOUCH_MOVE_POS_X = ev.getX();
                TOUCH_MOVE_POS_Y = ev.getY();
                break;
            case MotionEvent.ACTION_UP:
                TOUCH_UP_POS_X = ev.getX();
                TOUCH_UP_POS_Y = ev.getY();
                break;
        }

        mTouchLength = UnitUtil.convertPixelsToDp(TOUCH_UP_POS_X - TOUCH_DOWN_POS_X, getApplicationContext())
                * UnitUtil.convertPixelsToDp(TOUCH_UP_POS_X - TOUCH_DOWN_POS_X, getApplicationContext())
                + UnitUtil.convertPixelsToDp(TOUCH_UP_POS_Y - TOUCH_DOWN_POS_Y, getApplicationContext())
                * UnitUtil.convertPixelsToDp(TOUCH_UP_POS_Y - TOUCH_DOWN_POS_Y, getApplicationContext());

        mTouchMoveLength = UnitUtil.convertPixelsToDp(TOUCH_MOVE_POS_X - TOUCH_DOWN_POS_X, getApplicationContext())
                * UnitUtil.convertPixelsToDp(TOUCH_MOVE_POS_X - TOUCH_DOWN_POS_X, getApplicationContext())
                + UnitUtil.convertPixelsToDp(TOUCH_MOVE_POS_Y - TOUCH_DOWN_POS_Y, getApplicationContext())
                * UnitUtil.convertPixelsToDp(TOUCH_MOVE_POS_Y - TOUCH_DOWN_POS_Y, getApplicationContext());

        return super.dispatchTouchEvent(ev);
    }

    /*
     * API methods
     */
    protected void onExitBackPressed() {

    }

    protected void onErrorActivityFinish(String debugMessage) {

    }

    protected void onDataUpdate(String contentId, String epubBase64) {

    }

    protected void onObserver_AddBookMark(Context context, int bookmarkId, String strIdref, String strCfi,
                                          String strSelectedText, Date dateCurr) {

    }

    protected void onObserver_DeleteBookMark(Context context, int bookmarkId) {

    }

    protected void onObserver_AddHighlight(Context context, int highlightId, String strIdref, String strCfi,
                                           String strSelectedText, String strMemo, String color, Date dateCurr) {

    }

    protected void onObserver_ModifyHighlight(Context context, int highlightId, String strMemo, String color,
                                              Date dateCurr) {

    }

    protected void onObserver_DeleteHighlight(Context context, int highlightId) {

    }

    protected void onPrepareSpine(String idref) {

    }

    protected void onSearchComplete() {

    }

    /*
     * private methods
     */
    private void showToast(String text, int length) {
        if (mToast != null) {
            mToast.cancel();
        }

        mToast = Toast.makeText(getApplicationContext(), text, length);
        mToast.show();
    }

    private int currentTimeSecond() {
        return (int) ((System.currentTimeMillis() / 1000) & Integer.MAX_VALUE);
    }

    private String getStringOfTheme(String theme) {
        if (theme.equals(getResources().getString(R.string.author_theme_code))) {
            return getResources().getString(R.string.author_theme_string);
        } else if (theme.equals(getResources().getString(R.string.night_theme_code))) {
            return getResources().getString(R.string.night_theme_string);
        } else if (theme.equals(getResources().getString(R.string.tree_theme_code))) {
            return getResources().getString(R.string.tree_theme_string);
        } else if (theme.equals(getResources().getString(R.string.sky_theme_code))) {
            return getResources().getString(R.string.sky_theme_string);
        } else {
            return getResources().getString(R.string.default_theme_string);
        }
    }

    private String getThemeByString(String themeString) {
        if (themeString.equals(getResources().getString(R.string.author_theme_string))) {
            return getResources().getString(R.string.author_theme_code);
        } else if (themeString.equals(getResources().getString(R.string.night_theme_string))) {
            return getResources().getString(R.string.night_theme_code);
        } else if (themeString.equals(getResources().getString(R.string.tree_theme_string))) {
            return getResources().getString(R.string.tree_theme_code);
        } else if (themeString.equals(getResources().getString(R.string.sky_theme_string))) {
            return getResources().getString(R.string.sky_theme_code);
        } else {
            return getResources().getString(R.string.default_theme_code);
        }
    }

    private String getStringOfFontFace(String font) {
        if (font.equals(getResources().getString(R.string.sans_font_code))) {
            return getResources().getString(R.string.sans_font_string);
        } else if (font.equals(getResources().getString(R.string.serif_font_code))) {
            return getResources().getString(R.string.serif_font_string);
        } else if (font.equals(getResources().getString(R.string.monospace_font_code))) {
            return getResources().getString(R.string.monospace_font_string);
        } else if (font.equals(getResources().getString(R.string.roboto_font_code))) {
            return getResources().getString(R.string.roboto_font_string);
        } else {
            return getResources().getString(R.string.default_font_string);
        }
    }

    private String getFontFaceByString(String fontString) {
        if (fontString.equals(getResources().getString(R.string.sans_font_string))) {
            return getResources().getString(R.string.sans_font_code);
        } else if (fontString.equals(getResources().getString(R.string.serif_font_string))) {
            return getResources().getString(R.string.serif_font_code);
        } else if (fontString.equals(getResources().getString(R.string.monospace_font_string))) {
            return getResources().getString(R.string.monospace_font_code);
        } else if (fontString.equals(getResources().getString(R.string.roboto_font_string))) {
            return getResources().getString(R.string.roboto_font_code);
        } else {
            return getResources().getString(R.string.default_font_code);
        }
    }

    private void setActionVisibility(boolean visible) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                if (mControlsHeight == 0) {
                    mControlsHeight = mLayoutPageBar.getHeight();
                }
                if (mShortAnimTime == 0) {
                    mShortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
                }

                if (mLayoutPageBar.getVisibility() != View.VISIBLE) {
                    mLayoutPageBar.animate().translationY(mControlsHeight).setDuration(0);
                    mLayoutPageBar.setVisibility(View.VISIBLE);
                }

                if (visible) {
                    actionBar.show();
                    mLayoutPageBar.animate().translationY(0).setDuration(mShortAnimTime);
                } else {
                    actionBar.hide();
                    mLayoutPageBar.animate().translationY(mControlsHeight).setDuration(mShortAnimTime);
                    mSettingView.setVisibility(View.INVISIBLE);
                    mActionBarContainer.setAlpha(0);
                    mActionBarHandler.postDelayed(mActionBarRunnable, 10);
                }
            } else {
                if (visible) {
                    actionBar.show();
                    mLayoutPageBar.setVisibility(View.VISIBLE);
                } else {
                    actionBar.hide();
                    mLayoutPageBar.setVisibility(View.GONE);
                    mSettingView.setVisibility(View.INVISIBLE);
                }
            }

            if (visible && isAutoHide) {
                delayedActionHide(AUTO_HIDE_DELAY_MILLIS);
            }

            IS_VISIBLE = visible;
        } catch (Exception e) {
            onErrorActivityFinish(Log.getStackTraceString(e));
        }
    }

    private void delayedActionHide(int delayMillis) {
        if (AUTO_HIDE) {
            try {
                mHideHandler.removeCallbacks(mHideRunnable);
                mHideHandler.postDelayed(mHideRunnable, delayMillis);
            } catch (Exception e) {
                onErrorActivityFinish(Log.getStackTraceString(e));
            }
        }
    }

    private void setCurrentPageBookmarked(boolean isBookmarked) {
        isCurrentPageBookmarked = isBookmarked;
        try {
            if (isCurrentPageBookmarked) {
                mMenuItemBookmark.setIcon(R.drawable.menu_mark_light_on);
            } else {
                mMenuItemBookmark.setIcon(R.drawable.menu_mark_light_off);
            }
        } catch (Exception e) {
            onErrorActivityFinish(Log.getStackTraceString(e));
        }
        isBookmarkProcessFinish = true;
    }

    private void initHighlightView(int highlightId, String idref, String cfi, String content, String color, String memo,
                                   int visibility) {
        try {
            mHighlightId = highlightId;
            mHighlightIdref = idref;
            mHighlightCfi = cfi;
            mHighlightContent = content;
            mHighlightColor = color;
            mHighlightMemo = memo;

            mImageButtonGreen.setImageResource(R.drawable.highlight_green_off);
            mImageButtonBlue.setImageResource(R.drawable.highlight_blue_off);
            mImageButtonRed.setImageResource(R.drawable.highlight_red_off);
            mImageButtonPurple.setImageResource(R.drawable.highlight_purple_off);
            mImageButtonYellow.setImageResource(R.drawable.highlight_yellow_off);

            if (color.equals(mHighlightColorBlue)) {
                mImageButtonBlue.setImageResource(R.drawable.highlight_blue_on);
            } else if (color.equals(mHighlightColorRed)) {
                mImageButtonRed.setImageResource(R.drawable.highlight_red_on);
            } else if (color.equals(mHighlightColorPurple)) {
                mImageButtonPurple.setImageResource(R.drawable.highlight_purple_on);
            } else if (color.equals(mHighlightColorYellow)) {
                mImageButtonYellow.setImageResource(R.drawable.highlight_yellow_on);
            } else {
                mImageButtonGreen.setImageResource(R.drawable.highlight_green_on);
            }

            mEditTextMemo.setText(memo);
            mHighlightView.setVisibility(visibility);

            if (visibility == View.INVISIBLE || visibility == View.GONE) {
                mEpubWebView.requestFocus();
            } else {
                setActionVisibility(false);
            }
        } catch (Exception e) {
            onErrorActivityFinish(Log.getStackTraceString(e));
        }
    }

    private void touchUpHighlightRemove() {
        try {
            if (mHighlightId > -1) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(EpubViewerActivity.this);
                dialogBuilder.setMessage(getResources().getString(R.string.dialog_confirm_remove_highlight))
                        .setCancelable(false);
                dialogBuilder.setPositiveButton(getResources().getString(R.string.dialog_confirm_yes),
                        hvDialogPositiveOnClickListener);
                dialogBuilder.setNegativeButton(getResources().getString(R.string.dialog_confirm_no),
                        hvDialogNegativeOnClickListener);

                AlertDialog dialog = dialogBuilder.create();
                dialog.setTitle(getResources().getString(R.string.dialog_confirm_title));
                dialog.show();
            } else {
                initHighlightView(-1, "", "", "", mHighlightColorGreen, "", View.INVISIBLE);
            }

            mPaginationWebView.requestFocus();
        } catch (Exception e) {
            onErrorActivityFinish(Log.getStackTraceString(e));
        }
    }

    private void touchUpHighlightSave() {
        try {
            if (mHighlightId == -1) {
                if (!"".equals(mHighlightIdref) && !"".equals(mHighlightCfi) && !"".equals(mHighlightContent)
                        && mHighlightContent.length() > 0) {
                    long currentTimestamp = System.currentTimeMillis();
                    mHighlightId = currentTimeSecond();

                    Highlight highlight = new Highlight();
                    highlight.setIdref(mHighlightIdref);
                    highlight.setCfi(mHighlightCfi);
                    highlight.setMemo(mHighlightMemo);
                    highlight.setColor(mHighlightColor);
                    highlight.setContent(mHighlightContent.length() > 43 ? mHighlightContent.substring(0, 40) + "..."
                            : mHighlightContent);
                    highlight.setId(mHighlightId);
                    highlight.setCreateTime(currentTimestamp);

                    mEpubViewerParam.getHighlightList().add(highlight);

                    onDataUpdate(mEpubViewerParam.getContentId(),
                            EpubViewerParamUtil.createNewBase64FromObject(mEpubViewerParam));
                    onObserver_AddHighlight(getApplicationContext(), highlight.getId(), highlight.getIdref(),
                            highlight.getCfi(), highlight.getContent(), highlight.getMemo(), highlight.getColor(),
                            new Date(highlight.getCreateTime()));
                }
            } else {
                long currentTimestamp = System.currentTimeMillis();

                for (int i = 0; i < mEpubViewerParam.getHighlightList().size(); i++) {
                    if (mEpubViewerParam.getHighlightList().get(i).getId() == mHighlightId) {
                        mEpubViewerParam.getHighlightList().get(i).setMemo(mHighlightMemo);
                        mEpubViewerParam.getHighlightList().get(i).setColor(mHighlightColor);
                        mEpubViewerParam.getHighlightList().get(i).setCreateTime(currentTimestamp);
                    }
                }

                onDataUpdate(mEpubViewerParam.getContentId(),
                        EpubViewerParamUtil.createNewBase64FromObject(mEpubViewerParam));
                onObserver_ModifyHighlight(getApplicationContext(), mHighlightId, mHighlightMemo, mHighlightColor,
                        new Date(currentTimestamp));

                mEpubWebView.removeHighlight(mHighlightId);
            }

            mEpubWebView.addHighlight(mHighlightIdref, mHighlightCfi, mHighlightId, mHighlightColor);

            mPaginationWebView.requestFocus();
        } catch (Exception e) {
            onErrorActivityFinish(Log.getStackTraceString(e));
        }
    }

    private void searchCurrentText() {
        String keyword = mEditTextSearchBox.getText().toString();
        mCurrentKeyword = keyword;

        if (keyword.isEmpty()) {
            showToast(getString(R.string.toast_no_search_keyword), Toast.LENGTH_SHORT);
            return;
        }

        if (mSearchResultAdapter == null) {
            mSearchResultAdapter = new SearchResultAdapter(EpubViewerActivity.this);
            mListViewSearchResult.setAdapter(mSearchResultAdapter);
        }
        mSearchResultAdapter.clear();

        mEpubWebView.search(keyword);
    }

    /*
     * event callback methods
     */
    View.OnTouchListener hvOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                mEpubWebView.getCurrentSelectionCfi();
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                final int viewId = v.getId();
                mHighlightMemo = mEditTextMemo.getText().toString();

                if (viewId == R.id.remove_highlight) {
                    touchUpHighlightRemove();
                } else if (viewId == R.id.save_highlight) {
                    touchUpHighlightSave();
                } else if (viewId == R.id.color_green) {
                    initHighlightView(mHighlightId, mHighlightIdref, mHighlightCfi, mHighlightContent,
                            mHighlightColorGreen, mHighlightMemo, View.VISIBLE);
                } else if (viewId == R.id.color_blue) {
                    initHighlightView(mHighlightId, mHighlightIdref, mHighlightCfi, mHighlightContent,
                            mHighlightColorBlue, mHighlightMemo, View.VISIBLE);
                } else if (viewId == R.id.color_red) {
                    initHighlightView(mHighlightId, mHighlightIdref, mHighlightCfi, mHighlightContent,
                            mHighlightColorRed, mHighlightMemo, View.VISIBLE);
                } else if (viewId == R.id.color_purple) {
                    initHighlightView(mHighlightId, mHighlightIdref, mHighlightCfi, mHighlightContent,
                            mHighlightColorPurple, mHighlightMemo, View.VISIBLE);
                } else if (viewId == R.id.color_yellow) {
                    initHighlightView(mHighlightId, mHighlightIdref, mHighlightCfi, mHighlightContent,
                            mHighlightColorYellow, mHighlightMemo, View.VISIBLE);
                } else if (viewId == R.id.highlight_memo) {
                    mEditTextMemo.requestFocus();
                }
            }
            return false;
        }
    };
    DialogInterface.OnClickListener hvDialogPositiveOnClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            try {
                for (int i = 0; i < mEpubViewerParam.getHighlightList().size(); i++) {
                    Highlight highlight = mEpubViewerParam.getHighlightList().get(i);
                    if (highlight.getId() == mHighlightId) {
                        mEpubViewerParam.getHighlightList().remove(i);
                        break;
                    }
                }

                onDataUpdate(mEpubViewerParam.getContentId(),
                        EpubViewerParamUtil.createNewBase64FromObject(mEpubViewerParam));
                onObserver_DeleteHighlight(getApplicationContext(), mHighlightId);

                mEpubWebView.removeHighlight(mHighlightId);
                showToast(getResources().getString(R.string.toast_remove_highlight), Toast.LENGTH_SHORT);

                initHighlightView(-1, "", "", "", mHighlightColorGreen, "", View.INVISIBLE);
            } catch (Exception e) {
                onErrorActivityFinish(Log.getStackTraceString(e));
            }
        }
    };
    DialogInterface.OnClickListener hvDialogNegativeOnClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();

            initHighlightView(-1, "", "", "", mHighlightColorGreen, "", View.INVISIBLE);
        }
    };

    View.OnTouchListener opOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            float length;

            try {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        // touch move check
                        if (mTouchMoveLength > TOUCH_MOVE_LENGTH) {
                            return false;
                        } else {
                            return true;
                        }
                    case MotionEvent.ACTION_UP:
                        if (mHighlightView.getVisibility() == View.VISIBLE) {
                            initHighlightView(-1, "", "", "", mHighlightColorGreen, "", View.INVISIBLE);
                        }

                        if (v.getId() == R.id.page_prev) {
                            mEpubWebView.openPagePrev();
                        } else if (v.getId() == R.id.page_next) {
                            mEpubWebView.openPageNext();
                        }
                        return true;
                }
            } catch (Exception e) {
                onErrorActivityFinish(Log.getStackTraceString(e));
            }

            return false;
        }
    };

    // highlight memo text focus changed
    View.OnFocusChangeListener hmOnFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            try {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mHighlightView.getLayoutParams();

                if (!hasFocus) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(mEditTextMemo.getWindowToken(), 0);
                    if (isHighlightViewShowInBottom) {
                        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    }
                } else {
                    if (isHighlightViewShowInBottom) {
                        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
                    }
                }

                mHighlightView.setLayoutParams(params);
            } catch (Exception e) {
                onErrorActivityFinish(Log.getStackTraceString(e));
            }
        }
    };
    // epub webview touch event
    View.OnTouchListener wvOnTouchListener = new View.OnTouchListener() {
        private long touchDownTime;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            try {
                float length;

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        touchDownTime = System.currentTimeMillis();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        break;

                    case MotionEvent.ACTION_UP:
                        // touch move check
                        if (mTouchLength < TOUCH_MOVE_LENGTH
                                && (System.currentTimeMillis() - touchDownTime) < TOUCH_DOWN_TERM) {
                            if (mHighlightView.getVisibility() == View.VISIBLE) {
                                initHighlightView(-1, "", "", "", mHighlightColorGreen, "", View.INVISIBLE);
                            } else {
                                if (isBookmarkLoadedFinish) {
                                    setActionVisibility(!IS_VISIBLE);
                                }
                            }
                        }

                        break;
                }
            } catch (Exception e) {
                onErrorActivityFinish(Log.getStackTraceString(e));
            }

            return false;
        }
    };
    //
    View.OnTouchListener otOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            try {
                delayedActionHide(AUTO_HIDE_DELAY_MILLIS);
            } catch (Exception e) {
                onErrorActivityFinish(Log.getStackTraceString(e));
            }

            return true;
        }
    };

    OnClickListener soOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            setActionVisibility(false);
        }
    };

    // page bar change event
    SeekBar.OnSeekBarChangeListener pgOnSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        int cursor;

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (mPaginationWebView.getPaginationState() == PaginationWebView.PaginationState.DONE
                    && !mPaginationWebView.isPaginationNeeded()) {
                try {
                    cursor = progress;
                    mTextViewPager.setText(String.valueOf(progress + 1) + " / " + String.valueOf(mTotalPage));
                    delayedActionHide(AUTO_HIDE_DELAY_MILLIS);
                } catch (Exception e) {
                    onErrorActivityFinish(Log.getStackTraceString(e));
                }
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            try {
                isAutoHide = false;
                cursor = seekBar.getProgress();
                delayedActionHide(AUTO_HIDE_DELAY_MILLIS);
            } catch (Exception e) {
                onErrorActivityFinish(Log.getStackTraceString(e));
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            try {
                seekBar.setProgress(cursor);
                isAutoHide = true;
                mEpubWebView.openPageNumber(cursor + 1);
            } catch (Exception e) {
                onErrorActivityFinish(Log.getStackTraceString(e));
            }
        }
    };
    // brightness bar change event
    /*SeekBar.OnSeekBarChangeListener brOnSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            try {
                WindowManager.LayoutParams params = getWindow().getAttributes();
                progress = Math.max(progress, 1);
                params.screenBrightness = (float) progress / 255;
                getWindow().setAttributes(params);
            } catch (Exception e) {
                onErrorActivityFinish(Log.getStackTraceString(e));
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };*/

    // font size up event
    OnClickListener fsUpOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                if (isTocLoadedFinish) {
                    String strFontSize = mSpinnerFontSize.getSelectedItem().toString();
                    int intFontSize = Integer.parseInt(strFontSize);

                    if (intFontSize < maxFontSize) {
                        intFontSize += 10;
                        strFontSize = Integer.toString(intFontSize);

                        mSpinnerFontSize.setSelection(fsAdapter.getPosition(strFontSize));
                    }
                }
            } catch (Exception e) {
                onErrorActivityFinish(Log.getStackTraceString(e));
            }
        }
    };
    // font size down event
    OnClickListener fsDownOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                if (isTocLoadedFinish) {
                    String strFontSize = mSpinnerFontSize.getSelectedItem().toString();
                    int intFontSize = Integer.parseInt(strFontSize);

                    if (intFontSize > minFontSize) {
                        intFontSize -= 10;
                        strFontSize = Integer.toString(intFontSize);

                        mSpinnerFontSize.setSelection(fsAdapter.getPosition(strFontSize));
                    }
                }
            } catch (Exception e) {
                onErrorActivityFinish(Log.getStackTraceString(e));
            }
        }
    };

    OnClickListener saOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                boolean isStyleChanged = false;
                boolean isPaginationNeeded = false;
                String theme = getThemeByString(mSpinnerTheme.getSelectedItem().toString());
                String fontFace = getFontFaceByString(mSpinnerFontFace.getSelectedItem().toString());
                String fontSize = mSpinnerFontSize.getSelectedItem().toString();

                if (!mEpubViewerParam.getSetting().getTheme().equals(theme)) {
                    mEpubViewerParam.getSetting().setTheme(theme);
                    isStyleChanged = true;
                }

                if (!mEpubViewerParam.getSetting().getFontFace().equals(fontFace)) {
                    mEpubViewerParam.getSetting().setFontFace(fontFace);
                    isStyleChanged = true;
                }

                if (!mEpubViewerParam.getSetting().getFontSize().equals(fontSize)) {
                    mEpubViewerParam.getSetting().setFontSize(fontSize);

                    isPaginationNeeded = true;
                    for (Pagination pagination : mEpubViewerParam.getPaginationCacheList()) {
                        if (pagination.getFontSize().equals(mEpubViewerParam.getSetting().getFontSize())) {
                            mEpubWebView.getEpubViewerCallback().onPaginationDone(pagination.getTotalPage(),
                                    pagination.getJsonSpineItems());
                            isPaginationNeeded = false;
                            break;
                        }
                    }

                    isStyleChanged = true;
                }

                if (isStyleChanged) {
                    mPaginationWebView.setPaginationNeeded(isPaginationNeeded);
                    mEpubWebView.setViewerStyle(mEpubViewerParam.getSetting());
                    mPaginationWebView.setViewerStyle(mEpubViewerParam.getSetting());

                    onDataUpdate(mEpubViewerParam.getContentId(),
                            EpubViewerParamUtil.createNewBase64FromObject(mEpubViewerParam));
                }

                setActionVisibility(false);
            } catch (Exception e) {
                onErrorActivityFinish(Log.getStackTraceString(e));
            }
        }
    };

    OnClickListener searchOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            searchCurrentText();
        }
    };

    OnItemClickListener searchResultOnItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            SearchResult searchResult = mSearchResultAdapter.getItem(position);
            mEpubWebView.openPageCfi(searchResult.getIdref(), searchResult.getCfi());

            mSearchContainer.setVisibility(View.GONE);
        }
    };

    OnKeyListener searchBoxOnKeyListener = new OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                searchCurrentText();
            }
            return false;
        }
    };

    /*
     * Readium callback
     */
    public class ViewerCallback extends EpubViewerCallback {

        public ViewerCallback(BaseEpubWebView caller) {
            super(caller);
        }

        @JavascriptInterface
        public void logJavascript(String message) {
            Log.d("javascript", message);
        }

        @JavascriptInterface
        public void onReadyToLoadEpub() {
            try {
                caller.setViewerState(BaseEpubWebView.ViewerState.OPENING_BOOK);
                caller.setPaginationNeeded(true);
                caller.setPaginationState(BaseEpubWebView.PaginationState.NONE);
                for (Pagination pagination : mEpubViewerParam.getPaginationCacheList()) {
                    if (pagination.getFontSize().equals(mEpubViewerParam.getSetting().getFontSize())
                            && pagination.isPortrait() == UnitUtil.isScreenPortrait(EpubViewerActivity.this)) {
                        caller.updateSpineItems(pagination.getJsonSpineItems());
                        caller.setPaginationNeeded(false);
                        caller.setPaginationState(BaseEpubWebView.PaginationState.DONE);
                        mEpubWebView.getEpubViewerCallback().onPaginationDone(pagination.getTotalPage(),
                                pagination.getJsonSpineItems());
                        break;
                    }
                }

                caller.setViewerStyle(mEpubViewerParam.getSetting());
                caller.loadBook(mEpubViewerParam.getLastReadPageIdref(), mEpubViewerParam.getLastReadPageCfi());
            } catch (Exception e) {
                onErrorActivityFinish(Log.getStackTraceString(e));
            }

        }

        @JavascriptInterface
        public void updateOpenBookData(final String openBookData) {
            try {
                caller.setViewerState(BaseEpubWebView.ViewerState.OPENED_BOOK);
                caller.setOpenBookData(openBookData);
            } catch (Exception e) {
                onErrorActivityFinish(Log.getStackTraceString(e));
            }
        }

        @JavascriptInterface
        public void onSpineLoaded() {
            try {
                caller.setViewerState(BaseEpubWebView.ViewerState.LOADED_PAGE);
                if (caller.getClass().getName().equals(EpubWebView.class.getName())) {
                    mHighlightHandler.postDelayed(mHighlightRunnable, 100);

                    if (isDefered) {
                        mDeferedIdref = null;
                        mDeferedCfi = null;
                    } else {
                        mMoveHandler.removeCallbacks(mMoveRunnable);
                        mMoveHandler.postDelayed(mMoveRunnable, 10);
                    }
                } else if (caller.getClass().getName().equals(PaginationWebView.class.getName())) {
                    if (caller.isPaginationNeeded()) {
                        caller.startPagination();

                        if (!"".equals(mEpubViewerParam.getPagingInfo())) {
                            mEpubViewerParam.setPagingInfo("");
                            onDataUpdate(mEpubViewerParam.getContentId(),
                                    EpubViewerParamUtil.createNewBase64FromObject(mEpubViewerParam));
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    mSeekBarPager.setEnabled(false);
                                    mTextViewPager.setText(getResources().getString(R.string.doing_pagination));
                                } catch (Exception e) {

                                }
                            }
                        });
                    }
                }
            } catch (Exception e) {
                onErrorActivityFinish(Log.getStackTraceString(e));
            }
        }

        @JavascriptInterface
        public void onPageMoved(final int pageNumber, final String pageIdref, final String pageCfi) {
            try {
                caller.setViewerState(BaseEpubWebView.ViewerState.LOADED_PAGE);
                if (caller.getClass().getName().equals(EpubWebView.class.getName())) {
                    if (pageNumber != 0) {
                        mCurrentPageNumber = pageNumber;
                    }
                    mCurrentPageIdref = pageIdref;
                    mCurrentPageCfi = pageCfi;

                    mEpubWebView.checkCurrentPageBookmarked(mEpubViewerParam.getBookmarkList());

                    String paginationInfo = String.valueOf(mCurrentPageNumber) + " / " + String.valueOf(mTotalPage);
                    if (!mEpubViewerParam.getLastReadPageIdref().equals(pageIdref)
                            || !mEpubViewerParam.getLastReadPageCfi().equals(pageCfi)
                            || !mEpubViewerParam.getPagingInfo().equals(paginationInfo)) {

                        mEpubViewerParam.setLastReadPageIdref(pageIdref);
                        mEpubViewerParam.setLastReadPageCfi(pageCfi);
                        mEpubViewerParam.setPagingInfo(paginationInfo);

                        onDataUpdate(mEpubViewerParam.getContentId(),
                                EpubViewerParamUtil.createNewBase64FromObject(mEpubViewerParam));
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                mSeekBarPager.setProgress(mCurrentPageNumber - 1);

                                if (mPaginationWebView.getPaginationState() == PaginationWebView.PaginationState.DONE
                                        && !mPaginationWebView.isPaginationNeeded()) {
                                    mSeekBarPager.setMax(mTotalPage - 1);
                                    mSeekBarPager.setEnabled(true);
                                    mTextViewPager.setText(
                                            String.valueOf(mCurrentPageNumber) + " / " + String.valueOf(mTotalPage));
                                }
                            } catch (Exception e) {

                            }
                        }
                    });
                }
            } catch (Exception e) {
                onErrorActivityFinish(Log.getStackTraceString(e));
            }
        }

        @JavascriptInterface
        public void onPaginationDone(final int totalPage, final String jsonSpineItems) {
            if (totalPage == 0 || "".equals(jsonSpineItems)) {
                return;
            }

            try {
                if (caller.getClass().getName().equals(EpubWebView.class.getName())) {
                    mTotalPage = totalPage;
                    mEpubWebView.updateSpineItems(jsonSpineItems);
                    mPaginationWebView.updateSpineItems(jsonSpineItems);
                } else if (caller.getClass().getName().equals(PaginationWebView.class.getName())) {
                    if (mPaginationWebView.getPaginationState() == BaseEpubWebView.PaginationState.DOING) {
                        mTotalPage = totalPage;

                        mEpubWebView.updateSpineItems(jsonSpineItems);

                        Pagination pagination = new Pagination();
                        pagination.setFontSize(mEpubViewerParam.getSetting().getFontSize());
                        pagination.setTotalPage(totalPage);
                        pagination.setJsonSpineItems(jsonSpineItems);
                        pagination.setIsPortrait(UnitUtil.isScreenPortrait(EpubViewerActivity.this));

                        mEpubViewerParam.getPaginationCacheList().add(pagination);
                    }
                }

                mPaginationWebView.setPaginationState(PaginationWebView.PaginationState.DONE);

                String paginationInfo = String.valueOf(mCurrentPageNumber) + " / " + String.valueOf(mTotalPage);
                if (!mEpubViewerParam.getPagingInfo().equals(paginationInfo)) {
                    mEpubViewerParam.setPagingInfo(paginationInfo);
                    onDataUpdate(mEpubViewerParam.getContentId(),
                            EpubViewerParamUtil.createNewBase64FromObject(mEpubViewerParam));
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mSeekBarPager.setProgress(mCurrentPageNumber - 1);
                            mSeekBarPager.setMax(mTotalPage - 1);
                            mSeekBarPager.setEnabled(true);
                            mTextViewPager
                                    .setText(String.valueOf(mCurrentPageNumber) + " / " + String.valueOf(mTotalPage));
                        } catch (Exception e) {

                        }
                    }
                });
            } catch (Exception e) {
                onErrorActivityFinish(Log.getStackTraceString(e));
            }
        }

        @JavascriptInterface
        public void onTocLoaded(final String tocJson) {
            try {
                if (caller.getClass().getName().equals(EpubWebView.class.getName())) {
                    tocItemList = ((EpubWebView) caller).makeTocList(tocJson);

                    isTocLoadedFinish = true;
                }
            } catch (Exception e) {
                onErrorActivityFinish(Log.getStackTraceString(e));
            }
        }

        @JavascriptInterface
        public void onContentSelected(final String idref, final String cfi, final String selectedElementText) {
            try {
                if (caller.getClass().getName().equals(EpubWebView.class.getName())) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (selectedElementText != null && !"".equals(selectedElementText)) {
                                    String elementText = selectedElementText.replace("\r", "").replace("\n", ".. ");
                                    initHighlightView(-1, idref, cfi, elementText, mHighlightColor, "", View.VISIBLE);
                                }
                            } catch (Exception e) {

                            }
                        }
                    });
                }
            } catch (Exception e) {
                onErrorActivityFinish(Log.getStackTraceString(e));
            }
        }

        @JavascriptInterface
        public void onBookmarkChecked(final boolean isBookmarked, final String idref, final String cfi) {
            try {
                if (caller.getClass().getName().equals(EpubWebView.class.getName())) {
                    if (isBookmarked) {
                        mBookmarkIdref = idref;
                        mBookmarkCfi = cfi;
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                setCurrentPageBookmarked(isBookmarked);
                                isBookmarkLoadedFinish = true;
                            } catch (Exception e) {

                            }
                        }
                    });
                }
            } catch (Exception e) {
                onErrorActivityFinish(Log.getStackTraceString(e));
            }
        }

        @JavascriptInterface
        public void onBookmarkAdded(final String idref, final String cfi, final String firstElementText) {
            try {
                if (caller.getClass().getName().equals(EpubWebView.class.getName())) {
                    boolean isAlreadyExist = false;

                    for (int i = 0; i < mEpubViewerParam.getBookmarkList().size(); i++) {
                        Bookmark bookmark = mEpubViewerParam.getBookmarkList().get(i);
                        if (bookmark.getIdref().equals(idref) && bookmark.getCfi().equals(cfi)) {
                            isAlreadyExist = true;
                            break;
                        }
                    }

                    if (!isAlreadyExist) {
                        String elementText = firstElementText.replace("\r", "").replace("\n", ".. ");

                        Bookmark bookmark = new Bookmark();
                        bookmark.setId(currentTimeSecond());
                        bookmark.setCreateTime(System.currentTimeMillis());
                        bookmark.setIdref(idref);
                        bookmark.setCfi(cfi);
                        bookmark.setContent(
                                elementText.length() > 43 ? elementText.substring(0, 40) + "..." : elementText);

                        mEpubViewerParam.getBookmarkList().add(bookmark);
                        showToast(getResources().getString(R.string.toast_add_bookmark), Toast.LENGTH_SHORT);

                        onDataUpdate(mEpubViewerParam.getContentId(),
                                EpubViewerParamUtil.createNewBase64FromObject(mEpubViewerParam));
                        onObserver_AddBookMark(getApplicationContext(), bookmark.getId(), bookmark.getIdref(),
                                bookmark.getCfi(), bookmark.getContent(), new Date(bookmark.getCreateTime()));
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                setCurrentPageBookmarked(true);
                            } catch (Exception e) {

                            }
                        }
                    });
                }
            } catch (Exception e) {
                onErrorActivityFinish(Log.getStackTraceString(e));
            }
        }

        @JavascriptInterface
        public void onHighlightAdded(final String idref, final String cfi) {
            try {
                if (caller.getClass().getName().equals(EpubWebView.class.getName())) {
                    if (caller.getClass().getName().equals(EpubWebView.class.getName())) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    initHighlightView(-1, "", "", "", mHighlightColorGreen, "", View.INVISIBLE);
                                } catch (Exception e) {

                                }
                            }
                        });
                    }
                }
            } catch (Exception e) {
                onErrorActivityFinish(Log.getStackTraceString(e));
            }
        }

        @JavascriptInterface
        public void onHighlightClicked(final String idref, final String cfi, final int highlightId) {
            try {
                if (caller.getClass().getName().equals(EpubWebView.class.getName())) {

                    for (int i = 0; i < mEpubViewerParam.getHighlightList().size(); i++) {
                        final int index = i;
                        if (mEpubViewerParam.getHighlightList().get(index).getId() == highlightId) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        initHighlightView(mEpubViewerParam.getHighlightList().get(index).getId(),
                                                mEpubViewerParam.getHighlightList().get(index).getIdref(),
                                                mEpubViewerParam.getHighlightList().get(index).getCfi(),
                                                mEpubViewerParam.getHighlightList().get(index).getContent(),
                                                mEpubViewerParam.getHighlightList().get(index).getColor(),
                                                mEpubViewerParam.getHighlightList().get(index).getMemo(), View.VISIBLE);
                                    } catch (Exception e) {

                                    }
                                }
                            });
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                onErrorActivityFinish(Log.getStackTraceString(e));
            }
        }

        @JavascriptInterface
        public void onHighlightRemoved() {

        }

        @JavascriptInterface
        public void onShowPrevPageButton() {
            try {
                if (caller.getClass().getName().equals(EpubWebView.class.getName())) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                mLayoutOpenPagePrev.setVisibility(View.VISIBLE);
                                mLayoutOpenPagePrev.setEnabled(true);
                            } catch (Exception e) {

                            }
                        }
                    });
                }
            } catch (Exception e) {
                onErrorActivityFinish(Log.getStackTraceString(e));
            }
        }

        @JavascriptInterface
        public void onHidePrevPageButton() {
            try {
                if (caller.getClass().getName().equals(EpubWebView.class.getName())) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                mLayoutOpenPagePrev.setVisibility(View.INVISIBLE);
                                mLayoutOpenPagePrev.setEnabled(false);
                            } catch (Exception e) {

                            }
                        }
                    });
                }
            } catch (Exception e) {
                onErrorActivityFinish(Log.getStackTraceString(e));
            }
        }

        @JavascriptInterface
        public void onShowNextPageButton() {
            try {
                if (caller.getClass().getName().equals(EpubWebView.class.getName())) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                mLayoutOpenPageNext.setVisibility(View.VISIBLE);
                                mLayoutOpenPageNext.setEnabled(true);
                            } catch (Exception e) {

                            }
                        }
                    });
                }
            } catch (Exception e) {
                onErrorActivityFinish(Log.getStackTraceString(e));
            }
        }

        @JavascriptInterface
        public void onHideNextPageButton() {
            try {
                if (caller.getClass().getName().equals(EpubWebView.class.getName())) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                mLayoutOpenPageNext.setVisibility(View.INVISIBLE);
                                mLayoutOpenPageNext.setEnabled(false);
                            } catch (Exception e) {

                            }
                        }
                    });
                }
            } catch (Exception e) {
                onErrorActivityFinish(Log.getStackTraceString(e));
            }
        }

        @JavascriptInterface
        public void prepareSpine(final String idref) {
            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onPrepareSpine(idref);
                        // start spine loading
                        caller.loadSpine();
                    }
                });
            } catch (Exception e) {
                onErrorActivityFinish(Log.getStackTraceString(e));
            }
        }

        @JavascriptInterface
        public void updateSearchResult(final String result) {
            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject joResult = new JSONObject(result);
                            String keyword = joResult.getString("keyword");
                            //
                            if (mCurrentKeyword.equals(keyword)) {
                                String idref = joResult.getString("idref");
                                JSONArray jaSearchResults = joResult.getJSONArray("result");
                                for (int i = 0; i < jaSearchResults.length(); i++) {
                                    JSONObject joSearchResult = jaSearchResults.getJSONObject(i);

                                    String cfi = joSearchResult.getString("cfi");
                                    int offset = joSearchResult.getInt("offset");
                                    String surroundingText = joSearchResult.getString("surroundingText");

                                    int startTextOffset = Math.max(0, offset - 80);
                                    int endTextOffset = Math.min(surroundingText.length(), startTextOffset + 150);
//				                    Log.d("SurroundingText", "startTextOffset: " + startTextOffset + ", textLength + 1: " + (textLength + 1) + ", surroundingText.length(): " + surroundingText.length());
                                    String cutSurroundingText = surroundingText.substring(startTextOffset, endTextOffset);
                                    int keywordIndex = surroundingText.toLowerCase().indexOf(mCurrentKeyword.toLowerCase());

                                    if (keywordIndex > -1) {
                                        SearchResult searchResult = new SearchResult();

                                        searchResult.setKeyword(keyword);
                                        searchResult.setIdref(idref);
                                        searchResult.setCfi(cfi);
                                        searchResult.setOffset(offset);
                                        searchResult.setSurroundingText(cutSurroundingText);

                                        if (mSearchResultAdapter != null) {
                                            mSearchResultAdapter.add(searchResult);
                                            mSearchResultAdapter.notifyDataSetChanged();
                                        }
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            // do nothing.
                        }
                    }
                });
            } catch (Exception e) {
                onErrorActivityFinish(Log.getStackTraceString(e));
            }
        }

        @JavascriptInterface
        public int getAndroidVersion() {
            return Build.VERSION.SDK_INT;
        }

        @JavascriptInterface
        public String getSyntheticSpread() {
            if (UnitUtil.getSmallestWidth(EpubViewerActivity.this) >= 550 && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                return "double";
            } else {
                return "single";
            }
        }

        @JavascriptInterface
        public void onSearchDone() {
            onSearchComplete();
        }

        @JavascriptInterface
        public void onLeftPageCurl() {
            mPageCurlHandler.post(new Runnable() {
                @Override
                public void run() {
                    mImageViewLeftPageCurl.setVisibility(View.VISIBLE);
                    mImageViewLeftPageCurl.getDrawable().setVisible(true, true);
                    mImageViewLeftPageCurl.post(new Runnable() {
                        @Override
                        public void run() {
                            ((AnimationDrawable) mImageViewLeftPageCurl.getDrawable()).setOneShot(true);
                            ((AnimationDrawable) mImageViewLeftPageCurl.getDrawable()).start();
                        }
                    });
                }
            });
            mPageCurlHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mImageViewLeftPageCurl.setVisibility(View.GONE);
                    mImageViewLeftPageCurl.getDrawable().setVisible(false, true);
                }
            }, 160);
        }

        @JavascriptInterface
        public void onRightPageCurl() {
            mPageCurlHandler.post(new Runnable() {
                @Override
                public void run() {
                    mImageViewRightPageCurl.setVisibility(View.VISIBLE);
                    mImageViewRightPageCurl.getDrawable().setVisible(true, true);
                    mImageViewRightPageCurl.post(new Runnable() {
                        @Override
                        public void run() {
                            ((AnimationDrawable) mImageViewRightPageCurl.getDrawable()).start();
                        }
                    });
                }
            });
            mPageCurlHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mImageViewRightPageCurl.setVisibility(View.GONE);
                    mImageViewRightPageCurl.getDrawable().setVisible(false, true);
                }
            }, 160);
        }
    }

    class SearchResultAdapter extends ArrayAdapter<SearchResult> {

        public SearchResultAdapter(Context context) {
            super(context, R.layout.item_search_result, new ArrayList<SearchResult>());
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_search_result, null);
            }

            SearchResult item = getItem(position);

            TextView tvSearchResultText = (TextView) convertView.findViewById(R.id.search_result_text);

            int keywordIndex = item.getSurroundingText().toLowerCase().indexOf(mCurrentKeyword.toLowerCase());
            String boldSurroundingText = "";
            if (item.getOffset() - 80 > 0) {
                boldSurroundingText += "...";
            }
            boldSurroundingText += item.getSurroundingText().substring(0, keywordIndex);
            boldSurroundingText += "<b>" + item.getSurroundingText().substring(keywordIndex, keywordIndex + mCurrentKeyword.length()) + "</b>";
            boldSurroundingText += item.getSurroundingText().substring(keywordIndex + mCurrentKeyword.length());

            tvSearchResultText.setText(Html.fromHtml(boldSurroundingText));

            return convertView;
        }
    }
}
