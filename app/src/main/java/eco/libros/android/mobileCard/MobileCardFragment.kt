package eco.libros.android.mobileCard

import android.app.Dialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import eco.libros.android.R
import eco.libros.android.common.crypt.PasswordCrypt
import eco.libros.android.common.dialog.CustomAuthDialog
import eco.libros.android.common.http.LibrosRepository
import eco.libros.android.common.model.UserCardModel
import eco.libros.android.common.model.UserCardModelVO
import eco.libros.android.common.utill.AppUtils
import eco.libros.android.common.utill.LibrosUtil
import eco.libros.android.common.variable.GlobalVariable
import eco.libros.android.databinding.FragmentMobileCardBinding
import kotlinx.android.synthetic.main.fragment_header.view.*
import kotlinx.android.synthetic.main.fragment_mobile_card.*
import kotlinx.android.synthetic.main.fragment_withdraw.view.*
import kotlinx.coroutines.*
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.security.InvalidAlgorithmParameterException
import java.security.NoSuchAlgorithmException
import javax.crypto.BadPaddingException
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "userId"
private const val ARG_PARAM2 = "libCode"
private const val ARG_PARAM3 = "userLibNo"
private const val ARG_PARAM4 = "certifyKind"
/**
 * A simple [Fragment] subclass.
 * Use the [MobileCardFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MobileCardFragment : BottomSheetDialogFragment(), View.OnClickListener {
    // TODO: Rename and change types of parameters
    private var userId: String? = null
    private var userLibCode: String? = null
    private var userLibNo: String? = null
    private var certifyKind: String? = null

    private var _binding: FragmentMobileCardBinding? = null
    private val binding get() = _binding!!
    var cardJob: Job? = null
    var serviceResult: UserCardModelVO? = null
    override fun getTheme(): Int {
        return R.style.BottomSheetDialogTheme
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), theme).apply {
            setCanceledOnTouchOutside(false)
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        arguments?.let {
            userId = it.getString(ARG_PARAM1)
            userLibCode = it.getString(ARG_PARAM2)
            userLibNo = it.getString(ARG_PARAM3)
            certifyKind = it.getString(ARG_PARAM4)
        }
        Log.d("TESTUSERID", userId.toString())
        // Inflate the layout for this fragment
        _binding = FragmentMobileCardBinding.inflate(inflater, container, false)
        cardJob = CoroutineScope(Dispatchers.Main).launch {
            cardTask()
        }
        cardJob?.start()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.closeBtn.setOnClickListener(this)
        binding.imgBarcode.setOnClickListener(this)
        binding.smartAuth.setOnClickListener(this)
        binding.libModify.setOnClickListener(this)
        binding.btnUserNo.setOnClickListener(this)
        binding.noCardView.setOnClickListener {
            Log.d("TESTDD", "TOUCH")
            cardJob?.cancelChildren()
            cardJob = CoroutineScope(Dispatchers.Main).launch {
                cardTask()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cardJob?.cancelChildren()
    }

    fun showProgress() {
        binding.progressBar.visibility = View.VISIBLE
        binding.cardLayout.visibility = View.INVISIBLE
    }

    fun hideProgress() {
        binding.progressBar.visibility = View.GONE
        binding.cardLayout.visibility = View.VISIBLE
    }

    fun resultError() {
        binding.noCardView.visibility = View.VISIBLE
        binding.cardLayout.visibility = View.INVISIBLE
        binding.errorText.text = serviceResult!!.result.resultMessage
    }

    fun showCard() {
        binding.noCardView.visibility = View.GONE
        binding.cardLayout.visibility = View.VISIBLE
    }

    private suspend fun cardTask() {
        AppUtils.hideSoftKeyBoard(requireActivity())
        showProgress()
        withContext(Dispatchers.IO) {
            if (userId == null && userLibNo == null) {
                return@withContext
            }

            var encryptYn = "N"
            var tempId = ""

            try {
                val crypt = PasswordCrypt(context)
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


    private fun showContent(user: UserCardModel) {
        showCard()
        if (user.memberBarCodeCard != null && user.memberQrCodeCard != null) {
            val barcode = user.memberBarCodeCard
            val qrCode = user.memberQrCodeCard
            val barcodeByte = LibrosUtil.getDecodeImage(barcode.toByteArray())
            val qrCodeByte = LibrosUtil.getDecodeImage(qrCode.toByteArray())
            user.barcode = BitmapFactory.decodeByteArray(barcodeByte, 0, barcodeByte?.size!!)
            user.qrCode = BitmapFactory.decodeByteArray(qrCodeByte, 0, qrCodeByte?.size!!)
        }
        binding.status.text = user.userStatus
        binding.userName.text = user.libraryUserName
        binding.userNo.text = user.libraryUserNo
        binding.btnUserNo.tag = user.libraryUserNo
        if (user.isLibUserNoShowYn != null && user.isLibUserNoShowYn == "Y") {
            binding.userNo.transformationMethod = null
        } else {
            binding.userNo.transformationMethod = PasswordTransformationMethod()
        }
        binding.imgBarcode.setImageBitmap(user.barcode)
        binding.imgQr.setImageBitmap(user.qrCode)
        binding.toolbarText.text = user.libraryName
        binding.userLent.text = "도서 ${user.userLoanCount} 건"
        binding.userAppendix.text = "부록 ${user.userBookAppendLoanCount} 건"
        binding.userReserveCount.text = "${user.userReserveCount} 건"


    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MobileCardFragment.
         */
        // TODO: Rename and change types and number of parameters
//        @JvmStatic
//        fun newInstance(param1: String, param2: String) =
//            MobileCardFragment().apply {
//                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
//                }
//            }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
//            R.id.no_card_view ->{
//                Log.d("TESTDD","TOUCH")
//                CoroutineScope(Dispatchers.Main).launch {
//                    cardJob
//                }
//            }
            R.id.close_btn -> dismiss()
            R.id.btn_userNo -> activity?.supportFragmentManager?.let {
                LibrosUtil.showMsgCustomDialog(
                        it,
                        "회원번호",
                        v.tag.toString(),
                        "확인"
                )
            }
            R.id.img_barcode -> {
                startActivity(Intent(context, BarcodeDetailActivity::class.java).apply {
                    putExtra("userId", userId)
                    putExtra("userLibCode", userLibCode)
                    putExtra("userLibNo", userLibNo)
                })
            }
            R.id.smart_auth -> {
                TedPermission.with(context)
                        .setPermissions(
                                android.Manifest.permission.CAMERA
                        )
                        .setPermissionListener(object : PermissionListener {
                            override fun onPermissionGranted() {
                                val integrator =
                                        IntentIntegrator.forSupportFragment(this@MobileCardFragment)
                                integrator.apply {
                                    setRequestCode(GlobalVariable.BARCODE_SMARTPHONE_AUTH)
                                    setBeepEnabled(false)
                                    setOrientationLocked(true)
                                    initiateScan()
                                }
//                                    .setBeepEnabled(false)
//                                    .setOrientationLocked(true)
//                                    .initiateScan()
//                                var qrCodeScanner: IntentIntegrator = IntentIntegrator(activity)
//                                qrCodeScanner.addExtra("")
//                                IntentIntegrator qrCodeScanner = new IntentIntegrator(MyActivity.this);
//                                quCodeScanner.addExtra("ITEM_ID", scanedItemId);//add your data
//                                qrCodeScanner.setOrientationLocked(false);
//                                qrCodeScanner.initiateScan();

                            }

                            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                                LibrosUtil.showMsgWindow(
                                        activity,
                                        "알림",
                                        "해당 권한을 허용해주셔야 이용 가능합니다.",
                                        "확인"
                                )
                            }
                        })
                        .check()
            }
            R.id.lib_modify -> {
                val customAuthDialog: CustomAuthDialog =
                if (certifyKind.equals("LOANNOCARD")) {
                        CustomAuthDialog.newInstance(
                            "홍길동",
                            "1234567890",
                            "123456",
                            certifyKind.toString()
                        )
                } else {
                        CustomAuthDialog.newInstance(
                            "홍길동",
                            "1234567890",
                            certifyKind.toString()
                        )
                }
                activity?.supportFragmentManager?.let { customAuthDialog.show(it, "auth_fragment") }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GlobalVariable.BARCODE_SMARTPHONE_AUTH) {
            val result: IntentResult? =
                    IntentIntegrator.parseActivityResult(
                            IntentIntegrator.REQUEST_CODE,
                            resultCode,
                            data
                    )
            val contents = result?.contents
            if (contents != null) {
                try {
                    Toast.makeText(requireContext(), contents, Toast.LENGTH_SHORT)
                            .show()
                } catch (ignored: Throwable) {
                    Toast.makeText(
                            requireContext(),
                            getString(R.string.toast_barcode_error_msg),
                            Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}