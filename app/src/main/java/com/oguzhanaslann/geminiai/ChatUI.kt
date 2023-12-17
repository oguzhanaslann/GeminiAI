package com.oguzhanaslann.geminiai

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.example.geminiai.R
import com.oguzhanaslann.geminiai.ui.theme.GeminiAITheme

@Composable
fun UserChatMessageView(
    modifier: Modifier = Modifier,
    text: String
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            Icons.Outlined.Person,
            contentDescription = "Person Icon"
        )
        Spacer(Modifier.width(4.dp))

        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

@Composable
fun BotChatMessageView(
    modifier: Modifier = Modifier,
    text: String
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            modifier = Modifier
                .padding(start = 2.dp)
                .size(18.dp),
            painter = painterResource(id = R.drawable.ic_robot),
            contentDescription = "Person Icon"
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

@Preview
@Composable
private fun PreviewChatMessageView() {
    GeminiAITheme {
        Column {
            UserChatMessageView(text = "Hello")
            BotChatMessageView(text = "Hello")
        }
    }
}

@Composable
fun ChatList(
    modifier: Modifier,
    uiState: ChatState
) {
    LazyColumn(
        modifier = modifier
    ) {
        items(uiState.chatHistory) {
            it.onError {
                Text(
                    text = it.localizedMessage.orEmpty(),
                    color = Color.Red,
                    modifier = Modifier.padding(all = 8.dp)
                )
            }.onLoading {
                CircularProgressIndicator(color =MaterialTheme.colorScheme.onBackground)
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
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectedImageView(
    modifier: Modifier,
    bitmap: ImageBitmap,
    onClearClicked: (ImageBitmap) -> Unit = {}
) {
    BadgedBox(
        modifier = modifier,
        badge = {
            IconButton(
                modifier = Modifier.size(24.dp),
                onClick = { onClearClicked(bitmap) },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                )
            ) {
                Icon(
                    modifier = Modifier.size(12.dp),
                    imageVector = Icons.Filled.Close,
                    contentDescription = null
                )
            }
        },
        content = {
            ShapeableImage(
                shape = RoundedCornerShape(8.dp),
                bitmap = bitmap
            )
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewSelectedImageView() {
    val context = LocalContext.current
    GeminiAITheme {
        Column(
            modifier = Modifier
        ) {
            SelectedImageView(
                modifier = Modifier.size(48.dp),
                bitmap = context.resources.getDrawable(R.drawable.baseline_photo_camera_24)
                    .toBitmap().asImageBitmap()
            )
        }
    }

}