package com.oguzhanaslann.geminiai

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalInspectionMode

/**
 *  Dispatches the content to be shown based on the current inspection mode.
 *  if the app is in preview mode, the previewContent is shown, otherwise the mainContent is shown.
 *
 * @param mainContent = that is shown when the app is running
 * @param previewContent = that is shown when the app is in preview mode
 */
@Composable
fun PreviewDispatchedView(
    mainContent: @Composable () -> Unit,
    previewContent: @Composable () -> Unit
) {
    if (LocalInspectionMode.current) {
        previewContent()
    } else {
        mainContent()
    }
}