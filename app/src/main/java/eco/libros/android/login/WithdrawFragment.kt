package eco.libros.android.login

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.*
import eco.libros.android.R
import eco.libros.android.common.CustomFragment
import eco.libros.android.common.CustomFragmentManager
import eco.libros.android.common.LibrosData
import eco.libros.android.common.ProgressFragment
import eco.libros.android.common.crypt.PasswordCrypt
import eco.libros.android.common.utill.LibrosUtil
import eco.libros.android.settings.SettingsMainActivity
import kotlinx.android.synthetic.main.fragment_pw_find.*
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
 * Use the [WithdrawFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class WithdrawFragment : CustomFragment(), View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var edit_pw: EditText? = null
    var check: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view : View = inflater.inflate(R.layout.fragment_withdraw, container, false)
        check = false
        view.findViewById<Button>(R.id.btn_cancel).setOnClickListener(this)
        view.findViewById<Button>(R.id.btn_confirm).setOnClickListener(this)
        view.findViewById<CheckBox>(R.id.user_inf_accept_btn).setOnCheckedChangeListener(this)
        view.findViewById<CheckBox>(R.id.user_inf_accept_btn).isChecked = check as Boolean
        edit_pw = view.findViewById<EditText>(R.id.edit_pw_confirm)
        return  view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as SettingsMainActivity).findViewById<TextView>(R.id.toolbar_text).text = "환경설정"
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment WithdrawFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                WithdrawFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.btn_cancel -> activity?.let { it1 -> CustomFragmentManager.removeCurrentFragment(it1) }
            R.id.btn_confirm -> withdrawCheck()
        }
    }

    private fun withdrawCheck() {
        when{
            TextUtils.isEmpty(edit_pw?.text.toString()) -> LibrosUtil.showMsgWindow(activity,"알람","비밀번호를 입력해주세요.","확인")
            !check!! -> activity?.supportFragmentManager?.let { LibrosUtil.showMsgCustomDialog(it,"알람","유의사항 및 탈퇴에 동의해주세요.","확인") }
            else ->WithdrawTask(edit_pw?.text.toString().trim()).backgroundTask()
        }

    }

    inner class WithdrawTask(var pw: String){
        private var progressFragment = ProgressFragment()
        fun backgroundTask(){
            //onPreExecute
            try {
                progressFragment = ProgressFragment.newInstance("회원 탈퇴 중입니다.")
                progressFragment.show(requireActivity().supportFragmentManager, "progress")
            } catch (e: WindowManager.BadTokenException) {
                Log.e("error", e.message.toString())
            }
            //doInBackground
            Log.d("TESTPW",pw.trim())
            CoroutineScope(Dispatchers.Main).launch {
                var userId = LibrosUtil.getUserId(requireContext(),false,false)
                var encryptYn = "N"
                var tempId = ""
                var tempPw = ""

                try {
                    val crypt = PasswordCrypt(context)
                    crypt.init()
                    tempId = crypt.encodePw(userId)
                    tempPw = crypt.encodePw(pw)
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
                            tempPw = pw as String
                            encryptYn = "N"
                        }
                        else -> {
                            tempId = userId as String
                            tempPw = pw as String
                            encryptYn = "N"
                        }
                    }
                }
                tempId = LibrosUtil.getEncodedStr(tempId).toString()
                tempPw = LibrosUtil.getEncodedStr(tempPw).toString()
                val result = withContext(Dispatchers.Default) {
                    val sb = StringBuffer()
                    sb.append(resources.getString(R.string.url_libros_web)).append("preferences/userDelete?")
                    sb.append("userId=$tempId").append("&userPw=$tempPw").append("&encryptYn=$encryptYn").append("&deviceType=" + resources.getString(R.string.device_type))
                    Log.d("testSend", sb.toString())
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
                if (result.getResultCode() != null && result.getResultCode().equals("Y")) {
                    val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(edit_pw?.windowToken, 0)

                    LibrosUtil.showCustomMsgWindow(requireActivity(),"알람",result.getResultMsg(),"확인", true, DialogInterface.OnClickListener { dialog, which ->
                        dialog.dismiss()
                        LibrosUtil.logoutTask(activity)
                    })
                } else {
                    LibrosUtil.showMsgWindow(requireActivity(), "오류", result.getResultMsg(), "확인")
                }
            }
        }
    }
    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        when(buttonView?.id)
        {
            R.id.user_inf_accept_btn -> check = isChecked
        }
    }
}