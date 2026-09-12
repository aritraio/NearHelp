package com.example.nearhelp.ui.map

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nearhelp.theme.VictimBackground
import com.example.nearhelp.theme.VictimBlueBorder
import com.example.nearhelp.theme.VictimBlueCard
import com.example.nearhelp.theme.VictimBorder
import com.example.nearhelp.theme.VictimGreenBorder
import com.example.nearhelp.theme.VictimGreenCard
import com.example.nearhelp.theme.VictimOrangeBorder
import com.example.nearhelp.theme.VictimOrangeCard
import com.example.nearhelp.theme.VictimPinkBorder
import com.example.nearhelp.theme.VictimPinkCard
import com.example.nearhelp.theme.VictimPrimary
import com.example.nearhelp.theme.VictimTextDark
import com.example.nearhelp.theme.VictimTextMuted
import com.example.nearhelp.ui.victim.MapPlaceholder
import com.example.nearhelp.ui.victim.SectionHeader
import com.example.nearhelp.ui.victim.VictimBottomNavBar
import com.example.nearhelp.ui.victim.VictimNavTab
import com.example.nearhelp.ui.victim.VictimShapes

/**
 * Victim Nearby Help — light redesign (mockup Nearby Help):
 * search, map with hospital pins, category chips, hospital list,
 * immediate-help banner. Preserves ViewModel contract.
 */
@Composable
fun CommunityGeoMapScreen(
  onNavigateBack: () -> Unit,
  onNavigateToHome: () -> Unit = onNavigateBack,
  onNavigateToAssistant: () -> Unit = {},
  onNavigateToProfile: () -> Unit = {},
  onNavigateToTracking: () -> Unit = {},
  onNavigateToNavigation: () -> Unit = {},
  modifier: Modifier = Modifier,
  viewModel: CommunityGeoMapViewModel = viewModel(),
  showBottomBar: Boolean = true,
) {
  val uiState by viewModel.uiState.collectAsState()
  var query by remember { mutableStateOf("") }
  var selectedChip by remember { mutableStateOf("Hospitals") }

  val hospitals = uiState.incident.hospitals.map {
    Triple(it.name.ifBlank { "Hospital" }, "${it.distanceKm} km • Open 24 hours", it.bedAvailability)
  }.ifEmpty {
    listOf(
      Triple("AMRI Hospital", "1.8 km • Open 24 hours", 38),
      Triple("Apollo Hospital", "3.2 km • Open 24 hours", 42),
      Triple("Fortis Hospital", "4.6 km • Open 24 hours", 51),
    )
  }.filter { query.isBlank() || it.first.contains(query, ignoreCase = true) }

  Column(modifier = modifier.fillMaxSize().background(VictimBackground).statusBarsPadding()) {
    Column(
      modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 10.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      // Brand row
      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.size(34.dp).clip(CircleShape).background(VictimPrimary), contentAlignment = Alignment.Center) {
          Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Row(modifier = Modifier.weight(1f)) {
          Text(text = "Near", fontWeight = FontWeight.Black, fontSize = 21.sp, color = VictimTextDark)
          Text(text = "Help", fontWeight = FontWeight.Black, fontSize = 21.sp, color = VictimPrimary)
        }
        Box(modifier = Modifier.size(38.dp).clip(CircleShape).background(VictimPinkCard).clickable { onNavigateBack() }, contentAlignment = Alignment.Center) {
          Icon(imageVector = Icons.Default.Person, contentDescription = "Profile", tint = VictimTextDark, modifier = Modifier.size(19.dp))
        }
      }

      Column {
        Text(text = "Nearby Help", fontSize = 29.sp, fontWeight = FontWeight.Black, color = VictimTextDark)
        Text(text = "Find hospitals and emergency support around you.", fontSize = 14.sp, color = VictimTextMuted)
      }

      OutlinedTextField(
        value = query, onValueChange = { query = it },
        placeholder = { Text("Search hospitals, clinics, first aid...", fontSize = 14.sp, color = VictimTextMuted) },
        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = VictimTextDark) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = VictimBorder, unfocusedBorderColor = VictimBorder,
          focusedContainerColor = Color(0xFFF8FAFC), unfocusedContainerColor = Color(0xFFF8FAFC),
          focusedTextColor = VictimTextDark, unfocusedTextColor = VictimTextDark,
        ),
      )

      MapPlaceholder(modifier = Modifier.height(210.dp)) {
        Box(modifier = Modifier.fillMaxSize()) {
          // You dot
          Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(52.dp).clip(CircleShape).background(Color(0xFF2563EB).copy(alpha = 0.18f)), contentAlignment = Alignment.Center) {
              Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(Color(0xFF2563EB)).border(3.dp, Color.White, CircleShape))
            }
            Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color.White).border(1.dp, VictimBorder, RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 3.dp)) {
              Text(text = "You", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VictimTextDark)
            }
          }
          HospitalPin(label = "AMRI Hospital", dist = "1.8 km", modifier = Modifier.align(Alignment.TopStart).padding(start = 24.dp, top = 26.dp))
          HospitalPin(label = "Apollo Hospital", dist = "3.2 km", modifier = Modifier.align(Alignment.BottomStart).padding(start = 18.dp, bottom = 26.dp))
          HospitalPin(label = "Fortis Hospital", dist = "4.6 km", modifier = Modifier.align(Alignment.CenterEnd).padding(end = 14.dp))
          Box(modifier = Modifier.align(Alignment.TopEnd).padding(10.dp).size(42.dp).clip(CircleShape).background(Color.White).border(1.dp, VictimBorder, CircleShape), contentAlignment = Alignment.Center) {
            Icon(imageVector = Icons.Default.MyLocation, contentDescription = null, tint = VictimTextDark, modifier = Modifier.size(20.dp))
          }
        }
      }

      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MapChip(label = "Hospitals", bg = VictimPinkCard, border = VictimPinkBorder, icon = Icons.Default.LocalHospital, selected = selectedChip == "Hospitals", onClick = { selectedChip = "Hospitals" }, modifier = Modifier.weight(1f))
        MapChip(label = "First Aid Centres", bg = VictimBlueCard, border = VictimBlueBorder, icon = Icons.Default.Add, selected = selectedChip == "First Aid Centres", onClick = { selectedChip = "First Aid Centres" }, modifier = Modifier.weight(1f))
        MapChip(label = "Pharmacies", bg = VictimGreenCard, border = VictimGreenBorder, icon = Icons.Default.LocalHospital, selected = selectedChip == "Pharmacies", onClick = { selectedChip = "Pharmacies" }, modifier = Modifier.weight(1f))
        MapChip(label = "Other Support", bg = VictimOrangeCard, border = VictimOrangeBorder, icon = Icons.Default.LocationOn, selected = selectedChip == "Other Support", onClick = { selectedChip = "Other Support" }, modifier = Modifier.weight(1f))
      }

      SectionHeader(title = "Nearby Hospitals", actionLabel = "View all", onAction = {})
      hospitals.forEach { (name, meta, _) ->
        Row(
          modifier = Modifier.fillMaxWidth().clip(VictimShapes.Card16).background(Color.White)
            .border(1.dp, VictimBorder, VictimShapes.Card16).padding(12.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFE8EEF3)), contentAlignment = Alignment.Center) {
            Icon(imageVector = Icons.Default.LocalHospital, contentDescription = null, tint = VictimTextMuted, modifier = Modifier.size(28.dp))
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(text = name, fontSize = 15.5.sp, fontWeight = FontWeight.Bold, color = VictimTextDark)
            Row {
              Text(text = meta.substringBefore(" •"), fontSize = 13.sp, color = VictimTextMuted)
              Text(text = " • ", fontSize = 13.sp, color = VictimTextMuted)
              Text(text = "Open 24 hours", fontSize = 13.sp, color = Color(0xFF22C55E), fontWeight = FontWeight.SemiBold)
            }
          }
          Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onNavigateToNavigation() }) {
            Box(modifier = Modifier.size(38.dp).clip(CircleShape).background(VictimBlueCard), contentAlignment = Alignment.Center) {
              Icon(imageVector = Icons.Default.Navigation, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(18.dp))
            }
            Text(text = "Directions", fontSize = 11.sp, color = VictimTextMuted)
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(38.dp).clip(CircleShape).background(VictimBlueCard), contentAlignment = Alignment.Center) {
              Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(18.dp))
            }
            Text(text = "Call", fontSize = 11.sp, color = VictimTextMuted)
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth().clip(VictimShapes.Card16).background(VictimBlueCard)
          .border(1.dp, VictimBlueBorder, VictimShapes.Card16).padding(14.dp).clickable { onNavigateToTracking() },
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.8f)), contentAlignment = Alignment.Center) {
          Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(22.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(text = "Need immediate help?", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = VictimTextDark)
          Text(text = "Use the SOS button from Home.", fontSize = 13.sp, color = VictimTextMuted)
        }
        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color(0xFF2563EB))
      }
      Spacer(modifier = Modifier.height(4.dp))
    }
    if (showBottomBar) {
      VictimBottomNavBar(selected = VictimNavTab.MAP, onSelect = {
        when (it) {
          VictimNavTab.HOME -> onNavigateToHome()
          VictimNavTab.CHAT -> onNavigateToAssistant()
          VictimNavTab.MAP -> Unit
          VictimNavTab.PROFILE -> onNavigateToProfile()
        }
      })
    }
  }
}

@Composable
private fun HospitalPin(label: String, dist: String, modifier: Modifier = Modifier) {
  Row(modifier = modifier.clip(RoundedCornerShape(12.dp)).background(Color.White).border(1.dp, VictimBorder, RoundedCornerShape(12.dp)).padding(horizontal = 8.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
    Box(modifier = Modifier.size(22.dp).clip(CircleShape).background(VictimPrimary), contentAlignment = Alignment.Center) {
      Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
    }
    Spacer(modifier = Modifier.width(6.dp))
    Column {
      Text(text = label, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = VictimTextDark)
      Text(text = dist, fontSize = 11.sp, color = VictimTextMuted)
    }
  }
}

@Composable
private fun MapChip(label: String, bg: Color, border: Color, icon: ImageVector, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
  Row(
    modifier = modifier.clip(RoundedCornerShape(14.dp)).background(if (selected) bg else Color.White)
      .border(1.dp, if (selected) border else VictimBorder, RoundedCornerShape(14.dp))
      .clickable { onClick() }.padding(horizontal = 8.dp, vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(imageVector = icon, contentDescription = null, tint = VictimPrimary, modifier = Modifier.size(20.dp))
    Spacer(modifier = Modifier.width(6.dp))
    Text(text = label, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = VictimTextDark, lineHeight = 14.sp)
  }
}
