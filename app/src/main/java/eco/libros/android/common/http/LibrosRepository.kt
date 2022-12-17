package eco.libros.android.common.http

class LibrosRepository {
    private val librosClient = LibrosService.libros_client
    private val libropiaClient = LibrosService.libropia_client
    private val libropiaCardClient = LibrosService.libropiaCardClient

    suspend fun getLoginRepo(userId: String, userPw: String, encryptFromId: String, pushTokenId: String, deviceNumber: String, deviceType: String ) =
            librosClient?.getLoginRepo(userId,userPw,encryptFromId,pushTokenId, deviceNumber,deviceType)

    suspend fun getSignUpRepo(userId: String, userPw: String, checkUserPw: String, encryptFromId: String, encryptYn: String, nickName: String, pushYn: String, deviceType: String ) =
            librosClient?.getSignUpRepo(userId = userId, userPw = userPw, checkUserPw = checkUserPw, encryptFromIds = encryptFromId,
                    encryptYn = encryptYn, nickName = nickName, pushYn = pushYn, deviceType = deviceType)

    suspend fun getLibLoginRepo(userId: String, userPw: String, encryptFromId: String, deviceNumber: String, deviceType: String ) =
        libropiaClient?.getLoginRepo(userId,userPw,encryptFromId, deviceNumber,deviceType)

    suspend fun findPwRepo(userId: String, encryptYn: String, deviceType: String) = librosClient?.findPwRepo(userId = userId, encryptYn = encryptYn, deviceType = deviceType)

    suspend fun checkAuthRepo(userId: String, authNum: String, encryptYn: String, deviceType: String) = librosClient?.checkAuthRepo(userId = userId, authNum = authNum,encryptYn = encryptYn, deviceType = deviceType)

    suspend fun pwChangeRepo(userId: String, userPw: String, userPwCheck: String, encryptYn: String, deviceType: String) =
        librosClient?.pwCheckRepo(userId = userId, userPw = userPw, userPwCheck = userPwCheck, encryptYn = encryptYn, deviceType = deviceType)

    suspend fun getRepositories(userId: String, encryptFromId: String, deviceType: String ) = libropiaClient?.getRepositories(userId = userId,encryptFromId =encryptFromId,deviceType = deviceType)

    suspend fun getCardRepo(userId: String, libCode: String, libUserNo: String, deviceType: String, encryptFromId: String ) =
            libropiaCardClient?.getCardRepo(userId,libCode,libUserNo,deviceType,encryptFromId)

    suspend fun getTermsRepo(userId: String, encryptFromId: String, deviceType: String ) =
            libropiaClient?.getTermsRepo(userId,encryptFromId,deviceType)

    suspend fun getNoticeRepo(libCode: String, deviceType: String) = libropiaClient?.getNoticeRepo(libCode, deviceType)
}