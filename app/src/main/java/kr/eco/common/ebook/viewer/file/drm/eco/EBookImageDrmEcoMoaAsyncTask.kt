package kr.eco.common.ebook.viewer.file.drm.eco

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.util.Log
import android.view.WindowManager
import android.view.WindowManager.BadTokenException
import androidx.fragment.app.FragmentActivity
import eco.libros.android.R
import eco.libros.android.common.ProgressFragment
import eco.libros.android.common.api.LibrosUpload
import eco.libros.android.common.utill.LibrosLog
import eco.libros.android.common.utill.LibrosUtil
import eco.libros.android.common.variable.GlobalVariable
import eco.libros.android.ebook.download.FileManager
import eco.libros.android.myContents.MyEbookListModel
import eco.libros.android.ui.MainActivity
import eco.libros.android.utils.CompressZip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.xml.sax.SAXException
import java.io.File
import java.io.IOException
import javax.xml.parsers.ParserConfigurationException

class EBookImageDrmEcoMoaAsyncTask(_activity: Activity, _fileType: String) {
    var progressBar = ProgressFragment()

    init {
        mActivity = _activity
        fileType = _fileType
        myActivity = mActivity as FragmentActivity
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var mActivity: Activity
        lateinit var fileType: String
        lateinit var myActivity: FragmentActivity
    }

    fun task(_drmLicence: Any?, _fileName: Any?, _eBookInfo: Any?) {
        CoroutineScope(Main).launch {
            onPreExecute()
            val obj = arrayOfNulls<Any>(5)

            withContext(IO) {
                var drmLicence = _drmLicence.toString()
                var fileName = _fileName.toString()
                var eBookInfo: MyEbookListModel = _eBookInfo as MyEbookListModel

                var down = DrmECOMoaRelease(
                    activity = mActivity,
                    eBookLibName = eBookInfo.eBookLibName,
                    filePath = mActivity.resources.getString(
                        R.string.sdcard_dir_name
                    ),
                    _fileName = fileName,
                    moaBookLicense = drmLicence,
                    fileType = fileType
                )

                var result: Boolean = false

                try {
                    if (fileType != null && fileType.equals("PDF", true)) {
                        result = down.setPdfWithoutDrm()
                    } else {
                        result = down.setImageWithoutDrm()
                    }
                } catch (e: IOException) {
                    LibrosLog.print(e.toString())
                    null
                } catch (e: ParserConfigurationException) {
                    LibrosLog.print(e.toString())
                    null
                } catch (e: SAXException) {
                    LibrosLog.print(e.toString())
                    null
                }

                if (result && fileName != null && eBookInfo != null) {
                    updateEBookDownStatic(eBookInfo.libCode)
                }


                obj[0] = result
                obj[1] = eBookInfo
                obj[2] = fileName
                obj[3] = drmLicence

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
                val successResult: Boolean = obj[0] as Boolean
                val eBookData = obj[1] as MyEbookListModel
                val fileName = obj[2].toString()
                val drmLicense = obj[3].toString()

                if (successResult) {
                    if (fileName != null) {
                        withContext(IO) {
//                            EbookDownloadDBFacade(mActivity).insertData(
//                                ebook = EbookListVO(
//                                    fileName = fileName,
//                                    libCode = eBookData.libCode,
//                                    title = eBookData.title,
//                                    libName = eBookData.eBookLibName,
//                                    thumbnail = eBookData.thumbnail,
//                                    bookId = eBookData.id,
//                                    lentKey = eBookData.lentKey,
//                                    comKey = eBookData.lentKey,
//                                    isbn = eBookData.isbn,
//                                    fileType = "EPUB",
//                                    drmInfo = drmLicense,
//                                    drm = eBookData.comCode,
//                                    downloadYn = "Y",
//                                    returnDate = eBookData.lendingExpiredDate,
//                                    useStartTime = eBookData.useStartTime,
//                                    useEndTime = eBookData.useEndTime
//                                )
//                            )
                        }

//                        withContext(IO){
//                            ViewerDBFacade(mActivity).insertOrUpdateBook(
//                                eBookData.lentKey,"${LibrosUtil.getEPUBRootPath(mActivity)}/${mActivity.resources.getString(R.string.sdcard_dir_name)}/${fileName}/${fileName}",null
//                            )
//                        }
                        val mSocket = (mActivity as MainActivity).mSocket
                        val file = arrayOfNulls<String?>(1)

                        // Type the path of the files in here

                        val path = "${LibrosUtil.getEPUBRootPath(mActivity)}/${mActivity.resources.getString(R.string.sdcard_dir_name)}/${fileName}"
//                        file[0] = "$path/$fileName"
//                        FileManager().zipFolder("$path/$fileName","$path/ziptest.zip")
//                        FileManager().zipFile(file, "$path/$fileName/zipTest.zip")

                        val zipFile = CompressZip().compress(
                            "$path/$fileName",
                            path,
                            fileName,
                            mSocket
                        )

//                        LibrosRepository().checkAuthRepo(tempId, userAuthNum,encryptYn,resources.getString(R.string.device_type))?.let { response ->
//                            if(response.isSuccessful){
//                                Log.d("Testauth,",response.raw().toString())
//                                return@withContext response.body()
//                            } else
//                                return@withContext null
//                        }
//                        showMsgDialog("알림", "다운로드 되었습니다.", "확인",eBookData.lentKey)
                        runBlocking {
                            LibrosUpload().upload(eBookData.uploadUrl, zipFile, eBookData, mSocket)
                        }
//                        val deleteFile: File = File(path)

//                        if (deleteFile != null && deleteFile.exists()) {
//                            FileManager().deleteFolder(deleteFile)
//                        }



                    } else {
                        LibrosUtil.showMsgWindow(
                            activity = mActivity,
                            title = "알림",
                            msg = "다운로드 되지 않았습니다.",
                            btnMsg = "확인"
                        )
                    }
                } else {
                    if (fileName != null) {
                        withContext(IO) {
                            var path: String? = null

                            if (eBookData.fileType != null && eBookData.fileType.equals(
                                    "pdf",
                                    true
                                )
                            ) {
                                path = mActivity.getExternalFilesDir(null)?.absolutePath
                            } else {
                                path =
                                    mActivity.getExternalFilesDir(null)?.path+ "/" + mActivity.resources.getString(
                                        R.string.sdcard_dir_name
                                    )
                            }

                            var file: File = File(path, fileName)

                            if (file != null && file.exists()) {
                                FileManager().deleteFolder(file)
                            }
                        }
                    }
                    LibrosUtil.showMsgWindow(
                        activity = mActivity,
                        title = "알림",
                        msg = "다운로드 되지 않았습니다.",
                        btnMsg = "확인"
                    )
                }
            }
        }
    }

    private suspend fun onPreExecute() {
        try {
            progressBar = ProgressFragment.newInstance("데이터 처리 중입니다")
            progressBar.show(myActivity.supportFragmentManager, "progress")
        } catch (e: WindowManager.BadTokenException) {
            Log.e("error", e.message.toString())
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

    private fun showMsgDialog(title: String, content: String, btnMsg: String, lentKey : String) {
        try {
            AlertDialog.Builder(mActivity)
                .setTitle(title)
                .setMessage(content)
                .setPositiveButton(
                    btnMsg
                ) { dialog, _ ->
                    dialog.dismiss()

                    val intent = Intent(GlobalVariable.DOWNLOAD_RESULT)
//                    intent.action = GlobalVariable.DOWNLOAD_RESULT
                    intent.putExtra("lent_key",lentKey)
                    mActivity.sendBroadcast(intent)
                }.create().show()
        } catch (e: BadTokenException) {
            // TODO: handle exception
            LibrosLog.print(e.toString())
        }
    }
}