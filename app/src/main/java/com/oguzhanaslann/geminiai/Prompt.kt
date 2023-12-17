package com.oguzhanaslann.geminiai

import android.graphics.Bitmap

data class Prompt(
    val text : String,
    val images: List<Bitmap>? = null
) {
    companion object {
        fun empty() = Prompt(String.empty)
    }
}