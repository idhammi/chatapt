package id.idham.chatapt.ui.chat

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import id.idham.chatapt.data.Repository
import id.idham.chatapt.database.ChatMessage
import id.idham.chatapt.model.ContentsItem
import id.idham.chatapt.model.GenerationConfig
import id.idham.chatapt.model.GenerativeRequest
import id.idham.chatapt.model.PartsItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatViewModel(
    private val repository: Repository
) : ViewModel() {

    private val _uiState: MutableStateFlow<ChatUiState> = MutableStateFlow(ChatUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    val inputText = mutableStateOf("")

    init {
        viewModelScope.launch {
            repository.getAllMessages().collectLatest { chatMessages ->
                withContext(Dispatchers.Main) {
                    _messages.value = chatMessages
                }
            }
        }
    }

    fun sendMessage() {
        val newMessage = inputText.value
        if (newMessage.isNotBlank()) {
            val userMessage = ChatMessage(message = newMessage, isUser = true)
            viewModelScope.launch(Dispatchers.IO) {
                repository.insertMessage(userMessage)
                withContext(Dispatchers.Main) {
                    inputText.value = ""
                }
            }
            getAiResponse(newMessage)
        }
    }

    private fun getAiResponse(userMessage: String) {
        val request = GenerativeRequest(
            contents = listOf(
                ContentsItem(
                    parts = listOf(PartsItem(text = userMessage))
                )
            ),
            generationConfig = GenerationConfig(maxOutputTokens = 50)
        )
        viewModelScope.launch {
            flow {
                emit(ChatUiState.Loading)
                val result = repository.generateContent(request)

                result.onSuccess {
                    val response = it.candidates.first().content.parts.first().text
                    val aiMessage = ChatMessage(message = response, isUser = false)

                    viewModelScope.launch(Dispatchers.IO) {
                        repository.insertMessage(aiMessage)
                    }
                    emit(ChatUiState.Idle)
                }.onFailure {
                    emit(ChatUiState.Idle)
                }
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}

sealed interface ChatUiState {
    data object Loading : ChatUiState
    data object Idle : ChatUiState
}
