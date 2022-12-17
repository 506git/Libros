package eco.libros.android.common.model

import android.graphics.Bitmap
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class UserCardModelVO(
        @SerializedName("Result")
        val result: LibResultModel,
        @SerializedName("Contents")
        val contents: UserCardModel
)

class UserCardModel (
    @SerializedName("IllMemberYn")
    val illMemberYn: String,

    @SerializedName("LibraryUserName")
    val libraryUserName: String,

    @SerializedName("IsLibUserNoShowYn")
    var isLibUserNoShowYn: String,

    @SerializedName("LibraryUserNo")
    val libraryUserNo: String,

    @SerializedName("UserStatus")
    val userStatus: String,

    @SerializedName("LibraryName")
    var libraryName: String,

    @SerializedName("LoanStopDate")
    val loanStopDate: String,

    @SerializedName("UserReservCount")
    val userReserveCount: String,

    @SerializedName("MemberBarCodeCard")
    var memberBarCodeCard: String,

    @SerializedName("MemberQrCodeCard")
    var memberQrCodeCard: String,

    @SerializedName("KlMemberYn")
    var klMemberYn: String,

    @SerializedName("LibraryCode")
    var libraryCode: String,

    @SerializedName("UserBookAppendLoanCount")
    var userBookAppendLoanCount: String,

    @SerializedName("UserLoanCount")
    var userLoanCount: String,

    @Expose
    @SerializedName("barcode")
    var barcode: Bitmap,

    @Expose
    @SerializedName("qrCode")
    var qrCode: Bitmap
)
