package com.example.medixmvp.core.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@Singleton
class TtsManager @Inject constructor(@ApplicationContext context: Context) : TextToSpeech.OnInitListener {

    private val tts = TextToSpeech(context, this)

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale("es", "ES")
        }
    }

    fun speak(text: String): Flow<TtsEvent> = callbackFlow {
        val utteranceId = "utt_${System.currentTimeMillis()}"
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) { trySend(TtsEvent.Started) }
            override fun onDone(utteranceId: String?) { trySend(TtsEvent.Done); close() }
            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) { trySend(TtsEvent.Error); close() }
        })
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        awaitClose {}
    }

    fun stop() = tts.stop()

    fun shutdown() = tts.shutdown()
}

sealed interface TtsEvent {
    data object Started : TtsEvent
    data object Done : TtsEvent
    data object Error : TtsEvent
}
