package eco.libros.android.ebook.download

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.util.Log
import android.view.WindowManager
import androidx.fragment.app.FragmentActivity
import eco.libros.android.R
import eco.libros.android.common.ProgressFragment
import eco.libros.android.common.database.EbookDownloadDBFacade
import eco.libros.android.common.database.ViewerDBFacade
import eco.libros.android.common.model.EbookListVO
import eco.libros.android.common.utill.LibrosLog
import eco.libros.android.common.utill.LibrosUtil
import eco.libros.android.common.variable.GlobalVariable
import eco.libros.android.myContents.MyEbookListModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.eco.common.ebook.viewer.file.drm.eco.EBookImageDrmEcoMoaAsyncTask
import java.io.File

class EBookImageDrmYES24Task(_activity: Activity) {
    var progressBar = ProgressFragment()

    init {
        activity = _activity
        myActivity = activity as FragmentActivity

    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private lateinit var activity: Activity
        lateinit var myActivity: FragmentActivity
    }

    private suspend fun onPreExecute() {
        try {
            progressBar = ProgressFragment.newInstance("데이터 처리 중입니다")
            progressBar.show(myActivity.supportFragmentManager, "progress")
        } catch (e: WindowManager.BadTokenException) {
            Log.e("error", e.message.toString())
        }
    }

    fun task(_down: Any?, _fileName: Any?, _eBookInfo: Any?) {
        CoroutineScope(Main).launch {
            onPreExecute()
            val obj = arrayOfNulls<Any>(3)
            withContext(IO) {
                val down = _down as EBookDownloadYES24
                val fileName = _fileName as String

                var result = false
                try {
                    result = down.setImageWithoutDrm(fileName)
                } catch (e: Exception) {
                    LibrosLog.print(e.toString())
                }

                if (result && fileName != null && _eBookInfo != null) {
                    val eBookInfo = _eBookInfo as MyEbookListModel
                    updateEBookDownStatic(eBookInfo.libCode)
                }
                Log.d("TEst RESULT", result.toString())
                obj[0] = result
                obj[1] = _eBookInfo
                obj[2] = fileName
            }

            if (progressBar.isAdded) {
                try {
                    progressBar.dismissAllowingStateLoss()
                } catch (e: WindowManager.BadTokenException) {
                    // TODO: handle exception
                    Log.e("error", e.message.toString())
                } catch (e: IllegalArgumentException) {
                    // TODO: handle exception
                    Log.e("error", e.message.toString())
                }
            }

            withContext(Main) {
                val successResult = obj[0] as Boolean
                val eBookInfo = obj[1] as MyEbookListModel
                val fileName = obj[2] as String

                if (successResult) {
                    if (fileName != null) {
                        withContext(IO) {
                            EbookDownloadDBFacade(activity).insertData(
                                    ebook = EbookListVO(
                                            fileName = fileName,
                                            libCode = eBookInfo.libCode,
                                            title = eBookInfo.title,
                                            libName = eBookInfo.libName,
                                            thumbnail = eBookInfo.thumbnail,
                                            bookId = eBookInfo.id,
                                            lentKey = eBookInfo.lentKey,
                                            comKey = eBookInfo.lentKey,
                                            isbn = eBookInfo.isbn,
                                            fileType = "EPUB",
                                            drmInfo = "",
                                            drm = eBookInfo.comCode,
                                            downloadYn = "Y",
                                            returnDate = eBookInfo.lendingExpiredDate,
                                            useStartTime = eBookInfo.useStartTime,
                                            useEndTime = eBookInfo.useEndTime
                                    )
                            )
                        }
                        withContext(IO) {
                            ViewerDBFacade(activity).insertOrUpdateBook(
                                    eBookInfo.lentKey,
                                    "${LibrosUtil.getEPUBRootPath(activity).toString()}/${activity.applicationContext.getString(R.string.sdcard_dir_name)}/$fileName/${fileName.substring(0, fileName.indexOf(".epub"))}",
                                    null)
                        }
                        showMsgDialog("알림", "다운로드 되었습니다.", "확인", eBookInfo.lentKey)
                    } else {
                        LibrosUtil.showMsgWindow(
                                activity = activity,
                                title = "알림",
                                msg = "다운로드 되지 않았습니다.",
                                btnMsg = "확인"
                        )
                    }
                } else {
                    if (fileName != null) {
                        val file = File(LibrosUtil.getEPUBRootPath(activity).toString() + "/" + activity.resources.getString(R.string.sdcard_dir_name), fileName)
                        if (file != null && file.exists()) {
                            FileManager().deleteFolder(file)
                        }
                    }
                    LibrosUtil.showMsgWindow(
                            activity = activity,
                            title = "알림",
                            msg = "다운로드 되지 않았습니다.",
                            btnMsg = "확인"
                    )
                }
            }
        }
    }

    private fun showMsgDialog(title: String, content: String, btnMsg: String, lentKey: String) {
        try {
            AlertDialog.Builder(activity)
                    .setTitle(title)
                    .setMessage(content)
                    .setPositiveButton(
                            btnMsg
                    ) { dialog, _ ->
                        dialog.dismiss()
                        Log.d("TESTLENTKEY", lentKey)
                        val intent = Intent(GlobalVariable.DOWNLOAD_RESULT)
                        intent.putExtra("lent_key", lentKey)
                        activity.sendBroadcast(intent)
                    }.create().show()
        } catch (e: WindowManager.BadTokenException) {
            // TODO: handle exception
            LibrosLog.print(e.toString())
        }
    }

    private fun updateEBookDownStatic(libCode: String) {

//		LibropiaDao dao = new LibropiaDao();
//		dao.setServiceClass("MN_04_01_01_SERVICE");
//
//		try {
//			dao.setMethodNames("도서관부호",libCode);
//		} catch (Exception e) {
//			//not handle
//		}
//
//		try {
//			dao.executeService();
//		} catch (Exception e) {
//			//not handle
//		}
    }
}