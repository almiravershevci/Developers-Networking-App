package com.example.developernetworkingapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.developernetworkingapp.data.repository.SearchRepository
import com.example.developernetworkingapp.di.AppContainer
import com.example.developernetworkingapp.ui.state.SearchUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchViewModel(
    private val repository: SearchRepository = AppContainer.searchRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeSearch().collect { _uiState.update { state -> state.copy(content = it) } }
        }
    }

    fun updateQuery(value: String) = _uiState.update { it.copy(query = value) }
}
