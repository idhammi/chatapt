package id.idham.chatapt.ui.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.idham.chatapt.database.ChatMessage
import id.idham.chatapt.ui.theme.ChatAPTTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun ChatScreen(viewModel: ChatViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val messages = viewModel.messages.collectAsStateWithLifecycle().value

    ChatContent(
        uiState = uiState,
        inputText = viewModel.inputText.value,
        messages = messages,
        onTextChanged = viewModel.inputText::value::set,
        onSend = viewModel::sendMessage
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ChatContent(
    uiState: ChatUiState,
    inputText: String,
    messages: List<ChatMessage>,
    onTextChanged: (String) -> Unit,
    onSend: () -> Unit,
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "ChatAPT") }
            )
        },
        bottomBar = {
            ChatInputBar(
                uiState = uiState,
                text = inputText,
                onSend = {
                    onSend()
                    coroutineScope.launch {
                        delay(100)
                        if (messages.isNotEmpty()) {
                            listState.animateScrollToItem(messages.size - 1)
                        }
                    }
                },
                onTextChanged = onTextChanged
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                state = listState,
            ) {
                items(messages) { message ->
                    ChatMessageItem(message = message)
                }
            }
        }
    }

    // Scroll to bottom if new message inserted
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.scrollToItem(messages.size - 1)
        }
    }
}

@Composable
fun ChatInputBar(
    uiState: ChatUiState,
    text: String,
    onSend: () -> Unit,
    onTextChanged: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .navigationBarsPadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = text,
            onValueChange = { onTextChanged(it) },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Ketik pesan...") }
        )
        Spacer(modifier = Modifier.width(8.dp))
        FilledIconButton(
            modifier = Modifier.size(48.dp),
            onClick = onSend,
            enabled = uiState !is ChatUiState.Loading
        ) {
            if (uiState is ChatUiState.Loading) {
                CircularProgressIndicator()
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.Send,
                    contentDescription = "Send"
                )
            }
        }
    }
}

@Composable
fun ChatMessageItem(message: ChatMessage) {
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val color = if (message.isUser) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }

    Column(
        horizontalAlignment = alignment,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = color,
        ) {
            Text(
                text = message.message,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatScreen_Preview() {
    ChatAPTTheme {
        ChatContent(
            uiState = ChatUiState.Idle,
            inputText = "",
            messages = emptyList(),
            onTextChanged = {},
            onSend = {}
        )
    }
}
