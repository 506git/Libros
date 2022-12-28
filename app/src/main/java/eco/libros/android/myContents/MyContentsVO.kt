package eco.libros.android.myContents

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class MyContentsVO(
        @SerializedName("result")
        val result: LibResultModel,
        @SerializedName("entry_data")
        val contents: ArrayList<MyEbookListModel>,
        @SerializedName("entry_count")
        val count: String
)

data class MyEbookListModel(
        @SerializedName("thumbnail")
        val thumbnail: String,

        @SerializedName("author")
        val author: String,

        @SerializedName("lent_key")
        val lentKey: String,

        @SerializedName("cover")
        val cover: String,

        @SerializedName("epubID")
        val ePubId: String?,

        @SerializedName("title")
        val title: String,

        @SerializedName("comcode")
        val comCode: String,

        @SerializedName("return_link")
        val returnLink: String,

        @SerializedName("ebook_lib_name")
        val eBookLibName: String,

        @SerializedName("extending_count")
        val extendingCount: String,

        @SerializedName("lending_date")
        val lendingDate: String,

        @SerializedName("lending_expired_date")
        val lendingExpiredDate: String,

        @SerializedName("drm_url_info")
        val drmUrlInfo: String,

        @SerializedName("download_link")
        val downloadLink: String,

        @SerializedName("lib_name")
        val libName: String,

        @SerializedName("ISBN")
        val isbn: String,

        @SerializedName("FirstLicense")
        val firstLicense: String,

        @SerializedName("lib_code")
        val libCode: String,

        @SerializedName("file_type")
        val fileType: String,

        @Expose
        @SerializedName("publisher")
        val publisher: String,

        @SerializedName("id")
        val id: String,

        @SerializedName("drm_key")
        val drm_key: String,

        @SerializedName("DownloadYn")
        val downloadYn: String = "N",

        @Expose
        @SerializedName("use_start_time")
        val useStartTime: String? = null.toString(),

        @Expose
        @SerializedName("use_end_time")
        val useEndTime: String? = null.toString(),

        @Expose
        @SerializedName("downloaded_FileName")
        val downloadedFileName: String = null.toString(),

        @Expose
        @SerializedName("uploadUrl")
        val uploadUrl: String = null.toString()
)

data class LibResultModel(
        @SerializedName("result_code")
        val resultCode: String,

        @SerializedName("result_message")
        var resultMessage: String
)
