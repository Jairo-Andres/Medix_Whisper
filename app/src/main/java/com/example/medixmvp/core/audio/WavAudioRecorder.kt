package com.example.medixmvp.core.audio

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class WavAudioRecorder @Inject constructor(@ApplicationContext private val context: Context) {

    private var recorder: AudioRecord? = null
    private var recordingThread: Thread? = null
    private var isRecording: Boolean = false
    private var outputFile: File? = null

    private val sampleRate = 16_000

    fun start(): File {
        val minBuffer = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        outputFile = File(context.cacheDir, "medix_${System.currentTimeMillis()}.wav")
        val file = outputFile ?: error("No se pudo crear archivo")
        writeWavHeader(file)

        recorder = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            minBuffer
        )

        isRecording = true
        recorder?.startRecording()

        recordingThread = Thread {
            FileOutputStream(file, true).use { stream ->
                val buffer = ByteArray(minBuffer)
                while (isRecording) {
                    val read = recorder?.read(buffer, 0, buffer.size) ?: 0
                    if (read > 0) stream.write(buffer, 0, read)
                }
            }
        }.also { it.start() }
        return file
    }

    suspend fun stop(): File? = withContext(Dispatchers.IO) {
        isRecording = false
        recordingThread?.join()
        recorder?.stop()
        recorder?.release()
        recorder = null
        outputFile?.also { updateWavHeader(it) }
    }

    private fun writeWavHeader(file: File) {
        FileOutputStream(file).use { stream ->
            val header = ByteArray(44)
            stream.write(header)
        }
    }

    private fun updateWavHeader(file: File) {
        val totalAudioLen = file.length() - 44
        val byteRate = sampleRate * 2
        RandomAccessFile(file, "rw").use { raf ->
            raf.seek(0)
            raf.writeBytes("RIFF")
            raf.writeIntLE((totalAudioLen + 36).toInt())
            raf.writeBytes("WAVE")
            raf.writeBytes("fmt ")
            raf.writeIntLE(16)
            raf.writeShortLE(1)
            raf.writeShortLE(1)
            raf.writeIntLE(sampleRate)
            raf.writeIntLE(byteRate)
            raf.writeShortLE(2)
            raf.writeShortLE(16)
            raf.writeBytes("data")
            raf.writeIntLE(totalAudioLen.toInt())
        }
    }
}

private fun RandomAccessFile.writeIntLE(value: Int) {
    write(byteArrayOf(
        (value and 0xFF).toByte(),
        ((value shr 8) and 0xFF).toByte(),
        ((value shr 16) and 0xFF).toByte(),
        ((value shr 24) and 0xFF).toByte()
    ))
}

private fun RandomAccessFile.writeShortLE(value: Int) {
    write(byteArrayOf(
        (value and 0xFF).toByte(),
        ((value shr 8) and 0xFF).toByte()
    ))
}
