package eco.libros.android.settings

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import eco.libros.android.R
import eco.libros.android.common.CustomFragment
import eco.libros.android.common.CustomFragmentManager
import eco.libros.android.common.LibrosData
import eco.libros.android.common.ProgressFragment
import eco.libros.android.common.crypt.PasswordCrypt
import eco.libros.android.common.utill.LibrosUtil
import eco.libros.android.login.PwFindFragment
import kotlinx.android.synthetic.main.fragment_pw_change.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SettingPwChangeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingPwChangeFragment : CustomFragment(), View.OnClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var edit_pw: EditText? = null
    private var edit_pw_new: EditText? = null
    private var edit_pw_new_confirm: EditText? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view : View = inflater.inflate(R.layout.fragment_setting_pw_change, container, false)
        // Inflate the layout for this fragment

        view.findViewById<TextView>(R.id.txt_pw_find).setOnClickListener(this)
        view.findViewById<Button>(R.id.btn_change).setOnClickListener(this)
        edit_pw = view.findViewById(R.id.edit_pw)
        edit_pw_new = view.findViewById(R.id.edit_pw_new)
        edit_pw_new_confirm = view.findViewById(R.id.edit_pw_new_confirm)
        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SettingPwChangeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SettingPwChangeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.txt_pw_find -> {
                val fragment = PwFindFragment()
                CustomFragmentManager.addSettingFragmentOnMain(requireActivity(),fragment,"비밀번호찾기")
            }
            R.id.btn_change -> {
                inputCheck()
            }
        }
    }

    fun inputCheck(){
        val reg = Regex("^^[A-Za-z[0-9]]{4,20}\$")
        val password = context?.let { LibrosUtil.getUserPw(it) }
        Log.d("testPW", password.toString())
        when{
            TextUtils.isEmpty(edit_pw?.text) -> LibrosUtil.showMsgWindow(
                    requireActivity(),
                    "알림",
                    "현재 비밀번호를 입력하세요",
                    "확인"
            )
            TextUtils.isEmpty(edit_pw_new?.text) -> LibrosUtil.showMsgWindow(
                    requireActivity(),
                    "알림",
                    "새 비밀번호를 입력하세요",
                    "확인"
            )
            TextUtils.isEmpty(edit_pw_new_confirm?.text) -> LibrosUtil.showMsgWindow(
                    requireActivity(),
                    "알림",
                    "비밀번호 확인을 입력하세요",
                    "확인"
            )
            edit_pw?.text.toString() != password ->
                LibrosUtil.showMsgWindow(
                        requireActivity(),
                        "알림",
                        "입력하신 현재 비밀번호가 올바르지 않습니다.",
                        "확인"
                )
            edit_pw_new?.text?.equals(edit_pw_new_confirm?.text)!! ->
                LibrosUtil.showMsgWindow(
                        requireActivity(),
                        "알림",
                        "입력한 새 비밀번호가 일치하지 않습니다. 다시 입력해주세요.",
                        "확인"
                )
            !edit_pw_new?.text!!.matches(reg) -> LibrosUtil.showMsgWindow(
                    requireActivity(),
                    "알림",
                    "비밀번호 형식이 잘못되었습니다. 영문, 숫자만 입력 가능합니다.",
                    "확인"
            )
            else -> PwChangeTask().backgroundTask()
        }
    }

    inner class PwChangeTask{
        private var progressFragment = ProgressFragment()
        fun backgroundTask(){
            //onPreExecute
            try {
                progressFragment = ProgressFragment.newInstance("비밀 번호 변경중입니다.")
                progressFragment.show(requireActivity().supportFragmentManager, "progress")
            } catch (e: WindowManager.BadTokenException) {
                Log.e("error", e.message.toString())
            }
            //doInBackground
            CoroutineScope(Dispatchers.Main).launch {
                val userId: String? = LibrosUtil.getUserId(requireContext(),false,false)
                val user_pw_new = edit_pw_new?.text.toString()
                val user_pw_new_confirm = edit_pw_new_confirm?.text.toString()
                var encryptYn = "Y"

                val result = withContext(Dispatchers.Default) {

                    if(userId?.isEmpty()!!){
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
                            .append("userId=$tempId").append("&userPw=$tempPw").append("&checkUserPw=$tempPwCheck")
                            .append("&encryptYn=$encryptYn").append("&deviceType=" + resources.getString(R.string.device_type))
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
                    LibrosUtil.showMsgWindowFinish(requireActivity(),"알람",result.getResultMsg(),"확인")
                    LibrosUtil.setUserPw(requireContext(),edit_pw_new?.text.toString())
                } else {
                    LibrosUtil.showMsgWindow(requireActivity(), "오류", result?.getResultMsg(), "확인")
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as SettingsMainActivity).findViewById<TextView>(R.id.toolbar_text).text = "환경설정"
    }
}