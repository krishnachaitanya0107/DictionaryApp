package com.example.dictionaryapp.feature_dictionary.domain.use_case

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.dictionaryapp.BuildConfig
import com.example.dictionaryapp.R
import com.example.dictionaryapp.feature_dictionary.presentation.WordInfoViewModel
import com.example.dictionaryapp.ui.theme.titleColor
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionRequired
import com.google.accompanist.permissions.rememberPermissionState

@ExperimentalPermissionsApi
@Composable
fun OpenVoiceWithPermission(
    onDismiss: () -> Unit,
    vm: WordInfoViewModel,
    ctxFromScreen: Context,
    finished: () -> Unit
) {

    val voicePermissionState = rememberPermissionState(android.Manifest.permission.RECORD_AUDIO)
    val ctx = LocalContext.current

    fun newIntent(ctx: Context) {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts(
            "package",
            BuildConfig.APPLICATION_ID, null
        )
        intent.data = uri
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        ctx.startActivity(intent)
    }

    PermissionRequired(
        permissionState = voicePermissionState,
        permissionNotGrantedContent = {
            ShowPermissionsDialog(onDismiss = onDismiss) {
                voicePermissionState.launchPermissionRequest()
            }
        },
        permissionNotAvailableContent = {
            ShowPermissionsDialog(onDismiss = onDismiss) {
                newIntent(ctx)
            }
        }
    ) {
        startSpeechToText(vm, ctxFromScreen, finished = finished)
    }
}

@Composable
fun ShowPermissionsDialog(
    onDismiss: () -> Unit,
    onClick: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(elevation = 12.dp, shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = stringResource(id = R.string.request_permission_msg),
                    color = MaterialTheme.colors.titleColor
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.primary,
                        contentColor = MaterialTheme.colors.titleColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Grant Permission")
                }
            }
        }
    }
}

@Composable
fun ShowIconDialog(
    onDismiss: () -> Unit,
    text: String
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(elevation = 12.dp, shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = text,
                    color = MaterialTheme.colors.titleColor,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(12.dp))
                Icon(
                    painter = painterResource(id = R.drawable.ic_mic),
                    modifier = Modifier.size(80.dp),
                    contentDescription = "Mic",
                    tint = MaterialTheme.colors.primary
                )
            }
        }
    }

}

fun onCloseSpeechToText(vm: WordInfoViewModel) {
    Handler(Looper.getMainLooper()).postDelayed({
        vm.updateSpeechRecActive(isActive = false)
        vm.updateSpeechRecognitionMsg("")
        vm.updateSpeechRecognizer(null)
    }, 1000)
}

fun startSpeechToText(
    viewModel: WordInfoViewModel,
    ctx: Context,
    finished: () -> Unit
) {

    if (!viewModel.isSpeechRecActive.value && viewModel.speechRecognizer.value == null) {

        var recognizer: SpeechRecognizer

        val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechRecognizerIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
        )

        viewModel.apply {

            if (speechRecognizer.value == null) {
                updateSpeechRecognizer(
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(
                        ctx
                    )
                )
            }

            recognizer = speechRecognizer.value!!

            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(bundle: Bundle?) {
                    updateSpeechRecActive(isActive = true)
                    updateSpeechRecognitionMsg("Loading...")
                }

                override fun onBeginningOfSpeech() {
                    updateSpeechRecognitionMsg("Listening...")
                }

                override fun onRmsChanged(v: Float) {}
                override fun onBufferReceived(bytes: ByteArray?) {}
                override fun onEndOfSpeech() {
                    finished()
                    updateSpeechRecognitionMsg("Processing Speech...")
                }

                override fun onError(i: Int) {
                    Log.d("error", "Oops , Something went wrong $i")
                    if (i == SpeechRecognizer.ERROR_NO_MATCH) {
                        updateSpeechRecognitionMsg("No Results Found , Please Try Again")
                    } else {
                        updateSpeechRecognitionMsg("Oops , Something went wrong")
                    }

                    onCloseSpeechToText(viewModel)
                }

                override fun onResults(bundle: Bundle) {
                    val result = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (result != null) {
                        updateTextFromSpeech(text = result[0])
                    } else {
                        updateSpeechRecognitionMsg("No Results Found , Please Try Again")
                    }

                    onCloseSpeechToText(viewModel)
                }

                override fun onPartialResults(bundle: Bundle) {
                    val result = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    if (result != null) {
                        updateTextFromSpeech(text = result[0])
                    } else {
                        updateSpeechRecognitionMsg("Sorry , Didn't get that , Please Try Again")
                    }

                    onCloseSpeechToText(viewModel)
                }

                override fun onEvent(i: Int, bundle: Bundle?) {}

            })

        }

        recognizer.startListening(speechRecognizerIntent)

    }


}