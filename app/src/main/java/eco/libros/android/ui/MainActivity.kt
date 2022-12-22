package eco.libros.android.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import eco.libros.android.R
import eco.libros.android.common.utill.EBookDownloadTask
import eco.libros.android.myContents.MyEbookListModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity(){

    var context: Context? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val data = "{\"thumbnail\":\"https://epbook.eplib.or.kr/resources/images/yes24/Msize/112361381M.jpg\",\"author\":\"심윤경 저\",\"lent_key\":\"1506740\",\"return_key\":\"238298\",\"epubID\":\"1506740\",\"title\":\"나의 아름다운 할머니\",\"return_link\":\"http://211.253.36.163:38444/ebook/Libros_S16_011_ebook_procesing_json?version=v20&user_id=ecotest&lib_code=111042&id=112361381&proc_mode=return&lent_key=238298&udid=\",\"book_info_link\":\"http://211.253.36.163:38444/ebook/Libros_S16_010_ebook_detail_json?version=v20&lib_code=111042&id=112361381&udid=&user_id=ecotest&portal_id=\",\"comcode\":\"YES24\",\"cover\":\"https://epbook.eplib.or.kr/resources/images/yes24/Msize/112361381M.jpg\",\"ebook_lib_name\":\"은평구립공공도서관\",\"extending_count\":\"0\",\"lending_date\":\"2022-12-20\",\"lending_expired_date\":\"2022-12-26\",\"drm_url_info\":\"http://epbook.eplib.or.kr:8088/\",\"ISBN\":\"9791160949674\",\"platform_type\":\"\",\"lib_code\":\"111042\",\"file_type\":\"EPUB\",\"publisher\":\"사계절\",\"extension_link\":\"http://211.253.36.163:38444/ebook/Libros_S16_011_ebook_procesing_json?version=v20&user_id=ecotest&lib_code=111042&id=112361381&proc_mode=extension&lent_key=238298&udid=\",\"id\":\"112361381\",\"drm_key\":\"1506740\"}"
//        val data = "{\"thumbnail\":\"https://epbook.eplib.or.kr/resources/images/Msize/PRD000130942M.jpg\",\"author\":\"김영익,강흥보 지음\",\"lent_key\":\"238541\",\"return_key\":\"238541\",\"title\":\"2020-2022 앞으로 3년, 투자의 미래\",\"return_link\":\"http://211.253.36.163:38444/ebook/Libros_S16_011_ebook_procesing_json?version=v20&user_id=ecotest&lib_code=111042&id=PRD000130942&proc_mode=return&lent_key=238541&udid=\",\"book_info_link\":\"http://211.253.36.163:38444/ebook/Libros_S16_010_ebook_detail_json?version=v20&lib_code=111042&id=PRD000130942&udid=&user_id=ecotest&portal_id=\",\"comcode\":\"ECO_MOA\",\"cover\":\"https://epbook.eplib.or.kr/resources/images/Msize/PRD000130942M.jpg\",\"ebook_lib_name\":\"은평구립공공도서관\",\"extending_count\":\"0\",\"lending_date\":\"2022-12-22\",\"lending_expired_date\":\"2022-12-28\",\"drm_url_info\":\"https://epbook.eplib.or.kr/ebookPlatform/\",\"download_link\":\"\",\"ISBN\":\"9791160074598\",\"platform_type\":\"\",\"FirstLicense\":\"PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0idXRmLTgiPz48TElDRU5TRT48Q09OVEVOVD48RFJNPk08L0RSTT48VkVSPjEuMDwvVkVSPjxVVUlEPjJkZWQ4YTUyNjliNzQ4NzNhY2U0YzIxNjRlMWJkMjM0PC9VVUlEPjxGX1RZUEU+RVBVQjwvRl9UWVBFPjxET1dOX1VSTD5odHRwOi8vZXBib29rLmVwbGliLm9yLmtyOjgxMDAvcmVzb3VyY2VzL2ZpbGVzL1BSRDAwMDEzMDk0Mi5FUFVCPC9ET1dOX1VSTD48RFJNX09SRD5PUkQyMDIyMTIyMjIyMDEwMjk5MzcwPC9EUk1fT1JEPjxBVVRIX1VSTD5odHRwOi8vTW9hRHJtTGljZW5zZUFwaS5lY28ua3I6ODEvYXBpL0FwaV9PcmRlcl9HZXRDbGllbnRMaWNlbnNlLmFzcHg8L0FVVEhfVVJMPjxTX09SRD5CMkJfT1JHMDAwMDAzNl8zMzU3NzwvU19PUkQ+PENfRFQ+MjAyMi0xMi0yMiAxMzowMTowNiArMDkwMDwvQ19EVD48L0NPTlRFTlQ+PEhNQUM+MDJDOTExMTdBNDQ1MjNDNkVDNTdGRkIxODlBQTU1REVFRDc0REYxMjwvSE1BQz48L0xJQ0VOU0U+\",\"lib_code\":\"111042\",\"file_type\":\"EPUB\",\"publisher\":\"한즈미디어(한스미디어)\",\"extension_link\":\"http://211.253.36.163:38444/ebook/Libros_S16_011_ebook_procesing_json?version=v20&user_id=ecotest&lib_code=111042&id=PRD000130942&proc_mode=extension&lent_key=238541&udid=\",\"id\":\"PRD000130942\",\"drm_key\":\"238541\"}"
        findViewById<Button>(R.id.btn_start).setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                val gson = Gson()
                val list = gson.fromJson(data, MyEbookListModel::class.java)
                Log.d("test","$data")
                EBookDownloadTask(
                    _activity = this@MainActivity,
                    _ebookData = list,
                    _downloadPlace = "detail"
                ).downloadEBook()
            }
        }

    }

}
