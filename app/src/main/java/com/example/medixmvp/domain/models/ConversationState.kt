package com.example.medixmvp.domain.models

enum class ConversationStatus { Idle, Listening, Processing, Speaking, Error }

data class Appointment(
    val fecha: String,
    val doctor: String,
    val sede: String,
    val estado: String
)
