package com.example.mybackgroundservices

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.AssetFileDescriptor
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
import com.example.mybackgroundservices.CHUNG_LIB.MySingleton_LogsManager
import com.example.mybackgroundservices.CHUNG_LIB.ReadJSONFromAssets


import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.Locale


class MainActivity : AppCompatActivity() {
    companion object {
        lateinit var stt: Stt
    }
    var  tflite:Interpreter? = null
    lateinit var token2index:List<Any>
    var mapOftoken2index:List<String> = listOf()

    private var langDefault = "en"
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onResume() {
        super.onResume()

        val p1 = CheckPermission_Func.CheckPermission_Func.checkPermission(this,android.Manifest.permission.RECORD_AUDIO, 1)
        //val p2 = CheckPermission_Func.CheckPermission_Func.checkPermission(this,android.Manifest.permission.POST_NOTIFICATIONS, 2)
        if(p1 ){
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

        //load JSON file AI
        //====load json file for AI===//
        var token2indexString = ReadJSONFromAssets(baseContext,"token2index.json")
        Log.e("myLOG", "AI JSON COMPLETED: ->" + token2indexString)
        val gson = Gson()

        val itemType = object : TypeToken<List<Any>>() {}.type
         token2index = gson.fromJson<List<Any>>(token2indexString, itemType)

        Log.e("myLOG", "GSON: ->" + token2index)
         mapOftoken2index = token2index.map{
            it.toString().drop(1).dropLast(1).split("=").first()
        }

        //=====init AI=====///
        try {
            tflite = Interpreter(loadModelFile("smallbertner")!!)
            Log.e("myLOG", "AI init COMPLETED: ->"+ tflite.hashCode().toString())
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        //==check permission==//

         val p1 = CheckPermission_Func.CheckPermission_Func.checkPermission(this,android.Manifest.permission.RECORD_AUDIO, 1)
        //val p2 = CheckPermission_Func.CheckPermission_Func.checkPermission(this,android.Manifest.permission.POST_NOTIFICATIONS, 2)

        if(p1){
            if(!isServiceRunning(RunningService::class.java.name)) {
                Intent(this, RunningService::class.java).also {
                    it.action = RunningService.Action.START.toString()
                    //setup stt engine
                    initSttEngine(this,langDefault)
                    startService(it)
                }
            }

          activeButtons(this)
        }

    }



    //===================
    private fun initSttEngine(context: Context, langDefault:String) {
        stt = Stt(langDefault,application, object : SttListener {
            override fun onSttLiveSpeechResult(liveSpeechResult: String)
            {
                //Log.d(application.packageName, "Speech result - $liveSpeechResult")
                actionByVoice(context,liveSpeechResult)
            }

            override fun onSttFinalSpeechResult(speechResult: String) {
                //Log.d(application.packageName, "Speech result - $speechResult")
                actionByVoice(context,speechResult)
            }

            override fun onSttSpeechError(errMsg: String) {
                Log.d(application.packageName, "Speech error - $errMsg")
            }
        })
    }


    //===HELPER FUNCTION===//
    //nếu user ok permission thì kich hoat luôn isPermissionOK = true
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 1){
           CheckPermission_Func.CheckPermission_Func.checkPermission(this,android.Manifest.permission.POST_NOTIFICATIONS, 2)
        }
        if(requestCode == 2){
            activeButtons(this)
        }
    }

    private fun activeButtons(context:Context){

        //nut xin quyen khoi dong app khi dien thoai off
        val mButtonStop = findViewById<Button>(R.id.mButtonStop)
        mButtonStop.setOnClickListener {
            //Toast.makeText(this, "ACTION_MANAGE_OVERLAY_PERMISSION ", Toast.LENGTH_SHORT).show()
            startActivity( Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))

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






            //vào giai đoan kiem tra text string input và tao token data
            if(this.tflite != null) {
                //đây là text real input vào
                val ss = localTokenizer("hello my name is playgroundvina", token2index)
                Log.e("myLOG", "AI  localTokenizer: ->" + ss)


            }

        }
    }

    private fun loadModelFile(filename:String): MappedByteBuffer? {
        val fileDescriptor: AssetFileDescriptor = assets.openFd("$filename.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel: FileChannel = inputStream.channel
        val startOffset: Long = fileDescriptor.startOffset
        val declaredLength: Long = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }



    fun localTokenizer(textInput: String, token2index: List<Any>): Map<String, List<Int>> {
        val inputs = mutableMapOf(
            "token_ids" to mutableListOf<Int>(),
            "segment_ids" to MutableList(48) { 0 },
            "padding_mask" to mutableListOf<Int>()
        )

        var textInputModified = "[CLS] ${textInput.lowercase(Locale.ROOT)} [SEP]"
        val splitInputs = textInputModified.split(" ")


        Log.e("myLOG", "AI  mapOftoken2index: ->" + mapOftoken2index)
        for (item in splitInputs)
        {


            //nếu từ đó có trong token2index (TRƯỜNG HƠP TRÙNG 1 TỪ ĐƠN hello, my, name...)
            if (item in mapOftoken2index) {
                Log.e("myLOG", "AI  token có trong token2index: ->" + mapOftoken2index.indexOf(item))
                val x = mapOftoken2index.indexOf(item)
                inputs["token_ids"]?.add(x)
                //inputs["padding_mask"]?.add(1)
            }
            //nếu không có trong token2index đơn (TRƯỜNG HỢP LÀ 1 TỪ GHÉP LẠI TỪ NHIỀU TỪ KHÁC playgroundvina = playground + vina)
            else {
                //phân tích sâu thêm nếu từ đó có chứa 1 phần tử nào đó của mapOftoken2index, như từ playground trong playgroundvina
                //bước 1: lọc ra các từ có khả năng dòng họ với item nhất ghi vào array b
                val b = mapOftoken2index.withIndex().filter {
                    item.contains(it.value.replace("#",""), ignoreCase = true)

                }//.map { it.index }
                println(b)
                //bước 2: so trùng, ươu tiên không có "#" trước, ta cần tách ra 2 array 1 bên không có "#" và 1 bên có "#"
                val b2 = b.filter {
                    !it.value.contains("#")
                }
                val b3coThang =  b - b2
                println(b2)
                println(b3coThang)

                //chọn ra thằng có khả năng trùng lớn nhất trong array b2, chính là thằng dài nhất lấy ra làm đầu
                val chooseBest = b2.maxBy { it.value.length }
                println(chooseBest)
                Log.e("myLOG", "AI  token có trong token2index MOT PHAN: ->" + chooseBest.index)
                val x =  chooseBest.index
                inputs["token_ids"]?.add(x)

                //cắt lấy phần đuôi sau khi có phần đầu
                val cutOutTail = item.replace(chooseBest.value,"")
                println(cutOutTail)
                //đem phần đuôi đi phân tích với b3coThang
                val chooseBest2 = b3coThang.filter {
                    it.value.contains(cutOutTail)
                }
                    .maxBy { it.value.length }
                println(chooseBest2)
                Log.e("myLOG", "AI  token có trong token2index MOT PHAN: ->" + chooseBest2.index)
                val x2 =  chooseBest2.index
                inputs["token_ids"]?.add(x2)

                ///hoàn toàn không có nên fail 100%
                //var temp = item
                //var i = 0
                ///var prefix = ""
                //Log.e("myLOG", "AI  token KHONG có trong token2index: ->" + item)

            }

            Log.e("myLOG", "AI  token_ids: ->" + inputs["token_ids"])
        }

        repeat(48 - inputs["padding_mask"]!!.size) {
            inputs["padding_mask"]?.add(0)
        }

        repeat(48 - inputs["token_ids"]!!.size) {
            inputs["token_ids"]?.add(0)
        }

        return inputs
    }


    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
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
        intent.setAction(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
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

    fun actionByVoice(context: Context, txtCommand:String){
        if(txtCommand.contains("ello") ||
            txtCommand.contains("hello") ||
            txtCommand.contains("hi") ||
            txtCommand.contains("hey") ||
            txtCommand.contains("xin chào") ||
            txtCommand.contains("chào") ||
            txtCommand.contains("안녕하세요")
        ){

            MySingleton_LogsManager.init(context,"myLOG :- $txtCommand -> TO REOPEN APP")
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

            val playMusicURL = "https://www.youtube.com/watch?v=fo8baQK7qYc&autoplay=1"
            baseContext.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(playMusicURL)
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )

            MySingleton_LogsManager.init(context,"myLOG :- $txtCommand -> STOP MICRO PHONE PLAY MUSIC $playMusicURL")
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

                //if (runningServiceInfo.foreground) {
                    //service run in foreground
                //}
            }
        }
        return serviceRunning
    }

}

