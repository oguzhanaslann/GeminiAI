package com.oguzhanaslann.geminiai

data class ChatState(
    val prompt: String,
    val chatHistory: List<DataState<Message>>,
) {
    companion object {
        fun initial() = ChatState(String.empty, emptyList())
    }
}