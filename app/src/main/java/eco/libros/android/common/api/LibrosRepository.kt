package eco.libros.android.common.api

class LibrosRepository {
    private val librosClient = LibrosService.libros_client

    suspend fun getLoginRepo(userId: String, userPw: String, encryptFromId: String, pushTokenId: String, deviceNumber: String, deviceType: String ) =
            librosClient?.getLoginRepo(userId,userPw,encryptFromId,pushTokenId, deviceNumber,deviceType)
}