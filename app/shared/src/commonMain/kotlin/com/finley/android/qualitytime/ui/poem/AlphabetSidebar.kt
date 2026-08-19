package com.finley.android.qualitytime.ui.poem

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

    Column(
        modifier = modifier
            .width(26.dp)
            .padding(end = 6.dp)
            .padding(vertical = 44.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.35f)),
        verticalArrangement = Arrangement.spacedBy(1.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        letters.forEach { letter ->
            val exists = letter in letterIndexMap
            val isActive = letter == activeLetter

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(
                        if (isActive && exists) MaterialTheme.colorScheme.primary
                        else Color.Transparent
                    )
                    .clickable(enabled = exists) { onLetterClick(letter) }
            ) {
                Text(
                    text = letter,
                    fontSize = 10.sp,
                    fontWeight = if (isActive && exists) FontWeight.ExtraBold else FontWeight.Normal,
                    color = when {
                        isActive && exists -> MaterialTheme.colorScheme.onPrimary
                        exists -> MaterialTheme.colorScheme.onSurfaceVariant
                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f)
                    },
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
