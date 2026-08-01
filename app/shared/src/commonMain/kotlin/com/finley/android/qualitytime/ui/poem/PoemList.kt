package com.finley.android.qualitytime.ui.poem

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.finley.android.qualitytime.model.Poem

@Composable
fun PoemList(
    poems: List<Poem>,
    onPoemClick: (Poem) -> Unit,
    searchQuery: String = "",
    selectedLetter: String = "",
    sortKey: String = "default",
    listState: LazyListState = rememberLazyListState()
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(poems, key = { it.id }) { poem ->
            val animatedProgress = remember { Animatable(0f) }
            LaunchedEffect(poem.id) {
                animatedProgress.animateTo(1f, tween(500, easing = EaseOutQuart))
            }

            Box(modifier = Modifier.graphicsLayer {
                alpha = animatedProgress.value
                translationY = (1f - animatedProgress.value) * 50f
            }) {
                PoemCard(poem, onPoemClick, searchQuery, selectedLetter, sortKey)
            }
        }
    }
}

@Composable
fun PoemCard(poem: Poem, onPoemClick: (Poem) -> Unit, searchQuery: String = "", selectedLetter: String = "", sortKey: String = "default") {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.98f else 1f)

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = { onPoemClick(poem) }
            ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                val sortHighlight = Color.Yellow.copy(alpha = 0.25f)
                Text(
                    text = buildTitleText(poem.title, poem.titlePinyin, searchQuery, selectedLetter),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = if (sortKey == "title") Modifier.background(sortHighlight, CircleShape) else Modifier
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = poem.dynasty,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = if (sortKey == "dynasty") Modifier.background(sortHighlight, CircleShape) else Modifier
                    )
                    Text(" · ", color = Color.LightGray)
                    Text(
                        text = buildHighlightedText(poem.author, poem.authorPinyin, searchQuery),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        modifier = if (sortKey == "author") Modifier.background(sortHighlight, CircleShape) else Modifier
                    )
                }
            }
            if (poem.grade.isNotEmpty()) {
                Surface(
                    color = if (sortKey == "grade") {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    } else {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    },
                    shape = CircleShape
                ) {
                    Text(
                        text = poem.grade,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (sortKey == "grade") {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        }
                    )
                }
            }
        }
    }
}

/**
 * Build an AnnotatedString with search and/or letter-navigation highlighting for the title.
 */
@Composable
private fun buildTitleText(text: String, pinyin: String, searchQuery: String, selectedLetter: String): AnnotatedString {
    return buildAnnotatedString {
        append(text)

        // Search highlighting
        if (searchQuery.isNotBlank()) {
            val strippedQuery = PoemFilterEngine.stripToneMarks(searchQuery)
            if (hasChineseCharacters(searchQuery)) {
                var from = 0
                while (true) {
                    val idx = text.indexOf(searchQuery, from)
                    if (idx < 0) break
                    addStyle(
                        SpanStyle(background = Color.Yellow.copy(alpha = 0.5f), color = Color.Black),
                        idx, idx + searchQuery.length
                    )
                    from = idx + searchQuery.length
                }
            } else {
                val tokens = pinyin.split(" ").filter { it.isNotBlank() }
                tokens.forEachIndexed { i, token ->
                    if (i < text.length && PoemFilterEngine.stripToneMarks(token).contains(strippedQuery)) {
                        addStyle(
                            SpanStyle(background = Color.Yellow.copy(alpha = 0.5f), color = Color.Black),
                            i, i + 1
                        )
                    }
                }
            }
        }

        // Letter-navigation: highlight first character of poem title when matching
        if (selectedLetter.isNotEmpty()) {
            val firstLetter = PoemFilterEngine.stripToneMarks(pinyin)
                .firstOrNull()
                ?.uppercaseChar()
                ?.toString()
            if (firstLetter == selectedLetter && text.isNotEmpty()) {
                addStyle(
                    SpanStyle(
                        background = Color.Yellow.copy(alpha = 0.35f),
                        color = MaterialTheme.colorScheme.onPrimary
                    ),
                    0, 1
                )
            }
        }
    }
}

/**
 * Build an AnnotatedString with search result highlighting (no letter navigation).
 */
private fun buildHighlightedText(text: String, pinyin: String, query: String): AnnotatedString {
    if (query.isBlank()) return buildAnnotatedString { append(text) }

    val strippedQuery = PoemFilterEngine.stripToneMarks(query)

    return buildAnnotatedString {
        append(text)

        if (hasChineseCharacters(query)) {
            var from = 0
            while (true) {
                val idx = text.indexOf(query, from)
                if (idx < 0) break
                addStyle(
                    SpanStyle(background = Color.Yellow.copy(alpha = 0.5f), color = Color.Black),
                    idx, idx + query.length
                )
                from = idx + query.length
            }
        } else {
            val tokens = pinyin.split(" ").filter { it.isNotBlank() }
            tokens.forEachIndexed { i, token ->
                if (i < text.length && PoemFilterEngine.stripToneMarks(token).contains(strippedQuery)) {
                    addStyle(
                        SpanStyle(background = Color.Yellow.copy(alpha = 0.5f), color = Color.Black),
                        i, i + 1
                    )
                }
            }
        }
    }
}

private fun hasChineseCharacters(s: String): Boolean {
    return s.any { it in '\u4e00'..'\u9fff' || it in '\u3400'..'\u4dbf' }
}
