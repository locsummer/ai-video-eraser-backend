package com.aivideoeraser.android.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.aivideoeraser.android.data.model.PointDto
import com.aivideoeraser.android.data.model.RectDto
import kotlin.math.abs
import kotlin.math.min

enum class DrawingTool {
    RECTANGLE,
    ELLIPSE,
    BRUSH,
    TAP_TO_TRACK
}

@Composable
fun MaskCanvasOverlay(
    activeTool: DrawingTool,
    brushRadius: Float,
    onMaskUpdated: (boundingBox: RectDto?, trackingPoints: List<PointDto>?, brushPoints: List<PointDto>?) -> Unit,
    modifier: Modifier = Modifier
) {
    var dragStart by remember { mutableStateOf<Offset?>(null) }
    var dragEnd by remember { mutableStateOf<Offset?>(null) }
    val brushPoints = remember { mutableStateListOf<Offset>() }
    val trackingPoints = remember { mutableStateListOf<Offset>() }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(activeTool) {
                if (activeTool == DrawingTool.TAP_TO_TRACK) {
                    detectTapGestures { offset ->
                        trackingPoints.clear()
                        trackingPoints.add(offset)
                        onMaskUpdated(
                            null,
                            listOf(PointDto(offset.x.toDouble(), offset.y.toDouble())),
                            null
                        )
                    }
                } else {
                    detectDragGestures(
                        onDragStart = { offset ->
                            dragStart = offset
                            dragEnd = offset
                            if (activeTool == DrawingTool.BRUSH) {
                                brushPoints.add(offset)
                            }
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            dragEnd = change.position
                            if (activeTool == DrawingTool.BRUSH) {
                                brushPoints.add(change.position)
                            }
                        },
                        onDragEnd = {
                            val start = dragStart
                            val end = dragEnd
                            if (start != null && end != null) {
                                if (activeTool == DrawingTool.RECTANGLE || activeTool == DrawingTool.ELLIPSE) {
                                    val x = min(start.x, end.x).toDouble()
                                    val y = min(start.y, end.y).toDouble()
                                    val w = abs(end.x - start.x).toDouble()
                                    val h = abs(end.y - start.y).toDouble()
                                    onMaskUpdated(RectDto(x, y, w, h), null, null)
                                } else if (activeTool == DrawingTool.BRUSH) {
                                    val points = brushPoints.map { PointDto(it.x.toDouble(), it.y.toDouble()) }
                                    onMaskUpdated(null, null, points)
                                }
                            }
                        }
                    )
                }
            }
    ) {
        val maskColor = Color(0x80EC4899)
        val strokeColor = Color(0xFFF43F5E)

        // Draw Freehand Brush Stroke
        if (brushPoints.size > 1) {
            val path = Path().apply {
                moveTo(brushPoints.first().x, brushPoints.first().y)
                for (i in 1 until brushPoints.size) {
                    lineTo(brushPoints[i].x, brushPoints[i].y)
                }
            }
            drawPath(
                path = path,
                color = maskColor,
                style = Stroke(
                    width = brushRadius * 2,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }

        // Draw Rectangle
        if (activeTool == DrawingTool.RECTANGLE && dragStart != null && dragEnd != null) {
            val start = dragStart!!
            val end = dragEnd!!
            val topLeft = Offset(min(start.x, end.x), min(start.y, end.y))
            val size = Size(abs(end.x - start.x), abs(end.y - start.y))

            drawRect(color = maskColor, topLeft = topLeft, size = size)
            drawRect(color = strokeColor, topLeft = topLeft, size = size, style = Stroke(width = 4f))
        }

        // Draw Ellipse
        if (activeTool == DrawingTool.ELLIPSE && dragStart != null && dragEnd != null) {
            val start = dragStart!!
            val end = dragEnd!!
            val topLeft = Offset(min(start.x, end.x), min(start.y, end.y))
            val size = Size(abs(end.x - start.x), abs(end.y - start.y))

            drawOval(color = maskColor, topLeft = topLeft, size = size)
            drawOval(color = strokeColor, topLeft = topLeft, size = size, style = Stroke(width = 4f))
        }

        // Draw Tracking Points (Crosshair Target)
        trackingPoints.forEach { point ->
            drawCircle(color = Color(0xFF06B6D4), radius = 24f, center = point, style = Stroke(width = 4f))
            drawCircle(color = Color.White, radius = 6f, center = point)
        }
    }
}
