package eco.libros.android.login

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned.*
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import android.view.*
import eco.libros.android.R
import eco.libros.android.common.*
import eco.libros.android.common.crypt.PasswordCrypt
import eco.libros.android.common.http.LibrosRepository
import eco.libros.android.common.model.LibrosModelVO
import eco.libros.android.common.utill.AppUtils
import eco.libros.android.common.utill.LibrosUtil
import eco.libros.android.databinding.FragmentPwFindBinding
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_pw_find.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main

class PwFindFragment : CustomFragment(), View.OnClickListener {
    companion object{
        private var userId: String? = null
        private var userAuth: String? = null
    }

    private lateinit var binding : FragmentPwFindBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPwFindBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mailSpan = binding.mailError.text
        val word = "전화번호"
        val start: Int = mailSpan.indexOf(word)
        val end = start + word.length
        val phoneNumber = mailSpan.substring(end + 3, mailSpan.length)
        val spannableString = SpannableString(mailSpan).apply {
            setSpan(StyleSpan(Typeface.BOLD),start,end,SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(ForegroundColorSpan(Color.parseColor("#5C54A3")), end + 3, mailSpan.length, SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE)
            setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber")).apply { startActivity(this) }
                }
            }, end + 3, mailSpan.length, SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        binding.mailError.apply {
            movementMethod = LinkMovementMethod.getInstance()
            text = spannableString
        }
        binding.checkBtn.setOnClickListener(this)
        binding.sendAuthBtn.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        AppUtils.hideSoftKeyBoard(requireActivity())
        when (v?.id) {
            R.id.send_auth_btn -> inputCheck()
            R.id.check_btn -> authCheck()
        }
    }

    private fun inputCheck() {
        val emailAddress: String = binding.editEmail.text.toString().trim()
        val regExp = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")
        var msg = ""
        when {
            emailAddress.isEmpty() -> msg = "이메일을 입력하세요"
            !emailAddress.matches(regExp) -> msg = "이메일 형식이 잘못되었습니다."
        }
        if (TextUtils.isEmpty(msg)){
            CoroutineScope(Main).launch {
                emailTask()
            }
        } else
        LibrosUtil.showMsgWindow(requireActivity(), "알림", msg, "확인")
    }

    private fun authCheck() {
        val emailAuth = binding.editAuth.text.toString().trim()
        when{
            emailAuth.isEmpty() -> LibrosUtil.showMsgWindow(requireActivity(), "알림", "인증 번호를 입력해주세요", "확인")
            else ->{
                CoroutineScope(Main).launch {
                    authTask()
                }
            }
        }
    }

    private suspend fun emailTask(){
        showProgress(requireActivity(),"이메일 확인중입니다.")
        val userId = binding.editEmail.text.toString().trim()
        val crypt = PasswordCrypt(context).apply { init() }
//        crypt.init()
        var tempId = LibrosUtil.encryptedTxt(userId,crypt)
        var encryptYn = "N"

        if (tempId  == "N"){
            tempId = userId
            encryptYn = "N"
        } else
            encryptYn = "Y"

        tempId = LibrosUtil.getEncodedStr(tempId).toString()

        val librosRepository = LibrosRepository()
        val result = withContext(IO){
            librosRepository.findPwRepo(userId = tempId,encryptYn = encryptYn,deviceType = resources.getString(R.string.device_type))?.let {
                response ->
                Log.d("testFIND",response.errorBody().toString())
                if (response.isSuccessful){
//                    Log.d("testFIND",response.errorBody().toString())
                    return@withContext response.body()
                } else if (response.isSuccessful.not()){
                    LibrosUtil.showMsgWindow(requireActivity(),  "오류", "일시적인 네트워크 오류입니다.", "확인")
                }
            }

        } as LibrosModelVO

        hideProgress()

        if(result != null){
            if(result.result?.resultCode != null && result.result.resultCode.equals("Y",true)){
                LibrosUtil.showMsgWindow(requireActivity(),"알람",result.result.resultMessage,"확인")
            } else
                LibrosUtil.showMsgWindow(requireActivity(),"오류", result.result?.resultMessage,"확인")
        } else
            LibrosUtil.showMsgWindow(requireActivity(), "오류", "일시적인 네트워크 오류입니다.", "확인")

    }

    private suspend fun authTask(){
        showProgress(requireActivity(),"인증번호 확인중입니다.")

        var result = withContext(IO){
            var userAuthNum = binding.editAuth.text.toString().trim()
            var userId = binding.editEmail.text.toString().trim()
            var encryptYn = "Y"

            val crypt = PasswordCrypt(context).apply {init() }
            var tempId = LibrosUtil.encryptedTxt(userId,crypt)

            tempId = LibrosUtil.getEncodedStr(tempId).toString()
            if(tempId == "N"){
                encryptYn = "N"
                tempId = userId
            }

            val librosRepository = LibrosRepository()
            librosRepository.checkAuthRepo(tempId, userAuthNum,encryptYn,resources.getString(R.string.device_type))?.let { response ->
                if(response.isSuccessful){
                    Log.d("Testauth,",response.raw().toString())
                    return@withContext response.body()
                } else
                    return@withContext null
            }
        }

        hideProgress()
        if (result != null){
            if (result.result?.resultCode.equals("Y",true)){
                edit_auth.isEnabled = false
                showAlert(result.result?.resultMessage)
            } else
                LibrosUtil.showMsgWindow(requireActivity(), "오류", result.result?.resultMessage, "확인")
        } else
            LibrosUtil.showMsgWindow(requireActivity(), "오류", resources.getString(R.string.msg_can_not_communication), "확인")
    }

    private fun showAlert(msg: String?) {
        AlertDialog.Builder(context).apply {
            setTitle("알림")
            setMessage(msg)
            setCancelable(false)
            setPositiveButton("확인") { dialog, _ ->
                dialog.dismiss()
                val fragment : PwChangeFragment = PwChangeFragment.newInstance(userId.toString())
                CustomFragmentManager.addFragmentOnMain(requireActivity(),fragment,"비민번호변경")
            }
            setNegativeButton("취소"){
                    dialog, _ ->
                dialog.dismiss()
            }
            create().show()
        }
    }
}