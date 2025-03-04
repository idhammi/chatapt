package id.idham.chatapt.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import id.idham.chatapt.data.Repository
import id.idham.chatapt.database.ChatMessage
import id.idham.chatapt.model.ContentsItem
import id.idham.chatapt.model.GenerationConfig
import id.idham.chatapt.model.GenerativeRequest
import id.idham.chatapt.model.PartsItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatViewModel(
    private val repository: Repository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState = _uiState.asStateFlow()

    // store conversation history to be used for AI response
    // this will be cleared when user closes the app
    private val conversationHistory = MutableStateFlow<MutableList<ContentsItem>>(mutableListOf())

    init {
        val remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 60
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(mapOf("show_question_templates" to true))

        viewModelScope.launch {
            remoteConfig.fetchAndActivate()
                .addOnCompleteListener { task ->
                    viewModelScope.launch(Dispatchers.IO) { // Keep network operations off the Main thread
                        val showTemplates = if (task.isSuccessful) {
                            remoteConfig.getBoolean("show_question_templates")
                        } else true

                        // Safely update UI state on the Main thread
                        withContext(Dispatchers.Main) {
                            loadInitialData(showTemplates)
                        }
                    }
                }
        }
    }

    private fun loadInitialData(showTemplates: Boolean) {
        // Load messages from database
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAllMessages().collectLatest { chatMessages ->
                withContext(Dispatchers.Main) {
                    _uiState.update { it.copy(messages = chatMessages) }
                }
            }
        }

        // Load question templates (conditionally)
        viewModelScope.launch(Dispatchers.IO) {
            val templates = if (showTemplates) {
                listOf(
                    "Apa itu Android Compose?",
                    "Jelaskan tentang Gemini API.",
                    "Bagaimana cara menggunakan Room Database?"
                )
            } else {
                emptyList()
            }

            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(questionTemplates = templates) }
            }
        }
    }

    fun updateInputText(newInputText: String) {
        _uiState.update { it.copy(inputText = newInputText) }
    }

    fun sendMessage() {
        val newMessage = uiState.value.inputText
        if (newMessage.isNotBlank()) {
            val userMessage = ChatMessage(message = newMessage, isUser = true)
            viewModelScope.launch(Dispatchers.IO) {
                repository.insertMessage(userMessage)
            }
            _uiState.update { it.copy(inputText = "") }

            val userContent = ContentsItem(
                role = "user",
                parts = listOf(PartsItem(text = newMessage))
            )
            addMessageToHistory(userContent)

            getAiResponse()
        }
    }

    fun sendTemplateMessage(template: String) {
        _uiState.update { it.copy(inputText = template) }
        sendMessage()
    }

    private fun getAiResponse() {
        val allContents = conversationHistory.value.toList()

        val request = GenerativeRequest(
            contents = allContents,
            generationConfig = GenerationConfig(maxOutputTokens = 50)
        )
        viewModelScope.launch {
            flow {
                emit(true)
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

                    emit(false)
                }.onFailure {
                    emit(false)
                }
            }.collect { state ->
                _uiState.update { it.copy(isLoading = state) }
            }
        }
    }

    private fun addMessageToHistory(content: ContentsItem) {
        conversationHistory.value.add(content)
        if (conversationHistory.value.size > MAX_HISTORY_SIZE) {
            conversationHistory.value.removeAt(0)
        }
    }
}

private const val MAX_HISTORY_SIZE = 10
