package com.oguzhanaslann.geminiai.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.geminiai.R
import com.oguzhanaslann.geminiai.ui.theme.GeminiAITheme

@Composable
fun UserChatMessageView(
    modifier : Modifier = Modifier,
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
    modifier : Modifier = Modifier,
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
            UserChatMessageView(text ="Hello")
            BotChatMessageView(text = "Hello")
        }
    }
}