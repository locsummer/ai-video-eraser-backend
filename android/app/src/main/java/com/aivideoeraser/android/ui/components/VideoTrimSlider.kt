package com.aivideoeraser.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aivideoeraser.android.ui.theme.AccentPurple
import com.aivideoeraser.android.ui.theme.SurfaceDark
import com.aivideoeraser.android.ui.theme.TextSecondary
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoTrimSlider(
    videoDurationSeconds: Double,
    maxAllowedSeconds: Int,
    onTrimChanged: (start: Double, end: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var range by remember(videoDurationSeconds) {
        val initialEnd = if (videoDurationSeconds > 0) {
            kotlin.math.min(videoDurationSeconds, maxAllowedSeconds.toDouble()).toFloat()
        } else {
            10f
        }
        mutableStateOf(0f..initialEnd)
    }

    val maxVal = if (videoDurationSeconds > 0) videoDurationSeconds.toFloat() else 15f

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(SurfaceDark, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Trim Segment",
                color = Color.White,
                fontSize = 14.sp
            )
            val selectedDuration = range.endInclusive - range.start
            Text(
                text = String.format(Locale.US, "%.1fs / Max %ds", selectedDuration, maxAllowedSeconds),
                color = if (selectedDuration > maxAllowedSeconds) MaterialTheme.colorScheme.error else AccentPurple,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        RangeSlider(
            value = range,
            onValueChange = { newRange ->
                range = newRange
                onTrimChanged(newRange.start.toDouble(), newRange.endInclusive.toDouble())
            },
            valueRange = 0f..maxVal,
            colors = SliderDefaults.colors(
                thumbColor = AccentPurple,
                activeTrackColor = AccentPurple,
                inactiveTrackColor = Color(0xFF475569)
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = String.format(Locale.US, "Start: %.1fs", range.start),
                color = TextSecondary,
                fontSize = 11.sp
            )
            Text(
                text = String.format(Locale.US, "End: %.1fs", range.endInclusive),
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}
