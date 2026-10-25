package com.aivideoeraser.android.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aivideoeraser.android.data.model.TaskStatus
import com.aivideoeraser.android.ui.theme.*
import com.aivideoeraser.android.viewmodel.TaskViewModel

@Composable
fun ProcessingScreen(
    taskId: String,
    taskViewModel: TaskViewModel,
    onComplete: (taskId: String) -> Unit,
    onCancelled: () -> Unit
) {
    val task by taskViewModel.currentTask.collectAsState()

    LaunchedEffect(taskId) {
        taskViewModel.startPollingTask(taskId)
    }

    LaunchedEffect(task?.status) {
        if (task?.status == TaskStatus.COMPLETED) {
            onComplete(taskId)
        } else if (task?.status == TaskStatus.CANCELLED) {
            onCancelled()
        }
    }

    val progress = task?.progress ?: 0

    Scaffold(
        containerColor = DarkBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Circular Progress Indicator
            Box(
                modifier = Modifier.size(180.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 10.dp,
                    color = AccentPurple,
                    trackColor = SurfaceLight
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$progress%",
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "AI Rendering",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Stage Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = task?.statusMessage ?: "Allocating GPU cloud worker...",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center
                    )

                    val currFrame = task?.currentFrame ?: 0
                    val totalFrames = task?.totalFrames ?: 300
                    if (totalFrames > 0) {
                        Text(
                            text = "Frame: $currFrame / $totalFrames",
                            color = AccentCyan,
                            fontSize = 13.sp
                        )
                    }

                    Text(
                        text = "Temporal consistency inpainting in progress",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Cancel Task Button
            OutlinedButton(
                onClick = { taskViewModel.cancelTask(taskId) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Cancel Task (Auto Refund)")
            }
        }
    }
}
