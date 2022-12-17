package kr.co.smartandwise.eco_epub3_module.Drm;

import android.content.Context;

public class DrmHelper {

	public static EbookFile getEBook(Context context, String drm, String epubFileName, String id, String pw, String libSeq) {
		if (drm.equalsIgnoreCase("YES24")) {
			return new EbookDrmYes24(context, epubFileName, null, id, pw);
		} else if (drm.equalsIgnoreCase("OPMS")) {
			return new EbookDrmOPMS(context, epubFileName, libSeq);
		} else {
			return null;
		}
	}
}
