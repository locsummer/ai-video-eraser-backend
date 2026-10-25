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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aivideoeraser.android.data.model.EraseMode
import com.aivideoeraser.android.data.model.ModelEngine
import com.aivideoeraser.android.data.model.QualityMode
import com.aivideoeraser.android.ui.components.*
import com.aivideoeraser.android.ui.theme.*
import com.aivideoeraser.android.viewmodel.EditorViewModel
import com.aivideoeraser.android.viewmodel.TaskSubmitState

@Composable
fun EditorScreen(
    videoUri: Uri,
    editorViewModel: EditorViewModel,
    onBack: () -> Unit,
    onTaskCreated: (taskId: String) -> Unit
) {
    val context = LocalContext.current
    val activeTool by editorViewModel.activeTool.collectAsState()
    val brushRadius by editorViewModel.brushRadius.collectAsState()
    val autoTracking by editorViewModel.autoTracking.collectAsState()
    val submitState by editorViewModel.submitState.collectAsState()

    var showSettingsSheet by remember { mutableStateOf(false) }

    LaunchedEffect(videoUri) {
        editorViewModel.selectedVideoUri.value = videoUri
    }

    LaunchedEffect(submitState) {
        if (submitState is TaskSubmitState.Success) {
            onTaskCreated((submitState as TaskSubmitState.Success).task.id)
        }
    }

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
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text(
                    text = "AI Mask Editor",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                IconButton(onClick = { showSettingsSheet = true }) {
                    Icon(imageVector = Icons.Default.Tune, contentDescription = "Settings", tint = Color.White)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Video Player + Canvas Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                VideoPlayerView(
                    videoUri = videoUri,
                    modifier = Modifier.fillMaxSize()
                )

                MaskCanvasOverlay(
                    activeTool = activeTool,
                    brushRadius = brushRadius,
                    onMaskUpdated = { box, trackPts, brushPts ->
                        editorViewModel.boundingBox.value = box
                        editorViewModel.trackingPoints.value = trackPts
                        editorViewModel.brushPoints.value = brushPts
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Tools & Trim Control Panel
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Tool Selector Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    ToolButton(
                        icon = Icons.Default.GpsFixed,
                        label = "Track AI",
                        isSelected = activeTool == DrawingTool.TAP_TO_TRACK,
                        onClick = { editorViewModel.activeTool.value = DrawingTool.TAP_TO_TRACK }
                    )
                    ToolButton(
                        icon = Icons.Default.CropLandscape,
                        label = "Rectangle",
                        isSelected = activeTool == DrawingTool.RECTANGLE,
                        onClick = { editorViewModel.activeTool.value = DrawingTool.RECTANGLE }
                    )
                    ToolButton(
                        icon = Icons.Default.RadioButtonUnchecked,
                        label = "Ellipse",
                        isSelected = activeTool == DrawingTool.ELLIPSE,
                        onClick = { editorViewModel.activeTool.value = DrawingTool.ELLIPSE }
                    )
                    ToolButton(
                        icon = Icons.Default.Brush,
                        label = "Brush",
                        isSelected = activeTool == DrawingTool.BRUSH,
                        onClick = { editorViewModel.activeTool.value = DrawingTool.BRUSH }
                    )
                }

                // Brush Radius Slider if Brush is active
                if (activeTool == DrawingTool.BRUSH) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(text = "Size", color = TextSecondary, fontSize = 12.sp)
                        Slider(
                            value = brushRadius,
                            onValueChange = { editorViewModel.brushRadius.value = it },
                            valueRange = 10f..60f,
                            colors = SliderDefaults.colors(thumbColor = AccentPurple, activeTrackColor = AccentPurple),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Video Trim Slider
                VideoTrimSlider(
                    videoDurationSeconds = 15.0,
                    maxAllowedSeconds = 15,
                    onTrimChanged = { start, end ->
                        editorViewModel.trimStart.value = start
                        editorViewModel.trimEnd.value = end
                    }
                )

                // Erase Action Button
                Button(
                    onClick = { editorViewModel.uploadAndSubmitTask(context) },
                    enabled = submitState !is TaskSubmitState.Uploading && submitState !is TaskSubmitState.Submitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
                ) {
                    if (submitState is TaskSubmitState.Uploading || submitState is TaskSubmitState.Submitting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(text = if (submitState is TaskSubmitState.Uploading) "Uploading Video..." else "Creating Task...")
                    } else {
                        Icon(imageVector = Icons.Default.AutoFixHigh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Erase Object (10 Credits)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun ToolButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(if (isSelected) AccentPurple else SurfaceLight, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color.White else TextSecondary,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = if (isSelected) Color.White else TextSecondary,
            fontSize = 11.sp
        )
    }
}
