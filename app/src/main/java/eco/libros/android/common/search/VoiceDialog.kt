package eco.libros.android.common.search

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import eco.libros.android.R
import eco.libros.android.ui.MainActivity
import kotlinx.android.synthetic.main.fragment_e_book.*
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [VoiceDialog.newInstance] factory method to
 * create an instance of this fragment.
 */
class VoiceDialog : DialogFragment(), View.OnClickListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var mRecognizer: SpeechRecognizer? = null

    fun getInstance(): VoiceDialog {
        return VoiceDialog()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view : View = inflater.inflate(R.layout.fragment_voice_dialog, container, false)
        mRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        mRecognizer?.setRecognitionListener(recognitionListener())
        mRecognizer?.startListening(
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, activity?.packageName)
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(RecognizerIntent.EXTRA_PROMPT, "책 이름을 말해주세요.")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                startActivityForResult(this,MainActivity.AUDIO_REQUEST_CODE)
            }
        )

        return view
    }

    override fun onDestroyView() {
        if (mRecognizer != null){
            mRecognizer?.cancel()
            mRecognizer?.destroy()
        }

        super.onDestroyView()
    }

    private fun recognitionListener() = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            Toast.makeText(activity, "음성인식 시작", Toast.LENGTH_LONG).show()
        }

        override fun onRmsChanged(rmsdB: Float) {

        }

        override fun onBufferReceived(buffer: ByteArray?) {

        }

        override fun onPartialResults(partialResults: Bundle?) {

        }

        override fun onEvent(eventType: Int, params: Bundle?) {

        }

        override fun onBeginningOfSpeech() {

        }

        override fun onEndOfSpeech() {

        }

        override fun onError(error: Int) {
            var message: String? = null
            when (error) {
                SpeechRecognizer.ERROR_AUDIO -> message = "오디오 에러"
                SpeechRecognizer.ERROR_CLIENT -> message = "클라이언트 에러"
                SpeechRecognizer.ERROR_NETWORK -> message = "네트워크 에러"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> message = "시간초과"
                SpeechRecognizer.ERROR_NO_MATCH -> message = "찾을 수 없음"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> message = "권한이 없음"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> message = "바쁨"
                else -> message = "네트워크 오류"
            }
            Toast.makeText(activity, "오류원인 : $message", Toast.LENGTH_LONG).show()
        }

        override fun onResults(results: Bundle?) {
            val result : String = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)!![0]
            Toast.makeText(context,result, Toast.LENGTH_LONG).show()
            webView.loadUrl("javascript:document.querySelector('total-controller').shadowRoot.querySelector('lib-main-controll').testVoice('$result')")
        }
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment VoiceDialog.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            VoiceDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onClick(v: View?) {
        TODO("Not yet implemented")
    }
}