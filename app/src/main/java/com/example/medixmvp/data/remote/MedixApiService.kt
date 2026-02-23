package com.example.medixmvp.data.remote

import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface MedixApiService {
    @GET("/health")
    suspend fun health(): HealthResponseDto

    @Multipart
    @POST("/asr/transcribe")
    suspend fun transcribe(@Part file: MultipartBody.Part): AsrResponseDto

    @POST("/dialog/next")
    suspend fun dialogNext(@Body request: DialogRequestDto): DialogResponseDto

    @GET("/appointments")
    suspend fun appointments(@Query("cedula") cedula: String): AppointmentsResponseDto
}
