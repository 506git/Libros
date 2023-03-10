/*
 * Copyright (C) 2013 ePapyrus, Inc. All rights reserved.
 *
 * This file is part of the PlugPDF Android project.
 */

/*
 * SimpleReaderControlView.java
 *
 * Version:
 *       id
 *
 * Revision:
 *      logs
 */

package com.epapyrus.plugpdf;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.LruCache;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.epapyrus.plugpdf.SimpleReaderControlPanel.PanelType;
import com.epapyrus.plugpdf.core.BaseReaderControl;
import com.epapyrus.plugpdf.core.PlugPDF;
import com.epapyrus.plugpdf.core.PlugPDFUtility;
import com.epapyrus.plugpdf.core.annotation.tool.BaseAnnotTool.AnnotToolType;
import com.epapyrus.plugpdf.core.gesture.BaseGestureProcessor.GestureType;
import com.epapyrus.plugpdf.core.viewer.BasePlugPDFDisplay.PageDisplayMode;

import java.io.FileOutputStream;
import java.io.IOException;

import static android.R.attr.bitmap;
import static android.R.attr.height;
import static android.R.attr.width;

/**
 * Menu control built-in class.
 * This is the UI processing class with several PDF operations to be displayed to the user.
 *
 * @author ePapyrus
 * @see <a href="http://www.plugpdf.com">http://www.plugpdf.com</a>
 */
public class SimpleReaderControlView extends RelativeLayout {

	private BaseReaderControl mController;
	private SimpleReaderControlPanel mControlPanel;
	private AnnotSettingMenu mAnnotSettingMenu;
	Activity mAct;

	private boolean mButtonsVisible;
	private boolean mTopBarIsSearch;

	private ViewFlipper mTopBarSwitcher;
	private TextView mPageNumberView;
	private ImageView mPageThumbnail[] = new ImageView[9];
	private TextView mPageNumber[] = new TextView[9];
	int mPageIdxList[] = new int[9];
	private SeekBar mPageSlider;
	private Button mSearchButton;
	private Button mOutlineButton;
	private Button mEditButton;

	// search
	private Button mSearchCancelButton;
	private EditText mSearchText;
	private Button mSearchBack;
	private Button mSearchFwd;

	// edit
	private Button mEditCancelButton;
	private Button mEditNoteButton;
	private Button mEditInkButton;
	private Button mEditEraserButton;
	private Button mEditHighlightButton;
	private Button mEditUnderlineButton;
	private Button mEditStrikeoutButton;

	private Button mRotateButton;
	private Button mPageDisplayModeButton;
	private Button mBrightnessButton;
	private TextView mTitle;

	private int mPageIdx;
	private Bitmap mBitmap[] = new Bitmap[9];

	private boolean enableHiddenTopBar;
	private boolean enableHiddenBottomBar;

	private BitmapCache mBitmapCache;
	private Point mDiviceSize;
	private int mNumberOfPreView = 0;
	/**
	 * Constructor.
	 *
	 * @param context {@link Context} instance
	 */
	public SimpleReaderControlView(Context context) {
		super(context);
	}

	/**
	 * Constructor.
	 *
	 * @param context  {@link Context} instance
	 * @param attrs    {@link AttributeSet}
	 */
	public SimpleReaderControlView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	/**
	 * Creates the UI layout.
	 *
	 * @param controller Object of {@link BaseReaderControl} class
	 */
	public void createUILayout(BaseReaderControl controller) {

		mController = controller;

		mTitle = (TextView) findViewById(R.id.rc_title);
		mTopBarSwitcher = (ViewFlipper) findViewById(R.id.flipper);
		mPageNumberView = (TextView) findViewById(R.id.rc_page_number);
		mPageThumbnail[0] = (ImageView) findViewById(R.id.rc_page_thumbnail_0);
		mPageThumbnail[1] = (ImageView) findViewById(R.id.rc_page_thumbnail_1);
		mPageThumbnail[2] = (ImageView) findViewById(R.id.rc_page_thumbnail_2);
		mPageThumbnail[3] = (ImageView) findViewById(R.id.rc_page_thumbnail_3);
		mPageThumbnail[4] = (ImageView) findViewById(R.id.rc_page_thumbnail_4);
		mPageThumbnail[5] = (ImageView) findViewById(R.id.rc_page_thumbnail_5);
		mPageThumbnail[6] = (ImageView) findViewById(R.id.rc_page_thumbnail_6);
		mPageThumbnail[7] = (ImageView) findViewById(R.id.rc_page_thumbnail_7);
		mPageThumbnail[8] = (ImageView) findViewById(R.id.rc_page_thumbnail_8);

		mPageNumber[0] = (TextView)findViewById(R.id.rc_page_0);
		mPageNumber[1] = (TextView)findViewById(R.id.rc_page_1);
		mPageNumber[2] = (TextView)findViewById(R.id.rc_page_2);
		mPageNumber[3] = (TextView)findViewById(R.id.rc_page_3);
		mPageNumber[4] = (TextView)findViewById(R.id.rc_page_4);
		mPageNumber[5] = (TextView)findViewById(R.id.rc_page_5);
		mPageNumber[6] = (TextView)findViewById(R.id.rc_page_6);
		mPageNumber[7] = (TextView)findViewById(R.id.rc_page_7);
		mPageNumber[8] = (TextView)findViewById(R.id.rc_page_8);

		mPageSlider = (SeekBar) findViewById(R.id.rc_page_slider);
		mSearchButton = (Button) findViewById(R.id.rc_search);
		mEditButton = (Button) findViewById(R.id.rc_edit);
		mSearchCancelButton = (Button) findViewById(R.id.rc_search_cancel);
		mSearchText = (EditText) findViewById(R.id.rc_search_text);
		mSearchBack = (Button) findViewById(R.id.rc_search_back);
		mSearchFwd = (Button) findViewById(R.id.rc_search_forward);
		mEditCancelButton = (Button) findViewById(R.id.rc_edit_cancel);
		mEditNoteButton = (Button) findViewById(R.id.rc_edit_note);
		mEditInkButton = (Button) findViewById(R.id.rc_edit_ink);
		mEditEraserButton = (Button) findViewById(R.id.rc_edit_eraser);
		mEditHighlightButton = (Button) findViewById(R.id.rc_edit_tm_highlight);
		mEditUnderlineButton = (Button) findViewById(R.id.rc_edit_tm_underline);
		mEditStrikeoutButton = (Button) findViewById(R.id.rc_edit_tm_strikeout);
		mOutlineButton = (Button) findViewById(R.id.rc_outline);
		mRotateButton = (Button) findViewById(R.id.rc_rotate);
		mPageDisplayModeButton = (Button) findViewById(R.id.rc_page_display_mode);
		mBrightnessButton = (Button) findViewById(R.id.rc_brightness);

	}

	/**
	 * Initializes the UI components of the PDF document viewer.
	 *
	 * @param act {@link Activity}
	 */
	public void init(Activity act) {

		mAct = act;

		mAnnotSettingMenu = new AnnotSettingMenu(getContext());

		mControlPanel = new SimpleReaderControlPanel(getContext(), this,
				mController);
		showOutlineButton(true);

		setEnableHiddenTopBar(false);
		setEnableHiddenBottomBar(false);
		mTopBarSwitcher.setVisibility(View.INVISIBLE);
		mPageNumberView.setVisibility(View.INVISIBLE);
		mPageSlider.setVisibility(View.INVISIBLE);
		for(int i = 0; i < 9; i++){
			mPageThumbnail[i].setVisibility(View.INVISIBLE);
			mPageNumber[i].setVisibility(View.INVISIBLE);
		}
		mBitmapCache = new BitmapCache();

		mPageSlider
				.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
					public void onStopTrackingTouch(SeekBar seekBar) {
						mController.goToPage(seekBar.getProgress());
					}

					public void onStartTrackingTouch(SeekBar seekBar) {
					}

					public void onProgressChanged(SeekBar seekBar,
												  int progress, boolean fromUser) {
						updatePageNumber(seekBar.getProgress() + 1,
								seekBar.getMax() + 1);
					}
				});

		mSearchButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				searchModeOn();
			}
		});

		if (mController.canModifyAnnot()) {
			mEditButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					editModeOn();
				}
			});
		} else {
			mEditButton.setVisibility(GONE);
		}


		mSearchCancelButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				searchModeOff();
			}
		});

		mSearchBack.setEnabled(false);
		mSearchFwd.setEnabled(false);

		mSearchText.addTextChangedListener(new TextWatcher() {

			public void afterTextChanged(Editable s) {
				boolean haveText = s.toString().length() > 0;
				mSearchBack.setEnabled(haveText);
				mSearchFwd.setEnabled(haveText);

				mController.resetSearchInfo();
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
										  int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
									  int count) {
			}
		});

		mSearchText
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					public boolean onEditorAction(TextView v, int actionId,
												  KeyEvent event) {
						if (actionId == EditorInfo.IME_ACTION_NEXT
								|| actionId == EditorInfo.IME_ACTION_DONE) {
							mController.search(
									mSearchText.getText().toString(), 1);
						}
						return false;
					}
				});

		mSearchBack.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mController.search(mSearchText.getText().toString(), -1);
			}
		});
		mSearchFwd.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				mController.search(mSearchText.getText().toString(), 1);
			}
		});

		mEditCancelButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				editModeOff();
				mController.changeGestureType(GestureType.VIEW);
			}
		});

		mEditNoteButton.setOnClickListener(mEditButtonClickHandler);
		mEditInkButton.setOnClickListener(mEditButtonClickHandler);
		mEditInkButton.setOnLongClickListener(mEditButtonClickHandler);
		mEditEraserButton.setOnClickListener(mEditButtonClickHandler);
		mEditHighlightButton.setOnClickListener(mEditButtonClickHandler);
		mEditHighlightButton.setOnLongClickListener(mEditButtonClickHandler);
		mEditUnderlineButton.setOnClickListener(mEditButtonClickHandler);
		mEditUnderlineButton.setOnLongClickListener(mEditButtonClickHandler);
		mEditStrikeoutButton.setOnClickListener(mEditButtonClickHandler);
		mEditStrikeoutButton.setOnLongClickListener(mEditButtonClickHandler);

		mRotateButton.setTag(0);
		mRotateButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mRotateButton.getTag().equals(0)) {
					mRotateButton.setTag(1);
					mRotateButton
							.setBackgroundResource(R.drawable.st_btn_rotate_lock);
					PlugPDFUtility.setRotationLock(mAct, true);
				} else {
					mRotateButton.setTag(0);
					mRotateButton
							.setBackgroundResource(R.drawable.st_btn_rotate);
					PlugPDFUtility.setRotationLock(mAct, false);
				}
			}
		});

		mPageDisplayModeButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mControlPanel.show(PanelType.DISPLAYMODE, v);
			}
		});

		mBrightnessButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mControlPanel.show(PanelType.BRIGHTNESS, v);
			}
		});
		Display display = mAct.getWindowManager().getDefaultDisplay();
		mDiviceSize = new Point();
		display.getSize(mDiviceSize);

	}

	/**
	 * Sets the title.
	 *
	 * @param title {@link String} value to set the title
	 */
	public void setTitle(String title) {
		mTitle.setText(title);
	}

	/**
	 * The top menu show/hide.
	 * @param isTextFieldFocused If the values is ture, do not hide top menu.
	 */
	public void toggleControlTabBar(boolean isTextFieldFocused) {
		if (!mButtonsVisible)
			show();
		else
			if(!isTextFieldFocused) hideTopMenu();
	}


	/**
	 * Refreshes the layout.
	 */
	public void refreshLayout() {
		mControlPanel.refreshLayout();
	}

	/**
	 * Shows the top menu.
	 */
	public void show() {
		if (mButtonsVisible)
			return;

		mButtonsVisible = true;

		if (mTopBarIsSearch) {
			mSearchText.requestFocus();
			showKeyboard();
		}
		if (!isEnableHiddenTopBar()) {
			Animation anim = new TranslateAnimation(0, 0,
					-mTopBarSwitcher.getHeight(), 0);
			anim.setDuration(200);
			anim.setAnimationListener(new Animation.AnimationListener() {
				public void onAnimationStart(Animation animation) {
					mTopBarSwitcher.setVisibility(View.VISIBLE);
				}

				public void onAnimationRepeat(Animation animation) {
				}

				public void onAnimationEnd(Animation animation) {
				}
			});
			mTopBarSwitcher.startAnimation(anim);
		}
		if (!isEnableHiddenBottomBar()) {
			Animation anim = new TranslateAnimation(0, 0,
					mPageSlider.getHeight(), 0);
			anim.setDuration(200);
			anim.setAnimationListener(new Animation.AnimationListener() {
				public void onAnimationStart(Animation animation) {
					mPageSlider.setVisibility(View.VISIBLE);
				}

				public void onAnimationRepeat(Animation animation) {
				}

				public void onAnimationEnd(Animation animation) {
					mPageNumberView.setVisibility(View.VISIBLE);
					for(int i = 4 - mNumberOfPreView; i < 4 + mNumberOfPreView + 1; i++){
						mPageThumbnail[i].setVisibility(View.VISIBLE);
						mPageNumber[i].setVisibility(View.VISIBLE);
					}
				}
			});
			mPageSlider.startAnimation(anim);
		}
	}


	/**
	 * Hides the top menu.
	 */
	public void hideTopMenu() {
		if (!mButtonsVisible)
			return;
		mButtonsVisible = false;

		hideKeyboard();
		if (!isEnableHiddenTopBar()) {
			Animation anim = new TranslateAnimation(0, 0, 0,
					-mTopBarSwitcher.getHeight());
			anim.setDuration(200);
			anim.setAnimationListener(new Animation.AnimationListener() {
				public void onAnimationStart(Animation animation) {
				}

				public void onAnimationRepeat(Animation animation) {
				}

				public void onAnimationEnd(Animation animation) {
					mTopBarSwitcher.setVisibility(View.INVISIBLE);
				}
			});
			mTopBarSwitcher.startAnimation(anim);
		}
		if (!isEnableHiddenBottomBar()) {
			Animation anim = new TranslateAnimation(0, 0, 0,
					mPageSlider.getHeight());
			anim.setDuration(200);
			anim.setAnimationListener(new Animation.AnimationListener() {
				public void onAnimationStart(Animation animation) {
					mPageNumberView.setVisibility(View.INVISIBLE);
					for(int i = 0; i < 9; i++){
						mPageThumbnail[i].setVisibility(View.GONE);
						mPageNumber[i].setVisibility(View.GONE);
					}
				}

				public void onAnimationRepeat(Animation animation) {
				}

				public void onAnimationEnd(Animation animation) {
					mPageSlider.setVisibility(View.INVISIBLE);
				}
			});
			mPageSlider.startAnimation(anim);
		}
	}

	private AsyncTask<Void,Integer,Void> prevFetchTask;
	/**
	 * Updates the page slider.
	 *
	 * @param pageIdx    Current page number.
	 * @param pageCount  Total number of pages in the PDF document.
	 */
	public void updatePageNumber(int pageIdx, final int pageCount) {
		mPageNumberView.setText(String.format("%d/%d", pageIdx, pageCount));
		for(int i = 0; i < 9; i++){
			mPageNumber[i].setText("" + (pageIdx + i - 4));
		}
		mPageSlider.setMax(pageCount - 1);
		mPageSlider.setProgress(pageIdx - 1);
		mPageIdx = pageIdx - 1;
		if (mTopBarIsSearch && !mSearchText.getText().toString().isEmpty()) {
			mController.search(mSearchText.getText().toString(), 0);
		}

		if ( prevFetchTask != null) {
			prevFetchTask.cancel(true);
		}

		prevFetchTask = new android.os.AsyncTask<Void, Integer, Void>() {
			protected Void doInBackground(Void... v) {
				PointF pageSize = mController.getPageSize(mPageIdx);
				int numberOfPreView = 2 * mNumberOfPreView + 1;

				int start = 4 - mNumberOfPreView;
				int end = 4 + mNumberOfPreView + 1;

				int pageWidth = mDiviceSize.x < mDiviceSize.y? mDiviceSize.x / numberOfPreView  : mDiviceSize.y / numberOfPreView;
				if (numberOfPreView == 1) pageWidth /= 9;
				int pageHeight = pageWidth * (int)pageSize.y / (int)pageSize.x;

				for(int i = start; i < end; i++) {
					mBitmap[i] = Bitmap.createBitmap(pageWidth, pageHeight, PlugPDF.bitmapConfig());
				}
				int jump = 0;
				for(int i = 4 ; start <= i && i < end; i += jump) {
					if(this.isCancelled()) {
						return null;
					}
					Bitmap bitmap = mBitmapCache.bitmap(mPageIdx + i - mNumberOfPreView - start);
					int pageIdx = mPageIdx + i - mNumberOfPreView - start;
					mPageIdxList[i] = pageIdx;
					if (bitmap != null && !bitmap.isRecycled()) {
						if(bitmap.getWidth() != pageWidth) {
							mBitmap[i] = Bitmap.createScaledBitmap(bitmap, pageWidth, pageHeight, true);
						} else {
							mBitmap[i] = bitmap;
						}
					} else {
						if(pageIdx < 0 || pageIdx >= pageCount)	{
							mBitmap[i] = null;
						} else {
							publishProgress(i);
							mController.drawPage(mBitmap[i], pageIdx, pageWidth, pageHeight, 0, 0, pageWidth, pageHeight);
							mBitmapCache.addBitmap(pageIdx, mBitmap[i]);
						}
					}
					publishProgress(i);
					int sgn = 1;
					if(jump != 0) {
						sgn = Math.abs(jump) / jump;
					}
					jump = -1 * (Math.abs(jump) + 1) * sgn;
				}
				return null;
			}

			protected void onPreExecute() {
				int start = 4 - mNumberOfPreView;
				int end = 4 + mNumberOfPreView + 1;

				for(int i = 0; i < 9 ; i++){
					if(start <= i && i < end) {
						mPageThumbnail[i].setImageBitmap(mBitmap[i]);
					}
				}
			}

			protected void onPostExecute(Void ret) {
				SimpleReaderControlView.this.prevFetchTask = null;
			}
			protected void onProgressUpdate(Integer... index){
				if(mPageIdxList[index[0]] >= 0) {
					mPageThumbnail[index[0]].setImageBitmap(mBitmap[index[0]]);
					mPageNumber[index[0]].setText("" + (mPageIdxList[index[0]] + 1));
					mPageNumber[index[0]].setTextColor(Color.BLACK);
				} else {
					mPageThumbnail[index[0]].setImageBitmap(null);
					mPageNumber[index[0]].setText("");
				}
			}
		};

		prevFetchTask.execute();
	}

	class BitmapCache {
		private LruCache<Integer, Bitmap> mCache;

		BitmapCache() {
			int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
			int cacheSize = maxMemory / 8;

			mCache = new LruCache<Integer, Bitmap>(cacheSize) {
				@Override
				protected int sizeOf(Integer key, Bitmap bitmap) {
					return bitmap.getByteCount() / 1024;
				}
			};
		}

		public void addBitmap(Integer key, Bitmap bitmap) {
			if (bitmap(key) == null) {
				mCache.put(key, bitmap);
			}
		}

		public Bitmap bitmap(Integer key) {
			return mCache.get(key);
		}
	}

	/**
	 * Sets the search mode.
	 *
	 * @param on {@link Boolean} value to set the search mode.
	 */
	private void setSearchMode(boolean on) {
		if (on) {
			mTopBarSwitcher.showNext();
		} else {
			mTopBarSwitcher.showPrevious();
		}
	}

	/**
	 * Enables the search mode.
	 */
	void searchModeOn() {
		mTopBarIsSearch = true;
		mSearchText.requestFocus();
		showKeyboard();
		setSearchMode(true);
	}

	/**
	 * Disables the search mode.
	 */
	void searchModeOff() {
		mTopBarIsSearch = false;
		hideKeyboard();
		setSearchMode(false);
		mController.resetSearchInfo();
	}

	/**
	 * Shows the keyboard.
	 */
	void showKeyboard() {
		InputMethodManager imm = (InputMethodManager) getContext()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null) {
			imm.showSoftInput(mSearchText, 0);
		}
	}

	/**
	 * Hides the keyboard.
	 */
	void hideKeyboard() {
		InputMethodManager imm = (InputMethodManager) getContext()
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null) {
			imm.hideSoftInputFromWindow(mSearchText.getWindowToken(), 0);
		}
	}

	/**
	 * Opens/closes the edit mode.
	 *
	 * @param on {@link Boolean} value to set the edit mode.
	 */
	private void setEditMode(boolean on) {
		if (on) {
//			mController.changeGestureType(GestureType.EDIT);
			mTopBarSwitcher.showNext();
			mTopBarSwitcher.showNext();
		} else {
//			mController.changeGestureType(GestureType.VIEW);
			mTopBarSwitcher.showPrevious();
			mTopBarSwitcher.showPrevious();
		}
	}

	/**
	 * Opens the edit mode.
	 */
	void editModeOn() {
		mPageSlider.setVisibility(View.VISIBLE);
		setEditMode(true);
	}

	/**
	 * Closes the edit mode.
	 */
	void editModeOff() {
		mPageSlider.setVisibility(View.INVISIBLE);
		mEditNoteButton.setSelected(false);
		mEditInkButton.setSelected(false);
		mEditEraserButton.setSelected(false);
		mEditHighlightButton.setSelected(false);
		mEditStrikeoutButton.setSelected(false);
		mEditUnderlineButton.setSelected(false);
		setEditMode(false);
	}

	/**
	 * Shows/hides the edit button.
	 *
	 * @param edit {@link Boolean} value to show/hide edit button.
	 */
	public void showEditButton(boolean edit) {
		if (edit) {
			mEditButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					editModeOn();
				}
			});
		} else {
			mEditButton.setVisibility(View.GONE);
		}
	}

	/**
	 * Enables/disables the PDF document's Outline button.
	 *
	 * @param show {@link Boolean} value to enable or disable the PDF document's Outline button.
	 */
	void showOutlineButton(boolean show) {
		if (show) {
			mOutlineButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					// mListener.showOutline();
					mControlPanel.show(PanelType.OUTLINE, v);
				}
			});
		} else {
			mOutlineButton.setVisibility(View.GONE);
		}
	}

	/**
	 * Saves the current state of the {@link Bundle} passed.
	 *
	 * @param state {@link Bundle} to save the current state.
	 */
	public void saveState(Bundle state) {
		if (!mButtonsVisible) {
			state.putBoolean("ButtonsHidden", true);
		}

		if (mTopBarIsSearch) {
			state.putBoolean("SearchMode", true);
		}
	}


	/**
	 * Restores the state from the passed-in {@link Bundle}
	 *
	 * @param state {@link Bundle}
	 */
	public void restoreState(Bundle state) {

		if (state == null || !state.getBoolean("ButtonsHidden", false)) {
			show();
		}

		if (state != null && state.getBoolean("SearchMode", false))
			searchModeOn();
	}

	/**
	 * Sets the display view mode in the horizontal direction.
	 */
	void setHorizontalMode() {
		mPageDisplayModeButton
				.setBackgroundResource(R.drawable.st_btn_view_mode_horizontal);
		mController.setPageDisplayMode(PageDisplayMode.HORIZONTAL);
	}

	/**
	 * Sets the display view mode in the bilateral vertical direction.
	 */
	void setBilateralVerticalMode() {
		mPageDisplayModeButton
				.setBackgroundResource(R.drawable.st_btn_view_mode_bilateral);
		mController.setPageDisplayMode(PageDisplayMode.BILATERAL_VERTICAL);
	}

	/**
	 * Sets the display view mode in the bilateral horizontal direction.
	 */
	void setBilateralHorizontalMode() {
		mPageDisplayModeButton
				.setBackgroundResource(R.drawable.st_btn_view_mode_bilateral);
		mController.setPageDisplayMode(PageDisplayMode.BILATERAL_HORIZONTAL);
	}

	/**
	 * Sets the display view mode with page flip effect by bilateral.
	 */
	void setBilateralRealisticMode() {
		mPageDisplayModeButton
				.setBackgroundResource(R.drawable.st_btn_view_mode_bilateral);
		mController.setPageDisplayMode(PageDisplayMode.BILATERAL_REALISTIC);
	}

	/**
	 * Sets the display view mode in the vertical direction.
	 */
	void setVerticalMode() {
		mPageDisplayModeButton
				.setBackgroundResource(R.drawable.st_btn_view_mode_vertical);
		mController.setPageDisplayMode(PageDisplayMode.VERTICAL);
	}

	/**
	 * Sets the display view mode in the vertical direction.
	 */
	void setContinuosMode() {
		mPageDisplayModeButton
				.setBackgroundResource(R.drawable.st_btn_view_mode_vertical);
		mController.setPageDisplayMode(PageDisplayMode.CONTINUOS);
	}

	/**
	 * Sets the preview display mode in each page.
	 */
	void setThumbnailMode() {
		mPageDisplayModeButton
				.setBackgroundResource(R.drawable.st_btn_view_mode_thumbnail);
		mController.setPageDisplayMode(PageDisplayMode.THUMBNAIL);
		// mEditButton.setVisibility(View.GONE);
		// mSearchButton.setVisibility(View.GONE);
		mPageSlider.setVisibility(INVISIBLE);
		mPageNumberView.setVisibility(INVISIBLE);
		for(int i = 0; i < 9; i++){
			mPageThumbnail[i].setVisibility(View.INVISIBLE);
		}
	}

	/**
	 * Sets the display view mode with page flip effect.
	 */
	void setRealisticMode() {
		/*mPageDisplayModeButton
				.setBackgroundResource(R.drawable.st_btn_view_mode_thumbnail);*/
		mController.setPageDisplayMode(PageDisplayMode.REALISTIC);
		// mEditButton.setVisibility(View.GONE);
		// mSearchButton.setVisibility(View.GONE);
	}

	EditButtonClickHandler mEditButtonClickHandler = new EditButtonClickHandler();


	/**
	 * Edits the button click's event handler.
	 */
	private class EditButtonClickHandler implements View.OnClickListener,
			View.OnLongClickListener {

		private boolean mLongClickEvented = false;

		/* (non-Javadoc)
		 * @see android.view.View.OnLongClickListener#onLongClick(android.view.View)
		 */
		@Override
		public boolean onLongClick(View v) {
			mLongClickEvented = true;

			if (v.getId() == mEditInkButton.getId()) {
				mAnnotSettingMenu.show(v, 0, 0, AnnotToolType.INK);
			} else if (v.getId() == mEditHighlightButton.getId()) {
				mAnnotSettingMenu.show(v, 0, 0, AnnotToolType.HIGHLIGHT);
			} else if (v.getId() == mEditUnderlineButton.getId()) {
				mAnnotSettingMenu.show(v, 0, 0, AnnotToolType.UNDERLINE);
			} else if (v.getId() == mEditStrikeoutButton.getId()) {
				mAnnotSettingMenu.show(v, 0, 0, AnnotToolType.STRIKEOUT);
			}
			return false;
		}

		/* (non-Javadoc)
		 * @see android.view.View.OnClickListener#onClick(android.view.View)
		 */
		@Override
		public void onClick(View v) {
			if (mLongClickEvented) {
				mLongClickEvented = false;
				return;
			}

			if(!mButtonsVisible) {
				return;
			}

			if (v.getId() == mEditInkButton.getId()) {
				mEditEraserButton.setSelected(false);
				mEditNoteButton.setSelected(false);
				mEditHighlightButton.setSelected(false);
				mEditUnderlineButton.setSelected(false);
				mEditStrikeoutButton.setSelected(false);

				mEditInkButton.setSelected(!mEditInkButton.isSelected());
				if (mEditInkButton.isSelected()) {
					mController.changeGestureType(GestureType.EDIT);
					mController.setAnnotationTool(AnnotToolType.INK);
				} else {
					mController.changeGestureType(GestureType.VIEW);
					mController.setAnnotationTool(AnnotToolType.NONE);
				}

			} else if (v.getId() == mEditEraserButton.getId()) {
				mEditInkButton.setSelected(false);
				mEditNoteButton.setSelected(false);
				mEditHighlightButton.setSelected(false);
				mEditUnderlineButton.setSelected(false);
				mEditStrikeoutButton.setSelected(false);

				mEditEraserButton.setSelected(!mEditEraserButton.isSelected());
				if (mEditEraserButton.isSelected()) {
					mController.changeGestureType(GestureType.EDIT);
					mController.setAnnotationTool(AnnotToolType.ERASER);
				} else {
					mController.changeGestureType(GestureType.VIEW);
					mController.setAnnotationTool(AnnotToolType.NONE);
				}
			} else if (v.getId() == mEditNoteButton.getId()) {
				mEditInkButton.setSelected(false);
				mEditEraserButton.setSelected(false);
				mEditHighlightButton.setSelected(false);
				mEditUnderlineButton.setSelected(false);
				mEditStrikeoutButton.setSelected(false);

				mEditNoteButton.setSelected(!mEditNoteButton.isSelected());
				if (mEditNoteButton.isSelected()) {
					mController.changeGestureType(GestureType.EDIT);
					mController.setAnnotationTool(AnnotToolType.NOTE);
				} else {
					mController.changeGestureType(GestureType.VIEW);
					mController.setAnnotationTool(AnnotToolType.NONE);
				}
			} else if (v.getId() == mEditHighlightButton.getId()) {
				mEditInkButton.setSelected(false);
				mEditEraserButton.setSelected(false);
				mEditNoteButton.setSelected(false);
				mEditUnderlineButton.setSelected(false);
				mEditStrikeoutButton.setSelected(false);

				mEditHighlightButton.setSelected(!mEditHighlightButton.isSelected());
				if (mEditHighlightButton.isSelected()) {
					mController.changeGestureType(GestureType.EDIT);
					mController.setAnnotationTool(AnnotToolType.HIGHLIGHT);
				} else {
					mController.changeGestureType(GestureType.VIEW);
					mController.setAnnotationTool(AnnotToolType.NONE);
				}
			} else if (v.getId() == mEditUnderlineButton.getId()) {
				mEditInkButton.setSelected(false);
				mEditEraserButton.setSelected(false);
				mEditNoteButton.setSelected(false);
				mEditHighlightButton.setSelected(false);
				mEditStrikeoutButton.setSelected(false);

				mEditUnderlineButton.setSelected(!mEditUnderlineButton.isSelected());
				if (mEditUnderlineButton.isSelected()) {
					mController.changeGestureType(GestureType.EDIT);
					mController.setAnnotationTool(AnnotToolType.UNDERLINE);
				} else {
					mController.changeGestureType(GestureType.VIEW);
					mController.setAnnotationTool(AnnotToolType.NONE);
				}
			} else if (v.getId() == mEditStrikeoutButton.getId()) {
				mEditInkButton.setSelected(false);
				mEditEraserButton.setSelected(false);
				mEditNoteButton.setSelected(false);
				mEditHighlightButton.setSelected(false);
				mEditUnderlineButton.setSelected(false);

				mEditStrikeoutButton.setSelected(!mEditStrikeoutButton.isSelected());
				if (mEditStrikeoutButton.isSelected()) {
					mController.changeGestureType(GestureType.EDIT);
					mController.setAnnotationTool(AnnotToolType.STRIKEOUT);
				} else {
					mController.changeGestureType(GestureType.VIEW);
					mController.setAnnotationTool(AnnotToolType.NONE);
				}
			}
		}
	}

	/**
	 * Shows/hides the provided {@link Button} with respect to the given boolean value.
	 *
	 * @param button {@link Button} to show/hide
	 * @param enable {@link Boolean} value to enable or disable the {@link Button}
	 */
	private void enableAnnotButton(Button button, boolean enable) {
		if (enable) {
			button.setVisibility(View.VISIBLE);
		} else {
			button.setVisibility(View.GONE);
		}
	}

	/**
	 * Enables the annotation feature (Ink/Link/Note).
	 *
	 * @param types {@link String} value which enables the annotation feature.
	 * @param enable {@link Boolean} value to show/hide the annotation button.
	 */
	public void enableAnnotFeature(String types, boolean enable) {
		// INK, LINK, NOTE
		String targets[] = types.split(":");
		for (String target : targets) {
			if (target.equals("INK")) {
				enableAnnotButton(mEditInkButton, enable);
			} else if (target.equals("NOTE")) {
				enableAnnotButton(mEditNoteButton, enable);
			} else if (target.equals("LINK")) {
			}
		}

		if (mEditInkButton.getVisibility() == View.VISIBLE
				|| mEditNoteButton.getVisibility() == View.VISIBLE) {
			enableAnnotButton(mEditEraserButton, true);
		} else {
			enableAnnotButton(mEditEraserButton, false);
		}
	}

	public boolean isEnableHiddenTopBar() {
		return enableHiddenTopBar;
	}

	public void setEnableHiddenTopBar(boolean enableHiddenTopBar) {
		this.enableHiddenTopBar = enableHiddenTopBar;
	}

	public boolean isEnableHiddenBottomBar() {
		return enableHiddenBottomBar;
	}

	public void setEnableHiddenBottomBar(boolean enableHiddenBottomBar) {
		this.enableHiddenBottomBar = enableHiddenBottomBar;
	}

	@Override
	protected void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mControlPanel.refreshLayout();
	}

	protected void setNumberOfPreView(int numberOfPreView){
		if(numberOfPreView <= 0) mNumberOfPreView = 0;
		else if(numberOfPreView >= 4) mNumberOfPreView = 4;
		else mNumberOfPreView = numberOfPreView;
	}
}
