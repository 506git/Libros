package kr.co.smartandwise.eco_epub3_module.Drm;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;

import kr.co.smartandwise.eco_epub3_module.Activity.EpubViewerActivity;

public class EBookDownloadAsyncTask extends AsyncTask<Void, Integer, EBookDownloadYES24> {

	private Context context = null;
	private String downloadUrl;
	private Book book = null;
	private Class<? extends EpubViewerActivity> classEpubViewer = null;
	private ProgressDialog progressDialog = null;

	public EBookDownloadAsyncTask(Context context, Book book, String downloadUrl, Class<? extends EpubViewerActivity> classEpubViewer) {
		this.context = context;
		this.book = book;
		this.downloadUrl = downloadUrl;
		this.classEpubViewer = classEpubViewer;
	}

	@Override
	protected void onPreExecute() {
		progressDialog = new ProgressDialog(context);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setMessage("준비 중입니다.");
		progressDialog.setCancelable(false);
		try {
			progressDialog.show();
		} catch (Exception e) {
			// ignore
		}
	}

	@Override
	protected EBookDownloadYES24 doInBackground(Void... objects) {
		// yes24
		if (book instanceof Yes24Book) {
			Yes24Book yes24Book = (Yes24Book) book;

			EBookDownloadYES24 down = new EBookDownloadYES24(context, yes24Book.getUserId(),
					yes24Book.getUserPw(), yes24Book.geteBookId());
			try {
				String fileName = down.epubFileDownload(this);
				book.setePubFileName(fileName);
			} catch (Exception e) {
				e.printStackTrace();
			}

			return down;
		}
		// Markany
		else if (book instanceof MarkanyBook) {
			MarkanyBook markanyBook = (MarkanyBook) book;

			if (downloadUrl != null) {
				EBookDownloadOPMS downOpms = new EBookDownloadOPMS();

				String fileName = downOpms.down(context, downloadUrl, this, markanyBook);
				book.setePubFileName(fileName);
			}
		}
		return null;
	}

	@Override
	protected void onPostExecute(EBookDownloadYES24 eBookDownloadYES24) {
		if (progressDialog != null) {
			progressDialog.dismiss();
		}

		if (book != null && book.getePubFileName() != null) {

			if (book.getDrm() == Drm.YES24) {
				EBookImageDrmAsyncTask imageTask = new EBookImageDrmAsyncTask(context, (Yes24Book) book, classEpubViewer);
				imageTask.execute(eBookDownloadYES24, book.getePubFileName());
				return;
			} else if (book.getDrm() == Drm.MARKANY) {
				/*new AlertDialog.Builder(context).setTitle("알림").setMessage("완료 되었습니다.")
						.setPositiveButton("확인", new OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								try {
									MarkanyBook markanyBook = (MarkanyBook) book;

									if (markanyBook.getFileType().equalsIgnoreCase("epub")) {
										String rootPath = EBookFileUtil.getEBookStoragePath(context) + "/"
												+ markanyBook.getePubFileName() + "/" + markanyBook.getePubFileName();

										EbookFile eBookFile = DrmHelper.getEBook(context, markanyBook);
										eBookFile.initEbookData();

										EpubViewerParam evp = EpubViewerParamUtil.createNewObject(markanyBook.getBookId(),
												rootPath);

										String contentId = markanyBook.getBookId();
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
									} else if (markanyBook.getFileType().equalsIgnoreCase("pdf")) {

									}
									
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
						}).show();*/
			} else if (book.getDrm() == Drm.YES24) {
				/*new AlertDialog.Builder(context).setTitle("알림").setMessage("완료 되었습니다.")
				.setPositiveButton("확인", new OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						try {
							Yes24Book yes24Book = (Yes24Book) book;

							String rootPath = EBookFileUtil.getEBookStoragePath(context) + "/"
									+ yes24Book.getePubFileName() + "/" + yes24Book.getePubFileName();
							
							EbookFile eBookFile = DrmHelper.getEBook(context, yes24Book);
							eBookFile.initEbookData();

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
				}).show();*/
			}
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
	}

	public void onProgressUpdate(Integer... progress) {

		if (progress[0] > 100) {
			if (progressDialog != null) {
				progressDialog.dismiss();
			}
		} else {
			if (progressDialog != null) {
				progressDialog.setProgress(progress[0]);
			}
		}
	}
}