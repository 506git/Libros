package kr.eco.common.ebook.viewer.file.drm.eco

import android.app.Activity
import android.util.Log
import com.inn.moadrmlib.MoaDrmJni
import com.inn.moadrmlib.MoaDrmJni.moadrmInitializeForUseRtn
import com.inn.moadrmlib.MoaDrmWrapper
import eco.libros.android.common.utill.LibrosLog
import eco.libros.android.common.utill.LibrosUtil
import eco.libros.android.ebook.download.FileManager
import kr.eco.common.ebook.viewer.parser.EcoMoaImageEncryptionXMLHandler
import kr.eco.common.ebook.viewer.parser.EcoMoaPdfEncryptionXMLHandler
import org.xml.sax.InputSource
import org.xml.sax.SAXException
import java.io.*
import java.util.*
import javax.xml.parsers.ParserConfigurationException
import javax.xml.parsers.SAXParserFactory

class DrmECOMoaRelease(
    activity: Activity,
    eBookLibName: String,
    filePath: String,
    _fileName: String,
    moaBookLicense: String,
    fileType: String
) {
    private var activity: Activity? = null
    private var eBookFileFolderName: String = ""
    private var fileName: String? = null

    init {
        this.activity = activity
        this.fileName = _fileName

        eBookFileFolderName =
            if (fileType != null && fileType.equals("PDF", true))
                "${activity.getExternalFilesDir(null)?.absolutePath}/$fileName/$fileName/"
            else
                "${LibrosUtil.getEPUBRootPath(activity)}/$filePath/$fileName/$fileName/"

        var encryptionFilePath = "${eBookFileFolderName}meta-inf/encryption.xml"

        if (!File(encryptionFilePath).exists()){
            encryptionFilePath = eBookFileFolderName+"META-INF/encryption.xml"
        }

        var strEncryptionInfo: String

        try{
            strEncryptionInfo = String(getReadFile(encryptionFilePath)!!, Charsets.UTF_8)
        }catch (e: UnsupportedEncodingException){
            strEncryptionInfo = String(getReadFile(encryptionFilePath)!!)
        }

        val rtn = moadrmInitializeForUseRtn()
        MoaDrmWrapper.moadrmInitializeForUse(
            strEncryptionInfo,
            LibrosUtil.getOriginDeviceId(activity.baseContext),
            moaBookLicense,
            "EPUB",
            rtn
        )
    }

    @Throws(IOException::class, ParserConfigurationException::class, SAXException::class)
    fun setPdfWithoutDrm(): Boolean{
        var isSuccessDownLoad = false

        val parserFactory = SAXParserFactory.newInstance()
        val saxParser = parserFactory.newSAXParser()
        val reader = saxParser.xmlReader

        val rootFile: File = File(eBookFileFolderName + "/META-INF", "encryption.xml")

        if (rootFile.exists()) {

            val encryptionXmlHandler = EcoMoaPdfEncryptionXMLHandler()
            reader.contentHandler = encryptionXmlHandler
            reader.parse(InputSource(InputStreamReader(FileInputStream(rootFile))))
            val pdfList: ArrayList<String> = encryptionXmlHandler.getImagesPath()!!

            var decryptedImage: ByteArray? = null

            for (i in pdfList.indices) {

                decryptedImage = releaseDrmByte(eBookFileFolderName + pdfList[i])

                val oriFile: File = File(eBookFileFolderName + pdfList[i])

                if (oriFile.exists()) {
                    FileManager().deleteFolder(oriFile)
                }
                if (decryptedImage != null && decryptedImage.isNotEmpty()) {
                    FileManager().writePdfFile(
                        activity,
                        decryptedImage,
                        eBookFileFolderName + pdfList[i]
                    )
                }
                decryptedImage = null
//                task.onProgressUpdate((i / pdfList.size * 100))
            }
            isSuccessDownLoad = true
        }

        return isSuccessDownLoad
    }

    private fun releaseDrmByte(openFileName: String): ByteArray? {
        // 1. ?????? ????????? ???????????? ?????? ?????????.
        // ????????? ????????????, /meta-inf/encryption.xml ??? ?????????. ??? ????????? ???????????? ????????? ????????? ?????????

        // 2. ????????? ???????????? ?????? ????????? ????????? ??????.
        // ??????  ??????????????? "OEBPS/toc.ncx" ???????????????,

        var requestFileName: String? = null
        requestFileName = if (openFileName.contains(fileName.toString())){
            val index: Int = openFileName.lastIndexOf(fileName!!)
            openFileName.substring(index + fileName!!.length + 1)
        } else{
            openFileName
        }

        if (requestFileName == null){
            return null
        }

        val isEncrypted = MoaDrmWrapper.moadrmIsEncrypt(requestFileName)

        if (isEncrypted){
            // ???????????? ?????????
            // ????????? Case1) path?????? ????????? ??????
            // ?????? ????????? ??????????????? ???????????????, ?????? ?????? ????????????. ???????????? ???????????? ?????????
            val rtn = MoaDrmJni.moadrmFileDecryptRtn()
            MoaDrmWrapper.moadrmFileDecrypt(openFileName, rtn)

            return rtn.arrDecryptData
        } else
            return getReadFile(openFileName)
    }

    @Throws(IOException::class, ParserConfigurationException::class, SAXException::class)
    fun setImageWithoutDrm(): Boolean {

        var isSuccessDownLoad = false

        val parserFactory = SAXParserFactory.newInstance()
        val saxParser = parserFactory.newSAXParser()
        val reader = saxParser.xmlReader

        val rootFile: File = File("$eBookFileFolderName/META-INF", "encryption.xml")

        if (rootFile.exists()) {
            val encryptionXmlHandler = EcoMoaImageEncryptionXMLHandler()
            reader.contentHandler = encryptionXmlHandler
            reader.parse(InputSource(InputStreamReader(FileInputStream(rootFile))))
            val imageList: ArrayList<String> = encryptionXmlHandler.getImagesPath()!!

            var decryptedImage: ByteArray? = null

            for (i in imageList.indices) {
                decryptedImage = releaseDrmByte(eBookFileFolderName + imageList[i])

                if (decryptedImage != null && decryptedImage.isNotEmpty()) {
                    FileManager().writeFile(decryptedImage, eBookFileFolderName + imageList[i])
                }

                decryptedImage = null

            }
            isSuccessDownLoad = true
        }
        return isSuccessDownLoad
    }

    private fun getReadFile(path: String) : ByteArray? {
        if(path == null){
            return null
        }

        var fileContent: String? = null
        val file = File(path)

        var reader : BufferedReader? = null
        val builder = StringBuilder()

        try{
            BufferedReader(FileReader(file)).forEachLine {
                builder.append(it)
            }
            var line: String

//            while (reader.readLine().also { line = it } != null) {
//                builder.append(line)
//            }


            fileContent = builder.toString()
        } catch (e: IOException){
            LibrosLog.print(e.toString())
        } finally {
//            if (reader != null){
//                try{
//                reader.close()
//                }catch (e: IOException){
//                    LibrosLog.print(e.toString())
//                }
//            }
        }

        return if (fileContent == null){
            null
        } else
            fileContent.toByteArray()
    }
}