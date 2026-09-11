package com.example.nearhelp.ui.auth.screens

import androidx.compose.animation.core.Animatable
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nearhelp.theme.VictimBackground
import com.example.nearhelp.theme.VictimBackgroundSoft
import com.example.nearhelp.theme.VictimPinkCard
import com.example.nearhelp.theme.VictimPrimary
import com.example.nearhelp.ui.auth.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: AuthViewModel,
    modifier: Modifier = Modifier,
) {
    val logoScale = remember { Animatable(0.7f) }
    val logoAlpha = remember { Animatable(0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "SplashRadarPulse")
    val pulseRingScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "SplashPulseRing",
    )

    LaunchedEffect(Unit) {
        logoAlpha.animateTo(1f, animationSpec = tween(600))
        logoScale.animateTo(1f, animationSpec = tween(600, easing = FastOutSlowInEasing))

        delay(1400)
        if (viewModel.checkExistingSession()) {
            onNavigateToHome()
        } else {
            onNavigateToLogin()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        VictimBackground,
                        VictimBackgroundSoft,
                    )
                )
            ),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .scale(logoScale.value)
                .alpha(logoAlpha.value)
                .padding(24.dp),
        ) {
            // Victim mockup branding: red heart with white medical cross.
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(150.dp),
            ) {
                // Outer pulsing ring
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .scale(pulseRingScale)
                        .background(VictimPinkCard, CircleShape)
                )

                // Mid glow ring
                Box(
                    modifier = Modifier
                        .size(116.dp)
                        .background(Color.White.copy(alpha = 0.5f), CircleShape)
                        .border(1.5.dp, VictimPrimary.copy(alpha = 0.25f), CircleShape)
                )

                // Center Heart Logo
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(96.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "NearHelp Logo",
                        tint = VictimPrimary,
                        modifier = Modifier.size(96.dp),
                    )
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // App Title: Near (dark) + Help (red), per victim mockups.
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
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 44.sp,
                    letterSpacing = (-0.5).sp,
                ),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Tagline per victim mockups.
            Text(
                text = "Connect. Respond. Save time.",
                fontFamily = FontFamily.SansSerif,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                ),
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Protection Badge
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.85f))
                    .border(1.dp, Color(0xFF10B981).copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.LocalHospital,
                    contentDescription = "Good Samaritan",
                    tint = Color(0xFF059669),
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Good Samaritan Law Protected",
                    fontFamily = FontFamily.SansSerif,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                    ),
                    color = Color(0xFF065F46),
                )
            }
        }

        // Bottom Version Footer
        Text(
            text = "NearHelp AI v1.0 · Calm Guardian & Crisis Dispatch",
            fontFamily = FontFamily.SansSerif,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
            ),
            color = Color(0xFF64748B),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
        )
    }
}


