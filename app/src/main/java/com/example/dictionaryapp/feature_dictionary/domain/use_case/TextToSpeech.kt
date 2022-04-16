package com.example.dictionaryapp.feature_dictionary.domain.use_case

import com.example.dictionaryapp.feature_dictionary.domain.model.WordInfo


fun generateTextForNarration(wordInfo: WordInfo): String {

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
