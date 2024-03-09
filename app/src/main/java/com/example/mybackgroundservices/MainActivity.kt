package com.example.mybackgroundservices

import android.annotation.SuppressLint
import android.app.ActivityManager
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
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.util.Util
import com.example.foregroundservice.STT.Stt
import com.example.foregroundservice.STT.SttListener
import com.example.mybackgroundservices.CHUNG_LIB.CheckPermission_Func


class MainActivity : AppCompatActivity() {
    companion object {
        lateinit var stt: Stt
    }

    var langDefault = "en"
    override fun onResume() {
        super.onResume()

        val p1 = CheckPermission_Func.CheckPermission_Func.checkPermission(this,android.Manifest.permission.RECORD_AUDIO, 1)
        val p2 = CheckPermission_Func.CheckPermission_Func.checkPermission(this,android.Manifest.permission.POST_NOTIFICATIONS, 2)
        if(p1 && p2){
            if(!isServiceRunning(RunningService::class.java.name)) {
                Intent(this, RunningService::class.java).also {
                    it.action = RunningService.Action.START.toString()
                    //setup stt engine
                    initSttEngine(this,langDefault)
                    startService(it)
                }
            }
        }
    }
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

        //==check permission==//

         val p1 = CheckPermission_Func.CheckPermission_Func.checkPermission(this,android.Manifest.permission.RECORD_AUDIO, 1)
        val p2 = CheckPermission_Func.CheckPermission_Func.checkPermission(this,android.Manifest.permission.POST_NOTIFICATIONS, 2)

        if(p1 && p2){
            if(!isServiceRunning(RunningService::class.java.name)) {
                Intent(this, RunningService::class.java).also {
                    it.action = RunningService.Action.START.toString()
                    //setup stt engine
                    initSttEngine(this,langDefault)
                    startService(it)
                }
            }
            else{

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

    //===================
    private fun initSttEngine(context: Context, langDefault:String) {
        stt = Stt(langDefault,application, object : SttListener {
            override fun onSttLiveSpeechResult(liveSpeechResult: String)
            {
                Log.d(application.packageName, "Speech result - $liveSpeechResult")

                actionByVoice(liveSpeechResult)
            }

            fun triggerRebirth(context: Context, myClass: Class<*>?) {
                Log.d(application.packageName, "Speech result - triggerRebirth")
                val intent = Intent(baseContext, myClass)
                val pendingFlags = if (Util.SDK_INT >= 23) {
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
                
                val pendIntent = PendingIntent.getActivity(context, 0, intent,pendingFlags)
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
                //mToast?.cancel()
                //mToast = Toast.makeText(context, "triggerRebirth", Toast.LENGTH_SHORT)
               // mToast!!.show()
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

            fun actionByVoice(txtCommand:String){
                if(txtCommand.contains("ello") ||
                    txtCommand.contains("hello") ||
                    txtCommand.contains("hi") ||
                    txtCommand.contains("hey") ||
                    txtCommand.contains("xin chào") ||
                    txtCommand.contains("chào") ||
                    txtCommand.contains("안녕하세요")
                ){
                    Log.d(application.packageName, "Speech result - HELLO TO REOPEN APP")
                    triggerRebirth(context, MainActivity::class.java)
                }
                if(txtCommand.contains("open") ||
                    txtCommand.contains(" 열려 있는") ||
                    txtCommand.contains(" play") ||
                    txtCommand.contains(" nhạc") ||
                    txtCommand.contains(" 놀다")
                ) {
                    Intent(context, RunningService::class.java).also {
                        it.action = RunningService.Action.STOP.toString()
                        startService(it)
                    }

                    baseContext.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://www.youtube.com/watch?v=fo8baQK7qYc&autoplay=1")
                        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
            }

            override fun onSttFinalSpeechResult(speechResult: String) {
                Log.d(application.packageName, "Speech result - $speechResult")
                actionByVoice(speechResult)
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

           CheckPermission_Func.CheckPermission_Func.checkPermission(this,android.Manifest.permission.POST_NOTIFICATIONS, 2)

        }
        if(requestCode == 2){
            activeButtons()
        }
    }

    fun activeButtons(){

        //nut xin quyen khoi dong app khi dien thoai off
        val mButtonStop = findViewById<Button>(R.id.mButtonStop)
        mButtonStop.setOnClickListener {
            //Toast.makeText(this, "ACTION_MANAGE_OVERLAY_PERMISSION ", Toast.LENGTH_SHORT).show()
            startActivity( Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION));

        }

        //nut stop service va micro
        val mButtonStopVoice = findViewById<Button>(R.id.mButtonStopService)
        mButtonStopVoice.setOnClickListener {

                //Toast.makeText(this, "Start", Toast.LENGTH_SHORT).show()
                Intent(this, RunningService::class.java).also {
                    it.action = RunningService.Action.STOP.toString()
                    startService(it)
                }


        }

        //nut start ai ear service
        val mButtonRecordVoice = findViewById<Button>(R.id.mButtonStartService)
        mButtonRecordVoice.setOnClickListener {
            if(!isServiceRunning(RunningService::class.java.name)) {
                //Toast.makeText(this, "Start", Toast.LENGTH_SHORT).show()
                Intent(this, RunningService::class.java).also {
                    it.action = RunningService.Action.START.toString()
                    initSttEngine(this,langDefault)
                    startService(it)
                }
            }

        }

        //nut chuyen korean language
        val mButtonKoreanVoice = findViewById<Button>(R.id.mKoreanlanguage)
        mButtonKoreanVoice.setOnClickListener{


            if(!isServiceRunning(RunningService::class.java.name)) {
                langDefault = "ko"
                //Toast.makeText(this, "Start", Toast.LENGTH_SHORT).show()
                initSttEngine(this,langDefault)
                Intent(this, RunningService::class.java).also {
                    it.action = RunningService.Action.START.toString()
                    initSttEngine(this,langDefault)
                    startService(it)
                }
            }
            else{
                Intent(this, RunningService::class.java).also {
                    it.action = RunningService.Action.STOP.toString()
                    initSttEngine(this,langDefault)
                    startService(it)
                }
                langDefault = "ko"
                initSttEngine(this,langDefault)

                Intent(this, RunningService::class.java).also {
                    it.action = RunningService.Action.START.toString()
                    initSttEngine(this,langDefault)
                    startService(it)
                }
            }
        }
    }
}

