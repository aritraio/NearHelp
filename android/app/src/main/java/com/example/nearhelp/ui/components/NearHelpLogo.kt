package com.example.nearhelp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nearhelp.R
import com.example.nearhelp.theme.NearHelpTheme

/** The same CarePin geometry is used by launcher, themed icons, notifications and the app. */
@Composable
fun NearHelpSymbol(modifier: Modifier = Modifier, size: Dp = 48.dp) {
    Box(modifier.size(size).clip(RoundedCornerShape(size * .29f)).background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) {
        Icon(painterResource(R.drawable.ic_nearhelp_logo), "NearHelp", Modifier.fillMaxSize().padding(size * .06f), tint = MaterialTheme.colorScheme.onPrimary)
    }
}

@Composable
fun NearHelpWordmarkText(fontSize: Int = 30, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Text(buildAnnotatedString {
        withStyle(SpanStyle(color = colors.onBackground)) { append("Near") }
        withStyle(SpanStyle(color = colors.primary)) { append("Help") }
    }, modifier, style = MaterialTheme.typography.headlineMedium.copy(fontSize = fontSize.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-.5).sp))
}

@Composable
fun NearHelpBrandHeader(modifier: Modifier = Modifier, showSubtitle: Boolean = true, subtitleText: String = "Connect. Respond. Save time.") {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        NearHelpSymbol(size = 72.dp)
        NearHelpWordmarkText(fontSize = 38)
        if (showSubtitle) Text(subtitleText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun NearHelpTopAppBarLogo(modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        NearHelpSymbol(size = 28.dp)
        NearHelpWordmarkText(fontSize = 21)
    }
}

@Preview(showBackground = true)
@Composable
private fun NearHelpBrandPreview() { NearHelpTheme { NearHelpBrandHeader(Modifier.padding(24.dp)) } }
