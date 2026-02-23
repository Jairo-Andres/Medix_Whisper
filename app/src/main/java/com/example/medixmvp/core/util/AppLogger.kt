package com.example.medixmvp.core.util

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLogger @Inject constructor(@ApplicationContext context: Context) {
    private val file = File(context.filesDir, "medix_logs.txt")
    private val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun i(tag: String, msg: String) = write("INFO", tag, msg)
    fun e(tag: String, msg: String, tr: Throwable? = null) = write("ERROR", tag, "$msg ${tr?.message.orEmpty()}")

    private fun write(level: String, tag: String, msg: String) {
        val line = "${formatter.format(Date())} [$level] $tag: $msg\n"
        Log.d(tag, msg)
        file.appendText(line)
    }
}
