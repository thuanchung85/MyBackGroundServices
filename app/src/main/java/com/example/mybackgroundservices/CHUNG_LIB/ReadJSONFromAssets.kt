package com.example.mybackgroundservices.CHUNG_LIB
import android.content.Context
import android.util.Log
import com.google.gson.Gson


import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Dictionary
import kotlinx.serialization.*
import kotlinx.serialization.json.Json


fun ReadJSONFromAssets(context: Context, path: String): String {
        val identifier = "[ReadJSON]"

            val file = context.assets.open("$path")
            Log.i(
                identifier,
               "ReadJSONFromAssets"
            )
            val bufferedReader = BufferedReader(InputStreamReader(file))
            val stringBuilder = StringBuilder()
            bufferedReader.useLines { lines ->
                lines.forEach {
                    stringBuilder.append(it)
                }
            }
        return stringBuilder.toString()
    /*
            val jsonString = stringBuilder.toString().drop(1).dropLast(1).split(",")
            val f = jsonString.first()
            val l = jsonString.last()
            var dict = mutableMapOf() arrayOf<itemOfToken2Index>()
            for(i in jsonString){
                val f =   i.split(":")
                val f1= f.first().replace("\"", "").trim();
                val f2 = f.last().replace("\"", "").trim();
                println(f1 + f2)
                val itemOfToken2Index = itemOfToken2Index(f1, f2.toInt())
                println(itemOfToken2Index)
                dict.add(itemOfToken2Index)
            }
            Log.e("myLOG", "dict: $dict")
            return dict.toList()
*/

    }
