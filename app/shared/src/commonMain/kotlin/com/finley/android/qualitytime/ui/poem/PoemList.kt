package com.finley.android.qualitytime.ui.poem

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finley.android.qualitytime.model.Poem
import com.finley.android.qualitytime.ui.theme.PoemFont

@Composable
fun PoemList(
    poems: List<Poem>,
    onPoemClick: (Poem) -> Unit,
    searchQuery: String = "",
    selectedLetter: String = "",
    sortKey: String = "default",
    listState: LazyListState = rememberLazyListState(),
    header: (@Composable () -> Unit)? = null
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (header != null) {
            item(key = "home_header") { header() }
        }
        items(poems, key = { it.id }) { poem ->
            val animatedProgress = remember { Animatable(0f) }
            LaunchedEffect(poem.id) {
                animatedProgress.animateTo(1f, tween(450, easing = EaseOutQuart))
            }

            Box(modifier = Modifier.graphicsLayer {
                alpha = animatedProgress.value
                translationY = (1f - animatedProgress.value) * 24f
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
    val scale by animateFloatAsState(if (isPressed) 0.975f else 1f, label = "card")

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = { onPoemClick(poem) }
            ),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧「诗」字印章
            val gradeBg = gradeColor(poem.grade)
            Surface(
                modifier = Modifier.size(46.dp),
                shape = RoundedCornerShape(12.dp),
                color = gradeBg
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "诗",
                        fontFamily = PoemFont,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = gradeTextColor(poem.grade)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = buildTitleText(poem.title, poem.titlePinyin, searchQuery, selectedLetter),
                    fontFamily = PoemFont,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = if (sortKey == "title") {
                        Modifier.background(gradeBg.copy(alpha = 0.5f), CircleShape)
                    } else Modifier
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = poem.dynasty,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = if (sortKey == "dynasty") {
                            Modifier.background(gradeBg.copy(alpha = 0.5f), CircleShape)
                        } else Modifier
                    )
                    Text(
                        text = " · ",
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    Text(
                        text = buildHighlightedText(poem.author, poem.authorPinyin, searchQuery),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = if (sortKey == "author") {
                            Modifier.background(gradeBg.copy(alpha = 0.5f), CircleShape)
                        } else Modifier
                    )
                }
                val preview = poem.content.split("\n").firstOrNull { it.isNotBlank() }
                if (preview != null) {
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = preview,
                        fontFamily = PoemFont,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            if (poem.grade.isNotEmpty()) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        color = if (sortKey == "grade") {
                            MaterialTheme.colorScheme.primary
                        } else {
                            gradeBg
                        },
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = poem.grade,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (sortKey == "grade") {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                gradeTextColor(poem.grade)
                            }
                        )
                    }
                }
            }
        }
    }
}

// ─── 年级配色（柔和色板，稳定映射） ───────────────────────────────────────

private data class GradeStyle(val bg: Color, val fg: Color)

private val gradeStyles = listOf(
    GradeStyle(Color(0xFFE3F2ED), Color(0xFF005048)),
    GradeStyle(Color(0xFFF6ECD9), Color(0xFF6B4E00)),
    GradeStyle(Color(0xFFF8E4DE), Color(0xFF7A2E1E)),
    GradeStyle(Color(0xFFE5EAF6), Color(0xFF27417E)),
    GradeStyle(Color(0xFFEDE6F6), Color(0xFF4F2E78)),
    GradeStyle(Color(0xFFE0F2F4), Color(0xFF0E5458)),
    GradeStyle(Color(0xFFF1E3D6), Color(0xFF70401C))
)

private fun gradeStyleIndex(grade: String): Int {
    if (grade.isEmpty()) return 0
    return (kotlin.math.abs(grade.hashCode()) % gradeStyles.size)
}

private fun gradeColor(grade: String): Color = gradeStyles[gradeStyleIndex(grade)].bg
private fun gradeTextColor(grade: String): Color = gradeStyles[gradeStyleIndex(grade)].fg

// ─── 骨架屏 ───────────────────────────────────────────────────────────────

/** Full-screen error state shown when the bundled poem library cannot be read. */
@Composable
fun LoadErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(onClick = onRetry) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("重新加载")
        }
    }
}

@Composable
fun PoemListSkeleton(count: Int = 6) {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(count) {
            Surface(
                modifier = Modifier.fillMaxWidth().height(92.dp),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(46.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)
                    ) {}
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            modifier = Modifier
                                .width(160.dp)
                                .height(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha),
                            shape = RoundedCornerShape(8.dp)
                        ) {}
                        Surface(
                            modifier = Modifier
                                .width(110.dp)
                                .height(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha * 0.8f),
                            shape = RoundedCornerShape(8.dp)
                        ) {}
                        Surface(
                            modifier = Modifier
                                .width(200.dp)
                                .height(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha * 0.6f),
                            shape = RoundedCornerShape(8.dp)
                        ) {}
                    }
                }
            }
        }
    }
}

// ─── 搜索 / 字母高亮 ───────────────────────────────────────────────────────

@Composable
private fun buildTitleText(text: String, pinyin: String, searchQuery: String, selectedLetter: String): AnnotatedString {
    val highlightBg = MaterialTheme.colorScheme.primaryContainer
    val highlightFg = MaterialTheme.colorScheme.onPrimaryContainer

    return buildAnnotatedString {
        append(text)

        if (searchQuery.isNotBlank()) {
            val strippedQuery = PoemFilterEngine.stripToneMarks(searchQuery)
            if (hasChineseCharacters(searchQuery)) {
                var from = 0
                while (true) {
                    val idx = text.indexOf(searchQuery, from)
                    if (idx < 0) break
                    addStyle(
                        SpanStyle(background = highlightBg, color = highlightFg),
                        idx, idx + searchQuery.length
                    )
                    from = idx + searchQuery.length
                }
            } else {
                val tokens = pinyin.split(" ").filter { it.isNotBlank() }
                tokens.forEachIndexed { i, token ->
                    if (i < text.length && PoemFilterEngine.stripToneMarks(token).contains(strippedQuery)) {
                        addStyle(
                            SpanStyle(background = highlightBg, color = highlightFg),
                            i, i + 1
                        )
                    }
                }
            }
        }

        if (selectedLetter.isNotEmpty()) {
            val firstLetter = PoemFilterEngine.stripToneMarks(pinyin)
                .firstOrNull()
                ?.uppercaseChar()
                ?.toString()
            if (firstLetter == selectedLetter && text.isNotEmpty()) {
                addStyle(
                    SpanStyle(
                        background = highlightBg,
                        color = highlightFg
                    ),
                    0, 1
                )
            }
        }
    }
}

@Composable
private fun buildHighlightedText(text: String, pinyin: String, query: String): AnnotatedString {
    if (query.isBlank()) return buildAnnotatedString { append(text) }

    val strippedQuery = PoemFilterEngine.stripToneMarks(query)
    val highlightBg = MaterialTheme.colorScheme.primaryContainer
    val highlightFg = MaterialTheme.colorScheme.onPrimaryContainer

    return buildAnnotatedString {
        append(text)

        if (hasChineseCharacters(query)) {
            var from = 0
            while (true) {
                val idx = text.indexOf(query, from)
                if (idx < 0) break
                addStyle(
                    SpanStyle(background = highlightBg, color = highlightFg),
                    idx, idx + query.length
                )
                from = idx + query.length
            }
        } else {
            val tokens = pinyin.split(" ").filter { it.isNotBlank() }
            tokens.forEachIndexed { i, token ->
                if (i < text.length && PoemFilterEngine.stripToneMarks(token).contains(strippedQuery)) {
                    addStyle(
                        SpanStyle(background = highlightBg, color = highlightFg),
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
