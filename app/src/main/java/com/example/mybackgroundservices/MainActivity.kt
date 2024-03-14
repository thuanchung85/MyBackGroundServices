package com.example.mybackgroundservices

import android.app.ActivityManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mybackgroundservices.CHUNG_LIB.CheckPermission_Func
import java.util.ArrayList
import java.util.Locale


class MainActivity : AppCompatActivity() {

    private var speechRecognizer: SpeechRecognizer? = null
    private var speechRecognizerIntent: Intent? = null

    private var mSTTTtextview: TextView? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mSTTTtextview = findViewById<TextView>(R.id.mSTTTtextview)
        //==make stt==//
         speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
         speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechRecognizerIntent!!.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        speechRecognizerIntent!!.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        speechRecognizerIntent!!.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to text")
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {

            }

            override fun onBeginningOfSpeech() {

            }

            override fun onRmsChanged(rmsdB: Float) {

            }

            override fun onBufferReceived(buffer: ByteArray?) {

            }

            override fun onEndOfSpeech() {

            }

            override fun onError(error: Int) {

            }

            override fun onResults(results: Bundle?) {
                // Handle the recognized speech results here.
                val r: ArrayList<String>? = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (r != null) {
                    Log.d("Speech result", r[0])
                    mSTTTtextview!!.text = r[0]
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {

            }

            override fun onEvent(eventType: Int, params: Bundle?) {

            }
        })
        //==check permission==//

         val p1 = CheckPermission_Func.CheckPermission_Func.checkPermission(this,android.Manifest.permission.RECORD_AUDIO, 1)

        if(p1 ){

            if(!isServiceRunning(RunningService::class.java.name)) {
                Intent(this, RunningService::class.java).also {
                    it.action = RunningService.Action.START.toString()
                    //setup stt engine
                    startService(it)
                }
            }

          activeButtons()
        }



    }


    private fun isServiceRunning(serviceName: String): Boolean {
        var serviceRunning = false
        val am = this.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val l = am.getRunningServices(50)
        val i: Iterator<ActivityManager.RunningServiceInfo> = l.iterator()
        while (i.hasNext()) {
            val runningServiceInfo = i
                .next()

            if (runningServiceInfo.service.className == serviceName) {
                serviceRunning = true

                if (runningServiceInfo.foreground) {
                    //service run in foreground
                }
            }
        }
        return serviceRunning
    }


    //nếu user ok permission thì kich hoat luôn isPermissionOK = true
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 1){

           CheckPermission_Func.CheckPermission_Func.checkPermission(this,android.Manifest.permission.POST_NOTIFICATIONS, 2)

        }
        if(requestCode == 2){
            activeButtons()
        }
    }

    fun activeButtons(){

        //nut xin quyen khoi dong app khi dien thoai off
        val mWakeUpButtonPermission = findViewById<Button>(R.id.mWakeUpButtonPermission)
        mWakeUpButtonPermission.setOnClickListener {
            startActivity( Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
        }

        //nut stop service STT va micro
        val mButtonStopVoice = findViewById<Button>(R.id.mButtonStopService)
        mButtonStopVoice.setOnClickListener {
            Intent(this, RunningService::class.java).also {
                    it.action = RunningService.Action.STOP.toString()
                    startService(it)
            }
            speechRecognizer?.stopListening()

        }

        //nut start ai ear service STT
        val mButtonRecordVoice = findViewById<Button>(R.id.mButtonStartService)
        mButtonRecordVoice.setOnClickListener {
            if(!isServiceRunning(RunningService::class.java.name)) {
                Intent(this, RunningService::class.java).also {
                    it.action = RunningService.Action.START.toString()
                    startService(it)
                }
            }
            speechRecognizer?.startListening(speechRecognizerIntent);

        }

        //nut CALL CHAT GPT API
        val mCallCHATGPT = findViewById<Button>(R.id.mCallCHATGPT)
        mCallCHATGPT.setOnClickListener{


        }
    }

}

