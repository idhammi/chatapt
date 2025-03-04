package id.idham.chatapt.ui.chat

import id.idham.chatapt.database.ChatMessage

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val questionTemplates: List<String> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
