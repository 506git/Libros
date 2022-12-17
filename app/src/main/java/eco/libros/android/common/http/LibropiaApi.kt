package eco.libros.android.common.http

import eco.libros.android.common.model.LibListModelVO
import eco.libros.android.common.model.LibropiaNoticeDTO
import eco.libros.android.common.model.LibrosModelVO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface LibropiaApi {
    @GET("requestService.jsp?serviceName=MB_01_01_01_SERVICE")
    suspend fun getRepositories(
            @Query("userId",encoded = true)
            userId: String,
            @Query("encryptFromId")
            encryptFromId: String,
            @Query("deviceType")
            deviceType: String
    ): Response<LibListModelVO>

    @GET("requestService.jsp?serviceName=MB_04_02_01_SERVICE")
    suspend fun getTermsRepo(
            @Query("userId",encoded = true)
            userId: String,
            @Query("encryptFromId")
            encryptFromId: String,
            @Query("deviceType")
            deviceType: String
    ): Response<LibrosModelVO>

    @GET("requestService.jsp?serviceName=MB_15_02_01_SERVICE")
    suspend fun getNoticeRepo(
            @Query("libCode")
            libCode: String,
            @Query("deviceType")
            deviceType: String
    ): Response<LibropiaNoticeDTO>

    @GET("requestService.jsp?serviceName=MB_04_01_13_SERVICE")
    suspend fun getLoginRepo(
        @Query("userId",encoded = true)
        userId: String,
        @Query("userPassword",encoded = true)
        userPw: String,
        @Query("encryptFromId")
        encryptFromIds: String,
        @Query("deviceNumber")
        deviceNumber: String,
        @Query("deviceType")
        deviceType: String
    ): Response<LibListModelVO>

}