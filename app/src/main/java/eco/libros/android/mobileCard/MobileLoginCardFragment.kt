package eco.libros.android.mobileCard

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import eco.libros.android.R
import eco.libros.android.common.utill.LibrosUtil
import android.view.View as View

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MobileLoginCardFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MobileLoginCardFragment : BottomSheetDialogFragment(), View.OnClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var certifyKind: String? = null

    private var text_id: TextView? = null
    private var edit_id: EditText? = null
    private var text_no: TextView? = null
    private var edit_no: EditText? = null
    private var text_pw: TextView? = null
    private var edit_pw: EditText? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            certifyKind = it.getString("certifyKind")
        }
    }

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
        val view = inflater.inflate(R.layout.fragment_mobile_login_card, container, false)
        // Inflate the layout for this fragment
        view.findViewById<Button>(R.id.btn_auth).setOnClickListener(this)
        view.findViewById<ImageButton>(R.id.close_btn).setOnClickListener(this)
        view.findViewById<TextView>(R.id.toolbar_text).text = LibrosUtil.getSharedData(requireContext(),resources.getString(R.string.libros_lib_name))

        text_id = view?.findViewById(R.id.text_id)
        edit_id = view?.findViewById(R.id.edit_id)
        text_no = view?.findViewById(R.id.text_no)
        edit_no = view?.findViewById(R.id.edit_no)
        text_pw = view?.findViewById(R.id.text_pw)
        edit_pw = view?.findViewById(R.id.edit_pw)
        initView()
        return view
    }

    private fun initView() {
        when (certifyKind) {
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
         * @return A new instance of fragment MobileLoginCardFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MobileLoginCardFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.close_btn -> dismiss()
        }
    }
}