package com.nothing.card.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nothing.card.ui.theme.Ndot57Family
import com.nothing.card.ui.theme.NothingWhite
import com.nothing.card.ui.theme.SpaceMonoFamily

@Composable
fun SyncComparisonCard(
    localCount: Int,
    cloudCount: Int,
    lastSync: Long?
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        color = NothingWhite.copy(alpha = 0.05f),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, NothingWhite.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Local
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("LOCAL", style = MaterialTheme.typography.labelSmall, color = NothingWhite.copy(alpha = 0.5f), fontFamily = SpaceMonoFamily)
                    Text(
                        text = localCount.toString(),
                        style = MaterialTheme.typography.displayLarge.copy(fontFamily = Ndot57Family),
                        color = NothingWhite
                    )
                    Text("cards", style = MaterialTheme.typography.labelSmall, color = NothingWhite.copy(alpha = 0.3f), fontFamily = SpaceMonoFamily)
                }

                // Vertical Divider
                Box(modifier = Modifier.width(1.dp).height(40.dp).background(NothingWhite.copy(alpha = 0.1f)))

                // Cloud
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("CLOUD", style = MaterialTheme.typography.labelSmall, color = NothingWhite.copy(alpha = 0.5f), fontFamily = SpaceMonoFamily)
                    Text(
                        text = if (lastSync != null) cloudCount.toString() else "--",
                        style = MaterialTheme.typography.displayLarge.copy(fontFamily = Ndot57Family),
                        color = if (lastSync != null) NothingWhite else NothingWhite.copy(alpha = 0.2f)
                    )
                    Text("cards", style = MaterialTheme.typography.labelSmall, color = NothingWhite.copy(alpha = 0.3f), fontFamily = SpaceMonoFamily)
                }
            }

            if (lastSync != null) {
                Spacer(Modifier.height(20.dp))
                val date = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.ITALIAN).format(java.util.Date(lastSync))
                Text(
                    text = "LAST BACKUP: $date",
                    style = MaterialTheme.typography.labelSmall,
                    color = NothingWhite.copy(alpha = 0.4f),
                    fontFamily = SpaceMonoFamily,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
