package eco.libros.android.ebook.download

import android.app.Activity
import android.util.Log
import com.inn.moadrmlib.MoaDrmJni
import com.inn.moadrmlib.MoaDrmJni.moadrmLicenseRtn
import com.inn.moadrmlib.MoaDrmWrapper
import eco.libros.android.R
import eco.libros.android.common.CustomProgressFragment
import eco.libros.android.common.utill.LibrosLog
import eco.libros.android.common.utill.LibrosUtil
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.withContext
import java.io.*
import java.net.URL
import java.net.URLConnection
import java.util.*

class EBookDownloadECOMoa {

    companion object {
        lateinit var toStoreLicense: String
        lateinit var task: CustomProgressFragment
    }

    suspend fun down(activity: Activity, strOrderLicense: String, comCode: String, _task: CustomProgressFragment): String {
        task = _task
        var filePath = LibrosUtil.getEPUBRootPath(activity) + "/" + activity.resources.getString(R.string.sdcard_dir_name)
        val uuid = UUID.randomUUID().toString()
        var fileName: String? = "$uuid.epub"
        var makeDir: Boolean = false
        var dir: File = File(filePath)

        makeDir = if (!dir.exists())
            dir.mkdir()
        else
            true

        if (makeDir) {
            filePath = "$filePath/$uuid"
            dir = File(filePath)

            dir.mkdir()
        }

        val file = File(filePath, fileName)
        if (file.exists()) {
            file.delete()
        }

        try {
            //////////////////////////////////////////////////////////////////////
            // TODO: 주문라이선스로부터, 패키징된 파일을 받고, 인증을 받는 부분.
            //////////////////////////////////////////////////////////////////////

            val downEPUBPath = "$filePath/$fileName" //패키징된 파일을 임시로 다운로드 받을 폴더
            var errCode: Long = -1

            // 1. 주문 라이선스를 읽어서 JavaString으로 만들고, moadrmProcess 함수호출  (주문라이선스는 임시로 ORD123_R.XML에 넣고 불러온다고 가정함)
            // 1-1. 주문라이선스를 읽어냅니다.
            //	    		String strOrderLicense = ReadAssertFile("ORD123_R.XML");
            //		    	Log.v(TAG, "[APP:moadrmProcess] 주문라이선스 byte size:" + strOrderLicense.getBytes().length);

            // 1-2. 주무나이선스의 변조 검사
            errCode = MoaDrmWrapper.moadrmProcess(strOrderLicense)

            var downloadURL = ""
            if (errCode.toInt() == 0) {
                val rtn = MoaDrmWrapper.getMoadrmProcessResultRtn()
                errCode = MoaDrmWrapper.getMoadrmProcessResult(rtn)
                downloadURL = rtn.DOWN_URL
            }

            if (errCode.toInt() == 0 && downloadURL != "") {
                val url = URL(downloadURL)

                val connection: URLConnection = url.openConnection()
                connection.connect()

                val lengthOfFile = connection.contentLength
                withContext(IO) {
                    BufferedInputStream(url.openStream()).use {
                        input ->
                        FileOutputStream(downEPUBPath).use {
                            output ->
                            input.copyTo(output)
//                        val data = ByteArray(1024)
//                        var readCount: Int?
//                        var total: Long = 0
//                        while ((input.read(data).also { readCount = it }) != -1) {
//                            input.copyTo(output)
//                            output.write(data, 0, readCount!!)
//                            total += readCount!!
//                            val i = (total * 80 / lengthOfFile).toInt()
//                            task.progressTask(i)
//                        }
                        }
                    }
                }
            }

            // 4. 인증(2차 라이선스) 받아오기 (받아온 라이선스는, meta-inf/license.xml 에 파일로 저장하거나, DB에 저장했다가, 파일을 풀 때 불러다 쓴다.)
            val rtn = moadrmLicenseRtn()
            errCode = MoaDrmWrapper.moadrmLicense(LibrosUtil.getOriginDeviceId(activity), rtn)

            if (errCode.toInt() == 0) {  // 성공
                toStoreLicense = String(rtn.arrLicense)
                LibrosLog.print(toStoreLicense + "down2")
            }

            // 5. 압축을 풀거나, 압축채로도 복호화 할 수 있다.
            // 이 샘플에선 압축을 풀어둔 상태를 기준으로 보여준다.
            // 자세한 부분은, 아래 Use_AsynTask 에서 확인

            fileName = FileManager().unzip(activity, file, fileName, uuid, task)
            Log.d("TESTFILENAME", fileName!!)
        } catch (e: Exception) {
            LibrosLog.print(e.toString() + "down")
            return null.toString()
        }
        return fileName
    }

    fun getLicenseInfo(): String {
        return toStoreLicense
    }

    fun downPdf(activity: Activity, strOrderLicense: String, task: CustomProgressFragment, comCode: String): String {
        var filePath = activity.getExternalFilesDir(null)?.absolutePath

        var fileName = "book.zip"

        var dir = File(filePath)

        if (dir.exists()) {
            dir.mkdir()
        }

        var file = File(filePath, fileName)

        if (file.exists())
            file.delete()

        try {
            //////////////////////////////////////////////////////////////////////
            // TODO: 주문라이선스로부터, 패키징된 파일을 받고, 인증을 받는 부분.
            //////////////////////////////////////////////////////////////////////

            var downEPUBPath = "$filePath/$fileName"
            var errCode: Long = -1

            errCode = MoaDrmWrapper.moadrmProcess(strOrderLicense)

            var downloadURL = ""
            if (errCode.toInt() == 0) {
                var rtn = MoaDrmWrapper.getMoadrmProcessResultRtn()
                errCode = MoaDrmWrapper.getMoadrmProcessResult(rtn)
                downloadURL = rtn.DOWN_URL
            }

            if (errCode.toInt() == 0 && downloadURL != "") {
                val url = URL(downloadURL)
                val connection = url.openConnection()
                connection.connect()

                var lengthOfFile = connection.contentLength

                BufferedInputStream(url.openStream()).use { input ->
                    val output = FileOutputStream(downEPUBPath)
                    val data = ByteArray(1024)
                    var readCount: Int?
                    var total: Long = 0

                    while ((input.read(data).also { readCount = it }) != -1) {
                        output.write(data, 0, readCount!!)
                        total += readCount!!
                        val i = (total * 80 / lengthOfFile).toInt()
                        task.progressTask(i)
//                        task.progressTask(i)
//                        sendInt(task,i)
                    }
                    output.close()
                    input.close()
                }

                // 4. 인증(2차 라이선스) 받아오기 (받아온 라이선스는, meta-inf/license.xml 에 파일로 저장하거나, DB에 저장했다가, 파일을 풀 때 불러다 쓴다.)
                val rtn = MoaDrmJni.moadrmLicenseRtn()
                errCode = MoaDrmWrapper.moadrmLicense(LibrosUtil.getOriginDeviceId(activity), rtn)

                if (errCode.toInt() == 0) {  // 성공
                    toStoreLicense = String(rtn.arrLicense)
                    LibrosLog.print(toStoreLicense.toString() + "down2")
                }

                fileName = FileManager().unZipPdf(activity, file, fileName, task)
            }


        } catch (e: IOException) {
            LibrosLog.print(e.toString())
            null
        }
        return fileName
    }

}