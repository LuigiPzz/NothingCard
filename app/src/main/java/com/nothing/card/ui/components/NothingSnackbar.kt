package com.nothing.card.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nothing.card.ui.theme.NothingBlack
import com.nothing.card.ui.theme.NothingWhite
import com.nothing.card.ui.theme.SpaceMonoFamily

@Composable
fun NothingSnackbar(
    snackbarData: SnackbarData
) {
    Snackbar(
        modifier = Modifier
            .padding(12.dp)
            .border(1.dp, NothingWhite.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
        containerColor = NothingBlack,
        contentColor = NothingWhite,
        shape = RoundedCornerShape(12.dp),
        actionContentColor = MaterialTheme.colorScheme.primary,
        dismissActionContentColor = NothingWhite.copy(alpha = 0.5f)
    ) {
        Text(
            text = snackbarData.visuals.message.uppercase(),
            fontFamily = SpaceMonoFamily,
            fontSize = 12.sp,
            letterSpacing = 0.5.sp
        )
    }
}
