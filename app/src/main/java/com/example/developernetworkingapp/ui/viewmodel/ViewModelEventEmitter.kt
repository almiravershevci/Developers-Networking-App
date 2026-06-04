package com.example.developernetworkingapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

internal class ViewModelEventEmitter<T>(
    private val viewModel: ViewModel,
) {
    private val flow = MutableSharedFlow<T>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val events: SharedFlow<T> = flow.asSharedFlow()

    fun emit(event: T) {
        viewModel.viewModelScope.launch {
            flow.emit(event)
        }
    }
}

internal fun <T> ViewModel.eventEmitter(): ViewModelEventEmitter<T> =
    ViewModelEventEmitter(this)
