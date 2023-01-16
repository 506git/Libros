package eco.libros.android.common.utill

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.WindowManager
import androidx.fragment.app.FragmentActivity
import eco.libros.android.R
import eco.libros.android.common.CustomProgressFragment
import eco.libros.android.common.api.LibrosUpload
import eco.libros.android.common.database.EbookDownloadDBFacade
import eco.libros.android.common.model.EbookListVO
import eco.libros.android.common.variable.GlobalVariable
import eco.libros.android.ebook.download.*
import eco.libros.android.myContents.MyEbookListModel
import eco.libros.android.ui.MainActivity
import eco.libros.android.utils.CompressZip
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kr.eco.common.ebook.viewer.file.drm.eco.EBookImageDrmEcoMoaAsyncTask
import java.io.File
import java.util.*

class EBookDownloadTask(_activity: Activity, _ebookData: MyEbookListModel, _downloadPlace: String, socket : Socket) {

    suspend fun downloadEBook(): Unit? {
        val downloadUrl = ebookData.downloadLink
        val ebookId = ebookData.lentKey
        val comCode = ebookData.comCode
        val returnObj = kotlin.arrayOfNulls<Any>(5)
        var fileName: String? = null
//        var progress = mActivity.findViewById<ProgressBar>(R.id.progress)
        if (ebookLibName.isEmpty()) {
            return null
        }

        mSocket.emit("working_start", "start")

        CoroutineScope(Main).launch {
            try {
                progressBar = CustomProgressFragment.newInstance("다운로드 중입니다")
                progressBar.show(myActivity.supportFragmentManager, "progress")
            } catch (e: WindowManager.BadTokenException) {
                Log.d("error", e.message.toString())
            }
//            try {
            withContext(IO) {
                try {
                    when (comCode.toUpperCase(Locale.getDefault())) {
                        "ECO_MOA" -> {
                            if (ebookData.firstLicense.isNotEmpty()) {
                                lateinit var toStoreLicense: String

                                val downMoa = EBookDownloadECOMoa()
                                if (ebookData.fileType.isNotEmpty() && ebookData.fileType.equals("PDF", true)) {
                                    fileName = downMoa.downPdf(
                                        activity = mActivity,
                                        strOrderLicense = ebookData.firstLicense,
                                        comCode = comCode,
                                        task = progressBar
                                    )
                                    toStoreLicense = downMoa.getLicenseInfo()
                                    if (fileName == null) {
                                        return@withContext null
                                    }
                                } else {
                                    fileName = downMoa.down(
                                        activity = mActivity,
                                        strOrderLicense = ebookData.firstLicense,
                                        comCode = comCode,
                                        _task = progressBar
                                    )

                                    toStoreLicense = downMoa.getLicenseInfo()

                                    if (fileName == null) {
                                        return@withContext null
                                    }
                                }

                                if (toStoreLicense.isEmpty()) {
                                    return@withContext null
                                }
                                returnObj[0] = toStoreLicense
                                returnObj[3] = ebookData
                                returnObj[4] = 1

                            } else {
                                    mSocket.emit("working_error", "not found license")
                            }
                        }

                        "YES24" -> {
                            val libCode = ebookData.libCode

                            if (ebookData == null || ebookData.returnLink == null || ebookData.returnLink.trim().isEmpty()) {
                                return@withContext  null
                            }

                            val link = ebookData.returnLink
                            val ebookUserId = Uri.parse(link).getQueryParameter("user_id").toString()
                            ebookData.ePubId = ebookUserId
//                            val ebookUserId = userId
                            val ebookUserPw = ebookUserId
                            val downYes24 = EBookDownloadYES24(
                                mActivity,
                                mActivity.resources.getString(R.string.sdcard_dir_name),
                                ebookUserId,
                                ebookUserPw,
                                ebookId,
                                ebookData.drmUrlInfo,
                                ebookData.eBookLibName
                            )
                            returnObj[0] = downYes24
                            returnObj[3] = ebookData
                            fileName = downYes24.epubFileDownload(progressBar)
                            returnObj[4] = 1

                            if (fileName == null) {
                                return@withContext null
                            }
                        }

                        "OPMS_MARKANY", "OPMS" -> {
                                if (ebookData.downloadLink.isNotEmpty()) {
                                    progressBar.progressTask(5)
                                    ebookData.downloadLink =
                                        ebookData.downloadLink + LibrosUtil.getOriginDeviceId(
                                            mActivity
                                        ).toString()
                                    val downOPMS = EBookDownloadOPMS()
                                    fileName = downOPMS.down(
                                        mActivity,
                                        ebookData.downloadLink,
                                        progressBar,
                                        comCode
                                    )
                                }

                            if (fileName == null) {
                                mSocket.emit("working_error", "file not download")
                                return@withContext null
                            }
                            progressBar.progressTask(98)
                            var insertResult: Int = 1
                            try {
//                                EbookDownloadDBFacade(mActivity).insertData(
//                                    ebook = EbookListVO(
//                                        fileName = fileName!!,
//                                        fileType = ebookData.fileType,
//                                        isbn = ebookData.isbn,
//                                        bookId = ebookData.id,
//                                        returnDate = "",
//                                        title = ebookData.title,
//                                        libName = ebookData.libName,
//                                        libCode = ebookData.libCode,
//                                        drmInfo = "",
//                                        thumbnail = ebookData.thumbnail,
//                                        comKey = ebookData.lentKey,
//                                        lentKey = ebookData.lentKey,
//                                        useStartTime = ebookData.useStartTime,
//                                        useEndTime = ebookData.useEndTime,
//                                        downloadYn = "Y",
//                                        drm = ebookData.comCode
//                                    )
//                                )
//                                ViewerDBFacade(mActivity).insertOrUpdateBook(
//                                    ebookData.lentKey,
//                                    "${LibrosUtil.getEPUBRootPath(mActivity)}/${
//                                        mActivity.applicationContext.getString(
//                                            R.string.sdcard_dir_name
//                                        )
//                                    }/$fileName/$fileName",
//                                    null
//                                )
                                var path = ""
                                var zipFile : File? = null
                                val mSocket = (mActivity as MainActivity).mSocket

                                withContext(IO){
                                    path = "${LibrosUtil.getEPUBRootPath(mActivity)}/${mActivity.applicationContext.getString(R.string.sdcard_dir_name)}/${fileName}"
                                    zipFile = CompressZip().compress("$path/$fileName", path, fileName, mSocket)
                                }

                                runBlocking {
//                                    val mSocket = (mActivity as MainActivity).mSocket
                                    LibrosUpload().upload(ebookData.uploadUrl, zipFile!!, ebookData, mSocket)
                                }
//                                val deleteFile: File = File(path)
////
//                                if (deleteFile != null && deleteFile.exists()) {
//                                    FileManager().deleteFolder(deleteFile)
//                                }


                            } catch (e: java.lang.Exception) {
                                insertResult = -1
                            }

                            progressBar.progressTask(99)
                            returnObj[0] = null
                            returnObj[4] = insertResult

                        }
                        "BOOK_JAM", "BA" -> {
                            if (ebookData.downloadLink.isNotEmpty()) {
                                if (downloadUrl.isNotEmpty()) {
                                    var downBookJam = EBookDownloadBOOJAM()
                                    fileName =
                                        downBookJam.down(mActivity, downloadUrl, progressBar, null)
                                }
                                if (fileName == null) {
                                    return@withContext null
                                }
                                progressBar.progressTask(98)
                                var insertResult: Int = 1
                                try {
                                    EbookDownloadDBFacade(mActivity).insertData(
                                        ebook = EbookListVO(
                                            fileName = fileName!!,
                                            fileType = "",
                                            isbn = ebookData.isbn,
                                            bookId = ebookData.id,
                                            returnDate = "",
                                            title = ebookData.title,
                                            libName = ebookData.libName,
                                            libCode = ebookData.libCode,
                                            drmInfo = "",
                                            thumbnail = ebookData.thumbnail,
                                            comKey = ebookData.lentKey,
                                            lentKey = ebookData.lentKey,
                                            useStartTime = ebookData.useStartTime,
                                            useEndTime = ebookData.useEndTime,
                                            downloadYn = "Y",
                                            drm = ebookData.comCode
                                        )
                                    )
                                } catch (e: java.lang.Exception) {
                                    insertResult = -1
                                }

                                progressBar.progressTask(99)
                                returnObj[0] = null
                                returnObj[4] = insertResult
                            }
                        }
                        "ALADIN" -> {

                        }
                    }
                    returnObj[1] = fileName
                    returnObj[2] = comCode
                } catch (e: Exception) {
                    Log.d("TESTERROR", e.toString())
                    mSocket.emit("working_error", e.toString())
                    LibrosLog.print(e.toString())
                }
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

            if (returnObj[1] == null){
//                LibrosUtil.showMsgWindow(mActivity, "알림", "오류입니다. 관리자에게 문의하세요", "확인")
                mSocket.emit("working_error", "Error")
                mSocket.emit("status", "ready")
                return@launch
            }

            withContext(IO) {
                if (returnObj[1] != null && returnObj[4] != -1) {
                    when (comCode.toUpperCase(Locale.getDefault())) {
                        "ECO_MOA" -> {
                            val imageTask = EBookImageDrmEcoMoaAsyncTask(mActivity, ebookData.fileType)
                            imageTask.task(returnObj[0], returnObj[1], returnObj[3])
                        }
                        "YES24" -> {
                            val imageTask = EBookImageDrmYES24Task(mActivity)
                            imageTask.task(returnObj[0], returnObj[1], returnObj[3])
                        }
                        else -> {
                            val intent = Intent(GlobalVariable.DOWNLOAD_RESULT)
                            intent.putExtra("lent_key", ebookData.lentKey)
                            mActivity.sendBroadcast(intent)
                        }
                    }
                }
            }
        }
        return null
    }

    init {
        mActivity = _activity
        ebookData = _ebookData
        ebookLibName = ebookData.eBookLibName
        downloadPlace = _downloadPlace
        myActivity = mActivity as FragmentActivity
        mSocket = socket
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var mActivity: Activity
        lateinit var ebookLibName: String
        lateinit var ebookData: MyEbookListModel
        lateinit var downloadPlace: String
        lateinit var myActivity: FragmentActivity
        lateinit var mSocket: Socket
        var progressBar = CustomProgressFragment()
    }
}