package com.example.medixmvp.data.repository

import com.example.medixmvp.core.util.AppLogger
import com.example.medixmvp.core.util.AppResult
import com.example.medixmvp.data.datastore.SettingsDataStore
import com.example.medixmvp.data.remote.DialogRequestDto
import com.example.medixmvp.data.remote.MedixApiService
import java.io.File
import java.net.SocketTimeoutException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory

@Singleton
class MedixRepository @Inject constructor(
    private val client: OkHttpClient,
    private val json: Json,
    private val settingsDataStore: SettingsDataStore,
    private val logger: AppLogger
) {
    suspend fun health() = wrap("health") { service().health() }

    suspend fun transcribe(file: File) = wrap("asr") {
        val body = file.asRequestBody("audio/wav".toMediaType())
        service().transcribe(MultipartBody.Part.createFormData("file", file.name, body))
    }

    suspend fun dialog(sessionId: String, text: String) = wrap("dialog") {
        service().dialogNext(DialogRequestDto(sessionId, text))
    }

    suspend fun appointments(cedula: String) = wrap("appointments") { service().appointments(cedula) }

    private suspend fun service(): MedixApiService {
        val settings = settingsDataStore.settingsFlow.first()
        return Retrofit.Builder()
            .baseUrl("http://${settings.serverIp}:${settings.serverPort}")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(MedixApiService::class.java)
    }

    private suspend fun <T> wrap(tag: String, block: suspend () -> T): AppResult<T> {
        return try {
            AppResult.Success(block())
        } catch (ex: SocketTimeoutException) {
            logger.e(tag, "timeout, retrying", ex)
            try {
                delay(300)
                AppResult.Success(block())
            } catch (e: Exception) {
                AppResult.Error("No hay conexión con el servidor. Revisa la IP en Configuración.", e)
            }
        } catch (e: Exception) {
            logger.e(tag, "error", e)
            AppResult.Error("No pude conectarme al servidor", e)
        }
    }
}
