package eco.libros.android.common

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import eco.libros.android.R
import eco.libros.android.common.utill.AppUtils

abstract class CustomFragment : Fragment() {
    private var lastBackPressedTime: Long = 0
    private var fileUri: Uri? = null
    private var originalBrightness: Float = 0.5f
    private var alert: View? = null
    private var mRecognizer: SpeechRecognizer? = null

    private var currentPhotoPath: String? = null
    var progressFragment = ProgressFragment()

    private lateinit var callback: OnBackPressedCallback

    var webViewCanGoBack = false
    fun showProgress(activity: FragmentActivity, message: String) {
        try {
            progressFragment = ProgressFragment.newInstance(message)
            progressFragment.show(activity.supportFragmentManager, "progress")
        } catch (e: WindowManager.BadTokenException) {
            Log.e("error", e.message.toString())
        }
    }

//    fun initWebViewSetting(webView: WebView) {
//        activity?.window?.setFlags(
//                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
//                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
//        )
//        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
//        webView.settings.apply {
//            javaScriptEnabled = true
//            saveFormData = false
//            userAgentString = getString(R.string.user_agent)
//            domStorageEnabled = true
//            databaseEnabled = true
//
//        }
//        webView.addJavascriptInterface(
//                WebViewCallback(),
//                WEB_VIEW_CALLBACK
//        )
//
////        webView.setOnKeyListener { v, keyCode, event ->
////            if (event.keyCode == KeyEvent.KEYCODE_BACK && event.action == MotionEvent.ACTION_UP && webView.canGoBack()) {
////                Log.d("TEST","webviewBack")
////                webView.goBack()
////            } else {
////                Log.d("TEST","webviewNoBack")
////                requireActivity().onBackPressed()
////            }
////            true
////
////        }
//
//    }

    fun hideProgress() {
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
    }

    override fun onDestroy() {
        super.onDestroy()
        callback.remove()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.d("TESTDDD","DDD2")
                if (activity?.supportFragmentManager?.backStackEntryCount!! > 1) {
                    isEnabled = false
                    CustomFragmentManager.removeCurrentFragment(activity!!)
                    activity?.findViewById<TextView>(R.id.toolbar_text)?.text =
                            activity?.supportFragmentManager?.getBackStackEntryAt(activity?.supportFragmentManager?.backStackEntryCount!! - 2)?.name.toString()
                } else {
                    Log.d("TESTDDD","DDD")
                    requireActivity().finish()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // TODO Auto-generated method stub

        retainInstance = true
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    fun init(view: View) {
        view.findViewById<TextView>(R.id.toolbar_text)?.text =
                activity?.supportFragmentManager?.getBackStackEntryAt(activity?.supportFragmentManager?.backStackEntryCount!! - 1)?.name
        view.findViewById<ImageButton>(R.id.close_btn)?.setOnClickListener {
            activity?.let { it1 -> CustomFragmentManager.removeCurrentFragment(it1) }
        }
    }

    protected fun hideKeyboard() {
        AppUtils.hideSoftKeyBoard(requireActivity())
    }

    override fun onDestroyView() {
        // TODO Auto-generated method stub
        if (mRecognizer != null) {
            mRecognizer?.cancel()
            mRecognizer?.destroy()
        }
        super.onDestroyView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("TESTURLF", requestCode.toString())
        Log.d("TESTURLF", resultCode.toString())

    }

}