package eco.libros.android.login

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.EditText
import com.google.android.material.snackbar.Snackbar
import eco.libros.android.R
import eco.libros.android.common.CustomFragment
import eco.libros.android.common.CustomFragmentManager
import eco.libros.android.common.crypt.PasswordCrypt
import eco.libros.android.common.database.EbookDownloadDBFacade
import eco.libros.android.common.database.UserLibListDBFacade
import eco.libros.android.common.http.LibrosRepository
import eco.libros.android.common.model.LibListModelVO
import eco.libros.android.common.utill.LibrosUtil
import eco.libros.android.ui.IntroActivity
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.security.InvalidAlgorithmParameterException
import java.security.NoSuchAlgorithmException
import javax.crypto.BadPaddingException
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException

class LoginFragment : CustomFragment(), View.OnClickListener {
    private var userId: String? = null
    private var userPw: String? = null
    private var editEmail: EditText? = null
    private var editPw: EditText? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN or WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        editEmail = activity?.findViewById<EditText>(R.id.edit_email) as EditText
        editPw = activity?.findViewById<EditText>(R.id.edit_pw) as EditText

        editEmail!!.setOnKeyListener { _, keyCode, event ->
            if ((event.action == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                edit_pw.requestFocus()
            }
            return@setOnKeyListener false

        }


        editPw!!.setOnKeyListener { _, keyCode, event ->
            if ((event.action == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                btn_logIn.performClick()
            }
            return@setOnKeyListener false
        }

        txt_pw_find.setOnClickListener(this)
        btn_logIn.setOnClickListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onClick(v: View?) {
        hideKeyboard()
        when (v?.id) {
            R.id.txt_pw_find -> {
                val fragment = PwFindFragment()
                CustomFragmentManager.addFragment(requireActivity(), fragment, "비밀번호찾기")
            }
            R.id.btn_logIn -> {
                when {
                    edit_email.text.isEmpty() -> Snackbar.make(
                            mainLayout,
                            "이메일을 입력해주세요.",
                            Snackbar.LENGTH_LONG
                    ).show()
                    edit_pw.text.isEmpty() -> Snackbar.make(
                            mainLayout,
                            "비밀번호를 입력해주세요.",
                            Snackbar.LENGTH_LONG
                    ).show()
                    else ->
                        CoroutineScope(Main).async {
                            logInTask()
                        }
                }
            }
        }

    }

    private fun showAlert(msg: String?) {
        AlertDialog.Builder(context).apply {
            setTitle("알림")
            setMessage(msg)
            setCancelable(false)
            setPositiveButton("확인") { dialog, _ ->
                checkUser(userId.toString())
                Log.d("TESTLIBCODE=", LibrosUtil.getLibCode(context).toString())
//                LibrosUtil.setUserId(context, userId)
//                LibrosUtil.setUserPw(context, userPw)
                dialog.dismiss()
                activity?.setResult(Activity.RESULT_OK)
                activity?.finish()
//                Intent(context, MainActivity::class.java).apply {
//
//                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
//                    startActivity(this)
//                }
                CustomFragmentManager.removeCurrentFragment(requireActivity())
            }
            create().show()
        }
    }

    private fun checkUser(newUser: String) {
        if (newUser != LibrosUtil.getUserId(requireContext(), needEncoding = false, needEncrypt = false)) {
            LibrosUtil.delUserId(
                    requireContext(), context?.resources?.getString(R.string.libros_login_id)!!
            )
//            LibrosUtil.delUserId(
//                requireContext(), context?.resources?.getString(R.string.libros_last_lib_code)!!
//            )
            LibrosUtil.delSharedData(
                    requireContext(), context?.resources?.getString(R.string.libros_last_lib_code)
            )
            EbookDownloadDBFacade(requireContext()).deleteEBookDB()
            UserLibListDBFacade(requireContext()).deleteLibDB()
        }
        LibrosUtil.setUserId(requireContext(), userId)
        LibrosUtil.setUserPw(requireContext(), userPw)
        Log.d("testDele", "delete")
    }

    private suspend fun logInTask() {
        hideKeyboard()
        showProgress(requireActivity(),"로그인 중 입니다")
        userId = editEmail!!.text.toString().trim()
        userPw = editPw!!.text.toString().trim()
        var serviceResult: LibListModelVO? = null
        withContext(Dispatchers.IO) {
            if (userId!!.isEmpty() && userPw!!.isEmpty()) {
                return@withContext
            }

            var encryptYn = "N"
            var tempPw = ""
            var tempId = ""

            try {
                val crypt = PasswordCrypt(context)
                crypt.init()
                tempId = crypt.encodePw(userId)
                tempPw = crypt.encodePw(userPw)
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
                        tempPw = userPw as String
                        encryptYn = "N"
                    }
                    else -> {
                        tempId = userId as String
                        tempPw = userPw as String
                        encryptYn = "N"
                    }
                }
            }

            tempId = LibrosUtil.getEncodedStr(tempId).toString()
            tempPw = LibrosUtil.getEncodedStr(tempPw).toString()
            val librosRepository = LibrosRepository()
//            librosRepository.getLoginRepo(tempId, tempPw, encryptYn, LibrosUtil.getSharedData(requireContext(), "pushToken")!!,
//                    LibrosUtil.getOriginDeviceId(requireContext())!!, resources.getString(R.string.device_type))?.let { response ->
//                if (response.isSuccessful) {
//                    serviceResult = response.body()
//                }
//            }

            librosRepository.getLibLoginRepo(tempId, userPw.toString(), "Y", LibrosUtil.getOriginDeviceId(IntroActivity.context)!!, resources.getString(R.string.device_type))?.let { response ->
                if (response.isSuccessful) {
                    Log.d("TESTROAW",response.raw().request.toString())
                    serviceResult = response.body()
                } else
                    serviceResult = null
            }
        }

        hideProgress()
        if (serviceResult!!.result.toString().isEmpty()) {
            LibrosUtil.showMsgWindow(
                    requireActivity(),
                    "오류",
                    resources.getString(R.string.msg_can_not_communication),
                    "확인"
            )
            return
        }

        if (serviceResult!!.result.resultCode!!.isNotEmpty() && serviceResult!!.result.resultCode == "Y") {
            showAlert(serviceResult!!.result.resultMessage)
        } else {
            LibrosUtil.showMsgWindow(requireActivity(), "오류", serviceResult!!.result.resultMessage, "확인")
        }
    }
}

