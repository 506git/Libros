package eco.libros.android.common.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class UserLibInfVO(
        @Expose
        @SerializedName("LIBRARY_USER_ID")
        var libraryUserId: String,

        @Expose
        @SerializedName("LIBRARY_USER_PW")
        var libraryUserPw: String,

        @Expose
        @SerializedName("LIBRARY_USER_NO")
        var libraryUserNo: String,

        @Expose
        @SerializedName("EBOOK_ID")
        var eBookId: String,

        @Expose
        @SerializedName("EBOOK_PW")
        var eBookPw: String,

        @SerializedName("IS_USER_CERTIFY_YN")
        var isUserCertifyYn: String,

        @SerializedName("IS_EBOOK_CERTIFY_YN")
        var isEBookCertifyYn: String

)