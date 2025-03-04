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

    // store conversation history to be used for AI response
    // this will be cleared when user closes the app
    private val _conversationHistory = MutableStateFlow<MutableList<ContentsItem>>(mutableListOf())

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

            val userContent = ContentsItem(
                role = "user",
                parts = listOf(PartsItem(text = newMessage))
            )
            addMessageToHistory(userContent)

            getAiResponse()
        }
    }

    private fun getAiResponse() {
        val allContents = _conversationHistory.value.toList()

        val request = GenerativeRequest(
            contents = allContents,
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

                    val aiContent = ContentsItem(
                        role = "model",
                        parts = listOf(PartsItem(text = response))
                    )
                    addMessageToHistory(aiContent)

                    emit(ChatUiState.Idle)
                }.onFailure {
                    emit(ChatUiState.Idle)
                }
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    private fun addMessageToHistory(content: ContentsItem) {
        _conversationHistory.value.add(content)
        if (_conversationHistory.value.size > MAX_HISTORY_SIZE) {
            _conversationHistory.value.removeAt(0)
        }
    }
}

private const val MAX_HISTORY_SIZE = 10

sealed interface ChatUiState {
    data object Loading : ChatUiState
    data object Idle : ChatUiState
}
