package com.finley.android.qualitytime.ui.poem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finley.android.qualitytime.model.Poem
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
        // Decorative Stamp in background
        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 120.dp, end = 32.dp)
                .alpha(0.08f)
                .border(2.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(4.dp))
                .padding(8.dp),
            color = Color.Transparent
        ) {
            Text(
                text = "学",
                fontSize = 72.sp,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(top = 32.dp, bottom = 140.dp, start = 24.dp, end = 24.dp)
        ) {
            item {
                PoemHeader(poem, isSpeaking, highlightIndex, showPinyin)
                Spacer(modifier = Modifier.height(64.dp))
            }

            val lines = poem.content.split("\n").filter { it.isNotBlank() }
            val pinyinLines = computePinyinLines(poem, lines)

            itemsIndexed(lines) { index, line ->
                val actualIndex = index + 3
                val isHighlighted = isSpeaking && highlightIndex == actualIndex

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
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
                            style = MaterialTheme.typography.headlineSmall.copy(
                                lineHeight = 46.sp,
                                letterSpacing = 3.sp,
                                fontSize = 24.sp
                            ),
                            color = if (isHighlighted) MaterialTheme.colorScheme.primary else Color.DarkGray,
                            fontWeight = if (isHighlighted) FontWeight.ExtraBold else FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            item {
                if (poem.translation.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(64.dp))
                    DetailCard(title = "译文", content = poem.translation)
                }
                if (poem.appreciation.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    DetailCard(title = "赏析", content = poem.appreciation)
                }
            }
        }

        // Quick scroll buttons
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 8.dp, bottom = 100.dp)
        ) {
            QuickScrollButtons(listState)
        }

        // Play control
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .navigationBarsPadding()
                .height(64.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 12.dp
        ) {
            PoemPlayControl(
                poem = poem,
                isSpeaking = isSpeaking,
                onSpeak = onSpeak,
                onStop = onStop
            )
        }
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
                charFontSize = 32.sp,
                pinyinFontSize = 16.sp
            )
        } else {
            Text(
                text = poem.title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = if (isSpeaking && highlightIndex == 0) FontWeight.ExtraBold else FontWeight.Bold,
                    letterSpacing = 3.sp
                ),
                color = if (isSpeaking && highlightIndex == 0) MaterialTheme.colorScheme.primary else Color.Black
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "〔",
                style = MaterialTheme.typography.titleMedium,
                color = if (isSpeaking && highlightIndex == 1) MaterialTheme.colorScheme.primary else Color.Gray
            )
            if (showPinyin) {
                PinyinText(
                    text = poem.dynasty,
                    pinyin = poem.dynastyPinyin,
                    isHighlighted = isSpeaking && highlightIndex == 1,
                    charFontSize = 18.sp,
                    pinyinFontSize = 12.sp
                )
            } else {
                Text(
                    text = poem.dynasty,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSpeaking && highlightIndex == 1) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }
            Text(
                text = "〕",
                style = MaterialTheme.typography.titleMedium,
                color = if (isSpeaking && highlightIndex == 1) MaterialTheme.colorScheme.primary else Color.Gray
            )
            Spacer(modifier = Modifier.width(8.dp))
            if (showPinyin) {
                PinyinText(
                    text = poem.author,
                    pinyin = poem.authorPinyin,
                    isHighlighted = isSpeaking && highlightIndex == 2,
                    charFontSize = 18.sp,
                    pinyinFontSize = 12.sp
                )
            } else {
                Text(
                    text = poem.author,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSpeaking && highlightIndex == 2) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }
        }
    }
}

@Composable
private fun QuickScrollButtons(listState: androidx.compose.foundation.lazy.LazyListState) {
    val scope = rememberCoroutineScope()
    Column(
        modifier = Modifier,
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
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (canScrollUp) 0.9f else 0.3f),
            shadowElevation = 4.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.KeyboardArrowUp,
                    contentDescription = "滚动到顶部",
                    tint = if (canScrollUp) MaterialTheme.colorScheme.onPrimaryContainer else Color.LightGray,
                    modifier = Modifier.size(24.dp)
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
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (canScrollDown) 0.9f else 0.3f),
            shadowElevation = 4.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = "滚动到底部",
                    tint = if (canScrollDown) MaterialTheme.colorScheme.onPrimaryContainer else Color.LightGray,
                    modifier = Modifier.size(24.dp)
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
    onStop: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxHeight()
            .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isSpeaking) {
                Button(
                    onClick = onStop,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(horizontal = 24.dp)
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("停止朗读", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            } else {
                Button(
                    onClick = { onSpeak(poem) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(horizontal = 24.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("开始朗读", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
fun DetailCard(title: String, content: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.5f))
            .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(4.dp, 16.dp)
                    .background(MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 26.sp),
            color = Color.DarkGray
        )
    }
}
