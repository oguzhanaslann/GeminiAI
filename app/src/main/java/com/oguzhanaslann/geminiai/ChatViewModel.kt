package com.oguzhanaslann.geminiai

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    val _activeModel = MutableStateFlow<Model>(Model.Pro)
    val activeModel = _activeModel.asStateFlow()

    private val generativeModel: GenerativeModel get() = _activeModel.value.get()

    private val _uiState: MutableStateFlow<ChatState> =
        MutableStateFlow(ChatState.initial())
    val uiState: StateFlow<ChatState> =
        _uiState.asStateFlow()


    fun generateContent() {
        val prompt = _uiState.value.prompt
        _uiState.update {
            it.copy(
                chatHistory = it.chatHistory
                    .plus(DataState.Success(Message(it.prompt.text, it.prompt.text, Sender.User)))
                    .plus(DataState.Loading),
                prompt = it.prompt
            )
        }

        viewModelScope.launch {
            try {
                val content = content {
                    prompt.images?.forEach(::image)
                    text(prompt.text)
                }
                val response = generativeModel.generateContent(content)
                response.text?.let { outputContent ->
                    _uiState.update {
                        it.copy(
                            chatHistory = it.chatHistory
                                .dropLast(1)
                                .plus(
                                    DataState.Success(
                                        Message(
                                            outputContent,
                                            outputContent,
                                            Sender.Bot
                                        )
                                    )
                                ),
                            prompt = it.prompt.copy(text = String.empty)
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
        _uiState.update { it.copy(prompt = it.prompt.copy(text = prompt)) }
    }

    fun onNewChat() {
        _uiState.update { ChatState.initial() }
    }

    fun onModelSelected(model: Model) {
        _activeModel.value = model
        onNewChat()
    }

    fun onVisionImagesSelected(bitmaps: List<Bitmap>) {
        _uiState.update { it.copy(prompt = it.prompt.copy(images = bitmaps)) }
    }

    fun deleteImage(bitmap: Bitmap) {
        _uiState.update { it.copy(prompt = it.prompt.copy(images = it.prompt.images?.filter { it != bitmap })) }
    }

    fun onChangeAPIKey(apiKey: String) {
        _activeModel.value.updateKey(apiKey)
    }
}