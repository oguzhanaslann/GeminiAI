package com.oguzhanaslann.geminiai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(
    private val generativeModel: GenerativeModel
) : ViewModel() {

    private val _uiState: MutableStateFlow<ChatState> =
        MutableStateFlow(ChatState.initial())
    val uiState: StateFlow<ChatState> =
        _uiState.asStateFlow()


    fun generateContent() {
        val prompt = _uiState.value.prompt
        _uiState.update {
            it.copy(
                chatHistory = it.chatHistory
                    .plus(DataState.Success(Message(it.prompt,it.prompt, Sender.User)))
                    .plus(DataState.Loading),
                prompt = String.empty
            )
        }

        viewModelScope.launch {
            try {
                val response = generativeModel.generateContent(prompt)
                response.text?.let { outputContent ->
                    _uiState.update {
                        it.copy(
                            chatHistory = it.chatHistory
                                .dropLast(1)
                                .plus(DataState.Success(Message(outputContent,outputContent, Sender.Bot))),
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        chatHistory = it.chatHistory
                            .dropLast(1)
                            .plus(DataState.Error(e))
                    )
                }
            }
        }
    }

    fun onPromptChange(prompt: String) {
        _uiState.update { it.copy(prompt = prompt) }
    }
}