package com.oguzhanaslann.geminiai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.geminiai.R
import com.oguzhanaslann.geminiai.ui.BotChatMessageView
import com.oguzhanaslann.geminiai.ui.UserChatMessageView
import com.oguzhanaslann.geminiai.ui.theme.GeminiAITheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeminiAITheme {
                val viewModel = ChatViewModel()
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
    val model by chatViewModel.activeModel.collectAsState()
    GeminiAIScreenView(
        chatState,
        selectedModel = model,
        onPromptChange = chatViewModel::onPromptChange,
        onSendMessageClicked = { inputText -> chatViewModel.generateContent() },
        onNewChatClicked = chatViewModel::onNewChat,
        onModelSelected = chatViewModel::onModelSelected
    )
}

@Composable
fun GeminiAIScreenView(
    uiState: ChatState = ChatState.initial(),
    selectedModel: Model,
    onPromptChange: (String) -> Unit,
    onSendMessageClicked: (String) -> Unit = {},
    onNewChatClicked: () -> Unit = {},
    onModelSelected: (Model) -> Unit = {}
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerShape = RectangleShape,
                drawerContainerColor = Color(0xFF141414)
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .padding(start = 16.dp)
                        .padding(end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        modifier = Modifier
                            .size(24.dp),
                        painter = painterResource(id = R.drawable.ic_geminiai),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(id = R.string.app_name),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.weight(1f, true))
                    IconButton(onClick = {
                        onNewChatClicked()
                        scope.launch { drawerState.close() }
                    }) {
                        Icon(
                            modifier = Modifier.size(18.dp),
                            painter = painterResource(id = R.drawable.ic_new_chat),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }


                Row(Modifier.padding(16.dp)) {
                    Text(
                        text = "API key",
                        color = Color.White
                    )
                }
            }
        },
    ) {
        Scaffold(
            topBar = {
                GeminiAITopBar(
                    selectedModel = selectedModel,
                    onNavigationClicked = {
                        scope.launch { drawerState.toggle() }
                    },
                    onNewChatClicked = onNewChatClicked,
                    onModelSelected = onModelSelected
                )
            },
            content = {
                ChatScreenView(
                    modifier = Modifier.padding(it),
                    uiState = uiState,
                    onPromptChange = onPromptChange,
                    onSendMessageClicked = onSendMessageClicked
                )
            }
        )
    }
}

suspend fun DrawerState.toggle() = if (isClosed) open() else close()

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun GeminiAITopBar(
    selectedModel: Model,
    onNavigationClicked: () -> Unit = {},
    onNewChatClicked: () -> Unit = {},
    onModelSelected: (Model) -> Unit = {}
) {
    CenterAlignedTopAppBar(
        navigationIcon = {
            IconButton(onClick = onNavigationClicked) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        },
        title = {
            var dropDownState by remember { mutableStateOf(false) }
            Column {
                DropdownMenu(
                    modifier = Modifier.background(Color(0xFF141414)),
                    expanded = dropDownState,
                    onDismissRequest = { dropDownState = false }
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TextButton(onClick = {
                            onModelSelected(Model.Pro)
                            dropDownState = false
                        }) {
                            Text(
                                text = Model.Pro.name(),
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        TextButton(onClick = {
                            onModelSelected(Model.Vision)
                            dropDownState = false
                        }) {
                            Text(
                                text = Model.Vision.name(),
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .clickable { dropDownState = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedModel.name(),
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleMedium
                    )

                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        tint = MaterialTheme.colorScheme.onBackground,
                        contentDescription = null
                    )
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        actions = {
            IconButton(onClick = onNewChatClicked) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_new_chat),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    )
}

@Composable
private fun ChatScreenView(
    modifier: Modifier = Modifier,
    uiState: ChatState,
    onPromptChange: (String) -> Unit,
    onSendMessageClicked: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .then(modifier),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        ) {

            LazyColumn(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(bottom = 56.dp),
            ) {
                items(uiState.chatHistory) {
                    it.onError {
                        Text(
                            text = it.localizedMessage.orEmpty(),
                            color = Color.Red,
                            modifier = Modifier.padding(all = 8.dp)
                        )
                    }.onLoading {
                        CircularProgressIndicator()
                    }.onSuccess {
                        when (it.sender) {
                            Sender.Bot -> BotChatMessageView(
                                modifier = Modifier
                                    .fillParentMaxWidth()
                                    .heightIn(min = 48.dp),
                                text = it.content
                            )

                            Sender.User -> UserChatMessageView(
                                modifier = Modifier
                                    .fillParentMaxWidth()
                                    .heightIn(min = 48.dp),
                                text = it.content
                            )
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
            uiState = ChatState(
                value, listOf(
                    DataState.Success(Message("", "Hello", Sender.User)),
                    DataState.Success(Message("", "Hello to you", Sender.Bot)),
                    DataState.Error(Throwable("asdsad")),
                    DataState.Loading
                )
            ),
            onPromptChange = onChange,
            selectedModel = Model.Pro
        )
    }
}