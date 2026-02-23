package com.example.medixmvp.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.medixmvp.presentation.viewmodel.ConversationViewModel

@Composable
fun AppointmentsScreen(viewModel: ConversationViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    var cedula by remember(state.cedula) { mutableStateOf(state.cedula) }

    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = cedula,
            onValueChange = { cedula = it },
            label = { Text("Cédula") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = { viewModel.refreshAppointments(cedula) }, modifier = Modifier.fillMaxWidth()) { Text("Refrescar") }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.appointments) { ap ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Fecha: ${ap.fecha}")
                        Text("Doctor: ${ap.doctor}")
                        Text("Sede: ${ap.sede}")
                        Text("Estado: ${ap.estado}")
                    }
                }
            }
        }
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Volver") }
    }
}
