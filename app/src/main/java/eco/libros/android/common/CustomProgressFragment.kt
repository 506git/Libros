package eco.libros.android.common

import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import eco.libros.android.R
import kotlinx.android.synthetic.main.fragment_progress.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CustomProgressFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CustomProgressFragment : DialogFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var msg: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = requireActivity().layoutInflater.inflate(R.layout.fragment_custom_progress, null)
        arguments?.let {
            view.msg.text = it.getString(ARG_PARAM1)
            msg = it.getString(ARG_PARAM1)
        }

        val builder = AlertDialog.Builder(requireContext()).setView(view)
        val dialog: Dialog? = builder.create()
        dialog?.setCanceledOnTouchOutside(false)

        return dialog ?: throw IllegalAccessException("Activity cannot be null")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_custom_progress, container, false)
    }

    fun renameDialog(message: String) {
        dialog?.findViewById<TextView>(R.id.msg)?.text = message
//        view?.msg?.text = message
    }

    private fun ProgressBar.smoothProgress(percent: Int){
        val animation = ObjectAnimator.ofInt(this,"progress",percent)
        animation.apply {
            duration = 400
            interpolator = DecelerateInterpolator()
            start()
        }
    }

    fun progressTask(num: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            dialog?.findViewById<ProgressBar>(R.id.progressBar)?.smoothProgress(num*10)
            dialog?.findViewById<TextView>(R.id.msg)?.text = "${msg}.. $num%"
        }
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment CustomProgressFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String) =
            CustomProgressFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                }
            }
    }
}