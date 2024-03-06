package com.example.mybackgroundservices.CHUNG_LIB

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

public class CheckPermission_Func {
    object CheckPermission_Func {
        fun checkPermission(context: Context, permissionString:String): Boolean{
            val nameOfPremission = permissionString
            val p = ContextCompat.checkSelfPermission(context, nameOfPremission) != PackageManager.PERMISSION_GRANTED
            if(p){
                Toast.makeText(context, "Fail Permission: " + nameOfPremission, Toast.LENGTH_SHORT).show()
                //ask for permission
                ActivityCompat.requestPermissions(context as Activity, arrayOf(nameOfPremission), 1)
                return false
            }
            else{
                Toast.makeText(context, "OK Permission: " + nameOfPremission, Toast.LENGTH_SHORT).show()
                return  true
            }
        }

    }


}