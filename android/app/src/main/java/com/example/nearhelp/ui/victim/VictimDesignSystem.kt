package com.example.nearhelp.ui.victim

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nearhelp.theme.StatusLiveRed
import com.example.nearhelp.theme.StatusSafeGreen
import com.example.nearhelp.theme.VictimBackground
import com.example.nearhelp.theme.VictimBorder
import com.example.nearhelp.theme.VictimDivider
import com.example.nearhelp.theme.VictimPinkBorder
import com.example.nearhelp.theme.VictimPinkCard
import com.example.nearhelp.theme.VictimPrimary
import com.example.nearhelp.theme.VictimTextDark
import com.example.nearhelp.theme.VictimTextMuted

object VictimShapes {
    val Card16 = RoundedCornerShape(16.dp)
    val Card20 = RoundedCornerShape(20.dp)
    val Card24 = RoundedCornerShape(24.dp)
    val Pill = RoundedCornerShape(100.dp)
}

@Composable
fun NearHelpWordmark(
    fontSize: Int = 30,
    modifier: Modifier = Modifier,
) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = VictimTextDark)) { append("Near") }
            withStyle(SpanStyle(color = VictimPrimary)) { append("Help") }
        },
        style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.Black,
            fontSize = fontSize.sp,
            letterSpacing = (-0.5).sp,
        ),
        modifier = modifier,
    )
}

@Composable
fun NearHelpLogoMark(
    size: Int = 56,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size.dp),
    ) {
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = "NearHelp heart",
            tint = VictimPrimary,
            modifier = Modifier.size(size.dp),
        )
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size((size * 0.5).dp),
        )
    }
}

@Composable
fun NearHelpBrandHeader(
    tagline: String = "Connect. Respond. Save time.",
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        NearHelpLogoMark(size = 72)
        Spacer(modifier = Modifier.height(8.dp))
        NearHelpWordmark(fontSize = 38)
        Text(
            text = tagline,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp),
            color = VictimTextMuted,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun VictimPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = VictimPrimary,
            contentColor = Color.White,
            disabledContainerColor = VictimBorder,
            disabledContentColor = VictimTextMuted,
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
            ),
        )
    }
}

@Composable
fun VictimOutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.5.dp, VictimPrimary.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(vertical = 15.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = VictimPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
        )
    }
}

@Composable
fun VictimLocationCard(
    locationTitle: String = "Kolkata, West Bengal",
    accuracyText: String = "Accuracy: ±12 m • Updated now",
    actionLabel: String = "Update",
    onAction: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(VictimShapes.Card20)
            .background(Color.White)
            .border(1.dp, VictimBorder, VictimShapes.Card20)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Location",
            tint = VictimPrimary,
            modifier = Modifier.size(28.dp),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Your location", fontSize = 12.sp, color = VictimTextMuted)
            Text(
                text = locationTitle,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = VictimTextDark,
            )
            Text(text = accuracyText, fontSize = 12.sp, color = VictimTextMuted)
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFF8FAFC))
                .border(1.dp, VictimBorder, RoundedCornerShape(12.dp))
                .clickable { onAction() }
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = null,
                    tint = VictimTextDark,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = actionLabel, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = VictimTextDark)
            }
        }
    }
}

@Composable
fun StatusBadge(
    text: String,
    live: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val bg = if (live) VictimPinkCard else Color(0xFFECFDF5)
    val dot = if (live) StatusLiveRed else StatusSafeGreen
    val fg = if (live) VictimPrimary else StatusSafeGreen
    Row(
        modifier = modifier
            .clip(VictimShapes.Pill)
            .background(bg)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(9.dp).background(dot, CircleShape))
        Spacer(modifier = Modifier.width(7.dp))
        Text(text = text, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = fg)
    }
}

@Composable
fun PastelInfoCard(
    cardBg: Color,
    cardBorder: Color,
    title: String,
    subtitle: String,
    titleColor: Color = VictimTextDark,
    icon: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val clickableMod = if (onClick != null) modifier.clickable { onClick() } else modifier
    Row(
        modifier = clickableMod
            .fillMaxWidth()
            .clip(VictimShapes.Card20)
            .background(cardBg)
            .border(1.dp, cardBorder, VictimShapes.Card20)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center,
            ) { icon() }
            Spacer(modifier = Modifier.width(12.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = titleColor)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 13.sp, color = VictimTextMuted, lineHeight = 18.sp)
        }
        if (trailing != null) {
            Spacer(modifier = Modifier.width(8.dp))
            trailing()
        } else if (onClick != null) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = VictimPrimary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = VictimTextDark)
        if (actionLabel != null) {
            Text(
                text = "$actionLabel →",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = VictimPrimary,
                modifier = if (onAction != null) Modifier.clickable { onAction() } else Modifier,
            )
        }
    }
}

enum class VictimNavTab { HOME, CHAT, MAP, PROFILE }

@Composable
fun VictimBottomNavBar(
    selected: VictimNavTab,
    onSelect: (VictimNavTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = VictimBackground,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 22.dp, start = 8.dp, end = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            VictimNavItem(
                label = "Home",
                icon = Icons.Default.Home,
                isSelected = selected == VictimNavTab.HOME,
                onClick = { onSelect(VictimNavTab.HOME) },
            )
            VictimNavItem(
                label = "Chat",
                icon = Icons.Default.ChatBubbleOutline,
                isSelected = selected == VictimNavTab.CHAT,
                onClick = { onSelect(VictimNavTab.CHAT) },
            )
            VictimNavItem(
                label = "Map",
                icon = Icons.Default.Map,
                isSelected = selected == VictimNavTab.MAP,
                onClick = { onSelect(VictimNavTab.MAP) },
            )
            VictimNavItem(
                label = "Profile",
                icon = Icons.Default.Person,
                isSelected = selected == VictimNavTab.PROFILE,
                onClick = { onSelect(VictimNavTab.PROFILE) },
            )
        }
    }
}

@Composable
private fun VictimNavItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }.padding(horizontal = 14.dp, vertical = 4.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) VictimPrimary else VictimTextMuted,
            modifier = Modifier.size(25.dp),
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) VictimPrimary else VictimTextMuted,
        )
    }
}

@Composable
fun MapPlaceholder(
    modifier: Modifier = Modifier,
    content: @Composable (() -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(VictimShapes.Card20)
            .background(Color(0xFFE8EEF3))
            .border(1.dp, VictimBorder, VictimShapes.Card20),
        contentAlignment = Alignment.Center,
    ) {
        // Faint grid streets suggestion
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = "Kolkata • Howrah Bridge", fontSize = 12.sp, color = VictimTextMuted)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Victoria Memorial", fontSize = 12.sp, color = VictimTextMuted)
        }
        if (content != null) content()
    }
}

@Composable
fun VictimDividerLine(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(VictimDivider.copy(alpha = 0.6f)),
    )
}
