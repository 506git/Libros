package eco.libros.android.settings

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import eco.libros.android.R


class HeaderViewPreference(context: Context?, attrs: AttributeSet?) :
    Preference(context, attrs) {
    private var btnClose : ImageButton? = null
    private var btnQna : ImageButton? = null
    private var btnNotify : ImageButton? = null
    private var btnAlarm : ImageButton? = null
    private var userName : TextView? = null
    private var userId : TextView? = null
    private var imageClickListener: View.OnClickListener? = null

    override fun onBindViewHolder(holder: PreferenceViewHolder?) {
        super.onBindViewHolder(holder)
        btnClose = holder?.findViewById(R.id.close_btn) as ImageButton?
        btnQna = holder?.findViewById(R.id.btn_qna) as ImageButton?
        btnNotify = holder?.findViewById(R.id.btn_notify) as ImageButton?
        btnAlarm = holder?.findViewById(R.id.btn_alarm) as ImageButton?
        userName = holder?.findViewById(R.id.text_user_nickname) as TextView?
        userId = holder?.findViewById(R.id.text_user_id) as TextView?
        btnClose?.setOnClickListener(imageClickListener)
        btnQna?.setOnClickListener(imageClickListener)
        btnNotify?.setOnClickListener(imageClickListener)
        btnAlarm?.setOnClickListener(imageClickListener)
    }

    fun setImageClickListener(onClickListener: View.OnClickListener){
        imageClickListener = onClickListener
    }

    fun init(name : String, id: String){
        Log.d("TestName = ", "$name : $id")
        userName?.text = name
        userId?.text = id

    }
}