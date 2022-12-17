package eco.libros.android.common.http

import eco.libros.android.common.LibrosData
import eco.libros.android.common.model.LibListModelVO
import eco.libros.android.common.model.LibrosDataVO
import eco.libros.android.common.model.LibrosModelVO
import org.jsoup.Connection
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Query

interface LibrosApi {
    @GET("login?")
    suspend fun getLoginRepo(
            @Query("userId",encoded = true)
            userId: String,
            @Query("userPw",encoded = true)
            userPw: String,
            @Query("encryptYn")
            encryptYn: String,
            @Query("pushTokenId",encoded = true)
            pushTokenId: String,
            @Query("deviceNumber")
            deviceNumber: String,
            @Query("deviceType")
            deviceType: String
    ): Response<LibListModelVO>

    @GET("newUser?")
    suspend fun getSignUpRepo(
            @Query("userId",encoded = true)
            userId: String,
            @Query("userPw",encoded = true)
            userPw: String,
            @Query("checkUserPw",encoded = true)
            checkUserPw: String,
            @Query("encryptFromId")
            encryptFromIds: String,
            @Query("encryptYn")
            encryptYn: String,
            @Query("nickName")
            nickName: String,
            @Query("pushYn")
            pushYn: String,
            @Query("deviceType")
            deviceType: String
    ): Response<LibrosModelVO>

    @GET("pwFind/send?")
    suspend fun findPwRepo(
            @Query("userId",encoded = true)
            userId: String,
            @Query("encryptYn")
            encryptYn: String,
            @Query("deviceType")
            deviceType: String
    ): Response<LibrosModelVO>

    @GET("pwFind/check?")
    suspend fun checkAuthRepo(
            @Query("userId",encoded = true)
            userId: String,
            @Query("authNum")
            authNum: String,
            @Query("encryptYn")
            encryptYn: String,
            @Query("deviceType")
            deviceType: String
    ): Response<LibrosModelVO>

    @GET("pwFind/change?")
    suspend fun pwCheckRepo(
            @Query("userId",encoded = true)
            userId: String,
            @Query("userPw",encoded = true)
            userPw: String,
            @Query("checkUserPw",encoded = true)
            userPwCheck: String,
            @Query("encryptYn")
            encryptYn: String,
            @Query("deviceType")
            deviceType: String
    ): Response<LibrosModelVO>

}