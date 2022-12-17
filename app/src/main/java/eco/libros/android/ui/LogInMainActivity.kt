package eco.libros.android.ui

import android.animation.ObjectAnimator
import android.graphics.Point
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.animation.BounceInterpolator
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import eco.libros.android.R
import eco.libros.android.common.CustomFragmentManager
import eco.libros.android.login.LoginFragment
import eco.libros.android.login.OnKeyboardVisibilityListener
import eco.libros.android.login.SignUpFragment
import kotlinx.android.synthetic.main.activity_log_in_main.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LogInFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LogInMainActivity : BaseActivity(), OnKeyboardVisibilityListener {
    private var lastBackPressedTime: Long = 0
    var layout: View? = null
    var height: Int? = null

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in_main)
        setSupportActionBar(toolbar)

        toolbar.apply {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowTitleEnabled(false)
        }
        var menu : String? = null
        if (intent.hasExtra("menuId")) {
            menu = intent.getStringExtra("menuId")
        }
        var fragment : Fragment? = null
        when(menu){
            "login" -> {
                menu = "로그인"
                fragment = LoginFragment()

            }
            "singUp" -> {
                menu = "회원가입"
                fragment = SignUpFragment()
            }
        }
        CustomFragmentManager.addFragment(this, fragment, menu)
        setKeyboardVisibilityListener(this)
        layout = findViewById<View>(R.id.fragment_container)

    }

    private fun setKeyboardVisibilityListener(onKeyboardVisibilityListener: OnKeyboardVisibilityListener) {
        val parentView = (findViewById<ViewGroup>(R.id.mainLayout)).getChildAt(0)
        parentView.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            private var alreadyOpen = false
            private val defaultKeyboardHeightDP = 100
            private val EstimatedKeyboardDP =
                defaultKeyboardHeightDP + if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) 48 else 0
            private val rect: Rect = Rect()
            override fun onGlobalLayout() {
                val estimatedKeyboardHeight: Int = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, EstimatedKeyboardDP.toFloat(), parentView.resources.displayMetrics).toInt()
                parentView.getWindowVisibleDisplayFrame(rect)
                val heightDiff: Int = parentView.rootView.height - (rect.bottom - rect.top)
                val isShown = heightDiff >= estimatedKeyboardHeight
                if (isShown == alreadyOpen) {
                    Log.i("Keyboard state", "Ignoring global layout change...")
                    return
                }
                alreadyOpen = isShown
                onKeyboardVisibilityListener.onVisibilityChanged(isShown)
            }
        })
    }

//    override fun onBackPressed() {
//        when (supportFragmentManager.backStackEntryCount - 1) {
//            0 -> this.finish()
//            else -> {
//                supportFragmentManager.popBackStackImmediate()
//            }
//        }
//    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        height = layout?.height
    }

    override fun onVisibilityChanged(visible: Boolean) {
        val param = layout?.layoutParams
        if(visible){
            param?.height = height?.plus(3)
        } else{
            param?.height = height
        }


        layout?.layoutParams = param
    }

}