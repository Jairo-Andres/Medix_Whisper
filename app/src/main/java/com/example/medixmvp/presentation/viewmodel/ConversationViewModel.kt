package com.example.medixmvp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medixmvp.core.audio.WavAudioRecorder
import com.example.medixmvp.core.tts.TtsEvent
import com.example.medixmvp.core.tts.TtsManager
import com.example.medixmvp.core.util.AppLogger
import com.example.medixmvp.core.util.AppResult
import com.example.medixmvp.data.datastore.SettingsDataStore
import com.example.medixmvp.data.repository.MedixRepository
import com.example.medixmvp.domain.models.Appointment
import com.example.medixmvp.domain.models.ConversationStatus
import com.example.medixmvp.domain.usecases.VoiceTurnUseCase
import com.example.medixmvp.presentation.state.ConversationUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@HiltViewModel
class ConversationViewModel @Inject constructor(
    private val recorder: WavAudioRecorder,
    private val ttsManager: TtsManager,
    private val voiceTurnUseCase: VoiceTurnUseCase,
    private val repository: MedixRepository,
    private val settingsDataStore: SettingsDataStore,
    private val logger: AppLogger
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConversationUiState())
    val uiState: StateFlow<ConversationUiState> = _uiState.asStateFlow()

    private var activeWavPath: java.io.File? = null

    init {
        viewModelScope.launch {
            settingsDataStore.settingsFlow.collectLatest {
                _uiState.value = _uiState.value.copy(continuousMode = it.continuousMode)
            }
        }
    }

    fun startListening() {
        if (_uiState.value.status == ConversationStatus.Speaking) return
        activeWavPath = recorder.start()
        _uiState.value = _uiState.value.copy(status = ConversationStatus.Listening, error = null)
    }

    fun stopListeningAndProcess() {
        viewModelScope.launch {
            val wav = recorder.stop() ?: return@launch
            _uiState.value = _uiState.value.copy(status = ConversationStatus.Processing)
            val sessionId = settingsDataStore.ensureSessionId()
            when (val result = voiceTurnUseCase(sessionId, wav)) {
                is AppResult.Success -> {
                    val userText = result.data.first
                    val dialog = result.data.second
                    logger.i("latency", "asr/dialog latency ${dialog.latencyMs}ms")
                    val newCedula = dialog.state["cedula"] ?: extractCedula(userText)
                    val eps = dialog.state["eps"].orEmpty()
                    if (userText.contains("cambiar usuario", true)) {
                        settingsDataStore.resetSessionId()
                        _uiState.value = ConversationUiState(
                            assistantText = "Nuevo usuario. Continuemos.",
                            status = ConversationStatus.Idle,
                            continuousMode = _uiState.value.continuousMode
                        )
                        return@launch
                    }
                    _uiState.value = _uiState.value.copy(
                        userText = userText,
                        assistantText = dialog.assistantText,
                        status = ConversationStatus.Speaking,
                        cedula = newCedula,
                        eps = eps,
                        error = null
                    )
                    if (dialog.actions.any { it.type == "show_appointments" || it.type == "refresh_appointments" } && newCedula.isNotBlank()) {
                        refreshAppointments(newCedula)
                    }
                    ttsManager.speak(dialog.assistantText).collectLatest { event ->
                        when (event) {
                            TtsEvent.Done -> {
                                _uiState.value = _uiState.value.copy(
                                    status = if (_uiState.value.continuousMode) ConversationStatus.Listening else ConversationStatus.Idle
                                )
                                if (_uiState.value.continuousMode) startListening()
                            }
                            TtsEvent.Error -> _uiState.value = _uiState.value.copy(status = ConversationStatus.Error)
                            TtsEvent.Started -> Unit
                        }
                    }
                }

                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        status = if (_uiState.value.continuousMode) ConversationStatus.Listening else ConversationStatus.Idle,
                        assistantText = "No pude escuchar, ¿puedes repetir?",
                        error = result.message
                    )
                    if (_uiState.value.continuousMode) startListening()
                }
            }
        }
    }

    fun stopVoice() {
        ttsManager.stop()
        _uiState.value = _uiState.value.copy(status = ConversationStatus.Idle)
    }

    fun setContinuousMode(enabled: Boolean) {
        viewModelScope.launch { settingsDataStore.saveContinuousMode(enabled) }
    }

    fun saveServer(ip: String, port: String) {
        viewModelScope.launch { settingsDataStore.saveServer(ip, port) }
    }

    fun testConnection() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(healthMessage = "Probando...")
            _uiState.value = when (val health = repository.health()) {
                is AppResult.Success -> _uiState.value.copy(healthMessage = "OK: ${health.data.status}")
                is AppResult.Error -> _uiState.value.copy(healthMessage = "ERROR: ${health.message}")
            }
        }
    }

    fun refreshAppointments(cedula: String) {
        viewModelScope.launch {
            if (cedula.isBlank()) return@launch
            when (val result = repository.appointments(cedula)) {
                is AppResult.Success -> {
                    val items = result.data.appointments.map { Appointment(it.fecha, it.doctor, it.sede, it.estado) }
                    _uiState.value = _uiState.value.copy(appointments = items)
                }
                is AppResult.Error -> _uiState.value = _uiState.value.copy(error = result.message)
            }
        }
    }

    private fun extractCedula(text: String): String {
        return Regex("\\b\\d{6,12}\\b").find(text)?.value.orEmpty()
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
    }
}
