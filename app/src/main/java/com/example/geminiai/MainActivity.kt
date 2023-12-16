package com.example.geminiai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.ai.client.generativeai.GenerativeModel
import com.example.geminiai.ui.theme.GeminiAITheme

class MainActivity : ComponentActivity() {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = BuildConfig.apiKey
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeminiAITheme {
                val viewModel = SummarizeViewModel(generativeModel)
                GeminiAIScreen(viewModel)
            }
        }
    }
}

@Composable
internal fun GeminiAIScreen(
    summarizeViewModel: SummarizeViewModel = viewModel()
) {

    val summarizeUiState by summarizeViewModel.uiState.collectAsState()
    GeminiAIScreenView(
        summarizeUiState,
        onSummarizeClicked = { inputText ->
            summarizeViewModel.summarize(inputText)
        })
}

@Composable
fun GeminiAIScreenView(
    uiState: SummarizeUiState = SummarizeUiState.Initial,
    onSummarizeClicked: (String) -> Unit = {}
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
                .verticalScroll(rememberScrollState())
        ) {

            Column(
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                when (uiState) {
                    SummarizeUiState.Initial -> {
                        // Nothing is shown
                        Column {
                            Image(
                                painter = painterResource(id = R.drawable.ic_geminiai),
                                contentDescription = null
                            )
                            Text(
                                text = stringResource(id = R.string.app_name),
                                color = Color.White
                            )
                        }
                    }

                    SummarizeUiState.Loading -> {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .padding(all = 8.dp)
                                .align(Alignment.CenterHorizontally)
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    is SummarizeUiState.Success -> {
                        Row(modifier = Modifier.padding(all = 8.dp)) {
                            Icon(
                                Icons.Outlined.Person,
                                contentDescription = "Person Icon"
                            )
                            Text(
                                text = uiState.outputText,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }

                    is SummarizeUiState.Error -> {
                        Text(
                            text = uiState.errorMessage,
                            color = Color.Red,
                            modifier = Modifier.padding(all = 8.dp)
                        )
                    }
                }
            }


            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                val (text, onValueChanged) = remember { mutableStateOf("tate") }
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 48.dp)
                        .heightIn(min = 56.dp)
                        .align(Alignment.CenterStart),
                    value = text,
                    onValueChange = onValueChanged,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF8c8375),
                        unfocusedBorderColor = Color(0xFF343642),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color(0xFF737576)
                    )
                )
                IconButton(
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.CenterEnd),
                    onClick = { onSummarizeClicked(text) }
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
        GeminiAIScreenView()
    }
}