package com.finley.android.qualitytime.ui.poem

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finley.android.qualitytime.model.Poem
import com.finley.android.qualitytime.ui.theme.PoemFont
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PoemDetail(
    poem: Poem,
    isSpeaking: Boolean,
    highlightIndex: Int,
    showPinyin: Boolean,
    onSpeak: (Poem) -> Unit,
    onStop: () -> Unit
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(highlightIndex) {
        if (highlightIndex >= 0) {
            val targetItem = if (highlightIndex <= 2) 0 else (highlightIndex - 3 + 1)
            delay(100)
            listState.animateScrollToItem(targetItem.coerceAtLeast(0))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(top = 20.dp, bottom = 120.dp, start = 20.dp, end = 20.dp)
        ) {
            item {
                PoemHeader(poem, isSpeaking, highlightIndex, showPinyin)
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(
                    modifier = Modifier
                        .width(120.dp)
                        .height(3.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                )
                Spacer(modifier = Modifier.height(28.dp))
            }

            val lines = poem.content.split("\n").filter { it.isNotBlank() }
            val pinyinLines = computePinyinLines(poem, lines)

            itemsIndexed(lines) { index, line ->
                val actualIndex = index + 3
                val isHighlighted = isSpeaking && highlightIndex == actualIndex

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isHighlighted) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                            } else Color.Transparent
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (showPinyin && pinyinLines.size > index) {
                        PoemSentenceWithPinyin(
                            line = line,
                            pinyin = pinyinLines[index],
                            isHighlighted = isHighlighted
                        )
                    } else {
                        Text(
                            text = line,
                            fontFamily = PoemFont,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                lineHeight = 46.sp,
                                letterSpacing = 3.sp,
                                fontSize = 24.sp
                            ),
                            color = if (isHighlighted) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            fontWeight = if (isHighlighted) FontWeight.ExtraBold else FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(28.dp))
                if (poem.translation.isNotEmpty()) {
                    DetailCard(title = "译文", content = poem.translation, accent = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                if (poem.explanation.isNotEmpty()) {
                    DetailCard(title = "注释", content = poem.explanation, accent = MaterialTheme.colorScheme.tertiary)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                if (poem.appreciation.isNotEmpty()) {
                    DetailCard(title = "赏析", content = poem.appreciation, accent = MaterialTheme.colorScheme.secondary)
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp, bottom = 96.dp)
        ) {
            QuickScrollButtons(listState)
        }

        PoemPlayControl(
            poem = poem,
            isSpeaking = isSpeaking,
            onSpeak = onSpeak,
            onStop = onStop,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        )
    }
}

@Composable
private fun PoemHeader(
    poem: Poem,
    isSpeaking: Boolean,
    highlightIndex: Int,
    showPinyin: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (showPinyin) {
            PinyinText(
                text = poem.title,
                pinyin = poem.titlePinyin,
                isHighlighted = isSpeaking && highlightIndex == 0,
                charFontSize = 34.sp,
                pinyinFontSize = 14.sp
            )
        } else {
            Text(
                text = poem.title,
                fontFamily = PoemFont,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = if (isSpeaking && highlightIndex == 0) FontWeight.ExtraBold else FontWeight.Bold,
                    letterSpacing = 4.sp
                ),
                color = if (isSpeaking && highlightIndex == 0) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            val metaColor = if (isSpeaking && highlightIndex == 1) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
            Text(
                text = "〔",
                fontFamily = PoemFont,
                style = MaterialTheme.typography.titleMedium,
                color = metaColor
            )
            if (showPinyin) {
                PinyinText(
                    text = poem.dynasty,
                    pinyin = poem.dynastyPinyin,
                    isHighlighted = isSpeaking && highlightIndex == 1,
                    charFontSize = 18.sp,
                    pinyinFontSize = 11.sp
                )
            } else {
                Text(
                    text = poem.dynasty,
                    fontFamily = PoemFont,
                    style = MaterialTheme.typography.titleMedium,
                    color = metaColor
                )
            }
            Text(
                text = "〕",
                fontFamily = PoemFont,
                style = MaterialTheme.typography.titleMedium,
                color = metaColor
            )
            Spacer(modifier = Modifier.width(10.dp))
            if (showPinyin) {
                PinyinText(
                    text = poem.author,
                    pinyin = poem.authorPinyin,
                    isHighlighted = isSpeaking && highlightIndex == 2,
                    charFontSize = 18.sp,
                    pinyinFontSize = 11.sp
                )
            } else {
                Text(
                    text = poem.author,
                    fontFamily = PoemFont,
                    style = MaterialTheme.typography.titleMedium,
                    color = metaColor
                )
            }
        }
    }
}

@Composable
private fun QuickScrollButtons(listState: androidx.compose.foundation.lazy.LazyListState) {
    val scope = rememberCoroutineScope()
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val canScrollUp by remember {
            derivedStateOf {
                listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
            }
        }
        Surface(
            onClick = { scope.launch { listState.animateScrollToItem(0) } },
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(
                1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (canScrollUp) 0.6f else 0.2f)
            ),
            shadowElevation = 2.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.KeyboardArrowUp,
                    contentDescription = "滚动到顶部",
                    tint = if (canScrollUp) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        val canScrollDown by remember {
            derivedStateOf {
                !listState.layoutInfo.visibleItemsInfo.any { it.index == listState.layoutInfo.totalItemsCount - 1 }
            }
        }
        Surface(
            onClick = {
                scope.launch {
                    listState.animateScrollToItem(listState.layoutInfo.totalItemsCount - 1)
                }
            },
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(
                1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (canScrollDown) 0.6f else 0.2f)
            ),
            shadowElevation = 2.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = "滚动到底部",
                    tint = if (canScrollDown) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun PoemPlayControl(
    poem: Poem,
    isSpeaking: Boolean,
    onSpeak: (Poem) -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "playing")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Surface(
        modifier = modifier.widthIn(min = 240.dp),
        shape = RoundedCornerShape(28.dp),
        color = if (isSpeaking) {
            MaterialTheme.colorScheme.secondary
        } else {
            MaterialTheme.colorScheme.primary
        },
        shadowElevation = 10.dp
    ) {
        Row(
            modifier = Modifier
                .height(60.dp)
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier
                    .size(38.dp)
                    .graphicsLayer {
                        scaleX = if (isSpeaking) pulse else 1f
                        scaleY = if (isSpeaking) pulse else 1f
                    },
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.22f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isSpeaking) {
                        Icon(
                            Icons.Default.Stop,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                    text = if (isSpeaking) "正在朗读" else "开始朗读",
                    fontFamily = PoemFont,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                if (isSpeaking) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.GraphicEq,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = poem.title,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 1
                        )
                    }
                }
            }
            TextButton(onClick = { if (isSpeaking) onStop() else onSpeak(poem) }) {
                Text(
                    text = if (isSpeaking) "停止" else "朗读",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Compute pinyin lines from poem content, matching CJK characters with pinyin tokens.
 */
internal fun computePinyinLines(poem: Poem, lines: List<String>): List<String> {
    if (poem.pinyin.isEmpty()) return emptyList()
    val allPinyins = poem.pinyin.replace("\n", " ").split(" ").filter { it.isNotBlank() }
    if (allPinyins.isEmpty()) return emptyList()

    var pinyinIndex = 0
    return lines.map { line ->
        val count = line.count { it in '\u4E00'..'\u9FFF' }
        if (pinyinIndex + count <= allPinyins.size) {
            val pinyinTokens = allPinyins.subList(pinyinIndex, pinyinIndex + count)
            pinyinIndex += count
            pinyinTokens.joinToString(" ")
        } else {
            pinyinIndex = allPinyins.size
            ""
        }
    }
}

@Composable
fun DetailCard(title: String, content: String, accent: Color = MaterialTheme.colorScheme.primary) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                RoundedCornerShape(16.dp)
            )
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(18.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accent)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title,
                fontFamily = PoemFont,
                style = MaterialTheme.typography.titleSmall,
                color = accent,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = content,
            fontFamily = PoemFont,
            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 28.sp, letterSpacing = 0.5.sp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f)
        )
    }
}
