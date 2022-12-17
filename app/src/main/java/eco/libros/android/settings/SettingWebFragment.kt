package eco.libros.android.settings

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.webkit.WebView.setWebContentsDebuggingEnabled
import android.widget.TextView
import eco.libros.android.BuildConfig
import eco.libros.android.R
import eco.libros.android.common.BaseWebViewFragment
import eco.libros.android.common.CustomFragment
import eco.libros.android.common.web.LibWebChromeClient
import eco.libros.android.home.fragment.HomeFragment
import kotlinx.android.synthetic.main.fragment_home.*
import java.util.regex.Pattern

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SettingWebFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingWebFragment : BaseWebViewFragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var url : String = ""
    private val ACCEPTED_URI_SCHEMA = Pattern.compile(
            "(?i)" +
                    "(" +
                    "(?:http|https|file):\\/\\/" +
                    "|(?:inline|data|about|javascript):" +
                    ")" +
                    "(.*)")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
            url = it.getString("webUrl").toString()
        }
    }
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view : View = inflater.inflate(R.layout.fragment_setting_web, container, false)
        view.findViewById<WebView>(R.id.settingWebView).apply {
            setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
            settings.javaScriptEnabled = true
            settings.saveFormData = false
            settings.userAgentString = getString(R.string.user_agent)
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            settings.javaScriptCanOpenWindowsAutomatically = true
            addJavascriptInterface(
                    WebViewCallback(),
                    WEB_VIEW_CALLBACK
            )
            webChromeClient = LibWebChromeClient(requireActivity(),null)
        }
        view.findViewById<WebView>(R.id.settingWebView).loadUrl(url)

        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SettingWebFragment.
         */
        // TODO: Rename and change types and number of parameters
//        @JvmStatic
//        fun newInstance(param1: String, param2: String) =
//            SettingWebFragment().apply {
//                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
//                }
//            }
        const val WEB_VIEW_CALLBACK = "LibrosApp"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as SettingsMainActivity).findViewById<TextView>(R.id.toolbar_text).text = "환경설정"
    }
}