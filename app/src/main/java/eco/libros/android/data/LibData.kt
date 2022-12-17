package eco.libros.android.data

import android.util.Log
import eco.libros.android.common.http.LibrosJsonNetwork
import eco.libros.android.common.model.LibrosDataVO
import org.json.JSONException
import org.json.JSONObject

class LibData{

    fun getNfcTagRegNo(url: String?): LibrosDataVO {
        val common = LibrosDataVO()
        val jsonNet = LibrosJsonNetwork()
        val jsonStr: String = jsonNet.getJsonData(url).toString()
        common.setNextwrokSuccess(jsonNet.isNetworkSuccess())
        if (!jsonNet.isNetworkSuccess()) {
            return common
        }
        try {
            val jsonObject = JSONObject(jsonStr)
            val resultObject = jsonObject.getJSONObject("Result")
            val resultCode = resultObject.getString("ResultCode")
            val resultMessage = resultObject.getString("ResultMessage")
            if (jsonObject.has("Contents")) {
                val contentJson = jsonObject.getJSONObject("Contents")
                if (contentJson.has("BookRegNo")) {
                    common.setContent(contentJson.getString("BookRegNo"))
                }
            }
            common.setResultCode(resultCode)
            common.setResultMsg(resultMessage)
        } catch (e: JSONException) {
            // TODO Auto-generated catch block
            Log.e("error",e.message.toString())
        }
        return common
    }
}