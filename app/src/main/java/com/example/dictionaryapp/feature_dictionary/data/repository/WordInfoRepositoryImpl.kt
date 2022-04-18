package com.example.dictionaryapp.feature_dictionary.data.repository

import com.example.dictionaryapp.core.util.Resource
import com.example.dictionaryapp.feature_dictionary.data.local.WordInfoDao
import com.example.dictionaryapp.feature_dictionary.data.remote.DictionaryApi
import com.example.dictionaryapp.feature_dictionary.domain.model.WordInfo
import com.example.dictionaryapp.feature_dictionary.domain.repository.WordInfoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException

class WordInfoRepositoryImpl(
    private val api: DictionaryApi,
    private val dao: WordInfoDao
) : WordInfoRepository {

    override fun getWordInfo(word: String): Flow<Resource<List<WordInfo>>> = flow {
        emit(Resource.Loading())

        val wordInfo = dao.getWordInfo(word).map { it.toWordInfo() }
        emit(Resource.Loading(data = wordInfo))

        try {
            val remoteWordInfo = api.getWordInfo(word = word)
            dao.deleteWordInfo(remoteWordInfo.map { it.word })
            dao.insertWordInfo(remoteWordInfo.map { it.toWordInfoEntity() })

        } catch (e: HttpException) {
            emit(Resource.Error("Oops! Something went wrong", wordInfo))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server , Check your internet connection", wordInfo))
        }

        val newWordInfo = dao.getWordInfo(word).map { it.toWordInfo() }

        emit(Resource.Success(newWordInfo))
    }

    override fun getPreviousSearches(): Flow<Resource<List<WordInfo>>> =flow {
        emit(Resource.Loading())

        val wordInfo = dao.getAllWordInfo().map { it.toWordInfo() }
        emit(Resource.Success(wordInfo))
    }

}