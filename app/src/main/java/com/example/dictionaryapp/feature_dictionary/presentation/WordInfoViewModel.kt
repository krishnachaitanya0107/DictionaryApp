package com.example.dictionaryapp.feature_dictionary.presentation

import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.MutableState
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

    private var _textFromSpeech = mutableStateOf("")
    val textFromSpeech = _textFromSpeech

    private val _currentSpeakingIndex = mutableStateOf(-1)
    val currentSpeakingIndex = _currentSpeakingIndex

    private val _speechRecognitionMsg = mutableStateOf("")
    val speechRecognitionMsg = _speechRecognitionMsg

    private val _clickToShowPermission = mutableStateOf(false)
    var clickToShowPermission = _clickToShowPermission

    private val _isSpeechRecActive = mutableStateOf(false)
    val isSpeechRecActive = _isSpeechRecActive

    private val _speechRecognizer: MutableState<SpeechRecognizer?> = mutableStateOf(null)
    val speechRecognizer = _speechRecognizer

    lateinit var textToSpeech: TextToSpeech

    private val _ttsError = mutableStateOf(true)
    var ttsError = _ttsError

    private var searchJob: Job? = null
    var isSearchClicked = false

    fun loadPreviousSearches() {
        searchJob = viewModelScope.launch {
            getWordInfo.invoke().onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.value = state.value.copy(
                            wordInfoItems = result.data ?: emptyList(),
                            isLoading = true
                        )
                    }

                    is Resource.Success -> {
                        _state.value = state.value.copy(
                            wordInfoItems = result.data ?: emptyList(),
                            isLoading = false
                        )
                    }

                    is Resource.Error -> {
                        _state.value = state.value.copy(
                            wordInfoItems = result.data ?: emptyList(),
                            isLoading = false
                        )
                    }
                }
            }.launchIn(this)
        }
    }

    fun updateQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateTextFromSpeech(text: String) {
        _textFromSpeech.value = text
    }

    fun updateTtsError(error: Boolean) {
        _ttsError.value = error
    }

    fun updateCurrentSpeakingIndex(index: Int) {
        _currentSpeakingIndex.value = index
    }

    fun updateSpeechRecActive(isActive: Boolean) {
        _isSpeechRecActive.value = isActive
    }

    fun updateSpeechRecognizer(speechRecognizer: SpeechRecognizer?) {
        _speechRecognizer.value = speechRecognizer
    }

    fun updateSpeechRecognitionMsg(message: String) {
        _speechRecognitionMsg.value = message
    }

    fun updateShowPermission(value: Boolean) {
        _clickToShowPermission.value = value
    }

    fun onSearch(query: String) {
        searchJob?.cancel()
        if (!isSearchClicked) {
            isSearchClicked = true
        }
        searchJob = viewModelScope.launch {
            val cleanedQuery = query.trim()

            getWordInfo.invoke(cleanedQuery).onEach { result ->
                when (result) {
                    is Resource.Loading -> {
                        _state.value = state.value.copy(
                            wordInfoItems = result.data ?: emptyList(),
                            isLoading = true
                        )
                    }

                    is Resource.Success -> {
                        _state.value = state.value.copy(
                            wordInfoItems = result.data ?: emptyList(),
                            isLoading = false
                        )
                    }

                    is Resource.Error -> {
                        _state.value = state.value.copy(
                            wordInfoItems = result.data ?: emptyList(),
                            isLoading = false
                        )
                        _eventFlow.emit(
                            UiEvent.ShowSnackbar(
                                result.message ?: "Unknown Error"
                            )
                        )
                        loadPreviousSearches()
                        isSearchClicked = false
                    }
                }
            }.launchIn(this)
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
    }

}