package com.oguzhanaslann.geminiai

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.geminiai.R
import com.oguzhanaslann.geminiai.ui.theme.GeminiAITheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
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
    val context = LocalContext.current

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 16)
    ) { uri ->
        val bitmaps = uri.map {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, it))
        }
        chatViewModel.onVisionImagesSelected(bitmaps)
    }

    GeminiAIScreenView(
        chatState,
        selectedModel = model,
        onPromptChange = chatViewModel::onPromptChange,
        onSendMessageClicked = chatViewModel::generateContent,
        onNewChatClicked = chatViewModel::onNewChat,
        onModelSelected = chatViewModel::onModelSelected,
        onImageAddClicked = {
            photoPicker.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        },
        onImageDeleteClicked = chatViewModel::deleteImage,
        onChangeAPIKey = chatViewModel::onChangeAPIKey
    )
}

@Composable
fun GeminiAIScreenView(
    uiState: ChatState = ChatState.initial(),
    selectedModel: Model,
    onPromptChange: (String) -> Unit,
    onSendMessageClicked: () -> Unit = {},
    onNewChatClicked: () -> Unit = {},
    onModelSelected: (Model) -> Unit = {},
    onImageAddClicked: () -> Unit = {},
    onImageDeleteClicked: (Bitmap) -> Unit = {},
    onChangeAPIKey: (String) -> Unit = {},
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = drawerState.isOpen) {
        scope.launch { drawerState.close() }
    }

    StandardDialog(
        show = showDialog,
        onDismissRequest = { showDialog = false },
        content = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(180.dp),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Set Api key",
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    val (key, onValueChange) = remember { mutableStateOf("") }
                    OutlinedTextField(value = key, onValueChange = onValueChange)
                    Row {
                        TextButton(onClick = {
                            onChangeAPIKey(key)
                            showDialog = false
                        }) {
                            Text(
                                text = "Apply",
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        TextButton(onClick = { showDialog = false }) {
                            Text(
                                text = "Cancel",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            GeminiAIDrawerView(
                scope = scope,
                drawerState = drawerState,
                onNewChatClicked = onNewChatClicked,
                onChangeAPIKey = {
                    scope.launch {
                        drawerState.close()
                        showDialog = true
                    }
                }
            )
        },
    ) {
        Scaffold(
            topBar = {
                GeminiAITopBar(
                    selectedModel = selectedModel,
                    onNavigationClicked = { scope.launch { drawerState.toggle() } },
                    onNewChatClicked = onNewChatClicked,
                    onModelSelected = onModelSelected
                )
            },
            content = {
                ChatScreenView(
                    modifier = Modifier.padding(it),
                    uiState = uiState,
                    selectedModel = selectedModel,
                    onPromptChange = onPromptChange,
                    onSendMessageClicked = onSendMessageClicked,
                    onImageAddClicked = onImageAddClicked,
                    onImageDeleteClicked = onImageDeleteClicked
                )
            }
        )
    }
}

@Composable
private fun GeminiAIDrawerView(
    scope: CoroutineScope,
    drawerState: DrawerState,
    onNewChatClicked: () -> Unit,
    onChangeAPIKey: () -> Unit
) {
    ModalDrawerSheet(
        drawerShape = RectangleShape,
        drawerContainerColor = MaterialTheme.colorScheme.surface
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
            TextButton(onClick = onChangeAPIKey) {
                Text(
                    text = "Change API Key",
                    color = Color.White
                )
            }
        }
    }
}


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
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface),
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
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        TextButton(onClick = {
                            onModelSelected(Model.Vision)
                            dropDownState = false
                        }) {
                            Text(
                                text = Model.Vision.name(),
                                color = MaterialTheme.colorScheme.onSurface,
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
    selectedModel: Model,
    onPromptChange: (String) -> Unit,
    onSendMessageClicked: () -> Unit,
    onImageAddClicked: () -> Unit,
    onImageDeleteClicked: (Bitmap) -> Unit = {}
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
            ChatList(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(
                        bottom = when {
                            uiState.prompt.images.isNullOrEmpty() -> 56.dp
                            else -> 72.dp
                        }
                    ),
                uiState
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
            ) {
                uiState.prompt.images?.let {
                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState()),
                    ) {
                        it.forEach { bitmap ->
                            SelectedImageView(
                                modifier = Modifier
                                    .padding(end = 24.dp)
                                    .size(48.dp),
                                bitmap = bitmap.asImageBitmap(),
                                onClearClicked = { onImageDeleteClicked(bitmap) }
                            )
                        }
                    }
                }


                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    OutlinedTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 48.dp)
                            .heightIn(min = 56.dp)
                            .align(Alignment.CenterStart),
                        value = uiState.prompt.text,
                        onValueChange = onPromptChange,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF8c8375),
                            unfocusedBorderColor = Color(0xFF343642),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color(0xFF737576)
                        ),
                        trailingIcon = visionTrailingAction(selectedModel, onImageAddClicked)
                    )
                    IconButton(
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.CenterEnd),
                        onClick = { onSendMessageClicked() }
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
}


@Composable
fun visionTrailingAction(
    selectedModel: Model,
    onImageAddClicked: () -> Unit = {}
): @Composable (() -> Unit)? {
    return when {
        selectedModel is Model.Vision -> composable {
            IconButton(
                modifier = Modifier.padding(end = 8.dp),
                onClick = onImageAddClicked
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_photo_camera_24),
                    tint = MaterialTheme.colorScheme.onBackground,
                    contentDescription = null
                )
            }
        }

        else -> null
    }
}


@Composable
@Preview(showBackground = true)
fun SummarizeScreenPreview() {
    GeminiAITheme {
        val (value, onChange) = remember { mutableStateOf("Gemini-GPT") }
        GeminiAIScreenView(
            uiState = ChatState(
                Prompt(value),
                listOf(
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