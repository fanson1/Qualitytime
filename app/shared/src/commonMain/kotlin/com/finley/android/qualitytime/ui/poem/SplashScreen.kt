package com.finley.android.qualitytime.ui.poem

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finley.android.qualitytime.ui.theme.PoemFont
import com.finley.android.qualitytime.ui.theme.QtyColors
import org.jetbrains.compose.resources.painterResource
import qualitytime.app.shared.generated.resources.Res
import qualitytime.app.shared.generated.resources.ic_logo

@Composable
fun SplashScreen(onComplete: () -> Unit = {}) {
    val infiniteTransition = rememberInfiniteTransition(label = "splash")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    val taglineAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tagline"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val background = MaterialTheme.colorScheme.background
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        background,
                        primaryContainer.copy(alpha = 0.55f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = glowAlpha),
                        Color.Transparent
                    ),
                    center = center,
                    radius = size.minDimension * 0.65f
                )
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(Res.drawable.ic_logo),
                contentDescription = "AI 宝宝 Logo",
                modifier = Modifier
                    .size(132.dp)
                    .scale(scale)
            )
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = "AI 宝宝",
                style = MaterialTheme.typography.headlineLarge,
                fontFamily = PoemFont,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 6.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "每天十分钟，陪宝宝一起成长",
                style = MaterialTheme.typography.bodyMedium,
                color = onSurfaceVariant,
                letterSpacing = 2.sp,
                modifier = Modifier.alpha(taglineAlpha)
            )
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 48.dp)
                .width(120.dp)
                .height(3.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp)
                )
        ) {
            val progress by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1400),
                    repeatMode = RepeatMode.Restart
                ),
                label = "progress"
            )
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width * progress
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        listOf(QtyColors.JadeLight, primaryColor)
                    ),
                    topLeft = Offset.Zero,
                    size = androidx.compose.ui.geometry.Size(width, size.height),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2)
                )
            }
        }
    }
}
