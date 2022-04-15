package com.example.dictionaryapp.feature_dictionary.domain.use_case

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
            Dialog(onDismissRequest = onDismiss) {
                Card(elevation = 12.dp, shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = stringResource(id = R.string.request_permission_msg),
                            color = MaterialTheme.colors.titleColor
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { voicePermissionState.launchPermissionRequest() },
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
        },
        permissionNotAvailableContent = {
            Dialog(onDismissRequest = onDismiss) {
                Card(elevation = 12.dp, shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = stringResource(id = R.string.request_permission_msg),
                            color = MaterialTheme.colors.titleColor
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { newIntent(ctx) },
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
    ) {
        startSpeechToText(vm, ctxFromScreen, finished = finished)
    }
}

fun startSpeechToText(vm: WordInfoViewModel, ctx: Context, finished: () -> Unit) {
    val speechRecognizer = SpeechRecognizer.createSpeechRecognizer(ctx)
    val speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
    speechRecognizerIntent.putExtra(
        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM,
    )

    speechRecognizer.setRecognitionListener(object : RecognitionListener {
        override fun onReadyForSpeech(bundle: Bundle?) {}
        override fun onBeginningOfSpeech() {
            Toast.makeText(ctx,"Listening...",Toast.LENGTH_SHORT).show()
        }
        override fun onRmsChanged(v: Float) {}
        override fun onBufferReceived(bytes: ByteArray?) {}
        override fun onEndOfSpeech() {
            finished()
            Toast.makeText(ctx,"Processing Speech...",Toast.LENGTH_SHORT).show()
            // changing the color of your mic icon to
            // gray to indicate it is not listening or do something you want
        }

        override fun onError(i: Int) {
            Log.d("error", "Oops , Something went wrong $i")
            if(i==SpeechRecognizer.ERROR_NO_MATCH){
                Toast.makeText(ctx,"No Results Found , Please Try Again",Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(ctx,"Oops , Something went wrong",Toast.LENGTH_SHORT).show()
            }
        }

        override fun onResults(bundle: Bundle) {
            val result = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (result != null) {
                vm.textFromSpeech = result[0]
            } else {
                Toast.makeText(ctx,"No Results Found , Please Try Again",Toast.LENGTH_SHORT).show()
            }
        }

        override fun onPartialResults(bundle: Bundle) {}
        override fun onEvent(i: Int, bundle: Bundle?) {}

    })
    speechRecognizer.startListening(speechRecognizerIntent)
}