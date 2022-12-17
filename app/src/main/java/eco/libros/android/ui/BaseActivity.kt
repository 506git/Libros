package eco.libros.android.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import eco.libros.android.R
import eco.libros.android.common.BaseWebViewFragment
import eco.libros.android.common.CustomFragment
import eco.libros.android.common.ProgressFragment
import eco.libros.android.home.fragment.HomeFragment
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.IllegalStateException

@SuppressLint("Registered")
open class BaseActivity : AppCompatActivity() {
    protected lateinit var activity: AppCompatActivity
    protected lateinit var handler: Handler
    var progressFragment = ProgressFragment()

    companion object{
        const val EXIT_APP_BACK_BUTTON_PERIOD = 3000
        private var lastBackPressedTime: Long = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity = this
        handler = Handler()
    }

    fun showProgress(message: String){
        try {
            progressFragment = ProgressFragment.newInstance(message)
            progressFragment.show(supportFragmentManager, "progress")
        } catch (e: WindowManager.BadTokenException) {
            Log.e("error", e.message.toString())
        } catch (e: IllegalStateException){
            Log.e("eeror",e.message.toString())
        }
    }
    fun renameDialog(text: String){
        progressFragment.renameDialog(text)
    }

    fun backNav(){
        when (supportFragmentManager.backStackEntryCount - 1) {
            0 -> when {
                System.currentTimeMillis() - lastBackPressedTime < EXIT_APP_BACK_BUTTON_PERIOD ->
                    finish()
                else -> {
                    Toast.makeText(this, getString(R.string.toast_back_button_msg), Toast.LENGTH_SHORT).show()
                    lastBackPressedTime = System.currentTimeMillis()
                }
            }
            else -> {
                supportFragmentManager.popBackStackImmediate()
//                onNavigateUp()
                selectedFragment()
            }
        }
    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if(fragment != null  && (CustomFragment::class.java.isInstance(fragment) || BaseWebViewFragment::class.java.isInstance(fragment))){
            Log.d("testBack","back")
            if (onBackPressedDispatcher.hasEnabledCallbacks()){
                Log.d("testBack","backPressed")
                onBackPressedDispatcher.onBackPressed()
            } else {
                super.onBackPressed()
            }
        }
        else {
            Log.d("testBack","backPressedfinish")
            finish()
        }

    }

    fun selectedFragment() {
        when (supportFragmentManager.getBackStackEntryAt(supportFragmentManager.backStackEntryCount - 1).name) {
            "home" -> bottom_nav_view.menu.findItem(R.id.btn_bottom_home).isChecked = true
            "newBook" -> bottom_nav_view.menu.findItem(R.id.btn_bottom_newBook).isChecked = true
            "eBook" -> bottom_nav_view.menu.findItem(R.id.btn_bottom_ebook).isChecked = true
            "myContents" -> bottom_nav_view.menu.findItem(R.id.btn_bottom_myContents).isChecked = true

        }
    }

    fun hideProgress(){
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
}