package eco.libros.android.model

import android.provider.ContactsContract

class LibrosUserDataVO {
    private var nextwrokSuccess = false
    private var resultCode: String? = null
    private var resultMsg: String? = null
    private var content: Any? = null
    private var totalCount: String? = null

    private var userNickName: String? = null
    private var userEmail: String? = null
    private var usrProfileImageURL: String? = null

    fun isNextwrokSuccess(): Boolean {
        return nextwrokSuccess
    }

    fun setNextwrokSuccess(nextwrokSuccess: Boolean) {
        this.nextwrokSuccess = nextwrokSuccess
    }
    fun getResultCode(): String? {
        return resultCode
    }

    fun setResultCode(resultCode: String?) {
        this.resultCode = resultCode
    }

    fun getResultMsg(): String? {
        return resultMsg
    }

    fun setResultMsg(resultMsg: String?) {
        this.resultMsg = resultMsg
    }
    fun setName(nickname: String?){
        this.userNickName = nickname
    }
    fun getName(): String?{
        return userNickName
    }
    fun setUserEmail(userEmail: String?){
        this.userEmail = userEmail
    }
    fun getUserEmail(): String?{
        return userEmail
    }
    fun setUserIamge(userImage: String?){
        this.usrProfileImageURL = userImage
    }
    fun getUserImage(): String?{
        return usrProfileImageURL
    }

}