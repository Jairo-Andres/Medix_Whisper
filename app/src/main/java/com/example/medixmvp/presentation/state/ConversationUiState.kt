package com.example.medixmvp.presentation.state

import com.example.medixmvp.domain.models.Appointment
import com.example.medixmvp.domain.models.ConversationStatus

data class ConversationUiState(
    val status: ConversationStatus = ConversationStatus.Idle,
    val userText: String = "",
    val assistantText: String = "Hola, soy Medix. ¿En qué te ayudo hoy?",
    val cedula: String = "",
    val eps: String = "",
    val continuousMode: Boolean = true,
    val appointments: List<Appointment> = emptyList(),
    val error: String? = null,
    val healthMessage: String = ""
)
