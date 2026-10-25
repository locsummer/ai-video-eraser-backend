package com.aivideoeraser.android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aivideoeraser.android.data.model.EraseTaskResponse
import com.aivideoeraser.android.data.model.SubscriptionPlanDto
import com.aivideoeraser.android.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TaskViewModel(
    private val taskRepository: TaskRepository = TaskRepository()
) : ViewModel() {

    private val _currentTask = MutableStateFlow<EraseTaskResponse?>(null)
    val currentTask: StateFlow<EraseTaskResponse?> = _currentTask.asStateFlow()

    private val _plans = MutableStateFlow<List<SubscriptionPlanDto>>(emptyList())
    val plans: StateFlow<List<SubscriptionPlanDto>> = _plans.asStateFlow()

    fun startPollingTask(taskId: String) {
        viewModelScope.launch {
            taskRepository.pollTaskProgress(taskId).collect { task ->
                _currentTask.value = task
            }
        }
    }

    fun cancelTask(taskId: String) {
        viewModelScope.launch {
            val res = taskRepository.cancelTask(taskId)
            res.onSuccess { _currentTask.value = it }
        }
    }

    fun submitFeedback(taskId: String, rating: Int, comment: String?, onComplete: () -> Unit) {
        viewModelScope.launch {
            taskRepository.submitFeedback(taskId, rating, comment)
            onComplete()
        }
    }

    fun loadPlans() {
        viewModelScope.launch {
            val res = taskRepository.getPlans()
            res.onSuccess { _plans.value = it }
        }
    }

    fun subscribe(planId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val res = taskRepository.subscribe(planId)
            res.onSuccess { onSuccess() }
        }
    }
}
