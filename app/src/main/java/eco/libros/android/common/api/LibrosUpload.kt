package eco.libros.android.common.api

import android.util.Log
import eco.libros.android.myContents.MyEbookListModel
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.File

class LibrosUpload {

    suspend fun upload(url : String, file : File, ebookdata : MyEbookListModel, mSocket: Socket) {
        mSocket.emit("status", "upload")
        val client = OkHttpClient().newBuilder().build()
        val mediaType = "text/plain".toMediaTypeOrNull()
        val body =
            MultipartBody.Builder().setType(MultipartBody.FORM)
                .addFormDataPart("_file",
                    file.name,
                    RequestBody.create(
                        "application/octet-stream".toMediaTypeOrNull(),
                        file))
                .addFormDataPart("ISBN", ebookdata.isbn)
                .addFormDataPart("title",ebookdata.title)
                .addFormDataPart("author",ebookdata.author)
                .addFormDataPart("comcode",ebookdata.comCode)
                .addFormDataPart("id",ebookdata.id)
                .addFormDataPart("epubID", ebookdata.ePubId.toString())
                .addFormDataPart("cover",ebookdata.cover)
                .addFormDataPart("thumbnail",ebookdata.thumbnail)
                .build()

        val request = Request.Builder().url(url).method("POST", body).build()
        withContext(Dispatchers.IO){
            withContext(Dispatchers.IO){
                client.newCall(request).execute()
            }
        }
        mSocket.emit("working_finish", file.name)
        mSocket.emit("status", "finish")
        mSocket.emit("status", "ready")
//        client.newCall(request).execute()
//        CoroutineScope(Dispatchers.IO).launch {
//
//        }

//
//
//        return response
    }
}