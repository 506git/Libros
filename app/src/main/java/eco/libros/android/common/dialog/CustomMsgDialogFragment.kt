package eco.libros.android.common.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.Button
import androidx.fragment.app.Fragment
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.DialogFragment
import eco.libros.android.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private const val ARG_PARAM3 = "param3"

/**
 * A simple [Fragment] subclass.
 * Use the [CustomMsgDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CustomMsgDialogFragment : DialogFragment() {
    // TODO: Rename and change types of parameters
    private var title: String? = null
    private var msg: String? = null
    private var button: String? = null

    private var btn: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            title = it.getString(ARG_PARAM1)
            msg = it.getString(ARG_PARAM2)
            button = it.getString(ARG_PARAM3)
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
        params?.width = (deviceWidth * 0.9).toInt()
        dialog?.window?.attributes = params as WindowManager.LayoutParams
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view : View = inflater.inflate(R.layout.fragment_custom_msg_dialog, container, false)

        view.findViewById<TextView>(R.id.title).text = title
        view.findViewById<TextView>(R.id.msg).text = msg
        view.findViewById<ImageButton>(R.id.close_btn).setOnClickListener {
            dialog?.dismiss()
        }
        btn = view.findViewById<Button>(R.id.btn)
        if (!TextUtils.isEmpty(button)){
            btn?.visibility = View.VISIBLE
            btn?.text = button
            btn?.setOnClickListener {
                dialog?.dismiss()
            }
        } else view.findViewById<Button>(R.id.btn).visibility = View.GONE

        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CustomMsgDialogFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(title: String, msg: String) =
                CustomMsgDialogFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, title)
                        putString(ARG_PARAM2, msg)
                    }
                }
        @JvmStatic
        fun newInstance(title: String, msg: String, button: String) =
            CustomMsgDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, title)
                    putString(ARG_PARAM2, msg)
                    putString(ARG_PARAM3, button)
                }
            }
    }
}