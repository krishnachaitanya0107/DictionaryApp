package com.example.dictionaryapp

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dictionaryapp.feature_dictionary.domain.model.WordInfo
import com.example.dictionaryapp.feature_dictionary.presentation.WordInfoItem
import com.example.dictionaryapp.feature_dictionary.presentation.WordInfoViewModel
import com.example.dictionaryapp.feature_dictionary.domain.use_case.OpenVoiceWithPermission
import com.example.dictionaryapp.feature_dictionary.domain.use_case.generateTextForNarration
import com.example.dictionaryapp.feature_dictionary.presentation.components.SearchTopBar
import com.example.dictionaryapp.ui.theme.DictionaryAppTheme
import com.example.dictionaryapp.ui.theme.titleColor
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import java.util.*

@ExperimentalPermissionsApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var textToSpeech: TextToSpeech
    private var ttsError = true
    private var cacheLoaded=false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DictionaryAppTheme {
                val viewModel: WordInfoViewModel = hiltViewModel()
                val state = viewModel.state.value

                val ctx = LocalContext.current
                var clickToShowPermission by rememberSaveable { mutableStateOf(false) }

                val searchQuery by viewModel.searchQuery
                val scaffoldState = rememberScaffoldState()

                initTextToSpeech(ctx)

                LaunchedEffect(key1 = true) {
                    viewModel.eventFlow.collectLatest { event ->
                        when (event) {
                            is WordInfoViewModel.UiEvent.ShowSnackbar -> {
                                scaffoldState.snackbarHostState.showSnackbar(event.message)
                            }
                        }
                    }
                }

                if (clickToShowPermission) {
                    OpenVoiceWithPermission(
                        onDismiss = { clickToShowPermission = false },
                        vm = viewModel,
                        ctxFromScreen = ctx
                    ) {
                        if (!viewModel.textFromSpeech.isNullOrEmpty()) {
                            viewModel.onSearch(viewModel.textFromSpeech!!)
                        }
                        clickToShowPermission = false
                    }
                }

                if(!cacheLoaded){
                    viewModel.loadPreviousSearches()
                    cacheLoaded=true
                }

                Scaffold(
                    scaffoldState = scaffoldState,
                    floatingActionButtonPosition = FabPosition.End,
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = { clickToShowPermission = true },
                            backgroundColor = MaterialTheme.colors.primary
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_mic),
                                contentDescription = "Mic",
                                tint = MaterialTheme.colors.titleColor
                            )
                        }
                    }
                ) {
                    Box(
                        modifier = Modifier.background(MaterialTheme.colors.background)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            SearchTopBar(
                                text = searchQuery,
                                onTextChange = {
                                    viewModel.updateQuery(it)
                                },
                                onSearchClicked = {
                                    viewModel.onSearch(it)
                                },
                                onCloseClicked = {
                                    viewModel.updateQuery("")
                                }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            LazyColumn(
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(state.wordInfoItems.size) { i ->
                                    val wordInfo = state.wordInfoItems[i]
                                    if (i > 0) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                    WordInfoItem(
                                        wordInfo = wordInfo,
                                        onClick = {
                                            if (!ttsError) {
                                                runTts(i, viewModel, wordInfo)
                                            }
                                        }
                                    )
                                    if (i < state.wordInfoItems.size - 1) {
                                        Divider()
                                    }
                                }
                            }
                        }

                        if (state.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        }
                    }
                }

            }
        }
    }


    private fun initTextToSpeech(ctx: Context) {
        textToSpeech = TextToSpeech(ctx) { i ->
            if (i != TextToSpeech.ERROR) {
                ttsError = false
            }
        }

        if (!ttsError) {
            textToSpeech.language = Locale.ENGLISH
        }
    }

    private fun runTts(
        i: Int,
        viewModel: WordInfoViewModel,
        wordInfo: WordInfo
    ) {

        if (textToSpeech.isSpeaking) {
            textToSpeech.stop()

            if (i != viewModel.currentSpeakingIndex) {
                textToSpeech.speak(
                    generateTextForNarration(wordInfo),
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    ""
                )
                viewModel.currentSpeakingIndex = i
            }
        } else {
            textToSpeech.speak(
                generateTextForNarration(wordInfo),
                TextToSpeech.QUEUE_FLUSH,
                null,
                ""
            )
            viewModel.currentSpeakingIndex = i
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            textToSpeech.shutdown()
        } catch (e: Exception) {
            Log.d("error", e.localizedMessage ?: "unknown error")
        }
    }

}