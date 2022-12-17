package eco.libros.android.mobileCard

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.databinding.DataBindingUtil
import eco.libros.android.R
import eco.libros.android.common.crypt.PasswordCrypt
import eco.libros.android.common.http.LibrosRepository
import eco.libros.android.common.model.UserCardModel
import eco.libros.android.common.model.UserCardModelVO
import eco.libros.android.common.utill.AppUtils
import eco.libros.android.common.utill.LibrosUtil
import eco.libros.android.databinding.ActivityBarcodeDetailBinding
import kotlinx.coroutines.*
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.security.InvalidAlgorithmParameterException
import java.security.NoSuchAlgorithmException
import javax.crypto.BadPaddingException
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException

class BarcodeDetailActivity : AppCompatActivity() {

    private var originalBrightness: Float = 0.5f

    var cardJob: Job? = null
    var serviceResult: UserCardModelVO? = null

    var userId:String? = null
    var userLibCode: String? = null
    var userLibNo : String? = null

    private lateinit var binding: ActivityBarcodeDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_barcode_detail)

        binding.btnBack.setOnClickListener {
            finish()
        }

        cardJob = CoroutineScope(Dispatchers.Main).launch {
            cardTask()
        }
        cardJob?.start()
        setBrightness(true)

        userId = if (intent.hasExtra("userId")) intent.getStringExtra("userId") else ""
        userLibCode = if (intent.hasExtra("userLibCode")) intent.getStringExtra("userLibCode") else ""
        userLibNo = if (intent.hasExtra("userLibNo")) intent.getStringExtra("userLibNo") else ""
    }

    private suspend fun cardTask() {
        AppUtils.hideSoftKeyBoard(this)
        showProgress()
        withContext(Dispatchers.IO) {
            if (userId == null && userLibNo == null) {
                return@withContext
            }

            var encryptYn = "N"
            var tempId = ""

            try {
                val crypt = PasswordCrypt(this@BarcodeDetailActivity)
                crypt.init()
                tempId = crypt.encodePw(userId)
                encryptYn = "Y"
            } catch (e: Exception) {
                when (e) {
                    is NoSuchAlgorithmException,
                    is UnsupportedEncodingException,
                    is NoSuchPaddingException,
                    is InvalidAlgorithmParameterException,
                    is IllegalBlockSizeException,
                    is BadPaddingException,
                    is IOException -> {
                        tempId = userId as String
                        encryptYn = "N"
                    }
                    else -> {
                        tempId = userId as String
                        encryptYn = "N"
                    }
                }
            }

            tempId = LibrosUtil.getEncodedStr(tempId).toString()
            val librosRepository = LibrosRepository()
            Log.d("testVALUE ", "$tempId, $userLibCode , $userLibNo, $encryptYn")
            librosRepository.getCardRepo(tempId, userLibCode!!, userLibNo!!, "003", encryptYn)?.let { response ->
                if (response.isSuccessful) {
                    Log.d("TESTURLFF", response.raw().request.url.toString())
                    serviceResult = response.body()
                }
            }
        }
        hideProgress()
        if (serviceResult!!.result.toString().isEmpty()) {
            resultError()
//            LibrosUtil.showMsgWindow(
//                requireActivity(),
//                "오류",
//                resources.getString(R.string.msg_can_not_communication),
//                "확인"
//            )
            return
        }

        if (serviceResult!!.result.resultCode!!.isNotEmpty() && serviceResult!!.result.resultCode == "Y") {
            showContent(serviceResult!!.contents)
        } else {
            resultError()
        }
    }

    private fun resultError() {

    }

    private fun showContent(user: UserCardModel) {
        if (user.memberBarCodeCard != null) {
            val barcode = user.memberBarCodeCard
            val barcodeByte = LibrosUtil.getDecodeImage(barcode.toByteArray())
            var bitmapImage = BitmapFactory.decodeByteArray(barcodeByte, 0, barcodeByte?.size!!)
            var matrix = Matrix()
            matrix.postRotate(90F)
            user.barcode = Bitmap.createBitmap(bitmapImage,0,0,bitmapImage.width, bitmapImage.height,matrix,true)
//            user.barcode = BitmapFactory.decodeByteArray(barcodeByte, 0, barcodeByte?.size!!)
        }
        binding.imgBarcode.setImageBitmap(user.barcode)
    }

    private fun showProgress() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun hideProgress(){
        binding.progressBar.visibility = View.GONE
    }

    private fun setBrightness(enable: Boolean){
        val newBrightness = when (true) {
            true -> {
                originalBrightness = window.attributes.screenBrightness
                1.0f
            }
            false -> originalBrightness
        }
        changeBrightness(newBrightness)
    }

    private fun changeBrightness(brightness: Float) {
        GlobalScope.launch(Dispatchers.IO) {
            launch(Dispatchers.Main) {
                val params = window.attributes
                params.screenBrightness = brightness
                window.attributes = params
            }
        }
    }

    override fun onDestroy() {
        setBrightness(true)
        super.onDestroy()
    }
}