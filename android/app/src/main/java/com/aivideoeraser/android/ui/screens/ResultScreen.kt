package com.aivideoeraser.android.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aivideoeraser.android.data.api.RetrofitClient
import com.aivideoeraser.android.ui.components.VideoPlayerView
import com.aivideoeraser.android.ui.theme.*
import com.aivideoeraser.android.viewmodel.TaskViewModel

@Composable
fun ResultScreen(
    taskId: String,
    taskViewModel: TaskViewModel,
    onDone: () -> Unit
) {
    val task by taskViewModel.currentTask.collectAsState()
    var rating by remember { mutableStateOf(5) }
    var feedbackComment by remember { mutableStateOf("") }
    var feedbackSubmitted by remember { mutableStateOf(false) }

    val resultUrl = "${RetrofitClient.baseUrl.removeSuffix("/api/v1/")}api/v1/files/stream/result/$taskId"
    val resultUri = remember(resultUrl) { Uri.parse(resultUrl) }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDone) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
                Text(
                    text = "Result Preview",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                IconButton(onClick = { /* Share */ }) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Result Video Player
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                VideoPlayerView(
                    videoUri = resultUri,
                    modifier = Modifier.fillMaxSize()
                )

                // Success Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(SuccessGreen.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Object Erased ✨",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Controls & Rating Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Save Button
                Button(
                    onClick = { /* Trigger download */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Save Video to Gallery", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                // 5-Star Feedback Card
                if (!feedbackSubmitted) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceLight)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "How was the inpainting quality?",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            // Stars
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                for (i in 1..5) {
                                    Icon(
                                        imageVector = if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = "Star $i",
                                        tint = Color(0xFFFBBF24),
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clickable { rating = i }
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    taskViewModel.submitFeedback(taskId, rating, feedbackComment) {
                                        feedbackSubmitted = true
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan)
                            ) {
                                Text(text = "Submit Rating", fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Thank you for your feedback!",
                        color = SuccessGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}
