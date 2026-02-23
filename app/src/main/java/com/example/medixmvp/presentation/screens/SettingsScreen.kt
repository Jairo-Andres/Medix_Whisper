package com.example.medixmvp.presentation.screens

import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.medixmvp.presentation.viewmodel.ConversationViewModel

@Composable
fun SettingsScreen(viewModel: ConversationViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    var ip by remember { mutableStateOf("192.168.1.50") }
    var port by remember { mutableStateOf("8000") }
    var showHelp by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(value = ip, onValueChange = { ip = it }, label = { Text("IP del servidor") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = port, onValueChange = { port = it }, label = { Text("Puerto") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = { viewModel.saveServer(ip, port) }, modifier = Modifier.fillMaxWidth()) { Text("Guardar") }
        Button(onClick = viewModel::testConnection, modifier = Modifier.fillMaxWidth()) { Text("Probar conexión") }
        Text(state.healthMessage)
        Text("PC y celular deben estar en la misma WiFi")
        Button(onClick = { showHelp = true }, modifier = Modifier.fillMaxWidth()) { Text("¿Cómo saber la IP del PC?") }
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Volver") }
    }

    if (showHelp) {
        AlertDialog(
            onDismissRequest = { showHelp = false },
            title = { Text("IP del PC") },
            text = { Text("En Windows: abre CMD y ejecuta ipconfig. Busca la dirección IPv4.") },
            confirmButton = { Button(onClick = { showHelp = false }) { Text("Entendido") } }
        )
    }
}
