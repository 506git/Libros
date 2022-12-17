package kr.co.smartandwise.eco_epub3_module.Util;

import android.content.Context;

import java.io.File;

public class EBookFileUtil {
	public static final String PRIVATE_BOOK_STORAGE_FOLDER = "/library_court";

	public static String getEBookStoragePath(Context context) {
		// TODO: for test
//		String eBookPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + context.getResources().getString(R.string.sdcard_dir_name);
		String eBookPath = context.getFilesDir().getAbsolutePath() + PRIVATE_BOOK_STORAGE_FOLDER;

		// TODO: don't make directory here
		File eBookDir = new File(eBookPath);
		if (!eBookDir.exists()) {
			eBookDir.mkdirs();
		}

		return eBookPath;

	}
}