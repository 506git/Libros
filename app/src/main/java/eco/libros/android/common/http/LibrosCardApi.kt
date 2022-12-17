package eco.libros.android.common.http

import eco.libros.android.common.model.UserCardModelVO
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface LibrosCardApi {
    @GET("requestService.jsp?serviceName=MB_04_04_01_SERVICE")
    suspend fun getCardRepo(
            @Query("userId", encoded = true)
            userId: String,
            @Query("libCode")
            libCode: String,
            @Query("libUserNo")
            libUserNo: String,
            @Query("deviceType")
            deviceType: String,
            @Query("encryptFromId")
            encryptFromId: String
    ): Response<UserCardModelVO>
}