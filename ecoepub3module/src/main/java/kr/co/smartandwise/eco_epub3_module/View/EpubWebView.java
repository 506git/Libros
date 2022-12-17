package kr.co.smartandwise.eco_epub3_module.View;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ActionMode;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import kr.co.smartandwise.eco_epub3_module.Model.Bookmark;
import kr.co.smartandwise.eco_epub3_module.Model.TocItem;

public class EpubWebView extends BaseEpubWebView {

	private ActionMode.Callback startActionModeCallback = null;
	private long touchDownTime = 0;

	public EpubWebView(Context context) {
		super(context);
	}

	public EpubWebView(Context context, AttributeSet attr) {
		super(context, attr);
	}

	public EpubWebView(Context context, AttributeSet attr, int defStyle) {
		super(context, attr, defStyle);
	}

	@Override
	public void destroy() {
		startActionModeCallback = null;
		super.destroy();
	}

	@Override
	public ActionMode startActionMode(ActionMode.Callback callback, int type) {
		if (startActionModeCallback != null) {
			return super.startActionMode(this.startActionModeCallback, 0); // 0 for ActionMode.TYPE_PRIMARY above Android M
		}

		return super.startActionMode(callback, type);
	}

	@Override
	public ActionMode startActionMode(ActionMode.Callback callback) {
		if (startActionModeCallback != null) {
			return super.startActionMode(this.startActionModeCallback);
		}

		return super.startActionMode(callback);
	}

	// @Override
	// public boolean onTouchEvent(MotionEvent event) {
	// View vActionBarContainer = ((Activity)
	// getContext()).findViewById(R.id.action_bar_container);
	// vActionBarContainer.setVisibility(GONE);
	//
	// if (event.getAction() == MotionEvent.ACTION_DOWN) {
	// Toast.makeText(getContext(), "Touch Down", Toast.LENGTH_SHORT).show();
	// touchDownTime = System.currentTimeMillis();
	// } else if (event.getAction() == MotionEvent.ACTION_UP) {
	// Toast.makeText(getContext(), "Touch Up", Toast.LENGTH_SHORT).show();
	// if (vActionBarContainer != null) {
	// long now = System.currentTimeMillis();
	// // long tap
	// if (now - touchDownTime < ViewConfiguration.getLongPressTimeout()) {
	// vActionBarContainer.setVisibility(GONE);
	// } else {
	// vActionBarContainer.setVisibility(VISIBLE);
	// }
	// }
	// }
	// return super.onTouchEvent(event);
	// }

	public void setStartActionModeCallback(ActionMode.Callback startActionModeCallback) {
		this.startActionModeCallback = startActionModeCallback;
	}

	/*
	 * public method
	 */
	public ArrayList<TocItem> makeTocList(String tocJson) {
		Gson gson = new Gson();
		Type type = new TypeToken<List<TocItem>>() {
		}.getType();

		return (ArrayList<TocItem>) gson.fromJson(tocJson, type);
	}

	/*
	 * EPUB Methods
	 */
	public void startPagination() {

	}

	public void openPagePrev() {
		if (this.viewerState != ViewerState.LOADING_PAGE) {
			this.viewerState = ViewerState.LOADING_PAGE;
			this.callJavascript("ReadiumSDK.reader.openPagePrev();");
		}
	}

	public void openPageNext() {
		if (this.viewerState != ViewerState.LOADING_PAGE) {
			this.viewerState = ViewerState.LOADING_PAGE;
			this.callJavascript("ReadiumSDK.reader.openPageNext();");
		}
	}

	public void openPageNumber(int pageNumber) {
		if (this.viewerState != ViewerState.LOADING_PAGE) {
			this.viewerState = ViewerState.LOADING_PAGE;
			this.callJavascript("ReadiumSDK.reader.openPageNumber('" + pageNumber + "');");
		}
	}

	public void openPageCfi(String idref, String cfi) {
		if (this.viewerState != ViewerState.LOADING_PAGE) {
			this.viewerState = ViewerState.LOADING_PAGE;
			this.callJavascript("ReadiumSDK.reader.openPageCfi('" + idref + "', '" + cfi + "');");
		}
	}

	public void openPageToc(String href) {
		if (this.viewerState != ViewerState.LOADING_PAGE) {
			this.viewerState = ViewerState.LOADING_PAGE;
			this.callJavascript("ReadiumSDK.reader.openPageToc('" + href + "');");
		}
	}

	public void getCurrentSelectionCfi() {
		if (this.viewerState != ViewerState.LOADING_PAGE) {
			this.callJavascript("ReadiumSDK.reader.getCurrentSelectionCfi();");
		}
	}

	public void bookmarkCurrentPage() {
		if (this.viewerState != ViewerState.LOADING_PAGE) {
			this.callJavascript("ReadiumSDK.reader.bookmarkCurrentPage(true);");
		}
	}

	public void checkCurrentPageBookmarked(ArrayList<Bookmark> bookmarkList) {
		if (this.viewerState != ViewerState.LOADING_PAGE) {
			JSONArray jBookmarkList = new JSONArray();

			for (Bookmark bookmark : bookmarkList) {
				JSONObject jBookmark = new JSONObject();
				try {
					jBookmark.put("idref", bookmark.getIdref());
					jBookmark.put("contentCFI", bookmark.getCfi());
				} catch (JSONException e) {

				}

				jBookmarkList.put(jBookmark);
			}

			this.callJavascript("ReadiumSDK.reader.getCurrentPageBookmark(" + jBookmarkList + ");");
		}
	}

	public void addHighlight(String idref, String cfi, int id, String color) {
		if (this.viewerState != ViewerState.LOADING_PAGE) {
			this.callJavascript("ReadiumSDK.reader.addHighlight('" + idref + "', '" + cfi + "', '" + String.valueOf(id)
					+ "', 'highlight', '{\"fill_color\":\"" + color + "\"}');");
		}
	}

	public void addSelectionHighlight(int id, String color) {
		if (this.viewerState != ViewerState.LOADING_PAGE) {
			this.callJavascript("ReadiumSDK.reader.addSelectionHighlight('" + String.valueOf(id)
					+ "', 'highlight', '{\"fill_color\":\"" + color + "\"}');");
		}
	}

	public void removeHighlight(int id) {
		if (this.viewerState != ViewerState.LOADING_PAGE) {
			this.callJavascript("ReadiumSDK.reader.removeHighlight('" + String.valueOf(id) + "');");
		}
	}

	public void search(String keyword) {
		if (this.viewerState != ViewerState.LOADING_PAGE) {
			this.callJavascript("ReadiumSDK.reader.search('" + keyword + "');");
		}
	}
}
