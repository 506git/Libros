package eco.libros.android.ebook

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.fragment.app.FragmentActivity
import eco.libros.android.R
import eco.libros.android.common.CustomProgressFragment
import eco.libros.android.common.ProgressFragment
import eco.libros.android.common.utill.LibrosLog
import eco.libros.android.common.utill.LibrosUtil
import eco.libros.android.ebook.download.FileManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.smartandwise.eco_epub3_module.Drm.yes24.AndroidZipUtil4IDS
import kr.co.smartandwise.eco_epub3_module.Drm.yes24.parser.ImageXMLHandler
import kr.co.smartandwise.eco_epub3_module.Drm.yes24.parser.MetaXMLHandler
import kr.eco.common.ebook.viewer.file.drm.eco.DrmECOMoaRelease
import kr.eco.common.ebook.viewer.file.drm.yes24.IDSClientDecrypt
import org.xml.sax.InputSource
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.*
import javax.xml.parsers.SAXParserFactory

class PreloadAsyncTask(_activity: Activity, _epubFileName: String, _drmType: String?, _epubBookData: String?, _customPath: String?, _moaLicense: String?) {


    init {
        activity = _activity
        epubFileName = _epubFileName
        drmType = _drmType
        epubBookData = _epubBookData
        filePath = _customPath
        moaLicense = _moaLicense
        myActivity = activity as FragmentActivity
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private lateinit var activity: Activity
        private lateinit var epubFileName: String
        private var drmType: String? = null
        private var epubBookData: String? = null
        private var filePath: String? = null
        private var moaLicense: String? = null
        lateinit var myActivity: FragmentActivity
        var progressBar = ProgressFragment()
    }

    suspend fun task(_userId: String, _libName: String) {
        var userId = _userId
        var libName = _libName
        CoroutineScope(Main).launch {
            try {
                progressBar = ProgressFragment.newInstance("파일 작업 중입니다")
                progressBar.show(myActivity.supportFragmentManager, "progress")
            } catch (e: WindowManager.BadTokenException) {
                Log.e("error", e.message.toString())
            }

            val decryptSuccess = withContext(IO) {
                try {
                    when (drmType) {
                        "YES24" -> {
                            decryptYES24(epubFileName, userId, libName, filePath)
                        }
                        "ECO_MOA" -> {
                            Log.d("TESTECOM", "ECO_MOA")
                            decryptECOMoa(epubFileName, filePath, moaLicense, progressBar)
                        }
                        else -> false
                    }
                } catch (e: Exception) {
                    LibrosLog.print(e.toString())
                    false
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
            withContext(Main) {
                if (decryptSuccess) {
                    activity.startActivity(Intent(activity, EpubViewer::class.java).apply {
                        putExtra("EPUB_BOOK_DATA", epubBookData)
                        putExtras(Bundle().apply {
                            putString("drmType", drmType)
                            putString("epubFileName", epubFileName)
                            putString("filePath", filePath)
                        })
                    })
                } else {
                    LibrosUtil.showMsgWindow(
                            activity,
                            "알림",
                            "파일 작업 실패 \n 전자책을 다시 다운로드 해 주시기 바랍니다.",
                            "확인"
                    )
                }
            }
        }


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

    private fun decryptECOMoa(_fileName: String, filePath: String?, storeLicense: String?, task: ProgressFragment): Boolean {
        var fileName = _fileName
        var isSuccessDownLoad = false

        var sdCardDirName = ""

//        task.progressTask(0, "읽는 중")
        sdCardDirName =
                if (filePath != null && filePath.trim().isNotEmpty()) {
                    filePath
                } else {
                    activity.getString(R.string.sdcard_dir_name)
                }
        val folderName = fileName
        val eBookFileFolderName = "${LibrosUtil.getEPUBRootPath(activity)}/$sdCardDirName/"
        var extractFileName = fileName

        if (fileName.contains(".epub")) {
            val index: Int? = fileName.indexOf(".epub")
            extractFileName = fileName.substring(0, index!!)
        } else {
            fileName += ".epub"
        }

        val extractFileDir = File(eBookFileFolderName, "$folderName/$extractFileName")
        if (!extractFileDir.exists()) {
            var res = extractFileDir.mkdir()
            if (!res) throw RuntimeException("mkdir failed : ${extractFileDir.path}")
        } else {
            return true
        }

        val decompressedFileName: String? =
                FileManager().unzipEpub(activity, null, fileName, extractFileName)

        val down = DrmECOMoaRelease(
                activity = activity,
                eBookLibName = "",
                filePath = activity.resources.getString(R.string.sdcard_dir_name),
                _fileName = decompressedFileName!!,
                moaBookLicense = storeLicense.toString(),
                fileType = "EPUB"
        )
        isSuccessDownLoad = down.setImageWithoutDrm()

        return isSuccessDownLoad
    }
}