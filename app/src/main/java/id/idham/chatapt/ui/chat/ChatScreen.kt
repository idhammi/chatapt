package id.idham.chatapt.ui.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.idham.chatapt.database.ChatMessage
import id.idham.chatapt.ui.theme.ChatAPTTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun ChatScreen(viewModel: ChatViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ChatContent(
        uiState = uiState,
        onTemplateClicked = viewModel::sendTemplateMessage,
        onTextChanged = viewModel::updateInputText,
        onSend = viewModel::sendMessage
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ChatContent(
    uiState: ChatUiState,
    onTemplateClicked: (String) -> Unit,
    onTextChanged: (String) -> Unit,
    onSend: () -> Unit,
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier.shadow(4.dp),
                title = { Text(text = "ChatAPT") }
            )
        },
        bottomBar = {
            ChatInputBar(
                uiState = uiState,
                onTemplateClicked = { onTemplateClicked(it) },
                onSend = {
                    onSend()
                    coroutineScope.launch {
                        delay(100)
                        if (uiState.messages.isNotEmpty()) {
                            listState.animateScrollToItem(uiState.messages.size - 1)
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
                items(uiState.messages) { message ->
                    ChatMessageItem(message = message)
                }
            }
        }
    }

    // Scroll to bottom if new message inserted
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.scrollToItem(uiState.messages.size - 1)
        }
    }
}

@Composable
fun ChatInputBar(
    uiState: ChatUiState,
    onTemplateClicked: (String) -> Unit,
    onSend: () -> Unit,
    onTextChanged: (String) -> Unit
) {
    var showTemplates by remember { mutableStateOf(true) }

    Column {
        HorizontalDivider()
        if (showTemplates) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 64.dp,
                        end = 8.dp,
                        top = if (uiState.questionTemplates.isNotEmpty()) 8.dp else 0.dp
                    ),
                horizontalAlignment = Alignment.End
            ) {
                uiState.questionTemplates.forEach { template ->
                    TemplateBubble(
                        template = template,
                        onClick = {
                            onTemplateClicked(template)
                            showTemplates = false
                        }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = uiState.inputText,
                onValueChange = { onTextChanged(it) },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ketik pesan...") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            FilledIconButton(
                modifier = Modifier.size(48.dp),
                onClick = {
                    onSend()
                    showTemplates = false
                },
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
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
}

@Composable
fun TemplateBubble(template: String, onClick: () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Text(
            text = template,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(8.dp),
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
    val paddingEnd = if (message.isUser) 0.dp else 56.dp
    val paddingStart = if (message.isUser) 56.dp else 0.dp

    Column(
        horizontalAlignment = alignment,
        modifier = Modifier
            .padding(start = paddingStart, end = paddingEnd)
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
            uiState = ChatUiState(
                questionTemplates = listOf("Halo", "Apa kabar?", "Ada yang bisa dibantu?")
            ),
            onTemplateClicked = {},
            onTextChanged = {},
            onSend = {}
        )
    }
}
