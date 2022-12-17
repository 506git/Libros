package eco.libros.android.common.http

import android.util.Log
import eco.libros.android.common.utill.LibrosLog
import eco.libros.android.common.variable.GlobalVariable
import org.apache.http.util.ByteArrayBuffer
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

class LibrosJsonNetwork {
    private var isSuccess = false

    fun getJsonData(urlStr: String?): String? {
        if (urlStr == null) {
            return null
        }
        isSuccess = false
        var `is`: InputStream? = null
        var reader: BufferedReader? = null
        val builder = StringBuilder()
        try {
            val url = URL(urlStr)
            val conn = url.openConnection() as HttpURLConnection
            conn.readTimeout = GlobalVariable.TIMEOUT_SOCKET
            conn.connectTimeout = GlobalVariable.TIMEOUT_CONNECTION
            conn.requestMethod = "GET"
            conn.doInput = true
            `is` = conn.inputStream
            val statusCode = conn.responseCode
            if (statusCode == 200) {
                reader = BufferedReader(InputStreamReader(`is`, "UTF-8"))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    builder.append(line)
                }
                isSuccess = true
            }
        }  catch (e: IOException) {
            Log.e("error", e.message.toString())
        } catch (e: OutOfMemoryError) {
            Log.e("error", e.message.toString())
        } finally {
            try {
                reader?.close()
                `is`?.close()
            } catch (e: IOException) {
                // TODO Auto-generated catch block
                Log.e("error", e.message.toString())
            }
        }
        if (builder != null) {
            Log.e("error", builder.toString())
        } else {
            Log.e("error", "null")
        }
        return builder.toString()
    }
    fun isNetworkSuccess(): Boolean {
        return isSuccess
    }

    fun getSqlFile(sqlUrl: String?): ByteArray? {
        var sqlByteFile: ByteArray? = null
        var updateSqlURL: URL? = null
        var connection: HttpURLConnection? = null
        var input: InputStream? = null
        var bis: BufferedInputStream? = null

        try {
            updateSqlURL = URL(sqlUrl)
            connection = updateSqlURL.openConnection() as HttpURLConnection
            connection.doInput = true
            connection!!.connect()
            input = connection.inputStream
            BufferedInputStream(input).use{ bis ->
                val baf = ByteArrayBuffer(1024 * 1024 * 5)
                var current = 0
                while (bis.read().also { current = it } != -1) {
                    baf.append(current)
                }
                sqlByteFile = baf.toByteArray()
            }

        } catch (e: IOException) {
            //not handle
            LibrosLog.print(e.toString())
        } finally {
            try {
                input?.close()
                connection?.disconnect()
            } catch (e: IOException) {
                //not handle
                LibrosLog.print(e.toString())
            }
        }
        return sqlByteFile
    }
//    fun getPostJsonData(url: String?, serviceName: String?, userImage: String?, userId: String?, enc: String?, deviceType: String?): String? {
//        if (url == null) {
//            return null
//        }
//        isNetworkSuccess = false
//        var `is`: InputStream? = null
//        var reader: BufferedReader? = null
//        val builder = StringBuilder()
//        try {
//            val httpClient: HttpClient = DefaultHttpClient()
//            val request = HttpPost(url)
//            val nameValue: Vector<BasicNameValuePair> = Vector<BasicNameValuePair>()
//            nameValue.add(BasicNameValuePair("serviceName", serviceName))
//            nameValue.add(BasicNameValuePair("userId", URLDecoder.decode(userId, "utf-8")))
//            nameValue.add(BasicNameValuePair("encryptFromId", enc))
//            nameValue.add(BasicNameValuePair("profileImage", userImage))
//            nameValue.add(BasicNameValuePair("deviceType", deviceType))
//            val result: HttpEntity = UrlEncodedFormEntity(nameValue, "utf-8")
//            request.setEntity(result)
//            val response: HttpResponse = httpClient.execute(request)
//            val status: Int = response.getStatusLine().getStatusCode()
//            if (status == HttpStatus.SC_OK) {
//                val entity: HttpEntity = response.getEntity()
//                `is` = entity.getContent()
//                reader = BufferedReader(InputStreamReader(`is`))
//                var line: String?
//                while (reader.readLine().also { line = it } != null) {
//                    builder.append(line)
//                }
//                isNetworkSuccess = true
//            }
//        } catch (e: IOException) {
//            // TODO: handle exception
//            LibropiaLog.show(e.message)
//        } finally {
//            try {
//                reader?.close()
//                `is`?.close()
//            } catch (e: IOException) {
//                // TODO Auto-generated catch block
//                LibropiaLog.show(e.message)
//            }
//        }
//        return builder.toString()
//    }
}
