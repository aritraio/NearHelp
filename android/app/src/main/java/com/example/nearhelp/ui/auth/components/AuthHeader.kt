package com.example.nearhelp.ui.auth.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nearhelp.theme.VictimPrimary

@Composable
fun AuthHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    showLogo: Boolean = true,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "AuthHeaderPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "AuthLogoPulseScale",
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (showLogo) {
            // Victim mockup branding: red heart with white medical cross.
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(92.dp),
            ) {
                // Outer Ambient Soft Glow
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .scale(pulseScale)
                        .background(Color(0x1FE52538), CircleShape)
                )

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(72.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "NearHelp Heart Logo",
                        tint = VictimPrimary,
                        modifier = Modifier.size(72.dp),
                    )
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Victim mockup wordmark: Near (dark) + Help (red).
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = Color(0xFF0F172A))) {
                            append("Near")
                        }
                        withStyle(SpanStyle(color = VictimPrimary)) {
                            append("Help")
                        }
                    },
                    fontFamily = FontFamily.SansSerif,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 34.sp,
                        letterSpacing = (-0.5).sp,
                    ),
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
        }

        // When branding logo is shown with the default titles, the wordmark above
        // already displays "NearHelp" — skip duplicate title (no logic change).
        val isDefaultBrandTitle = title == "NearHelp" || title == "Welcome to NearHelp"
        if (!isDefaultBrandTitle) {
            Text(
                text = title,
                fontFamily = FontFamily.SansSerif,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    letterSpacing = (-0.3).sp,
                ),
                color = Color(0xFF0F172A),
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = subtitle,
            fontFamily = FontFamily.SansSerif,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Normal,
                fontSize = 13.5.sp,
                lineHeight = 19.sp,
            ),
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center,
        )
    }
}


