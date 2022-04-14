package com.example.dictionaryapp.feature_dictionary.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dictionaryapp.feature_dictionary.domain.model.WordInfo
import com.example.dictionaryapp.ui.theme.descriptionColor
import com.example.dictionaryapp.ui.theme.titleColor

@Composable
fun WordInfoItem(
    wordInfo: WordInfo,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = wordInfo.word,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.titleColor
        )
        Text(
            text = wordInfo.phonetic,
            fontWeight = FontWeight.Light,
            color = MaterialTheme.colors.descriptionColor
        )
        Spacer(modifier = Modifier.height(16.dp))

        wordInfo.meanings.forEach { meaning ->
            Text(
                text = meaning.partOfSpeech,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.titleColor
            )
            meaning.definitions.forEachIndexed { i, definition ->
                Text(
                    text = "${i + 1}. ${definition.definition}",
                    color = MaterialTheme.colors.descriptionColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                definition.example?.let { example ->
                    Text(
                        text = "Example: $example",
                        color = MaterialTheme.colors.descriptionColor
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}