package eco.libros.android.login

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.gson.JsonObject
import eco.libros.android.R
import eco.libros.android.common.ProgressFragment
import eco.libros.android.common.http.LibrosRepository
import eco.libros.android.common.model.LibrosModelVO
import eco.libros.android.common.utill.AppUtils
import eco.libros.android.common.utill.LibrosUtil
import kotlinx.android.synthetic.main.activity_accept_detail.*
import kotlinx.android.synthetic.main.fragment_privacy_terms.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject


class AcceptDetailActivity : AppCompatActivity() {

    var progressFragment = ProgressFragment()
    var serviceResult: LibrosModelVO? = null

    var privateTerms: String? = null
    var serviceUseTerms: String? = null

    private val MIN_SCALE = 0.92f // 뷰가 몇퍼센트로 줄어들 것인지
    private val MIN_ALPHA = 0.6f

    lateinit var viewPager : ViewPager2

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accept_detail)
        setSupportActionBar(toolbar)

        toolbar.apply {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowTitleEnabled(false)
        }
        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        viewPager = findViewById<ViewPager2>(R.id.viewPager)

        tabLayout.addTab(
            tabLayout.newTab().setCustomView(R.layout.tab_item_setting).setText("서비스이용약관")
        )
        tabLayout.addTab(
            tabLayout.newTab().setCustomView(R.layout.tab_item_setting).setText("개인정보취급방침")
        )

        viewPager.offscreenPageLimit = 2

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> {
                        tabLayout.getTabAt(0)?.select()
                    }
                    1 -> {
                        tabLayout.getTabAt(1)?.select()
                    }

                }
            }
        })

        tabLayout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })


        CoroutineScope(Dispatchers.Main).launch {
            termsTask()
        }


    }

    private fun showProgress() {
        try {
            progressFragment = ProgressFragment.newInstance("")
            progressFragment.show(supportFragmentManager, "progress")
        } catch (e: WindowManager.BadTokenException) {
            Log.e("error", e.message.toString())
        }
    }

    private fun hideProgress() {
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

    private suspend fun termsTask() {
        AppUtils.hideSoftKeyBoard(this)
        showProgress()
        var result = withContext(Dispatchers.IO) {
            val librosRepository = LibrosRepository()
            librosRepository.getTermsRepo("", "Y", "003")?.let { response ->
                if (response.isSuccessful) {
                    Log.d("TESTURLFF", response.raw().request.url.toString())
                    serviceResult = response.body()
                    return@withContext response.body()
                }
            }
        } as LibrosModelVO
        hideProgress()
        if(result.result == null){
            return
        }

        if(result.result != null){
            if (result.result!!.resultCode!!.isNotEmpty() && result.result!!.resultCode == "Y") {
                jsonToContent(serviceResult!!.contents)
            } else {
                LibrosUtil.showMsgWindow(this@AcceptDetailActivity,"알림", serviceResult!!.result?.resultMessage,"확인")
            }
        } else {
            LibrosUtil.showMsgWindow(this@AcceptDetailActivity, "오류", "일시적인 네트워크 오류입니다.", "확인")
        }
    }

    private fun jsonToContent(result: JsonObject) {
        privateTerms = result.get("PrivateInfoPolicy").asString
        serviceUseTerms = result.get("ServiceUseAgreement").asString

        viewPager.adapter = ViewPagerAdapter(arrayListOf<String?>(serviceUseTerms, privateTerms))
        viewPager.setPageTransformer(ZoomOutPageTransformer())

        if (intent.hasExtra("position")) {
            viewPager.currentItem = intent.getIntExtra("position", 0)
        } else viewPager.currentItem = 0
    }

    private inner class ViewPagerAdapter(termList: ArrayList<String?>) : RecyclerView.Adapter<ViewPagerAdapter.PagerViewHolder>(){
        var item = termList

        inner class PagerViewHolder(parent: ViewGroup):
            RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.fragment_privacy_terms,parent,false)) {
            val term = itemView.txt_privacy_terms!!
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerViewHolder = PagerViewHolder((parent))

        override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                holder.term.text = Html.fromHtml(item[position],Html.FROM_HTML_MODE_LEGACY)
            } else {
                holder.term.text = Html.fromHtml(item[position])
            }
        }

        override fun getItemCount(): Int = item.size

    }

    inner class ZoomOutPageTransformer : ViewPager2.PageTransformer {
        override fun transformPage(view: View, position: Float) {
            view.apply {
                val pageWidth = width
                val pageHeight = height
                when {
                    position < -1 -> { // [-Infinity,-1)
                        // This page is way off-screen to the left.
                        alpha = 0f
                    }
                    position <= 1 -> { // [-1,1]
                        // Modify the default slide transition to shrink the page as well
                        val scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position))
                        val vertMargin = pageHeight * (1 - scaleFactor) / 2
                        val horzMargin = pageWidth * (1 - scaleFactor) / 2
                        translationX = if (position < 0) {
                            horzMargin - vertMargin / 2
                        } else {
                            horzMargin + vertMargin / 2
                        }

                        // Scale the page down (between MIN_SCALE and 1)
                        scaleX = scaleFactor
                        scaleY = scaleFactor

                        // Fade the page relative to its size.
                        alpha = (MIN_ALPHA +
                                (((scaleFactor - MIN_SCALE) / (1 - MIN_SCALE)) * (1 - MIN_ALPHA)))
                    }
                    else -> { // (1,+Infinity]
                        // This page is way off-screen to the right.
                        alpha = 0f
                    }
                }
            }
        }
    }

    companion object {

    }
}