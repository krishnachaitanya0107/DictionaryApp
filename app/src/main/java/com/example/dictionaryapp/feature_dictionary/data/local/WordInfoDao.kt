package com.example.dictionaryapp.feature_dictionary.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.dictionaryapp.feature_dictionary.data.local.entity.WordInfoEntity

@Dao
interface WordInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWordInfo(Infos: List<WordInfoEntity>)

    @Query("DELETE FROM wordinfoentity WHERE word in (:words)")
    suspend fun deleteWordInfo(words: List<String>)

    @Query("SELECT * FROM wordinfoentity WHERE word LIKE '%' || :word || '%'")
    suspend fun getWordInfo(word: String): List<WordInfoEntity>

    @Query("SELECT * FROM wordinfoentity ORDER BY id DESC")
    suspend fun getAllWordInfo():List<WordInfoEntity>
}