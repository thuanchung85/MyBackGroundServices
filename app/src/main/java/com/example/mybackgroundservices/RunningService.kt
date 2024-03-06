package com.example.mybackgroundservices

import android.annotation.SuppressLint
import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat


class RunningService: Service() {
    enum class Action {
        START, STOP
    }
    override fun onCreate() {
        super.onCreate()
    }

    @Override
    override fun onDestroy() {
        super.onDestroy()
    }

    @Override
    override fun onBind(intent: Intent?): IBinder? {
       return null
    }


    @Override
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        when(intent?.action){
            Action.START.name -> start()
            Action.STOP.name -> stopSelf()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun start(){
        val notification = NotificationCompat.Builder(this, "111")
            .build()
        startForeground(1, notification)
    }
}