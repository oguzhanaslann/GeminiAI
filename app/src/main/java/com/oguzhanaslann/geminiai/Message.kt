package com.oguzhanaslann.geminiai

data class Message(
    val id: String,
    val content: String,
    val sender: Sender,
)