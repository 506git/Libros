package eco.libros.android.login

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import eco.libros.android.R
import eco.libros.android.common.CustomFragment
import eco.libros.android.common.LibrosData
import eco.libros.android.common.ProgressFragment
import eco.libros.android.common.crypt.PasswordCrypt
import eco.libros.android.common.http.LibrosRepository
import eco.libros.android.common.model.LibrosModelVO
import eco.libros.android.common.utill.LibrosUtil
import eco.libros.android.databinding.FragmentPwChangeBinding
import kotlinx.android.synthetic.main.fragment_pw_change.*
import kotlinx.android.synthetic.main.fragment_pw_find.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PwChangeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PwChangeFragment : CustomFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var userId: String? = null

    private lateinit var binding: FragmentPwChangeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getString(ARG_PARAM1)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPwChangeBinding.inflate(inflater, container, false)
        binding.btnChange.setOnClickListener {
            var userPwNew = binding.editPwNew.text.trim()
            var userPwNewConfirm = binding.editPwNewConfirm.text.trim()
            checkPw(userPwNew.toString(), userPwNewConfirm.toString())
        }
        return binding.root
    }

    private fun checkPw(userPwNew: String, userPwNewConfirm: String) {
        val reg = Regex("^[a-zA-Z0-9]*$")
        var msg = ""
        when {
            userId == null -> msg = "아이디를 찾을수 없습니다. 다시 시도해주시길 바랍니다."
            userPwNew.isEmpty() -> msg = "비밀번호를 입력해주세요."
            userPwNewConfirm.isEmpty() -> msg = "비밀번호 확인을 입력해주세요."
            !userPwNew.matches(reg) -> msg = "비밀번호 형식이 잘못되었습니다. 영문 및 숫자만 입력 가능합니다."
            !userPwNewConfirm.matches(reg) -> msg = "비밀번호 형식이 잘못되었습니다. 영문 및 숫자만 입력 가능합니다."
            !userPwNew.equals(userPwNewConfirm, true) -> msg = "입력한 비밀번호가 서로 맞지 않습니다. 다시 입력해주세요."
        }
        if (TextUtils.isEmpty(msg)) {
            CoroutineScope(Main).launch {
                pwChangeTask(userPwNew, userPwNewConfirm)
            }
        } else
            LibrosUtil.showMsgWindow(activity, "알림", msg, "확인")
    }

    private suspend fun pwChangeTask(userPwNew: String, userPwNewConfirm: String) {
        hideKeyboard()
        showProgress(requireActivity(), "비밀 번호 변경중입니다.")

        val result = withContext(IO) {
            var encryptYn = "N"
            var tempId = ""
            var tempPw = ""
            var tempPwCheck = ""

            val crypt = PasswordCrypt(context).apply { init() }

            tempId = LibrosUtil.encryptedTxt(userId.toString(), crypt)
            tempPw = LibrosUtil.encryptedTxt(userPwNew, crypt)
            tempPwCheck = LibrosUtil.encryptedTxt(userPwNewConfirm, crypt)

            if (tempId.equals("N", true) || tempPw.equals("N", true) || tempPwCheck.equals("N", true)) {
                encryptYn = "N"
                tempId = userId.toString()
                tempPw = userPwNew
                tempPwCheck = userPwNewConfirm
            } else
                 encryptYn = "Y"

            val librosRepository = LibrosRepository()
            librosRepository.pwChangeRepo(tempId,tempPw,tempPwCheck,encryptYn,resources.getString(R.string.device_type))?.let { response ->
                if (response.isSuccessful) {
                    Log.d("TESTURLF",response.raw().request.toString())
                    return@withContext response.body()
                }
                return@withContext null
            }
        } as LibrosModelVO

        hideProgress()

        if (result.result != null){
            if (result.result?.resultCode != null && result.result?.resultCode.equals("Y",true)){
                LibrosUtil.showMsgWindowFinish(requireActivity(), "알람", result.result?.resultMessage, "확인")
            } else
                LibrosUtil.showMsgWindowFinish(requireActivity(), "오류", result.result?.resultMessage, "확인")
        } else
            LibrosUtil.showMsgWindow(requireActivity(), "오류", "일시적인 네트워크 오류입니다.", "확인")

    }

    inner class PWChangeTask {
        private var progressFragment = ProgressFragment()
        fun backgroundTask() {
            //onPreExecute
            try {
                progressFragment = ProgressFragment.newInstance("비밀 번호 변경중입니다.")
                progressFragment.show(requireActivity().supportFragmentManager, "progress")
                hideKeyboard()
            } catch (e: WindowManager.BadTokenException) {
                Log.e("error", e.message.toString())
            }
            //doInBackground
            CoroutineScope(Dispatchers.Main).launch {

                user_pw_new = edit_pw_new.text.toString()
                user_pw_new_confirm = edit_pw_new_confirm.text.toString()
                var encryptYn = "Y"

                val result = withContext(Dispatchers.Default) {

                    if (userId?.isEmpty()!!) {
                        progressFragment.dismiss()
                        return@withContext null
                    }

                    var encryptYn = "N"
                    var tempId = ""
                    var tempPw = ""
                    var tempPwCheck = ""
                    try {
                        val crypt = PasswordCrypt(context)
                        crypt.init()
                        tempId = crypt.encodePw(userId)
                        tempPw = crypt.encodePw(user_pw_new)
                        tempPwCheck = crypt.encodePw(user_pw_new_confirm)
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
                                tempPw = user_pw_new as String
                                tempPwCheck = user_pw_new as String
                                encryptYn = "N"
                            }
                            else -> {
                                tempId = userId as String
                                tempPw = user_pw_new as String
                                tempPwCheck = user_pw_new_confirm as String
                                encryptYn = "N"
                            }
                        }
                    }
                    tempId = LibrosUtil.getEncodedStr(tempId).toString()
                    tempPw = LibrosUtil.getEncodedStr(tempPw).toString()
                    tempPwCheck = LibrosUtil.getEncodedStr(tempPwCheck).toString()
                    val sb = StringBuffer()
                    sb.append(resources.getString(R.string.url_libros_web)).append("pwFind/change?")
                        .append("userId=$tempId").append("&userPw=$tempPw")
                        .append("&checkUserPw=$tempPwCheck")
                        .append("&encryptYn=$encryptYn")
                        .append("&deviceType=" + resources.getString(R.string.device_type))
                    Log.d("testPwChange", sb.toString())
                    LibrosData().getLibrosLoginResult(sb.toString())
                }
                if (progressFragment.isAdded) {
                    try {
                        progressFragment.dismissAllowingStateLoss()
                    } catch (e: WindowManager.BadTokenException) {
                        // TODO: handle exception
                        Log.e("error", e.message.toString())
                    } catch (e: IllegalArgumentException) {
                        // TODO: handle exception
                        Log.e("error", e.message.toString())
                    }
                }
                //onPostExecute
                if (result?.getResultCode() != null && result.getResultCode().equals("Y")) {
                    val imm =
                        context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

                    LibrosUtil.showMsgWindowFinish(requireActivity(), "알람", result.getResultMsg(), "확인")
                } else {
                    LibrosUtil.showMsgWindow(requireActivity(), "오류", result?.getResultMsg(), "확인")
                }
            }
        }
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PwChangeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic

        private var user_pw: String? = null
        private var user_pw_new: String? = null
        private var user_pw_new_confirm: String? = null

        fun newInstance(userId: String) =
            PwChangeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, userId)
                }
            }
    }
}