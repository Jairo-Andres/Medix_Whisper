package com.example.medixmvp.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.medixmvp.domain.models.ConversationStatus
import com.example.medixmvp.presentation.components.MicButton
import com.example.medixmvp.presentation.viewmodel.ConversationViewModel

@Composable
fun ConversationScreen(
    viewModel: ConversationViewModel,
    onOpenSettings: () -> Unit,
    onOpenAppointments: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Estado: ${state.status}", style = MaterialTheme.typography.titleLarge)
        Text("Cédula: ${state.cedula.ifBlank { "Pendiente" }}")
        Text("EPS: ${state.eps.ifBlank { "Pendiente" }}")

        Text("Usted dijo:", style = MaterialTheme.typography.titleLarge)
        Text(state.userText.ifBlank { "..." }, style = MaterialTheme.typography.bodyLarge)

        Text("Asistente:", style = MaterialTheme.typography.titleLarge)
        Text(state.assistantText, style = MaterialTheme.typography.bodyLarge)

        MicButton(isListening = state.status == ConversationStatus.Listening) {
            if (state.status == ConversationStatus.Listening) {
                viewModel.stopListeningAndProcess()
            } else {
                viewModel.startListening()
            }
        }

        Button(onClick = viewModel::stopVoice, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
            Text("Cancelar voz")
        }

        Column {
            Text("Conversación continua (recomendado)")
            Switch(checked = state.continuousMode, onCheckedChange = viewModel::setContinuousMode)
        }

        Button(onClick = onOpenAppointments, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
            Text("Historial de citas")
        }

        Button(onClick = onOpenSettings, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
            Text("Configuración")
        }

        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}
