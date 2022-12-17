package eco.libros.android.common

import android.util.Log
import com.google.gson.Gson
import eco.libros.android.common.http.LibrosJsonNetwork
import eco.libros.android.common.model.*
import eco.libros.android.myContents.MyContentsVO
import org.json.JSONException
import org.json.JSONObject

class LibrosData{

    fun getSimpleJsonResult(url: String?): LibrosDataVO {
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
            common.setResultCode(resultCode)
            common.setResultMsg(resultMessage)
        } catch (e: JSONException) {
            // TODO Auto-generated catch block
            Log.e("error", e.message.toString())
        }
        return common
    }

    fun getLibrosLoginResult(url: String?): LibrosDataVO {
        val common = LibrosDataVO()
        val jsonNet = LibrosJsonNetwork()
        val jsonStr: String? = jsonNet.getJsonData(url)
        common.setNextwrokSuccess(jsonNet.isNetworkSuccess())
        if (jsonNet.isNetworkSuccess() === false || jsonStr == null) {
            return common
        }
        try {
            val jsonObject = JSONObject(jsonStr)
            val resultObject = jsonObject.getJSONObject("Result")
            val resultCode = resultObject.getString("ResultCode")
            val resultMessage = resultObject.getString("ResultMessage")
            if (jsonObject.has("Contents")) {
                val contentJson = jsonObject.getJSONObject("Contents")
                if (contentJson.has("UserNickName")) {
                    common.setOption(contentJson.getString("UserNickName"))
                }
                if (contentJson.has("UserSex")) {
                    common.setOption1(contentJson.getString("UserSex"))
                }
                if (contentJson.has("IsPushSendYn")) {
                    common.setContent(contentJson.getString("IsPushSendYn"))
                }
            }
            common.setResultCode(resultCode)
            common.setResultMsg(resultMessage)
        } catch (e: JSONException) {
            // TODO Auto-generated catch block
           Log.e("error", e.message.toString())
        }
        return common
    }

    fun getUserLibList(url: String?): LibListModelVO? {

        val common : LibListModelVO?
        val jsonNet = LibrosJsonNetwork()
        val jsonStr: String? = jsonNet.getJsonData(url)
//        common.setNextwrokSuccess(jsonNet.isNetworkSuccess())
        if (!jsonNet.isNetworkSuccess() || jsonStr == null) {

            return null
        }
        return Gson().fromJson(jsonStr, LibListModelVO::class.java)
    }

    fun getMyContentList(url: String?): MyContentsVO? {

        val common : LibListModelVO?
        val jsonNet = LibrosJsonNetwork()
        val jsonStr: String? = jsonNet.getJsonData(url)
//        common.setNextwrokSuccess(jsonNet.isNetworkSuccess())
        if (!jsonNet.isNetworkSuccess() || jsonStr == null) {

            return null
        }
        return Gson().fromJson(jsonStr, MyContentsVO::class.java)
    }

    fun getLibrosUserInfo(url: String?): LibrosDataVO {
        val common = LibrosDataVO()
        val jsonNet = LibrosJsonNetwork()
        val jsonStr: String? = jsonNet.getJsonData(url)
        common.setNextwrokSuccess(jsonNet.isNetworkSuccess())
        if (jsonNet.isNetworkSuccess() === false || jsonStr == null) {
            return common
        }
        try {
            val jsonObject = JSONObject(jsonStr)
            val resultObject = jsonObject.getJSONObject("Result")
            val resultCode = resultObject.getString("ResultCode")
            val resultMessage = resultObject.getString("ResultMessage")
            if (jsonObject.has("Contents")) {
                val contentJson = jsonObject.getJSONObject("Contents")
                if (contentJson.has("userId")) {
                    common.setUserEmail(contentJson.getString("userId"))
                }
                if (contentJson.has("userName")) {
                    common.setName(contentJson.getString("userName"))
                }
                if (contentJson.has("UserProfileImageURL")) {
                    common.setUserIamge(contentJson.getString("UserProfileImageURL"))
                }
            }
            common.setResultCode(resultCode)
            common.setResultMsg(resultMessage)
        } catch (e: JSONException) {
            // TODO Auto-generated catch block
            Log.e("error", e.message.toString())
        }
        return common
    }

    fun getSqliteVersion(url: String): LibrosDataVO? {
        val common = LibrosDataVO()
        val jsonNet = LibrosJsonNetwork()
        val jsonStr: String? = jsonNet.getJsonData(url)
        Log.d("testurl",url)
        common.setNextwrokSuccess(jsonNet.isNetworkSuccess())
        if (jsonNet.isNetworkSuccess() === false || jsonStr == null) {
            return common
        }
        try {
            val jsonObject = JSONObject(jsonStr)
            val resultObject = jsonObject.getJSONObject("Result")
            val resultCode = resultObject.getString("ResultCode")
            val resultMessage = resultObject.getString("ResultMessage")
            val result = Array(2) { _ ->
                ""
            }
            if (jsonObject.has("Contents")) {
                val contentJson = jsonObject.getJSONObject("Contents")
                Log.d("testurl3",  contentJson.getString("CurrentVersionAddress"))
                if (contentJson.has("CurrentVersionAddress")) {
                    result[0] = contentJson.getString("CurrentVersionAddress")
//                    result?.set(0, contentJson.getString("CurrentVersionAddress"))

                }
                if (contentJson.has("CurrentVersion")) {
                    result[1] = contentJson.getString("CurrentVersion")
                }
            }
            common.setContent(result)
            common.setResultCode(resultCode)
            common.setResultMsg(resultMessage)
        } catch (e: JSONException) {
            // TODO Auto-generated catch block
            Log.e("error", e.message.toString())
        }
        return common
    }
}