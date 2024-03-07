package com.example.mybackgroundservices

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.foregroundservice.STT.Stt
import com.example.foregroundservice.STT.SttListener
import com.example.mybackgroundservices.CHUNG_LIB.CheckPermission_Func


class MainActivity : AppCompatActivity() {
    companion object {
        lateinit var stt: Stt
    }
    private var mToast: Toast? = null
    var isPermissionOK: Boolean = false

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
        //setup stt engine
        initSttEngine(this)
        //==check permission==//

        CheckPermission_Func.CheckPermission_Func.checkPermission(this,android.Manifest.permission.WAKE_LOCK)
        CheckPermission_Func.CheckPermission_Func.checkPermission(this,android.Manifest.permission.TURN_SCREEN_ON)

        var p = CheckPermission_Func.CheckPermission_Func.checkPermission(this,android.Manifest.permission.RECORD_AUDIO)
        var p1 = CheckPermission_Func.CheckPermission_Func.checkPermission(this,android.Manifest.permission.POST_NOTIFICATIONS)
        //if all OK
        if(p && p1 )
        {
            isPermissionOK = true
            activeButtons()
        }

        if(isPermissionOK){
            Toast.makeText(this, "WAKEUP", Toast.LENGTH_SHORT).show()
            Intent(this, RunningService::class.java).also {
                it.action = RunningService.Action.START.toString()
                startService(it)
            }
        }


    }



    //===================
    private fun initSttEngine(context: Context) {
        stt = Stt(application, object : SttListener {
            override fun onSttLiveSpeechResult(liveSpeechResult: String) {
                Log.d(application.packageName, "Speech result - $liveSpeechResult")

                mToast?.cancel()
                mToast = Toast.makeText(context, liveSpeechResult, Toast.LENGTH_SHORT)
                mToast!!.show()

                if(liveSpeechResult.contains("ello")){
                    //val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("myapp://"))
                    //startActivity(browserIntent)
                    //val intent: Intent = Intent(baseContext, MainActivity  ::class.java)
                    Log.d(application.packageName, "Speech result - HELLO TO REOPEN APP")
                    //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //baseContext.startActivity(intent)

                    triggerRebirth(context, MainActivity::class.java)

                }
                if(liveSpeechResult.contains("open")) {
                    baseContext.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,

                            Uri.parse("http://google.com")
                        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
            }

            fun triggerRebirth(context: Context, myClass: Class<*>?) {
                Log.d(application.packageName, "Speech result - triggerRebirth")
                val intent = Intent(baseContext, myClass)
                val pendIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY)
                intent.setAction(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.setComponent(
                    ComponentName(
                        applicationContext.packageName,
                        MainActivity::class.java.getName()
                    )
                )
                //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                mToast?.cancel()
                mToast = Toast.makeText(context, "triggerRebirth", Toast.LENGTH_SHORT)
                mToast!!.show()
                //baseContext.startActivity(intent)
                pendIntent.send(context, 0, intent)
                //Runtime.getRuntime().exit(0)
                val powerManager = context.getSystemService(POWER_SERVICE) as PowerManager
                if (!powerManager.isInteractive) { // if screen is not already on, turn it on (get wake_lock)
                    @SuppressLint("InvalidWakeLockTag") val wl = powerManager.newWakeLock(
                        PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE or PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
                        "id:wakeupscreen"
                    )
                    wl.acquire()
                }

            }

            override fun onSttFinalSpeechResult(speechResult: String) {
                Log.d(application.packageName, "Speech result - $speechResult")
                mToast?.cancel()
                mToast = Toast.makeText(context, speechResult, Toast.LENGTH_SHORT)
                mToast!!.show()
            }

            override fun onSttSpeechError(errMsg: String) {
                Log.d(application.packageName, "Speech error - $errMsg")
            }
        })
    }


    //nếu user ok permission thì kich hoat luôn isPermissionOK = true
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 1){
            isPermissionOK = true
            Toast.makeText(this, "ALL Permission granted", Toast.LENGTH_SHORT).show()

            activeButtons()
        }
    }

    fun activeButtons(){
        //nut stop chuông BG
        val mButtonStop = findViewById<Button>(R.id.mButtonStop)
        mButtonStop.setOnClickListener {
            Toast.makeText(this, "ACTION_MANAGE_OVERLAY_PERMISSION ", Toast.LENGTH_SHORT).show()
            startActivity( Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION));
            //Intent(this, RunningService::class.java).also {
                //it.action = RunningService.Action.STOP.toString()
                //startService(it)
            //}
        }

        //nut record voice
        val mButtonRecordVoice = findViewById<Button>(R.id.mButtonStartService)
        mButtonRecordVoice.setOnClickListener {
            if(isPermissionOK){
                Toast.makeText(this, "Start", Toast.LENGTH_SHORT).show()
                Intent(this, RunningService::class.java).also {
                    it.action = RunningService.Action.START.toString()
                    startService(it)
                }
            }

        }
    }
}

