package com.example.kmaerm.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kmaerm.data.model.ComparisonUiModel
import com.example.kmaerm.data.model.DemoResult
import com.example.kmaerm.data.repository.DemoRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class DemoComparisonViewModel : ViewModel() {

    private val repository = DemoRepository()

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    sealed class UiState {
        object Idle : UiState()
        data class Loading(val type: LoadingType) : UiState()
        data class Success(val data: ComparisonUiModel) : UiState()
        data class Error(val message: String) : UiState()
    }

    enum class LoadingType {
        SEQUENTIAL,
        PARALLEL,
        BOTH
    }


    fun runSequentialOnly() {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading(LoadingType.SEQUENTIAL)

                val result = repository.getSequentialResult()
                val uiModel = buildComparisonModel(result, null)

                _uiState.value = UiState.Success(uiModel)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    message = e.message ?: "Lỗi khi gọi API tuần tự"
                )
                e.printStackTrace()
            }
        }
    }


    fun runParallelOnly() {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading(LoadingType.PARALLEL)

                val result = repository.getParallelResult()
                val uiModel = buildComparisonModel(null, result)

                _uiState.value = UiState.Success(uiModel)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    message = e.message ?: "Lỗi khi gọi API song song"
                )
                e.printStackTrace()
            }
        }
    }


    fun runComparison() {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading(LoadingType.BOTH)

                coroutineScope {
                    val sequentialDeferred = async {
                        try {
                            repository.getSequentialResult()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            null
                        }
                    }
                    val parallelDeferred = async {
                        try {
                            repository.getParallelResult()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            null
                        }
                    }

                    val sequentialResult = sequentialDeferred.await()
                    val parallelResult = parallelDeferred.await()

                    if (sequentialResult == null && parallelResult == null) {
                        _uiState.value = UiState.Error("Không thể kết nối đến server (404). Kiểm tra API endpoint.")
                    } else {
                        val uiModel = buildComparisonModel(sequentialResult, parallelResult)
                        _uiState.value = UiState.Success(uiModel)
                    }
                }

            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    message = e.message ?: "Đã xảy ra lỗi khi gọi API"
                )
                e.printStackTrace()
            }
        }
    }


    private fun buildComparisonModel(
        sequential: DemoResult?,
        parallel: DemoResult?
    ): ComparisonUiModel {
        val seqTime = sequential?.metrics?.totalTimeMs ?: 0
        val parTime = parallel?.metrics?.totalTimeMs ?: 0

        val speedupRatio = if (seqTime > 0 && parTime > 0) {
            seqTime.toDouble() / parTime.toDouble()
        } else null

        val timeSaved = if (seqTime > 0 && parTime > 0) {
            seqTime - parTime
        } else null

        val efficiencyGain = if (seqTime > 0 && parTime > 0) {
            ((seqTime - parTime).toDouble() / seqTime) * 100
        } else null

        return ComparisonUiModel(
            sequentialResult = sequential,
            parallelResult = parallel,
            speedupRatio = speedupRatio,
            timeSaved = timeSaved,
            efficiencyGain = efficiencyGain
        )
    }


    fun reset() {
        _uiState.value = UiState.Idle
    }
}

