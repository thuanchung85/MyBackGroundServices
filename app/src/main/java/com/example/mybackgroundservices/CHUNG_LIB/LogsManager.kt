package com.example.mybackgroundservices.CHUNG_LIB

import android.content.Context
import android.util.Log


object MySingleton_LogsManager {

    private  val  mTAG = "LogsManager";

    fun init(ctx: Context, message:String) {
        Log.d(this.mTAG, message)
    }
}