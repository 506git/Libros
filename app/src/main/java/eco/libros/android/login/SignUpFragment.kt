package eco.libros.android.login

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import eco.libros.android.R
import eco.libros.android.common.CustomFragment
import eco.libros.android.common.crypt.PasswordCrypt
import eco.libros.android.common.http.LibrosRepository
import eco.libros.android.common.model.LibrosModelVO
import eco.libros.android.common.utill.AppUtils
import eco.libros.android.common.utill.LibrosUtil
import eco.libros.android.databinding.FragmentSignUpBinding
import eco.libros.android.ui.IntroActivity
import eco.libros.android.ui.LogInMainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignUpFragment : CustomFragment(), View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    var userInfCheck: Boolean = false
    var useCheck: Boolean = false
    var pushCheck: Boolean = false
    var width: Int = 0
    var height: Int = 0

    var emailAddress: String = ""
    var password: String = ""
    var passwordConfirm: String = ""
    var userNickName: String = ""

    private lateinit var binding : FragmentSignUpBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.btnCancel.setOnClickListener(this)
        binding.btnSignUp.setOnClickListener(this)
        binding.useAcceptInf.setOnClickListener(this)
        binding.userAcceptInf.setOnClickListener(this)
        binding.allCheck.setOnCheckedChangeListener(this)
        binding.useAcceptBtn.setOnCheckedChangeListener(this)
        binding.userInfAcceptBtn.setOnCheckedChangeListener(this)
        binding.pushAcceptBtn.setOnCheckedChangeListener(this)
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        hideKeyboard()
        when (buttonView?.id) {
            R.id.allCheck -> {
                binding.useAcceptBtn.isChecked = isChecked
                binding.userInfAcceptBtn.isChecked = isChecked
                binding.pushAcceptBtn.isChecked = isChecked
                userInfCheck = isChecked
                useCheck = isChecked
                pushCheck = isChecked
            }
            R.id.push_accept_btn -> {
                pushCheck = isChecked
            }
            R.id.user_inf_accept_btn -> {
                userInfCheck = isChecked
            }
            R.id.use_accept_btn -> {
                useCheck = isChecked
            }
        }
    }

    override fun onClick(v: View?) {
        hideKeyboard()
        when (v?.id) {
            R.id.btn_signUp -> {
                inputCheck()
            }
            R.id.btn_cancel -> {
                (activity as LogInMainActivity).finish()
            }
            R.id.use_accept_inf -> {
                startActivity(Intent(context, AcceptDetailActivity::class.java).apply {
                    putExtra("position", 0)
                })
            }
            R.id.user_accept_inf -> {
                startActivity(Intent(context, AcceptDetailActivity::class.java).apply {
                    putExtra("position", 1)
                })
            }

        }
    }

    private fun inputCheck() {
        emailAddress = binding.editEmail.text.toString().trim()
        password = binding.editPw.text.toString().trim()
        passwordConfirm = binding.editPwConfirm.text.toString().trim()
        userNickName = binding.editNickname.text.toString().trim()
        val reg = Regex("^[a-zA-Z0-9]*$")
        val regExp = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")
        var msg: String = ""

        when {
            emailAddress.isEmpty() -> msg = "이메일을 입력하세요"

            !emailAddress.matches(regExp) -> msg = "이메일 형식이 잘못되었습니다."

            password.isEmpty() -> msg = "비밀번호를 입력하세요"

            passwordConfirm.isEmpty() -> msg = "비밀번호 확인을 입력하세요"

            password != passwordConfirm -> msg = "입력한 비밀번호가 서로 맞지 않습니다. 다시 입력해주세요."

            !password.matches(reg) -> msg = "비밀번호 형식이 잘못되었습니다. 영문 및 숫자만 입력 가능합니다."

            !passwordConfirm.matches(reg) -> msg = "비밀번호 형식이 잘못되었습니다. 영문 및 숫자만 입력 가능합니다."

            userNickName.isEmpty() -> msg = " 닉네임을 입력하세요."

            !useCheck -> msg = "이용 약관에 동의 하시기 바랍니다."

            !userInfCheck -> msg = " 개인정보 처리방침에 동의 하시기 바랍니다."
        }
        if (!TextUtils.isEmpty(msg)) {
            LibrosUtil.showMsgWindow(activity, "알림", msg, "확인")
        } else {
            showAlert()
        }
    }

    private fun showAlert() {
        val msg = StringBuffer()
        AlertDialog.Builder(context).apply {
            setTitle("입력확인")
            setMessage(msg.let {
                msg.append("입력하신 정보입니다.\n\n").append("아이디    : $emailAddress\n")
                        .append("비밀번호 : $password\n\n").append("이 정보로 회원가입 하시겠습니까?")
            })
            setCancelable(false)
            setPositiveButton("회원가입") { dialog, _ ->
                dialog.dismiss()
                CoroutineScope(Main).launch{
                    userSignUpTask()
                }
            }
            setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }
            create().show()
        }
    }


    private suspend fun userSignUpTask(){
        lateinit var serviceResult: LibrosModelVO
        AppUtils.hideSoftKeyBoard(requireActivity())
        showProgress(requireActivity(),"")
        val result =  withContext(IO){
            val crypt = PasswordCrypt(IntroActivity.context)
            crypt.init()
            var tempId = LibrosUtil.encryptedTxt(emailAddress, crypt)
            var tempPw = LibrosUtil.encryptedTxt(password, crypt)
            var encryptYn = "Y"
            val push = if (pushCheck) "Y" else "N"
            if (tempId  == "N"){
                tempId = emailAddress
                encryptYn = "N"
            }
            if (tempPw == "N"){
                tempPw = password
                encryptYn = "N"
            }

            tempId = LibrosUtil.getEncodedStr(tempId).toString()
            tempPw = LibrosUtil.getEncodedStr(tempPw).toString()

            val librosRepository = LibrosRepository()
            librosRepository.getSignUpRepo(tempId, tempPw, tempPw, encryptYn,"Y", userNickName, push, activity?.resources?.getString(R.string.device_type).toString())?.let { response ->
                if (response.isSuccessful){
                    Log.d("TESTSERVICE",response.raw().request.toString())
                    return@withContext response.body()!!
                }
            } as LibrosModelVO
        }
        hideProgress()
        if (result.result?.resultCode?.isNotEmpty()!! && result.result.resultCode == "Y"){
            LibrosUtil.showMsgWindowFinish(requireActivity(), "알림", "회원 가입이 완료되었습니다.\n로그인 후 이용 바랍니다.", "확인")
        } else{
            LibrosUtil.showMsgWindow(activity,"알림", result.result.resultMessage,"확인")
        }
    }

}