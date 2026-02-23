package com.example.medixmvp.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HealthResponseDto(val status: String)

@Serializable
data class AsrResponseDto(
    val text: String,
    val meta: Map<String, String> = emptyMap(),
    @SerialName("latency_ms") val latencyMs: Long = 0L
)

@Serializable
data class DialogRequestDto(
    @SerialName("session_id") val sessionId: String,
    val text: String
)

@Serializable
data class ActionDto(
    val type: String,
    val payload: Map<String, String> = emptyMap()
)

@Serializable
data class DialogResponseDto(
    @SerialName("assistant_text") val assistantText: String,
    val actions: List<ActionDto> = emptyList(),
    val state: Map<String, String> = emptyMap(),
    @SerialName("latency_ms") val latencyMs: Long = 0L
)

@Serializable
data class AppointmentDto(
    val fecha: String,
    val doctor: String,
    val sede: String,
    val estado: String
)

@Serializable
data class AppointmentsResponseDto(
    val appointments: List<AppointmentDto> = emptyList()
)
