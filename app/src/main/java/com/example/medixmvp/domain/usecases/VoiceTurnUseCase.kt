package com.example.medixmvp.domain.usecases

import com.example.medixmvp.core.util.AppResult
import com.example.medixmvp.data.remote.DialogResponseDto
import com.example.medixmvp.data.repository.MedixRepository
import java.io.File
import javax.inject.Inject

class VoiceTurnUseCase @Inject constructor(
    private val repository: MedixRepository
) {
    suspend operator fun invoke(sessionId: String, wavFile: File): AppResult<Pair<String, DialogResponseDto>> {
        val asr = repository.transcribe(wavFile)
        if (asr is AppResult.Error) return asr
        asr as AppResult.Success
        val dialog = repository.dialog(sessionId, asr.data.text)
        return when (dialog) {
            is AppResult.Success -> AppResult.Success(asr.data.text to dialog.data)
            is AppResult.Error -> dialog
        }
    }
}
