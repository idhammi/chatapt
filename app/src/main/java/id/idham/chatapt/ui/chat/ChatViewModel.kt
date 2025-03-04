package id.idham.chatapt.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.idham.chatapt.data.Repository
import id.idham.chatapt.model.Content
import id.idham.chatapt.model.ContentsItem
import id.idham.chatapt.model.GenerativeRequest
import id.idham.chatapt.model.PartsItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: Repository
) : ViewModel() {

    private val _uiState: MutableStateFlow<ChatUiState> = MutableStateFlow(ChatUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        val request = GenerativeRequest(
            contents = listOf(
                ContentsItem(
                    parts = listOf(
                        PartsItem(
                            text = "Hello"
                        )
                    )
                )
            )
        )
        viewModelScope.launch {
            repository.generateContent(request)
            flow {
                emit(ChatUiState.Loading)
                val result = repository.generateContent(request)

                result.onSuccess {
                    emit(ChatUiState.Success(it.candidates.first().content))
                }.onFailure {
                    emit(ChatUiState.Error(it.message.toString()))
                }
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}

sealed interface ChatUiState {
    data object Loading : ChatUiState
    data class Success(val data: Content) : ChatUiState
    data class Error(val message: String) : ChatUiState
}
