package com.oguzhanaslann.geminiai

import com.example.geminiai.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel

sealed class Model {
    abstract fun create(): GenerativeModel

    abstract fun name(): String

    data object Pro : Model() {
        private val model by lazy {
            GenerativeModel(
                modelName = "gemini-pro",
                apiKey = BuildConfig.apiKey
            )
        }

        override fun create(): GenerativeModel = model

        override fun name(): String = "gemini-pro"
    }

    data object Vision: Model() {
        private val model by lazy {
            GenerativeModel(
                modelName = "gemini-pro-vision",
                apiKey = BuildConfig.apiKey
            )
        }

        override fun create(): GenerativeModel = model

        override fun name(): String = "gemini-pro-vision"
    }
}