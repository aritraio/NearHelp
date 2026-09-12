package com.example.nearhelp.ui.profile

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nearhelp.data.api.models.EmergencyContact
import com.example.nearhelp.theme.StatusSafeGreen
import com.example.nearhelp.theme.VictimBackground
import com.example.nearhelp.theme.VictimBlueBorder
import com.example.nearhelp.theme.VictimBlueCard
import com.example.nearhelp.theme.VictimBorder
import com.example.nearhelp.theme.VictimGreenBorder
import com.example.nearhelp.theme.VictimGreenCard
import com.example.nearhelp.theme.VictimPinkBorder
import com.example.nearhelp.theme.VictimPinkCard
import com.example.nearhelp.theme.VictimPrimary
import com.example.nearhelp.theme.VictimPurpleBorder
import com.example.nearhelp.theme.VictimPurpleCard
import com.example.nearhelp.theme.VictimTextDark
import com.example.nearhelp.theme.VictimTextMuted
import com.example.nearhelp.ui.victim.VictimBottomNavBar
import com.example.nearhelp.ui.victim.VictimNavTab
import com.example.nearhelp.ui.victim.VictimShapes

/**
 * Victim Profile — light redesign (mockup 10_59_25):
 * NearHelp + gear header, avatar + Verified + Trust Score,
 * Received/Helped pastel stats, grouped white setting rows.
 * Preserves ProfileViewModel contract + dialogs.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
  onNavigateBack: () -> Unit,
  onNavigateToHome: () -> Unit = onNavigateBack,
  onNavigateToAssistant: () -> Unit = {},
  onNavigateToMap: () -> Unit = {},
  viewModel: ProfileViewModel,
  modifier: Modifier = Modifier,
  showBottomBar: Boolean = true,
) {
  val uiState by viewModel.uiState.collectAsState()
  val context = LocalContext.current

  LaunchedEffect(uiState.error) {
    uiState.error?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show(); viewModel.clearError() }
  }
  LaunchedEffect(uiState.successMessage) {
    uiState.successMessage?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show(); viewModel.clearSuccessMessage() }
  }

  val user = uiState.user
  val displayName = user?.name?.ifBlank { "Aritra" } ?: "Aritra"
  val trustScore = (user?.trustScore ?: 84.0).toInt()

  Column(modifier = modifier.fillMaxSize().background(VictimBackground).statusBarsPadding()) {
    Column(
      modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 10.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      // Header: brand + gear
      Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onNavigateBack, modifier = Modifier.size(36.dp)) {
          Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = VictimTextDark, modifier = Modifier.size(20.dp))
        }
        Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(VictimPrimary), contentAlignment = Alignment.Center) {
          Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp))
        }
        Spacer(modifier = Modifier.width(7.dp))
        Row(modifier = Modifier.weight(1f)) {
          Text(text = "Near", fontWeight = FontWeight.Black, fontSize = 20.sp, color = VictimTextDark)
          Text(text = "Help", fontWeight = FontWeight.Black, fontSize = 20.sp, color = VictimPrimary)
        }
        if (uiState.isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = VictimPrimary, strokeWidth = 2.dp)
        else IconButton(onClick = { viewModel.loadProfile() }, modifier = Modifier.size(38.dp)) {
          Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = VictimTextDark, modifier = Modifier.size(22.dp))
        }
      }

      // Identity row
      Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Box {
          Box(modifier = Modifier.size(78.dp).clip(CircleShape).background(Color(0xFFE8EDF3)), contentAlignment = Alignment.Center) {
            Text(text = displayName.take(1).uppercase(), fontSize = 30.sp, fontWeight = FontWeight.Bold, color = VictimTextDark)
          }
          Box(
            modifier = Modifier.align(Alignment.BottomEnd).size(26.dp).clip(CircleShape).background(Color.White)
              .border(1.dp, VictimBorder, CircleShape).clickable { viewModel.openEditProfileDialog() },
            contentAlignment = Alignment.Center,
          ) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = VictimTextDark, modifier = Modifier.size(13.dp))
          }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(text = displayName, fontSize = 24.sp, fontWeight = FontWeight.Black, color = VictimTextDark)
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "Verified Account", fontSize = 13.5.sp, color = VictimTextMuted)
            Spacer(modifier = Modifier.width(5.dp))
            Icon(imageVector = Icons.Default.Verified, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(17.dp))
          }
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = VictimTextMuted, modifier = Modifier.size(14.dp))
            Text(text = "Kolkata, West Bengal", fontSize = 13.sp, color = VictimTextMuted)
          }
        }
        // Trust score card
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(VictimPinkCard).border(1.dp, VictimPinkBorder, RoundedCornerShape(16.dp)).padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(26.dp).clip(RoundedCornerShape(8.dp)).background(VictimPrimary), contentAlignment = Alignment.Center) {
              Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "Trust Score", fontSize = 11.5.sp, color = VictimTextMuted)
          }
          Text(text = "$trustScore / 100", fontSize = 18.sp, fontWeight = FontWeight.Black, color = VictimTextDark)
        }
      }
      Text(text = "\"A safer community, together.\"", fontSize = 14.sp, color = VictimTextMuted, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())

      // Stats
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
          modifier = Modifier.weight(1f).clip(VictimShapes.Card16).background(VictimPinkCard).border(1.dp, VictimPinkBorder, VictimShapes.Card16)
            .clickable { viewModel.openEmergencyHistoryDialog() }
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.8f)), contentAlignment = Alignment.Center) {
            Icon(imageVector = Icons.Default.Favorite, contentDescription = null, tint = VictimPrimary, modifier = Modifier.size(22.dp))
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(text = "2", fontSize = 20.sp, fontWeight = FontWeight.Black, color = VictimTextDark)
            Text(text = "Received Help", fontSize = 12.5.sp, color = VictimTextMuted)
          }
          Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = VictimTextDark, modifier = Modifier.size(18.dp))
        }
        Row(
          modifier = Modifier.weight(1f).clip(VictimShapes.Card16).background(VictimBlueCard).border(1.dp, VictimBlueBorder, VictimShapes.Card16)
            .clickable { viewModel.openEmergencyHistoryDialog() }
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.8f)), contentAlignment = Alignment.Center) {
            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(22.dp))
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(text = "5", fontSize = 20.sp, fontWeight = FontWeight.Black, color = VictimTextDark)
            Text(text = "Helped Others", fontSize = 12.5.sp, color = VictimTextMuted)
          }
          Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = VictimTextDark, modifier = Modifier.size(18.dp))
        }
      }

      // Personal Information
      Text(text = "Personal Information", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = VictimTextDark)
      ProfileRow(
        iconBg = Color(0xFFF1F5F9), icon = Icons.Default.Description, iconTint = VictimPrimary,
        title = "Medical Information", subtitle = "Blood group: ${user?.bloodGroup ?: "O+"} • allergies, conditions, etc.",
        onClick = { viewModel.openEditMedicalIdDialog() },
      )
      ProfileRow(
        iconBg = VictimGreenCard, icon = Icons.Default.Call, iconTint = StatusSafeGreen,
        title = "Emergency Contacts", subtitle = "${uiState.emergencyContacts.size} / 5 • People to reach in an emergency",
        onClick = { viewModel.openAddContactDialog() },
      )

      // Inline emergency contacts
      val contacts = uiState.emergencyContacts
      if (contacts.isNotEmpty()) {
        contacts.forEach { contact ->
          EmergencyContactRowLight(contact = contact, context = context,
            onEdit = { viewModel.openEditContactDialog(contact) },
            onDelete = { contact.id?.let { viewModel.deleteEmergencyContact(it) } })
          Spacer(modifier = Modifier.height(2.dp))
        }
      } else {
        Row(
          modifier = Modifier.fillMaxWidth().clip(VictimShapes.Card16).background(Color.White).border(1.dp, VictimBorder, VictimShapes.Card16).padding(12.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(text = "No emergency contacts yet", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = VictimTextDark)
            Text(text = "Add up to 5 contacts for 1-tap SOS notifications.", fontSize = 12.5.sp, color = VictimTextMuted)
          }
          Text(text = "+ Add", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = VictimPrimary, modifier = Modifier.clickable { viewModel.openAddContactDialog() }.padding(6.dp))
        }
      }

      // Medical summary chips (blood, conditions, allergies)
      Row(
        modifier = Modifier.fillMaxWidth().clip(VictimShapes.Card16).background(Color.White).border(1.dp, VictimBorder, VictimShapes.Card16).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(text = "Blood ${user?.bloodGroup ?: "O+"} • ${(user?.medicalConditions ?: listOf("Asthma")).joinToString(", ")}", fontSize = 12.5.sp, color = VictimTextDark, fontWeight = FontWeight.SemiBold)
          Text(text = "Allergies: ${(user?.knownAllergies ?: listOf("Penicillin")).joinToString(", ")}", fontSize = 12.sp, color = VictimPrimary)
        }
        Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = VictimTextMuted, modifier = Modifier.size(18.dp).clickable { viewModel.openEditMedicalIdDialog() })
      }

      // Responder Information
      Text(text = "Responder Information", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = VictimTextDark)
      ProfileRow(
        iconBg = VictimPurpleCard,
        icon = Icons.Default.Shield,
        iconTint = Color(0xFF7C3AED),
        title = "Qualifications & Certifications",
        subtitle = "Your medical skills and certifications",
        onClick = { viewModel.openQualificationsDialog() },
      )
      ProfileRow(
        iconBg = VictimGreenCard,
        icon = Icons.Default.CheckCircle,
        iconTint = StatusSafeGreen,
        title = "Verification Status",
        subtitle = "Account verification and documents",
        onClick = { viewModel.loadProfile() },
      )

      // Activity
      Text(text = "Activity", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = VictimTextDark)
      ProfileRow(
        iconBg = VictimPinkCard,
        icon = Icons.Default.History,
        iconTint = VictimPrimary,
        title = "Emergency History",
        subtitle = "View past emergencies (received & helped)",
        onClick = { viewModel.openEmergencyHistoryDialog() },
      )
      ProfileRow(
        iconBg = VictimBlueCard,
        icon = Icons.Default.BarChart,
        iconTint = Color(0xFF2563EB),
        title = "Impact & Statistics",
        subtitle = "Your contribution to a safer community",
        onClick = { viewModel.openEmergencyHistoryDialog() },
      )

      // Languages (light chips)
      Text(text = "Spoken Languages", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = VictimTextDark)
      val currentLanguages = user?.languages ?: listOf("en")
      val available = listOf("en" to "English", "bn" to "বাংলা", "hi" to "हिन्दी", "es" to "Español")
      FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        available.forEach { (code, label) ->
          val sel = currentLanguages.contains(code)
          Box(
            modifier = Modifier.clip(RoundedCornerShape(100.dp)).background(if (sel) VictimGreenCard else Color.White)
              .border(1.dp, if (sel) VictimGreenBorder else VictimBorder, RoundedCornerShape(100.dp))
              .clickable {
                val updated = if (sel) { if (currentLanguages.size > 1) currentLanguages.filter { it != code } else currentLanguages } else currentLanguages + code
                viewModel.updateLanguages(updated)
              }.padding(horizontal = 12.dp, vertical = 7.dp),
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              if (sel) { Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = StatusSafeGreen, modifier = Modifier.size(14.dp)); Spacer(modifier = Modifier.width(4.dp)) }
              Text(text = label, fontSize = 12.5.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal, color = if (sel) VictimTextDark else VictimTextMuted)
            }
          }
        }
      }
      Spacer(modifier = Modifier.height(8.dp))
    }
    if (showBottomBar) {
      VictimBottomNavBar(
        selected = VictimNavTab.PROFILE,
        onSelect = {
          when (it) {
            VictimNavTab.HOME -> onNavigateToHome()
            VictimNavTab.CHAT -> onNavigateToAssistant()
            VictimNavTab.MAP -> onNavigateToMap()
            VictimNavTab.PROFILE -> Unit
          }
        }
      )
    }
  }

  if (uiState.showEditProfileDialog) {
    EditProfileDialogLight(userName = user?.name ?: "", userPhone = user?.phone ?: "", userBlood = user?.bloodGroup ?: "O+",
      onDismiss = { viewModel.closeEditProfileDialog() },
      onSave = { name, phone, blood -> viewModel.updateProfile(name, phone, blood, null, null, null, null, null, null) })
  }
  if (uiState.showEditMedicalIdDialog) {
    EditMedicalIdDialogLight(
      blood = user?.bloodGroup ?: "O+", pacemaker = user?.hasPacemaker ?: false, donor = user?.isOrganDonor ?: false,
      conditions = user?.medicalConditions ?: emptyList(), allergies = user?.knownAllergies ?: emptyList(), notes = user?.medicalNotes ?: "",
      onDismiss = { viewModel.closeEditMedicalIdDialog() },
      onSave = { b, c, a, p, d, n -> viewModel.updateMedicalId(b, c, a, p, d, n) })
  }
  if (uiState.showAddContactDialog) {
    AddEditContactDialogLight(contact = null, onDismiss = { viewModel.closeAddContactDialog() },
      onSave = { n, p, r, primary -> viewModel.addEmergencyContact(n, p, r, primary) })
  }
  if (uiState.editingContact != null) {
    AddEditContactDialogLight(contact = uiState.editingContact, onDismiss = { viewModel.closeEditContactDialog() },
      onSave = { n, p, r, primary -> uiState.editingContact?.id?.let { viewModel.updateEmergencyContact(it, n, p, r, primary) } })
  }
  if (uiState.showQualificationsDialog) {
    QualificationsDialogLight(
      onDismiss = { viewModel.closeQualificationsDialog() },
      onSave = { title, org, idNum -> viewModel.saveQualification(title, org, idNum) },
    )
  }
  if (uiState.showEmergencyHistoryDialog) {
    EmergencyHistoryDialogLight(
      onDismiss = { viewModel.closeEmergencyHistoryDialog() },
    )
  }
}

@Composable
private fun ProfileRow(iconBg: Color, icon: ImageVector, iconTint: Color, title: String, subtitle: String, onClick: () -> Unit) {
  Row(
    modifier = Modifier.fillMaxWidth().clip(VictimShapes.Card16).background(Color.White)
      .border(1.dp, VictimBorder, VictimShapes.Card16).clickable { onClick() }.padding(12.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(modifier = Modifier.size(46.dp).clip(CircleShape).background(iconBg), contentAlignment = Alignment.Center) {
      Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
    }
    Spacer(modifier = Modifier.width(10.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = VictimTextDark)
      Text(text = subtitle, fontSize = 12.5.sp, color = VictimTextMuted)
    }
    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = VictimTextMuted, modifier = Modifier.size(18.dp))
  }
}

@Composable
private fun EmergencyContactRowLight(contact: EmergencyContact, context: Context, onEdit: () -> Unit, onDelete: () -> Unit) {
  Row(
    modifier = Modifier.fillMaxWidth().clip(VictimShapes.Card16).background(Color.White)
      .border(1.dp, VictimBorder, VictimShapes.Card16).padding(12.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(text = contact.name, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = VictimTextDark)
      Text(text = "${contact.relationship} • ${contact.phone}", fontSize = 12.5.sp, color = VictimTextMuted)
    }
    IconButton(onClick = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.phone}"))) }, modifier = Modifier.size(34.dp)) {
      Icon(imageVector = Icons.Default.Call, contentDescription = "Call", tint = StatusSafeGreen, modifier = Modifier.size(18.dp))
    }
    IconButton(onClick = {
      val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${contact.phone}")).apply {
        putExtra("sms_body", "EMERGENCY ALERT from NearHelp: I need urgent assistance.")
      }
      context.startActivity(intent)
    }, modifier = Modifier.size(34.dp)) {
      Icon(imageVector = Icons.AutoMirrored.Filled.Message, contentDescription = "SMS", tint = Color(0xFF2563EB), modifier = Modifier.size(18.dp))
    }
    IconButton(onClick = onEdit, modifier = Modifier.size(34.dp)) {
      Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = VictimTextMuted, modifier = Modifier.size(16.dp))
    }
    IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
      Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = VictimPrimary, modifier = Modifier.size(16.dp))
    }
  }
}

@Composable
private fun EditProfileDialogLight(userName: String, userPhone: String, userBlood: String, onDismiss: () -> Unit, onSave: (String, String, String) -> Unit) {
  var name by remember { mutableStateOf(userName) }
  var phone by remember { mutableStateOf(userPhone) }
  var blood by remember { mutableStateOf(userBlood) }
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Edit User Profile", fontWeight = FontWeight.Bold, color = VictimTextDark) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VictimPrimary, unfocusedBorderColor = VictimBorder, focusedTextColor = VictimTextDark, unfocusedTextColor = VictimTextDark))
        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VictimPrimary, unfocusedBorderColor = VictimBorder, focusedTextColor = VictimTextDark, unfocusedTextColor = VictimTextDark))
        Text("Blood Group", fontSize = 12.sp, color = VictimTextMuted)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-").forEach {
            val sel = blood == it
            Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(if (sel) VictimPrimary else Color(0xFFF1F5F9)).clickable { blood = it }.padding(horizontal = 10.dp, vertical = 6.dp)) {
              Text(it, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (sel) Color.White else VictimTextDark)
            }
          }
        }
      }
    },
    confirmButton = { Button(onClick = { onSave(name, phone, blood) }, colors = ButtonDefaults.buttonColors(containerColor = VictimPrimary)) { Text("Save", color = Color.White, fontWeight = FontWeight.Bold) } },
    dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = VictimTextMuted) } },
    containerColor = Color.White,
  )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditMedicalIdDialogLight(blood: String, pacemaker: Boolean, donor: Boolean, conditions: List<String>, allergies: List<String>, notes: String, onDismiss: () -> Unit, onSave: (String, List<String>, List<String>, Boolean, Boolean, String) -> Unit) {
  var b by remember { mutableStateOf(blood) }
  var p by remember { mutableStateOf(pacemaker) }
  var d by remember { mutableStateOf(donor) }
  var n by remember { mutableStateOf(notes) }
  val condList = remember { mutableStateListOf<String>().apply { addAll(conditions) } }
  val algList = remember { mutableStateListOf<String>().apply { addAll(allergies) } }
  var newCond by remember { mutableStateOf("") }
  var newAlg by remember { mutableStateOf("") }
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Edit Medical ID", fontWeight = FontWeight.Bold, color = VictimTextDark) },
    text = {
      Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Blood Group", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VictimTextMuted)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-").forEach {
            val sel = b == it
            Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(if (sel) VictimPrimary else Color(0xFFF1F5F9)).clickable { b = it }.padding(horizontal = 8.dp, vertical = 5.dp)) {
              Text(it, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (sel) Color.White else VictimTextDark)
            }
          }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
          Text("Cardiac Pacemaker", fontSize = 13.sp, color = VictimTextDark)
          Switch(checked = p, onCheckedChange = { p = it }, colors = SwitchDefaults.colors(checkedThumbColor = VictimPrimary))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
          Text("Organ Donor", fontSize = 13.sp, color = VictimTextDark)
          Switch(checked = d, onCheckedChange = { d = it }, colors = SwitchDefaults.colors(checkedThumbColor = StatusSafeGreen))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
          OutlinedTextField(value = newCond, onValueChange = { newCond = it }, placeholder = { Text("e.g. Asthma", fontSize = 12.sp) }, modifier = Modifier.weight(1f),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VictimPrimary, unfocusedBorderColor = VictimBorder))
          Spacer(modifier = Modifier.width(6.dp))
          Button(onClick = { if (newCond.isNotBlank()) { condList.add(newCond.trim()); newCond = "" } }, colors = ButtonDefaults.buttonColors(containerColor = VictimPrimary)) { Text("+", color = Color.White) }
        }
        condList.forEach {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(it, fontSize = 12.sp, color = VictimTextDark, modifier = Modifier.weight(1f))
            Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = VictimPrimary, modifier = Modifier.size(16.dp).clickable { condList.remove(it) })
          }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
          OutlinedTextField(value = newAlg, onValueChange = { newAlg = it }, placeholder = { Text("e.g. Penicillin", fontSize = 12.sp) }, modifier = Modifier.weight(1f),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VictimPrimary, unfocusedBorderColor = VictimBorder))
          Spacer(modifier = Modifier.width(6.dp))
          Button(onClick = { if (newAlg.isNotBlank()) { algList.add(newAlg.trim()); newAlg = "" } }, colors = ButtonDefaults.buttonColors(containerColor = VictimPrimary)) { Text("+", color = Color.White) }
        }
        algList.forEach {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(it, fontSize = 12.sp, color = VictimPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = VictimPrimary, modifier = Modifier.size(16.dp).clickable { algList.remove(it) })
          }
        }
        OutlinedTextField(value = n, onValueChange = { n = it }, label = { Text("Physician Notes") }, modifier = Modifier.fillMaxWidth(), minLines = 2,
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VictimPrimary, unfocusedBorderColor = VictimBorder))
      }
    },
    confirmButton = { Button(onClick = { onSave(b, condList.toList(), algList.toList(), p, d, n) }, colors = ButtonDefaults.buttonColors(containerColor = VictimPrimary)) { Text("Save", color = Color.White, fontWeight = FontWeight.Bold) } },
    dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = VictimTextMuted) } },
    containerColor = Color.White,
  )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddEditContactDialogLight(contact: EmergencyContact?, onDismiss: () -> Unit, onSave: (String, String, String, Boolean) -> Unit) {
  var name by remember { mutableStateOf(contact?.name ?: "") }
  var phone by remember { mutableStateOf(contact?.phone ?: "") }
  var rel by remember { mutableStateOf(contact?.relationship ?: "Family") }
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(if (contact != null) "Edit Emergency Contact" else "Add Emergency Contact", fontWeight = FontWeight.Bold, color = VictimTextDark) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Contact Name") }, modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VictimPrimary, unfocusedBorderColor = VictimBorder))
        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VictimPrimary, unfocusedBorderColor = VictimBorder))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          listOf("Mother", "Father", "Spouse", "Doctor", "Friend", "Neighbor").forEach {
            val sel = rel.equals(it, ignoreCase = true)
            Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(if (sel) VictimPinkCard else Color(0xFFF1F5F9)).border(1.dp, if (sel) VictimPinkBorder else VictimBorder, RoundedCornerShape(8.dp)).clickable { rel = it }.padding(horizontal = 8.dp, vertical = 5.dp)) {
              Text(it, fontSize = 12.sp, color = VictimTextDark, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal)
            }
          }
        }
      }
    },
    confirmButton = {
      Button(onClick = { if (name.isNotBlank() && phone.isNotBlank()) onSave(name, phone, rel, contact?.isPrimary ?: false) }, colors = ButtonDefaults.buttonColors(containerColor = VictimPrimary)) {
        Text("Save Contact", color = Color.White, fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = VictimTextMuted) } },
    containerColor = Color.White,
  )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QualificationsDialogLight(
  onDismiss: () -> Unit,
  onSave: (String, String, String) -> Unit,
) {
  val availableSkills = listOf(
    "CPR / AED Certified",
    "First Aid Provider",
    "EMT / Paramedic",
    "Doctor / Physician",
    "Registered Nurse (RN)",
    "Community First Responder",
  )
  var selectedSkill by remember { mutableStateOf(availableSkills[0]) }
  var organization by remember { mutableStateOf("") }
  var certNumber by remember { mutableStateOf("") }
  var hasAttachedDoc by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(VictimPurpleCard),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.Shield,
            contentDescription = null,
            tint = Color(0xFF7C3AED),
            modifier = Modifier.size(20.dp),
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text("Become a Responder", fontWeight = FontWeight.Black, fontSize = 18.sp, color = VictimTextDark)
          Text("Add qualifications & certifications", fontSize = 12.sp, color = VictimTextMuted)
        }
      }
    },
    text = {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Text("Select Primary Qualification", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = VictimTextDark)
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          availableSkills.forEach { skill ->
            val isSelected = selectedSkill == skill
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(if (isSelected) Color(0xFF7C3AED) else Color(0xFFF1F5F9))
                .clickable { selectedSkill = skill }
                .padding(horizontal = 10.dp, vertical = 7.dp),
            ) {
              Text(
                text = skill,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else VictimTextDark,
              )
            }
          }
        }

        OutlinedTextField(
          value = organization,
          onValueChange = { organization = it },
          label = { Text("Issuing Organization") },
          placeholder = { Text("e.g. Red Cross, St. John Ambulance, AHA") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF7C3AED),
            unfocusedBorderColor = VictimBorder,
            focusedTextColor = VictimTextDark,
            unfocusedTextColor = VictimTextDark,
          ),
        )

        OutlinedTextField(
          value = certNumber,
          onValueChange = { certNumber = it },
          label = { Text("License / Certificate ID") },
          placeholder = { Text("e.g. CPR-2026-8849") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF7C3AED),
            unfocusedBorderColor = VictimBorder,
            focusedTextColor = VictimTextDark,
            unfocusedTextColor = VictimTextDark,
          ),
        )

        // Upload verification document card
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (hasAttachedDoc) Color(0xFFECFDF5) else Color(0xFFF8FAFC))
            .border(1.dp, if (hasAttachedDoc) StatusSafeGreen else VictimBorder, RoundedCornerShape(12.dp))
            .clickable { hasAttachedDoc = !hasAttachedDoc }
            .padding(12.dp),
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(if (hasAttachedDoc) Color(0xFFD1FAE5) else Color(0xFFE2E8F0)),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = if (hasAttachedDoc) Icons.Default.CheckCircle else Icons.Default.Description,
                contentDescription = null,
                tint = if (hasAttachedDoc) StatusSafeGreen else VictimTextDark,
                modifier = Modifier.size(18.dp),
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = if (hasAttachedDoc) "Certificate Attached (PDF/Image)" else "Attach Certificate / ID Proof",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (hasAttachedDoc) StatusSafeGreen else VictimTextDark,
              )
              Text(
                text = if (hasAttachedDoc) "Tap to change file" else "Tap to upload medical credential photo",
                fontSize = 11.5.sp,
                color = VictimTextMuted,
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val org = if (organization.isBlank()) "Indian Red Cross Society" else organization.trim()
          val idNum = if (certNumber.isBlank()) "CERT-VERIFIED-${System.currentTimeMillis() % 10000}" else certNumber.trim()
          onSave(selectedSkill, org, idNum)
        },
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
        shape = RoundedCornerShape(12.dp),
      ) {
        Text("Submit for Verification", color = Color.White, fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = VictimTextMuted)
      }
    },
    containerColor = Color.White,
    shape = RoundedCornerShape(20.dp),
  )
}

private data class EmergencyIncidentItem(
  val id: String,
  val title: String,
  val location: String,
  val timeAgo: String,
  val responderDetails: String,
  val isReceivedHelp: Boolean,
  val status: String = "RESOLVED",
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EmergencyHistoryDialogLight(
  onDismiss: () -> Unit,
) {
  val incidents = remember {
    listOf(
      EmergencyIncidentItem(
        id = "INC-101",
        title = "Severe Bleeding / Cut Injury",
        location = "Salt Lake, Sector V",
        timeAgo = "2 days ago",
        responderDetails = "Responded by Dr. Amit Sen (Trauma Care) • Dispatched in 1m 40s",
        isReceivedHelp = true,
      ),
      EmergencyIncidentItem(
        id = "INC-102",
        title = "Cardiac Distress / CPR Alert",
        location = "New Town, Action Area 1",
        timeAgo = "Aug 24, 2026",
        responderDetails = "You responded as CPR Volunteer • AED Assisted",
        isReceivedHelp = false,
      ),
      EmergencyIncidentItem(
        id = "INC-103",
        title = "Asthma Inhaler Crisis",
        location = "Park Circus, Kolkata",
        timeAgo = "Jul 12, 2026",
        responderDetails = "Responded by Priya K. (BLS Certified) • Resolved",
        isReceivedHelp = true,
      ),
      EmergencyIncidentItem(
        id = "INC-104",
        title = "Pedestrian Fall / Fracture",
        location = "Gariahat Crossing",
        timeAgo = "May 30, 2026",
        responderDetails = "You assisted with limb stabilization until Ambulance arrival",
        isReceivedHelp = false,
      ),
    )
  }

  var selectedFilter by remember { mutableStateOf("All") }
  val filteredIncidents = remember(selectedFilter, incidents) {
    when (selectedFilter) {
      "Received (2)" -> incidents.filter { it.isReceivedHelp }
      "Helped (2)" -> incidents.filter { !it.isReceivedHelp }
      else -> incidents
    }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(VictimPinkCard),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.History,
            contentDescription = null,
            tint = VictimPrimary,
            modifier = Modifier.size(20.dp),
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text("Emergency History", fontWeight = FontWeight.Black, fontSize = 18.sp, color = VictimTextDark)
          Text("Past requests & community responses", fontSize = 12.sp, color = VictimTextMuted)
        }
      }
    },
    text = {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        // Filter row
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          listOf("All", "Received (2)", "Helped (2)").forEach { filter ->
            val isSel = selectedFilter == filter
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(100.dp))
                .background(if (isSel) VictimPrimary else Color(0xFFF1F5F9))
                .clickable { selectedFilter = filter }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
              Text(
                text = filter,
                fontSize = 11.5.sp,
                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                color = if (isSel) Color.White else VictimTextDark,
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Incidents list
        filteredIncidents.forEach { incident ->
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(14.dp))
              .background(if (incident.isReceivedHelp) VictimPinkCard.copy(alpha = 0.5f) else VictimBlueCard.copy(alpha = 0.5f))
              .border(
                1.dp,
                if (incident.isReceivedHelp) VictimPinkBorder else VictimBlueBorder,
                RoundedCornerShape(14.dp),
              )
              .padding(12.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (incident.isReceivedHelp) VictimPrimary else Color(0xFF2563EB))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                  Text(
                    text = if (incident.isReceivedHelp) "RECEIVED" else "HELPED",
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                  )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = incident.timeAgo,
                  fontSize = 11.5.sp,
                  color = VictimTextMuted,
                )
              }
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(Color(0xFFD1FAE5))
                  .padding(horizontal = 6.dp, vertical = 2.dp),
              ) {
                Text(
                  text = incident.status,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = StatusSafeGreen,
                )
              }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
              text = incident.title,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = VictimTextDark,
            )
            Text(
              text = "📍 ${incident.location}",
              fontSize = 12.sp,
              color = VictimTextMuted,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
              text = incident.responderDetails,
              fontSize = 11.5.sp,
              fontWeight = FontWeight.Medium,
              color = if (incident.isReceivedHelp) VictimPrimary else Color(0xFF2563EB),
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onDismiss,
        colors = ButtonDefaults.buttonColors(containerColor = VictimPrimary),
        shape = RoundedCornerShape(12.dp),
      ) {
        Text("Done", color = Color.White, fontWeight = FontWeight.Bold)
      }
    },
    containerColor = Color.White,
    shape = RoundedCornerShape(20.dp),
  )
}
