package com.krishna.dictionaryapp.feature_dictionary.domain.use_case

import com.krishna.dictionaryapp.core.util.Resource
import com.krishna.dictionaryapp.feature_dictionary.domain.model.WordInfo
import com.krishna.dictionaryapp.feature_dictionary.domain.repository.WordInfoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetWordInfo(
    private val repository: WordInfoRepository
) {

    operator fun invoke(word: String): Flow<Resource<List<WordInfo>>> {
        if (word.isBlank()) {
            return flow { }
        }
        return repository.getWordInfo(word)
    }

    operator fun invoke(): Flow<Resource<List<WordInfo>>> {
        return repository.getPreviousSearches()
    }

}