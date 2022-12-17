package eco.libros.android.push

import android.app.NotificationManager
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import eco.libros.android.R
import eco.libros.android.common.variable.GlobalVariable

class PushDialogActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //		requestWindowFeature(Window.FEATURE_NO_TITLE);
        //API 8.0 Only fullscreen opaque activities can request orientation 안드로이드 이슈.
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        setContentView(R.layout.activity_push_dialog)
        var msg: String? = ""
        if (intent.hasExtra("msg")) {
            msg = intent.getStringExtra("msg")
        }

        var ticker: String? = ""
        if (intent.hasExtra("ticker")) {
            ticker = intent.getStringExtra("ticker")
        }

        (findViewById<View>(R.id.textLoginTitle) as TextView).text = ticker
        (findViewById<View>(R.id.loginWinMsgText) as TextView).text = msg
        (findViewById<View>(R.id.btnLogin) as Button).text = "확인"

        findViewById<View>(R.id.btnLogin).setOnClickListener { // TODO Auto-generated method stub
            finish()
        }

        val nm =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(GlobalVariable.NOTIFICATION_NO)

    }
}