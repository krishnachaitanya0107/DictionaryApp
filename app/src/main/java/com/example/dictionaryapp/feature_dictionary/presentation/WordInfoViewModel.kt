package com.example.dictionaryapp.feature_dictionary.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dictionaryapp.core.util.Resource
import com.example.dictionaryapp.feature_dictionary.domain.use_case.GetWordInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WordInfoViewModel @Inject constructor(
    private val getWordInfo: GetWordInfo
) : ViewModel() {

    private val _searchQuery = mutableStateOf("")
    val searchQuery = _searchQuery

    private val _state = mutableStateOf(WordInfoState())
    val state: State<WordInfoState> = _state

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var searchJob: Job? = null

    fun updateQuery(query: String) {
        _searchQuery.value = query
    }

    fun onSearch(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            getWordInfo(query).onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        _state.value = state.value.copy(
                            result.data ?: emptyList(),
                            false
                        )
                    }
                    is Resource.Error -> {
                        _state.value = state.value.copy(
                            result.data ?: emptyList(),
                            false
                        )
                        _eventFlow.emit(
                            UiEvent.ShowSnackbar(
                                result.message ?: "Unknown Error"
                            )
                        )
                    }
                    is Resource.Loading -> {
                        _state.value=state.value.copy(
                            result.data?: emptyList(),
                            true
                        )
                    }
                }
            }.launchIn(this)
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
    }

}