package eco.libros.android.common.model

class LibrosDataVO {
    private var nextwrokSuccess = false
    private var resultCode: String? = null
    private var resultMsg: String? = null
    private var content: Any? = null
    private var option: Any? = null
    private var option1: Any? = null
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

    fun getContent(): Any? {
        return content
    }

    fun setContent(content: Any?) {
        this.content = content
    }

    fun getOption(): Any? {
        return option
    }

    fun setOption(option: Any?) {
        this.option = option
    }

    fun getOption1(): Any? {
        return option1
    }

    fun setOption1(option1: Any?) {
        this.option1 = option1
    }

    fun getTotalCount(): String? {
        return totalCount
    }

    fun setTotalCount(totalCount: String?) {
        this.totalCount = totalCount
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