package eco.libros.android.common.socket

import android.util.Log
import eco.libros.android.myContents.MyEbookListModel
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter

object SocketManager {
    private const val BASE_URL = "http://192.168.7.207:8000/namespace"

    private val mSocket by lazy {
        IO.socket(BASE_URL).connect()
    }

    fun emit(event : String, message : String){
        mSocket.emit(event, message)
        Log.d("TEST","EMIT")
    }

    fun onTest(): String {
        var data: String? = ""
        mSocket.on("socketTest", Emitter.Listener {

        })
        return data.toString()
    }

    fun onEbookData(): String {
        var data: String? = ""
        mSocket.on("socketTest", Emitter.Listener {
            data = it.toString()
        })
        return data.toString()
    }

    val test = Emitter.Listener {
        mSocket.emit("socketTest", "hi android")
        Log.d("TEST", "Socket is connected")
    }

}