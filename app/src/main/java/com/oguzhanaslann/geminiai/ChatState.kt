package com.oguzhanaslann.geminiai

data class ChatState(
    val prompt: Prompt,
    val chatHistory: List<DataState<Message>>,
) {
    companion object {
        fun initial() = ChatState(Prompt.empty(), emptyList())
    }
}