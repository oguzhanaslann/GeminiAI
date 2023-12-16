package com.oguzhanaslann.geminiai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.geminiai.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.oguzhanaslann.geminiai.ui.BotChatMessageView
import com.oguzhanaslann.geminiai.ui.UserChatMessageView
import com.oguzhanaslann.geminiai.ui.theme.GeminiAITheme

class MainActivity : ComponentActivity() {
    private val generativeModel = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = BuildConfig.apiKey
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContent {
            GeminiAITheme {
                val viewModel = ChatViewModel(generativeModel)
                GeminiAIScreen(viewModel)
            }
        }
    }
}

@Composable
internal fun GeminiAIScreen(
    chatViewModel: ChatViewModel = viewModel()
) {

    val chatState by chatViewModel.uiState.collectAsState()

    GeminiAIScreenView(
        chatState,
        onPromptChange = chatViewModel::onPromptChange,
        onSendMessageClicked = { inputText -> chatViewModel.generateContent() })
}

@Composable
fun GeminiAIScreenView(
    uiState: ChatState = ChatState.initial(),
    onPromptChange: (String) -> Unit,
    onSendMessageClicked: (String) -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(bottom = 16.dp)
        ) {

            LazyColumn(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(bottom = 56.dp),
            ) {
                items(uiState.chatHistory) {
                    when (it) {
                        is DataState.Error -> Text(
                            text = it.exception.localizedMessage.orEmpty(),
                            color = Color.Red,
                            modifier = Modifier.padding(all = 8.dp)
                        )
                        DataState.Initial -> Unit
                        DataState.Loading -> CircularProgressIndicator()
                        is DataState.Success -> {
                            when (it.data.sender) {
                                Sender.Bot -> BotChatMessageView(
                                    modifier = Modifier
                                        .fillParentMaxWidth()
                                        .heightIn(min = 48.dp),
                                    text = it.data.content
                                )

                                Sender.User -> UserChatMessageView(
                                    modifier = Modifier
                                        .fillParentMaxWidth()
                                        .heightIn(min = 48.dp),
                                    text = it.data.content
                                )
                            }
                        }
                    }
                }
            }


            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 48.dp)
                        .heightIn(min = 56.dp)
                        .align(Alignment.CenterStart),
                    value = uiState.prompt,
                    onValueChange = onPromptChange,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF8c8375),
                        unfocusedBorderColor = Color(0xFF343642),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color(0xFF737576)
                    ),
                )
                IconButton(
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.CenterEnd),
                    onClick = { onSendMessageClicked(uiState.prompt) }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White
                    )
                }
            }
        }
    }
}


@Composable
@Preview(showBackground = true)
fun SummarizeScreenPreview() {
    GeminiAITheme {
        val (value, onChange) = remember { mutableStateOf("Gemini-GPT") }
        GeminiAIScreenView(
            uiState = ChatState(value, listOf(
                DataState.Success(Message("","Hello", Sender.User)),
                DataState.Success(Message("","Hello to you", Sender.Bot)),
                DataState.Error(Throwable("asdsad")),
                DataState.Loading
            )),
            onPromptChange = onChange
        )
    }
}