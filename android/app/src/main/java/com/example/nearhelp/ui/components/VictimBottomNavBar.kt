package com.example.nearhelp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nearhelp.i18n.LocalizedText as Text

enum class VictimNavTab { HOME, CHAT, MAP, PROFILE }

@Composable
fun VictimBottomNavBar(selected: VictimNavTab, onSelect: (VictimNavTab) -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Column(modifier.fillMaxWidth().background(colors.background)) {
        HorizontalDivider(color = colors.outlineVariant)
        Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(vertical = 9.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            VictimNavTab.entries.forEach { tab ->
                val active = selected == tab
                val label = when (tab) {
                    VictimNavTab.HOME -> "Home"
                    VictimNavTab.CHAT -> "Assistant"
                    VictimNavTab.MAP -> "Nearby"
                    VictimNavTab.PROFILE -> "Profile"
                }
                val icon = when (tab) {
                    VictimNavTab.HOME -> Icons.Default.Home
                    VictimNavTab.CHAT -> Icons.Default.ChatBubbleOutline
                    VictimNavTab.MAP -> Icons.Default.Map
                    VictimNavTab.PROFILE -> Icons.Default.Person
                }
                Column(
                    Modifier.weight(1f).heightIn(min = 56.dp).selectable(active, role = Role.Tab, onClick = { onSelect(tab) }).padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Box(Modifier.clip(RoundedCornerShape(6.dp)).background(if (active) colors.surfaceVariant else colors.background).padding(horizontal = 7.dp, vertical = 3.dp)) {
                        Icon(icon, null, Modifier.size(23.dp), tint = if (active) colors.primary else colors.onSurfaceVariant)
                    }
                    Text(label, fontSize = 12.sp, fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal, color = if (active) colors.primary else colors.onSurfaceVariant)
                }
            }
        }
    }
}
