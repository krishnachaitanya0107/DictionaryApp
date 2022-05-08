package com.example.dictionaryapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.dictionaryapp.feature_dictionary.domain.use_case.OpenVoiceWithPermission
import com.example.dictionaryapp.feature_dictionary.domain.use_case.ShowIconDialog
import com.example.dictionaryapp.feature_dictionary.domain.use_case.initTextToSpeech
import com.example.dictionaryapp.feature_dictionary.domain.use_case.runTts
import com.example.dictionaryapp.feature_dictionary.presentation.WordInfoItem
import com.example.dictionaryapp.feature_dictionary.presentation.WordInfoViewModel
import com.example.dictionaryapp.feature_dictionary.presentation.components.SearchTopBar
import com.example.dictionaryapp.ui.theme.DictionaryAppTheme
import com.example.dictionaryapp.ui.theme.descriptionColor
import com.example.dictionaryapp.ui.theme.titleColor
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@ExperimentalPermissionsApi
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DictionaryAppTheme {
                val viewModel: WordInfoViewModel = hiltViewModel()
                val state = viewModel.state.value

                val ctx = LocalContext.current
                var initialized by rememberSaveable { mutableStateOf(false) }

                val statusBarColor = if (isSystemInDarkTheme()) Color.Black else Color.White

                val searchQuery by viewModel.searchQuery
                val scaffoldState = rememberScaffoldState()

                LaunchedEffect(key1 = true) {
                    viewModel.eventFlow.collectLatest { event ->
                        when (event) {
                            is WordInfoViewModel.UiEvent.ShowSnackbar -> {
                                scaffoldState.snackbarHostState.showSnackbar(event.message)
                            }
                        }
                    }
                }

                val systemUiController = rememberSystemUiController()

                SideEffect {
                    systemUiController.setStatusBarColor(
                        color = statusBarColor
                    )
                }

                if (viewModel.clickToShowPermission.value) {
                    OpenVoiceWithPermission(
                        onDismiss = remember(viewModel) {
                            {
                                viewModel.updateShowPermission(false)
                            }
                        },
                        vm = viewModel,
                        ctxFromScreen = ctx
                    ) {
                        if (!viewModel.textFromSpeech.value.isNullOrEmpty()) {
                            viewModel.onSearch(viewModel.textFromSpeech.value)
                        }
                        viewModel.updateShowPermission(false)
                    }
                }

                if (!initialized) {
                    viewModel.loadPreviousSearches()
                    initTextToSpeech(ctx, viewModel)
                    initialized = true
                }

                if (viewModel.isSpeechRecActive.value) {
                    ShowIconDialog(
                        onDismiss = { },
                        text = viewModel.speechRecognitionMsg.value
                    )
                }

                Scaffold(
                    scaffoldState = scaffoldState,
                    floatingActionButtonPosition = FabPosition.End,
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = remember(viewModel) {
                                {
                                    viewModel.updateShowPermission(true)
                                }
                            },
                            backgroundColor = MaterialTheme.colors.primary
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_mic),
                                contentDescription = "Mic",
                                tint = Color.White
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

                            if (!viewModel.isSearchClicked) {
                                Text(
                                    text = "Recent searches...",
                                    color = MaterialTheme.colors.descriptionColor
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                            }

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
                                            runTts(i, viewModel, wordInfo)
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

    override fun onDestroy() {
        super.onDestroy()
        // shutdown tts engine here
    }

}