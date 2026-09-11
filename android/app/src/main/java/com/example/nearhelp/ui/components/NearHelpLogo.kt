package com.example.nearhelp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Design tokens from ui/demo-ui-1st/AppLogo master asset.
private val LogoRed = Color(0xFFE52538)
private val LogoNavy = Color(0xFF0F172A)
private val LogoMuted = Color(0xFF64748B)
private val LogoWhite = Color(0xFFFFFFFF)
private val LogoGloss = Color(0x2EFFFFFF)

/**
 * Heart + Cross geometry in a 108 x 108 coordinate space, matching
 * ic_launcher_foreground.xml / ic_nearhelp_logo.xml. The heart spans
 * x=27..81, y=26..78 (centered on ~54,52, inside the 66dp safe zone);
 * the cross is centered at (54,51) with softly rounded corners.
 */
private fun heartPath(): Path = Path().apply {
    moveTo(54f, 78f)
    cubicTo(40f, 66f, 27f, 55f, 27f, 42f)
    cubicTo(27f, 32f, 34f, 26f, 42.5f, 26f)
    cubicTo(47.5f, 26f, 51.5f, 28.8f, 54f, 32.5f)
    cubicTo(56.5f, 28.8f, 60.5f, 26f, 65.5f, 26f)
    cubicTo(74f, 26f, 81f, 32f, 81f, 42f)
    cubicTo(81f, 55f, 68f, 66f, 54f, 78f)
    close()
}

private fun glossPath(): Path = Path().apply {
    moveTo(54f, 32.5f)
    lineTo(35.5f, 56.5f)
    lineTo(33.8f, 53.5f)
    cubicTo(32.6f, 50f, 32.2f, 46.2f, 32.8f, 42.4f)
    cubicTo(33.8f, 34.6f, 38.4f, 28.6f, 45.2f, 26.9f)
    close()
}

private fun DrawScope.drawHeartMark() {
    withTransform({ scale(scaleX = size.minDimension / 108f, scaleY = size.minDimension / 108f) }) {
        drawPath(path = heartPath(), color = LogoRed)
        drawPath(path = glossPath(), color = LogoGloss)
        // Medical cross: straight bars with ~2.8-unit rounded corners.
        drawRoundRect(
            color = LogoWhite,
            topLeft = Offset(49f, 40f),
            size = androidx.compose.ui.geometry.Size(10f, 22f),
            cornerRadius = CornerRadius(2.8f, 2.8f),
        )
        drawRoundRect(
            color = LogoWhite,
            topLeft = Offset(43f, 46f),
            size = androidx.compose.ui.geometry.Size(22f, 10f),
            cornerRadius = CornerRadius(2.8f, 2.8f),
        )
    }
}

@Composable
fun NearHelpWordmarkText(fontSize: Int = 30) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = LogoNavy)) { append("Near") }
            withStyle(SpanStyle(color = LogoRed)) { append("Help") }
        },
        style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.Black,
            fontSize = fontSize.sp,
            letterSpacing = (-0.5).sp,
        ),
        textAlign = TextAlign.Center,
    )
}

/**
 * Renders the heart with centered cross.
 */
@Composable
fun NearHelpSymbol(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    Canvas(modifier = modifier.size(size)) {
        drawHeartMark()
    }
}

/**
 * Renders the stacked:
 * 1. Heart + Cross symbol (centered)
 * 2. "NearHelp" text ("Near" in #0F172A, "Help" in #E52538)
 * 3. Optional subtitle in muted gray (#64748B)
 */
@Composable
fun NearHelpBrandHeader(
    modifier: Modifier = Modifier,
    showSubtitle: Boolean = true,
    subtitleText: String = "Connect. Respond. Save time."
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        NearHelpSymbol(size = 72.dp)
        Spacer(modifier = Modifier.height(8.dp))
        NearHelpWordmarkText(fontSize = 38)
        if (showSubtitle) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitleText,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
                color = LogoMuted,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * Compact horizontal lockup: Small Heart (28dp) + "NearHelp" text for Top AppBars.
 */
@Composable
fun NearHelpTopAppBarLogo(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NearHelpSymbol(size = 28.dp)
        Spacer(modifier = Modifier.width(8.dp))
        NearHelpWordmarkText(fontSize = 21)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun NearHelpSymbolPreview() {
    NearHelpSymbol(size = 96.dp)
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun NearHelpBrandHeaderPreview() {
    NearHelpBrandHeader()
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun NearHelpTopAppBarLogoPreview() {
    NearHelpTopAppBarLogo()
}
