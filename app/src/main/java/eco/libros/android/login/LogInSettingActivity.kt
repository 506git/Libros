package eco.libros.android.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import eco.libros.android.R
import eco.libros.android.common.variable.GlobalVariable
import eco.libros.android.ui.BaseActivity
import eco.libros.android.ui.LogInMainActivity
import kotlinx.android.synthetic.main.activity_log_in.*

class LogInSettingActivity : BaseActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_log_in)

        testLogo.setOnClickListener(this)
        btn_logIn.setOnClickListener(this)
        btn_signUp.setOnClickListener (this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == GlobalVariable.LIBROS_LOGIN_ACTIVITY) {
            if (resultCode == RESULT_OK) {
                Log.d("testOK","TESTDD")
                setResult(RESULT_OK)
                finish()
            }

        } else
            super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onClick(v: View?) {
        lateinit var menu : String
        when(v?.id){
            R.id.testLogo ->{
                menu = "test"
            }
            R.id.btn_logIn ->{
                menu = "login"
            }
            R.id.btn_signUp ->{
                menu = "singUp"
            }
        }
        Intent(this, LogInMainActivity::class.java).apply {
                putExtra("menuId",menu)
                startActivityForResult(this,GlobalVariable.LIBROS_LOGIN_ACTIVITY)
            }
    }
}