package eco.libros.android.common.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class EbookListVO(
        @SerializedName("EPUB_FILE_NAME")
        val fileName: String,

        @SerializedName("LIB_CODE")
        val libCode: String,

        @SerializedName("TITLE")
        val title: String,

        @SerializedName("LIB_NAME")
        val libName: String,

        @SerializedName("thumbnail")
        val thumbnail: String,

        @SerializedName("BOOK_ID")
        val bookId: String,

        @SerializedName("LENT_KEY")
        val lentKey: String,

        @SerializedName("COMKEY")
        val comKey: String,

        @SerializedName("ISBN")
        val isbn: String,

        @SerializedName("FILE_TYPE")
        val fileType: String,

        @SerializedName("DRM_INFO")
        val drmInfo: String,

        @SerializedName("DRM")
        val drm: String,

        @SerializedName("DOWNLOAD_YN")
        val downloadYn: String,

        @SerializedName("RETURN_DATE")
        val returnDate: String,

        @Expose
        @SerializedName("DOWNLOAD_YN")
        val useStartTime: String?,

        @Expose
        @SerializedName("RETURN_DATE")
        val useEndTime: String?
)

data class EBookDownloadLocalVO(
    @SerializedName("EPUB_FILE_NAME")
    val fileName: String,

    @SerializedName("DRM_INFO")
    val drmInfo: String
)

data class EbookDownloadListVO(
        @SerializedName("LENT_KEY")
        val lentKey: String,

        @SerializedName("DOWNLOAD_YN")
        val downloadYn: String
)