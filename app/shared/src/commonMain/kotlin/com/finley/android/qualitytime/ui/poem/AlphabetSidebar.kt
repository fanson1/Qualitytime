package com.finley.android.qualitytime.ui.poem

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AlphabetSidebar(
    letterIndexMap: Map<String, Int>,
    activeLetter: String,
    onLetterClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val letters = ('A'..'Z').map { it.toString() } + "#"

    Box(
        modifier = modifier
            .width(28.dp)
            .fillMaxHeight()
            .padding(end = 4.dp)
            .padding(vertical = 48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f))
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            letters.forEach { letter ->
                val exists = letter in letterIndexMap
                Text(
                    text = letter,
                    fontSize = 10.sp,
                    fontWeight = if (letter == activeLetter) FontWeight.ExtraBold else FontWeight.Normal,
                    color = if (exists) {
                        if (letter == activeLetter) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    },
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .clickable(enabled = exists) { onLetterClick(letter) }
                        .padding(vertical = 1.dp, horizontal = 4.dp)
                )
            }
        }
    }
}
