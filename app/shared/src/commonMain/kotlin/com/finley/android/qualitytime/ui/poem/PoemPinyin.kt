package com.finley.android.qualitytime.ui.poem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PoemSentenceWithPinyin(
    line: String,
    pinyin: String,
    isHighlighted: Boolean
) {
    val pinyins = pinyin.split(" ").filter { it.isNotBlank() }
    var pinyinIndex = 0

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalArrangement = Arrangement.Center
    ) {
        line.forEach { char ->
            val charPinyin = if (char in '\u4E00'..'\u9FFF') {
                pinyins.getOrElse(pinyinIndex++) { "" }
            } else ""
            RubyText(
                char = char.toString(),
                pinyin = charPinyin,
                isHighlighted = isHighlighted
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PinyinText(
    text: String,
    pinyin: String,
    isHighlighted: Boolean,
    charFontSize: TextUnit = 26.sp,
    pinyinFontSize: TextUnit = 13.sp
) {
    val pinyins = pinyin.split(" ").filter { it.isNotBlank() }
    var pinyinIndex = 0

    FlowRow(
        horizontalArrangement = Arrangement.Center,
        verticalArrangement = Arrangement.Center
    ) {
        text.forEach { char ->
            val charPinyin = if (char in '\u4E00'..'\u9FFF') {
                pinyins.getOrElse(pinyinIndex++) { "" }
            } else ""
            RubyText(
                char = char.toString(),
                pinyin = charPinyin,
                isHighlighted = isHighlighted,
                charFontSize = charFontSize,
                pinyinFontSize = pinyinFontSize
            )
        }
    }
}

@Composable
fun RubyText(
    char: String,
    pinyin: String,
    isHighlighted: Boolean,
    charFontSize: TextUnit = 26.sp,
    pinyinFontSize: TextUnit = 13.sp
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(
            text = pinyin,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = pinyinFontSize,
                color = if (isHighlighted) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.6f),
                fontWeight = if (isHighlighted) FontWeight.Bold else FontWeight.Normal
            )
        )
        Text(
            text = char,
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = charFontSize),
            color = if (isHighlighted) MaterialTheme.colorScheme.primary else Color.Black,
            fontWeight = if (isHighlighted) FontWeight.ExtraBold else FontWeight.Medium
        )
    }
}
