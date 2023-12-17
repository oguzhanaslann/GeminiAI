package com.oguzhanaslann.geminiai

import com.example.geminiai.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel

sealed class Model {
    abstract fun get(): GenerativeModel

    abstract fun name(): String

    abstract fun updateKey(key: String)

    data object Pro : Model() {
        private var model = GenerativeModel(
            modelName = "gemini-pro",
            apiKey = BuildConfig.apiKey
        )

        override fun get(): GenerativeModel = model

        override fun name(): String = "gemini-pro"

        override fun updateKey(key: String) {
            model = GenerativeModel(
                modelName = "gemini-pro",
                apiKey = key
            )
        }
    }

    data object Vision : Model() {
        private var model = GenerativeModel(
            modelName = "gemini-pro-vision",
            apiKey = BuildConfig.apiKey
        )

        override fun get(): GenerativeModel = model

        override fun name(): String = "gemini-pro-vision"

        override fun updateKey(key: String) {
            model = GenerativeModel(
                modelName = "gemini-pro-vision",
                apiKey = key
            )
        }
    }
}