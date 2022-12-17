package eco.libros.android.common.model

import com.google.gson.annotations.SerializedName

data class LibropiaNoticeDTO (
        @SerializedName("Result")
        val result: LibResultModel,
        @SerializedName("Contents")
        val contents: LibropiaNoticeModel?
)

data class LibropiaNoticeModel (
        @SerializedName("NoticeEndDate")
        var noticeEndDate: String?,

        @SerializedName("NoticeContents")
        var noticeContent: String?,

        @SerializedName("NoticeTitle")
        var noticeTitle: String?,

        @SerializedName("NoticeImageURL")
        var noticeImageUrl: String?,

        @SerializedName("NoticeViewSort")
        var noticeViewSort: String?,

        @SerializedName("LibraryName")
        var libraryName: String?,

        @SerializedName("NoticeStartDate")
        var noticeStartDate: String?
        )