package com.finley.android.qualitytime.ui.poem

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finley.android.qualitytime.model.Poem
import com.finley.android.qualitytime.ui.theme.PoemFont
import com.finley.android.qualitytime.ui.theme.QtyColors

@Composable
fun HomeHero(
    poem: Poem,
    poemCount: Int,
    authorCount: Int,
    dynastyCount: Int,
    onPoemClick: (Poem) -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(120, easing = EaseOutCubic),
        label = "hero"
    )

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(QtyColors.JadeDark, QtyColors.Jade, QtyColors.JadeLight)
                    )
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = { onPoemClick(poem) }
                )
        ) {
                // 装饰性印章
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 16.dp, end = 18.dp)
                        .size(52.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp, Color.White.copy(alpha = 0.35f)
                    )
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "文",
                            fontFamily = PoemFont,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = null,
                            tint = QtyColors.StarYellow,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "今日推荐",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White.copy(alpha = 0.92f),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = poem.dynasty,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = poem.title,
                        fontFamily = PoemFont,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 4.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = poem.author,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f),
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val preview = poem.content.split("\n").firstOrNull { it.isNotBlank() } ?: ""
                    Text(
                        text = preview,
                        fontFamily = PoemFont,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.75f),
                        letterSpacing = 2.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            modifier = Modifier.height(40.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 18.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.PlayArrow,
                                    contentDescription = null,
                                    tint = QtyColors.JadeDark,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "立即朗读",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = QtyColors.JadeDark
                                )
                            }
                        }
                        Text(
                            text = "查看详情",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

        Spacer(modifier = Modifier.height(16.dp))

        HeroStats(poemCount, authorCount, dynastyCount)
    }
}

@Composable
private fun HeroStats(poemCount: Int, authorCount: Int, dynastyCount: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Row(modifier = Modifier.padding(vertical = 16.dp)) {
            StatItem(
                value = poemCount.toString(),
                label = "首古诗",
                modifier = Modifier.weight(1f)
            )
            VerticalDivider(
                modifier = Modifier.height(30.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            StatItem(
                value = authorCount.toString(),
                label = "位诗人",
                modifier = Modifier.weight(1f)
            )
            VerticalDivider(
                modifier = Modifier.height(30.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            StatItem(
                value = dynastyCount.toString(),
                label = "个朝代",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatItem(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.AutoStories,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Black
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
