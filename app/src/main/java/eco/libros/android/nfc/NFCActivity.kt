package eco.libros.android.nfc

import android.app.PendingIntent
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.NfcV
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import eco.libros.android.R
import eco.libros.android.common.ProgressFragment
import eco.libros.android.common.utill.LibrosLog
import eco.libros.android.common.utill.LibrosUtil
import eco.libros.android.common.variable.GlobalVariable
import eco.libros.android.data.LibData
import eco.libros.android.nfc.library.FrameUtil
import eco.libros.android.nfc.library.ICodeSLI
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.IOException

class NFCActivity : AppCompatActivity() {

    private var mAdapter: NfcAdapter? = null
    private var bookTagId = ""

    private var nfcType: String? = null
    var pendingIntent: PendingIntent? = null
    var intentFiltersArray:Array<IntentFilter> = emptyArray()
    var techListsArray:Array<Array<String>> = emptyArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_n_f_c)
        val bundle = intent.extras

        nfcType = if (bundle!!.containsKey("nfcType")) {
            bundle.getString("nfcType")
        } else
            "eco_01"
        mAdapter = NfcAdapter.getDefaultAdapter(this)
        if (mAdapter == null) {
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show()
            return
        }
        this.nfcType = "eco_01"
        GlobalVariable.mode = GlobalVariable.INVENTORY
    }
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        processNfc(intent!!)
//        val fragment: Fragment?= null
//        if (fragment is HomeFragment){
//            val homeFragment: HomeFragment = fragment
//            homeFragment.processNfc(intent?.getParcelableExtra(NfcAdapter.EXTRA_TAG)!!)
//        }
//        val tagFromIntent: Tag = intent?.getParcelableExtra(NfcAdapter.EXTRA_TAG)!!
//        var mainFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as HomeFragment
//        mainFragment.processNfc(tagFromIntent!!)

    }

    private fun processNfc(intent: Intent){
        val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)

        val nfcv = NfcV.get(tag)
        val out = ByteArrayOutputStream()
        val flags: Byte = ICodeSLI.makeFlags(false, false, true, false, false, false, false, false)
        var response: ByteArray? = byteArrayOf()

        var tagResult = true
        Log.d("TESTTAG", nfcv.tag.id.toString())
        try {
            if (GlobalVariable.mode == GlobalVariable.INVENTORY) {
                bookTagId = FrameUtil.getFormattedUID4ByteArray(nfcv.tag.id)
                if (bookTagId != null && nfcType != null) {
                    var tagId: String? = null
                    if (nfcType.equals("eco_01", ignoreCase = true)) {
                        tagId = bookTagId
                    } else if (nfcType.equals("3M", ignoreCase = true)) {
                        out.write(
                            ICodeSLI.ReadMultipleBlocks(
                                flags,
                                nfcv.tag.id,
                                0x00.toByte(),
                                0x1B.toByte()
                            )
                        )
                        nfcv.connect()
                        response = nfcv.transceive(out.toByteArray())
                        tagId = get3mRegistNo(response)
                    }
                    if (tagId != null && tagId.trim().isNotEmpty()) {
//                        kr.co.eco.common.nfc.NfcActivity.RegistNoTask().execute(nfcType, tagId)
                        BookRegNoTask().task(tagId)
                    } else {
                        LibrosUtil.showMsgWindow(
                            this,
                            "알림",
                            "태그가 인식 되지 않았습니다.\n다시 시도하여 주십시오.",
                            "확인"
                        )
                    }
                }
            }
//            else {
//                if (bookTagId != null && bookTagId.trim({ it <= ' ' }) == FrameUtil.getFormattedUID4ByteArray(nfcv.tag.id).trim() == false) {
//                    LibropiaUtil.showMsgWindow(this@NfcActivity, "알림", "대출처리한 도서와 다른 도서가 인식 되었습니다.\n대출처리한 도서의 태그를 인식하여야 보안 해제가 됩니다.", "확인")
//                } else {
//                    when (GlobalVariable.mode) {
//                        GlobalVariable.WRITE_AFI ->                            // AFI 해제값(예: 0x00) ==> 도서관마다 다름
//                            // AFI 설정값(예: 0x07) ==> 도서관마다 다름
//                            out.write(ICodeSLI.WriteAFI(flags, nfcv.tag.id, 0xD7.toByte()))
//                        GlobalVariable.RESET_EAS -> out.write(ICodeSLI.ResetEAS(flags, nfcv.tag.id))
//                    }
//                    nfcv.connect()
//                    response = nfcv.transceive(out.toByteArray())
//                    if (nfcv.responseFlags.toInt() == 0x00) {
////                        showSuccessMsgWindow()
//                    } else {
////                        count++
////                        if (count >= 3) {
//                            LibrosUtil.showMsgWindow(activity, "알림", "NFC를 이용한 인식 중 알 수 없는 오류가 발생했습니다.\n도서의 대출은 완료 되었으나, 보안 해제가 되지 않았습니다.\n도서관 직원에게 문의 바랍니다.", "확인")
////                        }
//                    }
//                }
//            }
        } catch (e: IOException) {
            tagResult = false
            LibrosLog.print(e.message.toString())
        } finally {
            // 장치 연결 해제
            if (nfcv.isConnected) {
                try {
                    nfcv.close()
                } catch (e: IOException) {
                    // TODO Auto-generated catch block
                    LibrosLog.print(e.message.toString())
                }
            }
        }

        if (tagResult == false) {
            LibrosUtil.showMsgWindow(this, "알림", "태그가 인식 되지 않았습니다.\n다시 시도하여 주십시오.", "확인")
        }
    }

    private fun get3mRegistNo(response: ByteArray?): String? {
        val sb = StringBuffer()

        val readBlocks = FrameUtil.getReposeDataList(response)

        if (readBlocks != null && readBlocks.size > 20) {
            readBlocks.removeAt(0)
            if (readBlocks[0] == "04" && readBlocks[3] == "01") {
                for (i in 4..16) {
                    if (readBlocks[i] != "00") {
                        sb.append(FrameUtil.getHexToAscii(readBlocks[i]))
                    }
                }
            }
        }

        return sb.toString()
    }

    inner class BookRegNoTask {
        private var progressFragment = ProgressFragment()
        private var result: eco.libros.android.common.model.LibrosDataVO? = null
        var userId: String? = null
        fun task(tagId: String) {
            try {
                progressFragment = ProgressFragment.newInstance("책 조회 중입니다.")
                progressFragment.show(supportFragmentManager!!, "progress")
            } catch (e: WindowManager.BadTokenException) {
                Log.e("error", e.message.toString())
                return
            }

            //doInBackground
            CoroutineScope(Dispatchers.Main).launch {
                result = withContext(Dispatchers.Default) {
                    if (LibrosUtil.getUserId(this@NFCActivity, false, needEncrypt = false)!!.isNotEmpty()
                    ) {
                        userId = LibrosUtil.getUserId(this@NFCActivity, false, needEncrypt = false)
                    } else {
                        progressFragment.dismiss()
                        return@withContext null
                    }

                    val sb = StringBuffer()
                    sb.append(resources.getString(R.string.url_libropia_web)).append("serviceName=MB_20_02_01_SERVICE")
                    sb.append("&userId=$userId")
                    sb.append("&libCode=${LibrosUtil.getLibCode(this@NFCActivity)}")
                    sb.append("&encryptFromId=N")
                    sb.append("&deviceType=" + resources.getString(R.string.device_type))
                    sb.append("&tagUid=$tagId")
                    Log.d("testBook", sb.toString())
                    LibData().getNfcTagRegNo(sb.toString())
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
                    val tag = result?.getContent().toString()
                    Log.d("testTag", tag)
                    setResult( RESULT_OK,Intent().apply {
                        putExtra("nfcTag",tag)
                    })
                    finish()
//                    webView.loadUrl("javascript:document.querySelector('total-controller').shadowRoot.querySelector('library-controller').shadowRoot.querySelector('nfc-loan').getNfcTagId('$tag')")
                } else if (result == null){
                    LibrosUtil.showCustomMsgWindow(
                        this@NFCActivity,
                        "오류",
                        "회원 조회에 실패했습니다. 잠시 후 다시 실행해주세요",
                        "확인", true,
                        DialogInterface.OnClickListener { dialog, which ->
                            dialog.dismiss()
//                        finish()
                        })
                } else {
                    LibrosUtil.showCustomMsgWindow(
                        this@NFCActivity,
                        "오류",
                        result?.getResultMsg(),
                        "확인", false,
                        DialogInterface.OnClickListener { dialog, which ->
                            dialog.dismiss()
//                        finish()
                        })
                }
            }
            //onPostExecute

        }
    }

    override fun onResume() {
        super.onResume()
        if (mAdapter != null) {
            pendingIntent = PendingIntent.getActivity(
                this, 0, Intent(this, javaClass).addFlags(
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
                ), 0
            )
            val ndef: IntentFilter = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
            try {
                ndef.addDataType("*/*")
            } catch (e: IntentFilter.MalformedMimeTypeException) {
                throw RuntimeException("fail", e)
            }

            intentFiltersArray = arrayOf(ndef)
            techListsArray = arrayOf(arrayOf(NfcV::class.java.name))
            if (pendingIntent != null) {
                mAdapter?.enableForegroundDispatch(
                    this,
                    pendingIntent,
                    intentFiltersArray,
                    techListsArray
                )
            }
        }
    }

    fun nfcStart(nfcType: String){

    }

    override fun onPause() {
        super.onPause()
        if (mAdapter != null) {
            mAdapter!!.disableForegroundDispatch(this)
        }
    }

}