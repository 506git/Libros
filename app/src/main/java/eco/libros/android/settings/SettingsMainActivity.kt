package eco.libros.android.settings

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View.OnClickListener
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.preference.*
import com.theartofdev.edmodo.cropper.CropImage
import eco.libros.android.R
import eco.libros.android.common.*
import eco.libros.android.common.utill.LibrosUtil
import eco.libros.android.login.AcceptDetailActivity
import eco.libros.android.login.UserInfChangeFragment
import eco.libros.android.login.WithdrawFragment
import kotlinx.android.synthetic.main.fragment_header.*
import kotlinx.android.synthetic.main.settings_activity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SettingsMainActivity : AppCompatActivity() {
    var userNickname: String? = null
    var userEmail: String? = null
    var userProfileUrl: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        overridePendingTransition(R.anim.horizone_enter,R.anim.none)
        if (savedInstanceState == null) {
            supportFragmentManager
                    .beginTransaction().addToBackStack("환경설정")
                    .replace(R.id.fragment_container, SettingsFragment())
                    .commit()
        }
        val title: TextView = findViewById(R.id.toolbar_text)
        findViewById<ImageButton>(R.id.close_btn).setOnClickListener {
            this.onBackPressed()
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onDestroy() {
        super.onDestroy()
    }


    override fun onBackPressed() {

        if (onBackPressedDispatcher.hasEnabledCallbacks()) {
            if (SettingsFragment::class.java.isInstance(supportFragmentManager.findFragmentById(R.id.fragment_container))) {
                this.finish()
                overridePendingTransition(R.anim.none,R.anim.horizone_exit)
            }
            Log.d("testBack", "backPressed")
            onBackPressedDispatcher.onBackPressed()
            return
        }
        super.onBackPressed()

        Log.d("testBack", "backPressedfinish")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("TESTURLF2", requestCode.toString())
        Log.d("TESTURLF2", resultCode.toString())
        when (requestCode) {
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val fragment: Fragment? = supportFragmentManager.findFragmentById(R.id.fragment_container)
                    fragment?.onActivityResult(requestCode, resultCode, data)
//                    startActivityForResult(intent, CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
//                    super.onActivityResult(requestCode, resultCode, data)
                }
            }
        }
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        var btnClosePreference: HeaderViewPreference? = null
        override fun onPreferenceTreeClick(preference: Preference?): Boolean {
            when (preference?.key) {
                "user_inf_change" -> {
                    val userInfChangeFragment = UserInfChangeFragment()
//                    (activity as SettingsMainActivity).findViewById<TextView>(R.id.toolbar_text).text = preference.title.toString()
                    CustomFragmentManager.addSettingFragmentOnMain(requireActivity(), userInfChangeFragment, preference.title.toString())
                }
                "pw_change" -> {
                    val settingPwChangeFragment = SettingPwChangeFragment()
//                    (activity as SettingsMainActivity).findViewById<TextView>(R.id.toolbar_text).text = preference.title.toString()
                    CustomFragmentManager.addSettingFragmentOnMain(requireActivity(), settingPwChangeFragment, preference.title.toString())
                }
                "logout" -> {
                    LibrosUtil.showLogoutMsg(activity, true)
                    return true
                }
                "withdraw" -> {
                    val withdrawFragment = WithdrawFragment()
//                    (activity as SettingsMainActivity).findViewById<TextView>(R.id.toolbar_text).text = preference.title.toString()
                    CustomFragmentManager.addSettingFragmentOnMain(requireActivity(), withdrawFragment, preference.title.toString())
                }
                "version" -> {
                    context?.packageManager?.getPackageInfo(requireActivity().packageName, 0)?.versionName
//                    val version = context?.packageManager.getPackageInfo(packageName, 0).versionName
                }
                "use_accept" -> {
                    Intent(activity, AcceptDetailActivity::class.java).apply {
                        putExtra("position", 0)
                        startActivity(this)
                    }
                }
                "push" -> {

                }
                "notification", "qna" -> {
                    val webUrl =
                            when (preference.key) {
                                "notification" -> "http://220.72.184.140:3006/librosSetting.html?mode=notice"
                                "qna" -> "http://220.72.184.140:3006/librosSetting.html?mode=qna"
                                else -> "null"
                            }
                    val settingWeb = SettingWebFragment().apply {
                        arguments = Bundle().apply {
                            putString("webUrl", webUrl.toString())
                        }
                    }
                    (activity as SettingsMainActivity).findViewById<TextView>(R.id.toolbar_text).text = preference?.title.toString()
                    CustomFragmentManager.addSettingFragmentOnMain(requireActivity(), settingWeb, preference?.title.toString())
                }
                else -> {

                }
            }
            return false
        }

        private fun getVersionInfo(): String? {
            val info = context?.packageManager?.getPackageInfo(context?.packageName!!, 0)
            return info?.versionName
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            userInfTask().task()
            btnClosePreference = findPreference<HeaderViewPreference>("header")
            var preferenceManger: PreferenceManager = preferenceManager

            findPreference<PreferenceScreen>("version")?.summary = getVersionInfo()

            btnClosePreference?.setImageClickListener(OnClickListener {
                when (it.id) {
                    R.id.btn_notify -> {
                        findPreference<Preference>("notification")?.performClick()
                    }
                    R.id.btn_qna -> {
                        findPreference<Preference>("qna")?.performClick()
                    }
                    R.id.profile_img -> {

                    }
                    R.id.btn_alarm -> {
                        val settingWeb = SettingWebFragment()
//                        (activity as SettingsMainActivity).findViewById<TextView>(R.id.toolbar_text).text = it?.tag as String
                        CustomFragmentManager.addSettingFragmentOnMain(requireActivity(), settingWeb, it?.tag as String)
                    }
                }
            })
        }

        inner class userInfTask() {
            private var progressFragment = ProgressFragment()
            private var result: eco.libros.android.common.model.LibrosDataVO? = null
            var userId: String? = null
            fun task() {
                try {
                    progressFragment = ProgressFragment.newInstance("정보 조회 중입니다.")
                    progressFragment.show(activity?.supportFragmentManager!!, "progress")
                } catch (e: WindowManager.BadTokenException) {
                    Log.e("error", e.message.toString())
                    return
                }

                //doInBackground
                CoroutineScope(Dispatchers.Main).launch {
                    result = withContext(Dispatchers.Default) {
                        if (LibrosUtil.getUserId(context!!, false, needEncrypt = false)!!.isNotEmpty()
                        ) {
                            userId = LibrosUtil.getUserId(context!!, false, needEncrypt = false)
                        } else {
                            progressFragment.dismiss()
                            return@withContext null
                        }

                        val sb = StringBuffer()
                        sb.append(resources.getString(R.string.url_libros_web)).append("preferences/userLookup?")
                        sb.append("userId=$userId")
                        sb.append("&deviceType=" + resources.getString(R.string.device_type))
                        Log.d("testLogin", sb.toString())
                        LibrosData().getLibrosUserInfo(sb.toString())
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
                    if (result?.getResultCode() != null && result?.getResultCode().equals("Y")) {
                        btnClosePreference?.init(result!!.getName()!!, result!!.getUserEmail()!!)
                    } else if (result == null) {
                        LibrosUtil.showCustomMsgWindow(
                                activity,
                                "오류",
                                "회원 조회에 실패했습니다. 잠시 후 다시 실행해주세요",
                                "확인", true,
                                DialogInterface.OnClickListener { dialog, which ->
                                    dialog.dismiss()
//                        finish()
                                })
                    } else {
                        LibrosUtil.showCustomMsgWindow(
                                activity,
                                "오류",
                                result?.getResultMsg(),
                                "확인", true,
                                DialogInterface.OnClickListener { dialog, which ->
                                    dialog.dismiss()
//                        finish()
                                })
                    }
                }
                //onPostExecute

            }
        }
    }
}