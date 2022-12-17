package eco.libros.android.common

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import eco.libros.android.R
import kotlinx.android.synthetic.main.fragment_progress.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "msg"
private const val ARG_PARAM2 = "title"

/**
 * A simple [Fragment] subclass.
 * Use the [ProgressFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProgressFragment : DialogFragment() {
    // TODO: Rename and change types of parameters
    private var msg: String? = null

    override fun onResume() {
        super.onResume()
//        val dialogWidth = ActionBar.LayoutParams.MATCH_PARENT
//        val dialogHeight = resources.getDimensionPixelSize(R.dimen.dialog_fragment_width)
//
//        dialog?.window?.setLayout(dialogWidth,dialogHeight)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = requireActivity().layoutInflater.inflate(R.layout.fragment_progress, null)
        arguments?.let {
            view.msg.text = it.getString(ARG_PARAM1)
        }

        val builder = AlertDialog.Builder(requireContext()).setView(view)
        val dialog: Dialog? = builder.create()
        dialog?.setCanceledOnTouchOutside(false)
//        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog ?: throw IllegalAccessException("Activity cannot be null")
    }

    fun progressTask(num: Int) {
        CoroutineScope(Main).launch {
            dialog?.findViewById<ProgressBar>(R.id.progressBar)?.progress = num
        }
    }

    fun renameDialog(message: String) {
        dialog?.findViewById<TextView>(R.id.msg)?.text = message
//        view?.msg?.text = message
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        arguments?.let {
//            param1 = it.getString(ARG_PARAM1)
//            param2 = it.getString(ARG_PARAM2)
//        }

    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProgressFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String) =
            ProgressFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                }
            }
    }
}