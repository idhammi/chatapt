package id.idham.chatapt.ui.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.idham.chatapt.ui.theme.ChatAPTTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        when (state) {
            is ChatUiState.Loading -> {
                Text(
                    text = "Loading..",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            }

            is ChatUiState.Success -> {
                Text(
                    text = (state as ChatUiState.Success).data.parts.first().text,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            }

            is ChatUiState.Error -> {
                Text(
                    text = "Error..",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatScreen_Preview() {
    ChatAPTTheme {
        ChatScreen()
    }
}
