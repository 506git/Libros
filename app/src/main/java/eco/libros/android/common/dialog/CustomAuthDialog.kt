package eco.libros.android.common.dialog

import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import eco.libros.android.R
import kotlinx.android.synthetic.main.fragment_home.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val ARG_PARAM3 = "param3"
private const val ARG_PARAM4 = "certifyKind"

/**
 * A simple [Fragment] subclass.
 * Use the [CustomAuthDialog.newInstance] factory method to
 * create an instance of this fragment.
 */
class CustomAuthDialog : DialogFragment() {
    // TODO: Rename and change types of parameters
    private var name: String? = null
    private var userNo: String? = null
    private var userPw: String? = null
    private var certifyType: String? = null

    private var text_id: TextView? = null
    private var edit_id: EditText? = null
    private var text_no: TextView? = null
    private var edit_no: EditText? = null
    private var text_pw: TextView? = null
    private var edit_pw: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            name = it.getString(ARG_PARAM1)
            userNo = it.getString(ARG_PARAM2)
            userPw = it.getString(ARG_PARAM3)
            certifyType = it.getString(ARG_PARAM4)
        }
    }
    override fun onResume() {
        super.onResume()

        val windowManager = context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)

        val params : ViewGroup.LayoutParams? = dialog?.window?.attributes
        val deviceWidth = size.x
        val deviceHeight = size.y
        params?.height = (deviceHeight * 0.55).toInt()
        params?.width = (deviceWidth * 0.9).toInt()
        dialog?.window?.attributes = params as WindowManager.LayoutParams
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view : View = inflater.inflate(R.layout.fragment_custom_auth_dialog, container, false)
        view.findViewById<ImageButton>(R.id.close_btn).setOnClickListener {
            dialog?.dismiss()
//            webView.reload()
        }
        Log.d("TESTCERTIFY", certifyType.toString())
        text_id = view.findViewById(R.id.text_id)
        edit_id = view.findViewById(R.id.edit_id)
        text_no = view.findViewById(R.id.text_no)
        edit_no = view.findViewById(R.id.edit_no)
        text_pw = view.findViewById(R.id.text_pw)
        edit_pw = view.findViewById(R.id.edit_pw)
        initView()
        return view
    }

    private fun initView() {
        when (certifyType) {
            "IDPW" -> {
                idPwView()
            }
            "LOANNO" -> {
                loanNoView()
            }
            "LOANNOCARD" -> {
                loanNoCardView()
            }
        }
    }

    fun idPwView(){
        text_id?.text = "도서관 회원아이디"
        edit_id?.hint = "회원아이디를 입력해주세요"
        text_no?.visibility = View.GONE
        edit_no?.visibility = View.GONE
        text_pw?.visibility = View.VISIBLE
        edit_pw?.visibility = View.VISIBLE
    }

    fun loanNoView(){
        text_id?.text = "회원 이름"
        edit_id?.hint = "회원 이름을 입력해주세요"
        text_no?.visibility = View.VISIBLE
        edit_no?.visibility = View.VISIBLE
        text_pw?.visibility = View.GONE
        edit_pw?.visibility = View.GONE
    }

    private fun loanNoCardView(){
        text_id?.text = "회원 이름"
        edit_id?.hint = "회원 이름을 입력해주세요"
        text_no?.visibility = View.VISIBLE
        edit_no?.visibility = View.VISIBLE
        text_pw?.visibility = View.VISIBLE
        edit_pw?.visibility = View.VISIBLE
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CustomAuthDialog.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String, param3: String) =
            CustomAuthDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                    putString(ARG_PARAM4, param3)
                }
            }
        @JvmStatic
        fun newInstance(param1: String, param2: String, param3: String, param4: String) =
            CustomAuthDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                    putString(ARG_PARAM3, param3)
                    putString(ARG_PARAM4, param4)
                }
            }
    }
}