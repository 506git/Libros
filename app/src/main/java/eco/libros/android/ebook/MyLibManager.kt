package eco.libros.android.ebook

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import com.wjopms.bookapp.BookApplication
import eco.libros.android.R
import eco.libros.android.common.database.EbookDownloadDBFacade
import eco.libros.android.common.database.UserLibListDBFacade
import eco.libros.android.common.database.ViewerDBFacade
import eco.libros.android.common.database.ViewerDBHelper
import eco.libros.android.common.model.BookVO
import eco.libros.android.common.utill.LibrosLog
import eco.libros.android.common.utill.LibrosUtil
import eco.libros.android.myContents.MyEbookListModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kr.co.smartandwise.eco_epub3_module.Model.EpubViewerParam
import kr.co.smartandwise.eco_epub3_module.Util.EpubViewerParamUtil
import java.lang.Exception
import java.util.*

class MyLibManager {

    suspend fun showEBook(activity: Activity, lentKey: String, myLibraryMainUrl: String) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(
                    activity,
                    activity.resources.getString(R.string.msg_usb_mount),
                    Toast.LENGTH_LONG
            ).show()
            return
        }
        CoroutineScope(Main).async {
            val newEbook: BookVO? = withContext(IO) {
                ViewerDBFacade(activity).getBookById(lentKey)
            }
            val eBook = withContext(IO) {
                EbookDownloadDBFacade(activity).getDownloadFileName(lentKey)
            }

            if (newEbook != null) {
                newEbook.bookName = eBook.title
                if (newEbook.bookData == null || newEbook.bookData!!.trim().isEmpty()) {
                    val evp = EpubViewerParamUtil.createNewObject(
                            newEbook.contentId.toString(),
                            newEbook.rootPath
                    )
                    try {
                        newEbook.bookData = EpubViewerParamUtil.createNewBase64FromObject(evp)
                        ViewerDBFacade(activity).updateBookById(
                                newEbook.contentId,
                                newEbook.bookData!!
                        )
                    } catch (e: Exception) {
                        LibrosLog.print(e.toString())
                    }
                }
            }

            if (eBook == null) {
                return@async
            }

            // 시간 검사
            val strStartTime: String? = eBook.useStartTime
            val strEndTime: String? = eBook.useEndTime

            if (strStartTime != null && strEndTime != null && strStartTime.trim().isNotEmpty() && strEndTime.trim().isNotEmpty()) {
                val startTime = strStartTime.toInt()
                val endTime = strEndTime.toInt()

                val nowHours = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                if (startTime > nowHours || nowHours >= endTime) {
                    LibrosUtil.showMsgWindow(
                            activity,
                            "알림",
                            "이용 가능 시간이 아닙니다 \n이용 가능 시간 : ${startTime}시 ~ ${endTime}시",
                            "확인"
                    )
                }
            }

            //다운로드 되거나 이북을 읽을 수 있도록

            var epubId = eBook.lentKey

            if (epubId.isEmpty()) {
                epubId = "163905"
            }

            if (!eBook.drm.equals("BOOK_JAM", true) && (epubId == null || epubId.trim()
                            .isEmpty())
            ) {
                LibrosUtil.showMsgWindow(activity, "알림", "전자책 키를 찾을 수 없습니다.", "확인")
            }

            if (eBook.fileName != null && eBook.fileName.isNotEmpty()) {
                if (!eBook.drm.equals("BOOK_JAM", true) && eBook.fileType.equals("PDF", true)) {

                } else {

                    when(eBook.drm.toUpperCase(Locale.getDefault())){
                        "ECO_MOA", "YES24" ->{
                            val userInfo = withContext(IO) {
                                UserLibListDBFacade(activity).getCertifyInfo(libCode = eBook.libCode)
                            }

                            val eBookUserId = userInfo?.eBookId
                            val eBookUserPw = userInfo?.eBookPw

                            withContext(IO) {
                                PreloadAsyncTask(
                                        activity, eBook.fileName, eBook.drm,
                                        newEbook?.bookData!!, null, eBook.drmInfo
                                ).task(
                                        eBookUserId.toString(),
                                        eBookUserPw.toString()
                                )
                            }
                        }
                        "BOOK_JAM", "BA" ->{

                            withContext(Main) {
                                val application = BookApplication()
                                application.initialize(activity)
                                application.openBook("${LibrosUtil.getEPUBRootPath(activity)}/${activity.resources.getString(R.string.sdcard_dir_name)}/${eBook.fileName}")
                            }
                        }

                        else -> {
                             val book: BookVO? = ViewerDBFacade(activity).getBookById(lentKey)

                            book?.bookName = eBook.title
                            if (book?.bookData == null || book.bookData!!.trim().isEmpty()){
                                val evp: EpubViewerParam = EpubViewerParamUtil.createNewObject(book?.contentId, book?.rootPath)
                                try{
                                    book?.bookData = EpubViewerParamUtil.createNewBase64FromObject(evp)
                                    ViewerDBFacade(activity).updateBookById(book?.contentId.toString(), book?.bookData)
                                } catch (e: Exception){
                                    LibrosLog.print(e.toString())
                                }
                            }
                            activity.startActivity(Intent(activity, EpubViewer::class.java).apply {
                                putExtra("EPUB_BOOK_DATA", book?.bookData)
                                putExtras(Bundle().apply {
                                    putString("drmType", eBook.drm)
                                    putString("epubFileName", eBook.fileName)
                                    putString("filePath", book?.rootPath)
                                })
                            })
                        }
                    }
                }
            }
        }
    }
}