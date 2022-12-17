package eco.libros.android.common.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class BookVO(
    @SerializedName("content_id")
    val contentId: String,

    @Expose
    @SerializedName("book_name")
    var bookName: String,

    @SerializedName("root_path")
    val rootPath: String,

    @SerializedName("book_data")
    var bookData: String?
)