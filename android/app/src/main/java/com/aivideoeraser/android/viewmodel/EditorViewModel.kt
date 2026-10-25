package com.aivideoeraser.android.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aivideoeraser.android.data.model.*
import com.aivideoeraser.android.data.repository.TaskRepository
import com.aivideoeraser.android.data.repository.VideoRepository
import com.aivideoeraser.android.ui.components.DrawingTool
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class TaskSubmitState {
    object Idle : TaskSubmitState()
    object Uploading : TaskSubmitState()
    object Submitting : TaskSubmitState()
    data class Success(val task: EraseTaskResponse) : TaskSubmitState()
    data class Error(val message: String) : TaskSubmitState()
}

class EditorViewModel(
    private val videoRepository: VideoRepository = VideoRepository(),
    private val taskRepository: TaskRepository = TaskRepository()
) : ViewModel() {

    var selectedVideoUri = MutableStateFlow<Uri?>(null)
    var uploadedVideo = MutableStateFlow<VideoResponse?>(null)

    var activeTool = MutableStateFlow(DrawingTool.RECTANGLE)
    var eraseMode = MutableStateFlow(EraseMode.OBJECT_REMOVE)
    var modelEngine = MutableStateFlow(ModelEngine.PRO_PAINTER)
    var qualityMode = MutableStateFlow(QualityMode.STANDARD)
    var autoTracking = MutableStateFlow(true)
    var brushRadius = MutableStateFlow(25f)

    var trimStart = MutableStateFlow(0.0)
    var trimEnd = MutableStateFlow(10.0)

    var boundingBox = MutableStateFlow<RectDto?>(null)
    var trackingPoints = MutableStateFlow<List<PointDto>?>(null)
    var brushPoints = MutableStateFlow<List<PointDto>?>(null)

    private val _submitState = MutableStateFlow<TaskSubmitState>(TaskSubmitState.Idle)
    val submitState: StateFlow<TaskSubmitState> = _submitState.asStateFlow()

    fun uploadAndSubmitTask(context: Context) {
        val uri = selectedVideoUri.value ?: return
        viewModelScope.launch {
            _submitState.value = TaskSubmitState.Uploading
            val uploadRes = videoRepository.uploadVideo(context, uri)

            uploadRes.onSuccess { video ->
                uploadedVideo.value = video
                _submitState.value = TaskSubmitState.Submitting

                // Build Mask Data
                val shapeType = when (activeTool.value) {
                    DrawingTool.RECTANGLE -> "RECTANGLE"
                    DrawingTool.ELLIPSE -> "ELLIPSE"
                    DrawingTool.BRUSH -> "BRUSH"
                    DrawingTool.TAP_TO_TRACK -> "SAM_POINTS"
                }

                val maskData = MaskData(
                    shapeType = shapeType,
                    boundingBox = boundingBox.value,
                    trackingPoints = trackingPoints.value,
                    brushStroke = brushPoints.value,
                    brushRadius = brushRadius.value.toDouble()
                )

                val request = CreateEraseTaskRequest(
                    videoId = video.id,
                    eraseMode = eraseMode.value,
                    modelEngine = modelEngine.value,
                    qualityMode = qualityMode.value,
                    autoTracking = autoTracking.value,
                    trimConfig = TrimConfig(trimStart.value, trimEnd.value),
                    maskData = maskData
                )

                val taskRes = taskRepository.createEraseTask(request)
                taskRes.onSuccess { task ->
                    _submitState.value = TaskSubmitState.Success(task)
                }.onFailure {
                    _submitState.value = TaskSubmitState.Error(it.message ?: "Task submission failed")
                }

            }.onFailure {
                _submitState.value = TaskSubmitState.Error(it.message ?: "Video upload failed")
            }
        }
    }
}
