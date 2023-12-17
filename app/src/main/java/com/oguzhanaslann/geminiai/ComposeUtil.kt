package com.oguzhanaslann.geminiai

import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Composable

@Composable
fun composable(
    view: (@Composable () -> Unit)
): @Composable (() -> Unit)  {
    return view
}

suspend fun DrawerState.toggle() = if (isClosed) open() else close()