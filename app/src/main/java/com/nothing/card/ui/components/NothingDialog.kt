package com.nothing.card.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.nothing.card.ui.theme.NothingBlack
import com.nothing.card.ui.theme.NothingWhite

@Composable
fun NothingAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        modifier = modifier.border(
            width = 1.dp,
            color = NothingWhite.copy(alpha = 0.1f),
            shape = RoundedCornerShape(12.dp)
        ),
        dismissButton = dismissButton,
        icon = icon,
        title = title,
        text = text,
        shape = RoundedCornerShape(12.dp),
        containerColor = NothingBlack,
        textContentColor = NothingWhite,
        titleContentColor = NothingWhite,
        iconContentColor = NothingWhite
    )
}
