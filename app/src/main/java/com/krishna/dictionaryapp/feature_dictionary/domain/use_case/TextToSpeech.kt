package com.krishna.dictionaryapp.feature_dictionary.domain.use_case

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import com.krishna.dictionaryapp.feature_dictionary.domain.model.WordInfo
import com.krishna.dictionaryapp.feature_dictionary.presentation.WordInfoViewModel
import java.util.*


fun generateTextForNarration(
    wordInfo: WordInfo
): String {

    var res = "The word " + wordInfo.word + " means "
    wordInfo.meanings.forEach { meaning ->
        meaning.definitions.forEachIndexed { i, definition ->
            res += "${i + 1}. ${definition.definition}"
            definition.example?.let { example ->
                res += "For Example: $example"
            }
        }
    }
    return res
}

fun initTextToSpeech(
    ctx: Context,
    viewModel: WordInfoViewModel
) {

    viewModel.apply {

        textToSpeech = TextToSpeech(ctx) { i ->
            if (i != TextToSpeech.ERROR) {
                updateTtsError(error = false)
            }
        }

        if (!ttsError.value) {
            textToSpeech.language = Locale.ENGLISH
        }
    }
}

fun runTts(
    i: Int,
    viewModel: WordInfoViewModel,
    wordInfo: WordInfo
) {

    viewModel.apply {

        if (!ttsError.value) {
            if (textToSpeech.isSpeaking) {
                textToSpeech.stop()

                if (i != currentSpeakingIndex.value) {
                    textToSpeech.speak(
                        generateTextForNarration(wordInfo),
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        ""
                    )
                    updateCurrentSpeakingIndex(index = i)
                }
            } else {
                textToSpeech.speak(
                    generateTextForNarration(wordInfo),
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    ""
                )
                updateCurrentSpeakingIndex(index = i)
            }
        }
    }

}

fun shutDownTts(
    textToSpeech: TextToSpeech
){
    try {
        textToSpeech.shutdown()
    } catch (e: Exception) {
        Log.d("error", e.localizedMessage ?: "unknown error")
    }
}
