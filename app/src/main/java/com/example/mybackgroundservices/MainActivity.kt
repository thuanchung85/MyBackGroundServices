package com.example.mybackgroundservices

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.mybackgroundservices.CHUNG_LIB.*

class MainActivity : AppCompatActivity() {

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

        //==check permission==//

        var p = CheckPermission_Func.CheckPermission_Func.checkPermission(this,android.Manifest.permission.RECORD_AUDIO)
        var p1 = CheckPermission_Func.CheckPermission_Func.checkPermission(this,android.Manifest.permission.POST_NOTIFICATIONS)
        //if all OK
        if(p && p1 )
        {
            isPermissionOK = true
            //nut stop chuông BG
            val mButtonStop = findViewById<Button>(R.id.mButtonStop)
            mButtonStop.setOnClickListener {
                Toast.makeText(this, "stop", Toast.LENGTH_SHORT).show()
                Intent(this, RunningService::class.java).also {
                    it.action = RunningService.Action.STOP.toString()
                    startService(it)
                }
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



    //nếu user ok permission thì kich hoat luôn isPermissionOK = true
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 1){
            isPermissionOK = true
            Toast.makeText(this, "ALL Permission granted", Toast.LENGTH_SHORT).show()
        }
    }
}

