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
import eco.libros.android.common.api.LibrosUpload
import eco.libros.android.common.utill.LibrosLog
import eco.libros.android.common.utill.LibrosUtil
import eco.libros.android.common.variable.GlobalVariable
import eco.libros.android.myContents.MyEbookListModel
import eco.libros.android.ui.MainActivity
import eco.libros.android.utils.CompressZip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kr.co.smartandwise.eco_epub3_module.Drm.yes24.AndroidZipUtil4IDS
import kr.co.smartandwise.eco_epub3_module.Drm.yes24.parser.ImageXMLHandler
import kr.co.smartandwise.eco_epub3_module.Drm.yes24.parser.MetaXMLHandler
import kr.eco.common.ebook.viewer.file.drm.eco.EBookImageDrmEcoMoaAsyncTask
import kr.eco.common.ebook.viewer.file.drm.yes24.IDSClientDecrypt
import org.xml.sax.InputSource
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.ArrayList
import javax.xml.parsers.SAXParserFactory

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

                obj[0] = result
                obj[1] = _eBookInfo
                obj[2] = fileName
            }

            withContext(Main) {
                val successResult = obj[0] as Boolean
                val eBookInfo = obj[1] as MyEbookListModel
                val fileName = obj[2] as String

                if (successResult) {
                    if (fileName != null) {
//                        withContext(IO) {
//                            EbookDownloadDBFacade(activity).insertData(
//                                    ebook = EbookListVO(
//                                            fileName = fileName,
//                                            libCode = eBookInfo.libCode,
//                                            title = eBookInfo.title,
//                                            libName = eBookInfo.eBookLibName,
//                                            thumbnail = eBookInfo.thumbnail,
//                                            bookId = eBookInfo.id,
//                                            lentKey = eBookInfo.lentKey,
//                                            comKey = eBookInfo.lentKey,
//                                            isbn = eBookInfo.isbn,
//                                            fileType = "EPUB",
//                                            drmInfo = "",
//                                            drm = eBookInfo.comCode,
//                                            downloadYn = "Y",
//                                            returnDate = eBookInfo.lendingExpiredDate,
//                                            useStartTime = eBookInfo.useStartTime,
//                                            useEndTime = eBookInfo.useEndTime
//                                    )
//                            )
//                        }
//                        withContext(IO) {
//                            ViewerDBFacade(activity).insertOrUpdateBook(
//                                    eBookInfo.lentKey,
//                                    "${LibrosUtil.getEPUBRootPath(activity).toString()}/${activity.applicationContext.getString(R.string.sdcard_dir_name)}/$fileName/${fileName.substring(0, fileName.indexOf(".epub"))}",
//                                    null)
//                        }
                        val subFileName = fileName.substring(0, fileName.indexOf(".epub"))
                        val subPath = "${LibrosUtil.getEPUBRootPath(activity)}/${activity.applicationContext.resources.getString(R.string.sdcard_dir_name)}/$subFileName"
                        val path = "$subPath/${fileName}"

                        decryptYES24(fileName, eBookInfo.ePubId.toString(), eBookInfo.eBookLibName.toString(), null)

                        CompressZip().renameFileOne("${LibrosUtil.getEPUBRootPath(activity)}/${activity.applicationContext.resources.getString(R.string.sdcard_dir_name)}", fileName, subFileName)
                        val mSocket = (activity as MainActivity).mSocket

                        val zipFile = CompressZip().compress(
                            "$subPath/$subFileName",
                            subPath,
                            subFileName,
                            mSocket
                        )

                        runBlocking {
                            val mSocket = (activity as MainActivity).mSocket
                            LibrosUpload().upload(eBookInfo.uploadUrl, zipFile, eBookInfo, mSocket)
                        }
                        val deleteFile: File = File(path)
//
                        if (deleteFile != null && deleteFile.exists()) {
                            FileManager().deleteFolder(deleteFile)
                        }

//                        showMsgDialog("알림", "다운로드 되었습니다.", "확인", eBookInfo.lentKey)
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

    private fun decryptYES24(fileName: String, g_userId: String, g_passwd: String, filePath: String?): Boolean {
        var isSuccessDownLoad = false

        var sdCardDirName = ""

        if (filePath != null && filePath.trim().isNotEmpty()) {
            sdCardDirName = filePath
        } else {
            sdCardDirName = activity.resources.getString(R.string.sdcard_dir_name)
        }

        var folderName = fileName
        var eBookFileFolderName = "${LibrosUtil.getEPUBRootPath(activity)}/$sdCardDirName/"
        var extractFileName = fileName

        if (fileName.contains(".epub")) {
            val index = fileName.indexOf(".epub")
            extractFileName = fileName.substring(0, index)
        }

        val extractFileDir = File(eBookFileFolderName, "$folderName/$extractFileName")

        if (!extractFileDir.exists()) {
            val res = extractFileDir.mkdir()
            if (!res) throw java.lang.RuntimeException("mkdir failed : " + extractFileDir.path)
        } else {
            return true
        }

        val nomediaFile = File(extractFileDir.absolutePath, ".nomedia")
        if (!nomediaFile.exists()) {
            nomediaFile.createNewFile()
        }

        val oriFile: File = File(eBookFileFolderName, "$folderName/$fileName")

        AndroidZipUtil4IDS.decompressFile(oriFile, extractFileDir)


        //FileManager.deleteFiles(oriFile);
        val apiTest = IDSClientDecrypt(activity, sdCardDirName, fileName, g_userId, g_passwd)

        val parserFactory = SAXParserFactory.newInstance()
        val saxParser = parserFactory.newSAXParser()
        val reader = saxParser.xmlReader

        val rootFile = File(extractFileDir.absolutePath + "/META-INF", "container.xml")

        if (rootFile.exists()) {
            val metaXmlHandler = MetaXMLHandler()
            reader.contentHandler = metaXmlHandler
            reader.parse(InputSource(InputStreamReader(FileInputStream(rootFile))))
            val opfPath: String = metaXmlHandler.opfPath

            if (opfPath != null) {
                val folderIndex = opfPath.indexOf("/")
                val mainFolderName = opfPath.substring(0, folderIndex)
                val opfFile = File(extractFileDir.absolutePath, opfPath)

                if (opfFile.exists()) {
                    val imageXmlHandler = ImageXMLHandler()
                    reader.contentHandler = imageXmlHandler
                    reader.parse(InputSource(InputStreamReader(FileInputStream(opfFile))))

                    val imageFilesName: ArrayList<String> = imageXmlHandler.imagesPath
                    val x = 95.0 / imageFilesName.size
                    var totalCount = 0.0

                    for (j in imageFilesName.indices) {
                        val decryptedImage = apiTest.getDecryptedContent(null, mainFolderName + "/" + imageFilesName[j])

                        if (decryptedImage != null && decryptedImage.isNotEmpty()) {
                            FileManager().writeFile(decryptedImage, extractFileDir.absolutePath + "/" + mainFolderName + "/" + imageFilesName[j])
                        }

                        totalCount += x
                    }
                    isSuccessDownLoad = true
                }
            }
        }

        return isSuccessDownLoad
    }
}