package kr.co.smartandwise.eco_epub3_module.Drm;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import kr.co.smartandwise.eco_epub3_module.Activity.EpubViewerActivity;

//이미지가 많은 파일에서 변환이 늦어지는 이슈

public class EBookImageDrmAsyncTask extends AsyncTask<Object, Integer, Boolean> {

	private Context context = null;
	private Yes24Book yes24Book = null;
	private Class<? extends EpubViewerActivity> classEpubViewer = null;
	private ProgressDialog progressDialog = null;

	public EBookImageDrmAsyncTask(Context context, Yes24Book yes24Book, Class<? extends EpubViewerActivity> classEpubViewer) {
		this.context = context;
		this.yes24Book = yes24Book;
		this.classEpubViewer = classEpubViewer;
	}

	@Override
	protected void onPreExecute() {
		progressDialog = new ProgressDialog(context);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setMessage("DRM 해제 중입니다.");
		progressDialog.setCancelable(false);
		progressDialog.show();
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		if (values[0] > 100) {
			if (progressDialog != null) {
				progressDialog.dismiss();
			}
		} else {
			if (progressDialog != null) {
				progressDialog.setProgress(values[0]);
			}
		}
	}

	@Override
	protected Boolean doInBackground(Object... datas) {

		EBookDownloadYES24 down = (EBookDownloadYES24) datas[0];
		String fileName = (String) datas[1];

		Boolean result = false;
		try {
			result = down.setImageWithoutDrm(this, fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	@Override
	protected void onPostExecute(Boolean result) {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}

		boolean successResult = result;

		/*if (successResult) {
			if (yes24Book.getePubFileName() != null) {
				EbookDrmYes24 eBookDrm = (EbookDrmYes24) DrmHelper.getEBook(context, yes24Book);
				eBookDrm.initEbookData();

				new AlertDialog.Builder(context).setTitle("알림").setMessage("완료 되었습니다.")
						.setPositiveButton("확인", new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								try {
									String rootPath = EBookFileUtil.getEBookStoragePath(context) + "/"
											+ yes24Book.getePubFileName() + "/" + yes24Book.getePubFileName().substring(0, yes24Book.getePubFileName().indexOf(".epub"));

									EpubViewerParam evp = EpubViewerParamUtil.createNewObject(yes24Book.geteBookId(),
											rootPath);

									String contentId = yes24Book.geteBookId();
									DatabaseHelper dbHelper = new DatabaseHelper(context);
									kr.co.smartandwise.eco_epub3_module.Model.Book tmpBook = dbHelper.getBookById(String.valueOf(contentId));
									if (tmpBook == null) {
										tmpBook = new kr.co.smartandwise.eco_epub3_module.Model.Book();
										tmpBook.setBookData(EpubViewerParamUtil.createNewBase64FromObject(evp));
									}
									
									tmpBook.setContentId(contentId);
									tmpBook.setRootPath(rootPath);

									Intent intent = new Intent(context, classEpubViewer);
									intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
									intent.putExtra("EPUB_BOOK_DATA", tmpBook.getBookData());

									context.startActivity(intent);
									
									if (context instanceof Activity) {
										((Activity) context).finish();
									}
								} catch (Exception e) {
									new AlertDialog.Builder(context).setTitle("오류").setMessage("책을 열 수 없습니다.")
											.setPositiveButton("확인", new OnClickListener() {

										@Override
										public void onClick(DialogInterface dialog, int which) {
											if (context instanceof Activity) {
												((Activity) context).finish();
											}
										}
									}).show();
								}
							}
						}).show();
			} else {
				new AlertDialog.Builder(context).setTitle("오류").setMessage("책을 열 수 없습니다.")
						.setPositiveButton("확인", new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								if (context instanceof Activity) {
									((Activity) context).finish();
								}
							}
						}).show();
			}
		} else {
			if (yes24Book.getePubFileName() != null) {

				File file = new File(EBookFileUtil.getEBookStoragePath(context), yes24Book.getePubFileName());

				if (file != null && file.exists() == true) {
					FileManager.deleteFolder(file);
				}
			}

			new AlertDialog.Builder(context).setTitle("오류").setMessage("책을 열 수 없습니다.")
					.setPositiveButton("확인", new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (context instanceof Activity) {
								((Activity) context).finish();
							}
						}
					}).show();
		}*/
	}
}