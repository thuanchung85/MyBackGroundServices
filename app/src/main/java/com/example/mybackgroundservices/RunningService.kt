package com.example.mybackgroundservices

import android.annotation.SuppressLint
import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.Timer
import java.util.TimerTask


class RunningService: Service() {
    var counter: Int = 0

    private var timer: Timer? = null
    private var timerTask: TimerTask? = null
    fun startTimer() {
        timer = Timer()
        timerTask = object : TimerTask() {
            override fun run() {
                Log.i("Speech result", "=========  " + (counter++))
            }
        }
        timer!!.schedule(timerTask, 1000, 1000) //
    }

    fun stoptimertask() {
        if (timer != null) {
            timer!!.cancel()
            timer = null
        }
    }


    enum class Action {
        START, STOP
    }
    override fun onCreate() {
        super.onCreate()
    }

    @Override
    override fun onDestroy() {
        super.onDestroy()
        stoptimertask()
        MainActivity.stt.closeSpeechOperations()
    }

    @Override
    override fun onBind(intent: Intent?): IBinder? {
       return null
    }


    @Override
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        when(intent?.action){
            Action.START.name -> start()
            Action.STOP.name -> stop()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun start(){
        startTimer();
        val notification = NotificationCompat.Builder(this, "111")
            .build()

        startForeground(1, notification)
        MainActivity.stt.startSpeechRecognition()
    }
    private fun stop(){
        stoptimertask()
        MainActivity.stt.closeSpeechOperations()
        stopSelf()

    }
}