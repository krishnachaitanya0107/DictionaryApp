package com.krishna.dictionaryapp.feature_dictionary.domain.repository

import com.krishna.dictionaryapp.core.util.Resource
import com.krishna.dictionaryapp.feature_dictionary.domain.model.WordInfo
import kotlinx.coroutines.flow.Flow

interface WordInfoRepository {

    fun getWordInfo(word:String): Flow<Resource<List<WordInfo>>>

    fun getPreviousSearches(): Flow<Resource<List<WordInfo>>>

}