package eco.libros.android.common.model

import com.google.gson.JsonObject
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class LibrosModelVO(
        @SerializedName("Result")
        val result: LibResultModel?,
        @SerializedName("Contents")
        val contents: JsonObject
)

data class LibListModelVO(
        @SerializedName("Result")
        val result: LibResultModel,
        @SerializedName("Contents")
        val contents: LibContentModel
)

data class LibResultModel(
        @SerializedName("DebugMessage")
        val debugMessage: String,

        @SerializedName("ResultCode")
        val resultCode: String?,

        @SerializedName("ResultMessage")
        var resultMessage: String
)

data class LibContentModel(
        @SerializedName("UserLibraryInfoList")
        val userLibraryInfoList: ArrayList<UserLibListDataVO>
)

data class UserLibListDataVO(
        @Expose
        @SerializedName("IsEbookCertifyYn")
        var isEBookCertifyYn: String,

        @SerializedName("LibraryName")
        var libraryName: String,

        @SerializedName("LibraryCode")
        var libraryCode: String,

        @Expose
        @SerializedName("LibraryUserId")
        var libraryUserId: String,

        @Expose
        @SerializedName("LibraryUserPw")
        var libraryUserPw: String,

        @Expose
        @SerializedName("LibraryUserNo")
        var libraryUserNo: String,

        @Expose
        @SerializedName("CertifyKindId")
        var certifyKindId: String,

        @SerializedName("IsUserCertifyYn")
        var isUserCertifyYn: String,

        @SerializedName("IsEbookServiceYn")
        var isEBookServiceYn: String,

        @SerializedName("IsJoinYn")
        var isJoinYn: String,

        @SerializedName("IsSearchYn")
        var isSearchYn: String,

        @SerializedName("IsSeatServiceYn")
        var isSeatServiceYn: String,

        @Expose
        @SerializedName("EbookId")
        var eBookId: String,

        @SerializedName("EbookPw")
        var eBookPw: String,

        @Expose
        @SerializedName("LibraryMenuInfoList")
        val UserLibraryInfoList: ArrayList<LibraryMenuListDataVo>
)

data class LibraryMenuListDataVo(
        @SerializedName("LibraryCode")
        val libraryCode: String,

        @SerializedName("LibraryMenuId")
        val libraryMenuId: String,

        @SerializedName("LibraryMenuSeq")
        val libraryMenuSeq: String,

        @SerializedName("LibraryMenuIconId")
        val libraryMenuIconId: String,

//        @Expose
//        @SerializedName("app_link_type")
//        val appLinkType: String,

//        @Expose
//        @SerializedName("auth_yn_viewing")
//        val libraryMenuCertifyYn: String,
//
        @Expose
        @SerializedName("PageLinkPath")
        val pageLinkPath: String
)