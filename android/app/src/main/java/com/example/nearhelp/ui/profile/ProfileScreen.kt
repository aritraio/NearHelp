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
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.nearhelp.theme.VictimBorder
import com.example.nearhelp.theme.VictimPinkBorder
import com.example.nearhelp.theme.VictimPinkCard
import com.example.nearhelp.theme.VictimPrimary
import com.example.nearhelp.theme.VictimPurpleCard
import com.example.nearhelp.theme.VictimTextDark
import com.example.nearhelp.theme.VictimTextMuted
import com.example.nearhelp.ui.victim.VictimBottomNavBar
import com.example.nearhelp.ui.victim.VictimNavTab
import com.example.nearhelp.ui.victim.VictimShapes

/**
 * Victim Profile Screen — Refined and uncluttered layout matching 22869.png reference mockup.
 * Features:
 * - Brand logo badge & title with settings action (no back arrow button; universal gestures are used).
 * - Clean user identity row with avatar initial, edit pencil button, verified badge, and location.
 * - Right-aligned Trust Score badge pill.
 * - Single unified stats summary card (Help received & Helped others).
 * - Three clean grouped sections with circular pastel icons: Personal, Responder, Activity.
 * - Modal dialogs for detailed management (Medical ID, Emergency Contacts, Languages, Verification, Impact).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
  onNavigateBack: () -> Unit = {},
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
    uiState.error?.let {
      Toast.makeText(context, it, Toast.LENGTH_LONG).show()
      viewModel.clearError()
    }
  }
  LaunchedEffect(uiState.successMessage) {
    uiState.successMessage?.let {
      Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
      viewModel.clearSuccessMessage()
    }
  }

  val user = uiState.user
  val displayName = user?.name?.ifBlank { "Aritra" } ?: "Aritra"
  val trustScore = (user?.trustScore ?: 84.0).toInt()

  // Local dialog states for clean presentation
  var showEmergencyContactsDialog by remember { mutableStateOf(false) }
  var showLanguagesDialog by remember { mutableStateOf(false) }
  var showVerificationStatusDialog by remember { mutableStateOf(false) }
  var showImpactStatsDialog by remember { mutableStateOf(false) }
  var emergencyHistoryFilter by remember { mutableStateOf("All") }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(VictimBackground)
      .statusBarsPadding()
  ) {
    Column(
      modifier = Modifier
        .weight(1f)
        .verticalScroll(rememberScrollState())
        .padding(vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // 1. Header: Brand Logo & Title + Settings (No back button per user requirement)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Box(
          modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(VictimPrimary),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.People,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp),
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "Near",
            fontWeight = FontWeight.Black,
            fontSize = 22.sp,
            color = VictimTextDark,
          )
          Text(
            text = "Help",
            fontWeight = FontWeight.Black,
            fontSize = 22.sp,
            color = VictimPrimary,
          )
        }
        if (uiState.isLoading) {
          CircularProgressIndicator(
            modifier = Modifier.size(22.dp),
            color = VictimPrimary,
            strokeWidth = 2.dp,
          )
        } else {
          IconButton(
            onClick = { viewModel.loadProfile() },
            modifier = Modifier.size(40.dp),
          ) {
            Icon(
              imageVector = Icons.Default.Settings,
              contentDescription = "Settings",
              tint = VictimTextDark,
              modifier = Modifier.size(24.dp),
            )
          }
        }
      }

      // 2. User Identity Row: Avatar with Edit Badge, Name, Verified Badge, Location & Trust Score
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        // Avatar circle with overlapping edit button
        Box(modifier = Modifier.size(76.dp)) {
          Box(
            modifier = Modifier
              .size(76.dp)
              .clip(CircleShape)
              .background(Color(0xFFE2E8F0)),
            contentAlignment = Alignment.Center,
          ) {
            Text(
              text = displayName.take(1).uppercase(),
              fontSize = 32.sp,
              fontWeight = FontWeight.Bold,
              color = VictimTextDark,
            )
          }
          Box(
            modifier = Modifier
              .align(Alignment.BottomEnd)
              .size(26.dp)
              .clip(CircleShape)
              .background(Color.White)
              .border(1.5.dp, Color(0xFFE2E8F0), CircleShape)
              .clickable { viewModel.openEditProfileDialog() },
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = "Edit Profile",
              tint = VictimTextDark,
              modifier = Modifier.size(13.dp),
            )
          }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = displayName,
              fontSize = 22.sp,
              fontWeight = FontWeight.Bold,
              color = VictimTextDark,
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
              imageVector = Icons.Default.Verified,
              contentDescription = "Verified",
              tint = Color(0xFF2563EB),
              modifier = Modifier.size(20.dp),
            )
          }
          Spacer(modifier = Modifier.height(4.dp))
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.LocationOn,
              contentDescription = null,
              tint = VictimTextMuted,
              modifier = Modifier.size(15.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Kolkata, West Bengal",
              fontSize = 13.sp,
              color = VictimTextMuted,
            )
          }
        }

        // Trust Score Pill Card
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFFF1F2))
            .border(1.dp, Color(0xFFFECDD3), RoundedCornerShape(16.dp))
            .clickable { showVerificationStatusDialog = true }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
          Icon(
            imageVector = Icons.Default.Shield,
            contentDescription = null,
            tint = VictimPrimary,
            modifier = Modifier.size(26.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = "Trust Score",
              fontSize = 11.sp,
              color = VictimTextMuted,
            )
            Text(
              text = "$trustScore / 100",
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = VictimTextDark,
            )
          }
        }
      }

      // 3. Unified Stats Summary Card (Help received & Helped others)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp)
          .clip(RoundedCornerShape(18.dp))
          .background(Color(0xFFFFF5F6))
          .border(1.dp, Color(0xFFFFE4E6), RoundedCornerShape(18.dp))
          .padding(vertical = 16.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        // Help received
        Row(
          modifier = Modifier
            .weight(1f)
            .clickable {
              emergencyHistoryFilter = "Received (2)"
              viewModel.openEmergencyHistoryDialog()
            },
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
        ) {
          Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            tint = VictimPrimary,
            modifier = Modifier.size(24.dp),
          )
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "2",
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
              color = VictimTextDark,
            )
            Text(
              text = "Help received",
              fontSize = 12.sp,
              color = VictimTextMuted,
            )
          }
        }

        // Vertical divider
        Box(
          modifier = Modifier
            .width(1.dp)
            .height(34.dp)
            .background(Color(0xFFE2E8F0)),
        )

        // Helped others
        Row(
          modifier = Modifier
            .weight(1f)
            .clickable {
              emergencyHistoryFilter = "Helped (2)"
              viewModel.openEmergencyHistoryDialog()
            },
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
        ) {
          Box(
            modifier = Modifier
              .size(32.dp)
              .clip(CircleShape)
              .background(Color(0xFFEFF6FF)),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Default.People,
              contentDescription = null,
              tint = Color(0xFF2563EB),
              modifier = Modifier.size(18.dp),
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "5",
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
              color = VictimTextDark,
            )
            Text(
              text = "Helped others",
              fontSize = 12.sp,
              color = VictimTextMuted,
            )
          }
        }
      }

      // 4. Section: Personal
      ProfileSectionGroup(title = "Personal") {
        ProfileSettingItem(
          iconBg = Color(0xFFFEE2E2),
          icon = Icons.Default.Description,
          iconTint = VictimPrimary,
          title = "Medical information",
          subtitle = "Allergies, conditions, blood group and more",
          onClick = { viewModel.openEditMedicalIdDialog() },
        )
        HorizontalDivider(
          modifier = Modifier.padding(start = 72.dp, end = 16.dp),
          thickness = 0.8.dp,
          color = Color(0xFFF1F5F9),
        )
        ProfileSettingItem(
          iconBg = Color(0xFFDCFCE7),
          icon = Icons.Default.Call,
          iconTint = StatusSafeGreen,
          title = "Emergency contacts",
          subtitle = "People to be notified in an emergency",
          onClick = { showEmergencyContactsDialog = true },
        )
        HorizontalDivider(
          modifier = Modifier.padding(start = 72.dp, end = 16.dp),
          thickness = 0.8.dp,
          color = Color(0xFFF1F5F9),
        )
        ProfileSettingItem(
          iconBg = Color(0xFFE0F2FE),
          icon = Icons.Default.Language,
          iconTint = Color(0xFF0284C7),
          title = "Languages",
          subtitle = "Languages you can communicate in",
          onClick = { showLanguagesDialog = true },
        )
      }

      // 5. Section: Responder
      ProfileSectionGroup(title = "Responder") {
        ProfileSettingItem(
          iconBg = Color(0xFFEDE9FE),
          icon = Icons.Default.Shield,
          iconTint = Color(0xFF7C3AED),
          title = "Qualifications & certifications",
          subtitle = "Your training and certifications",
          onClick = { viewModel.openQualificationsDialog() },
        )
        HorizontalDivider(
          modifier = Modifier.padding(start = 72.dp, end = 16.dp),
          thickness = 0.8.dp,
          color = Color(0xFFF1F5F9),
        )
        ProfileSettingItem(
          iconBg = Color(0xFFDCFCE7),
          icon = Icons.Default.CheckCircle,
          iconTint = StatusSafeGreen,
          title = "Verification status",
          subtitle = "Identity and background verification",
          onClick = { showVerificationStatusDialog = true },
        )
      }

      // 6. Section: Activity
      ProfileSectionGroup(title = "Activity") {
        ProfileSettingItem(
          iconBg = Color(0xFFFEE2E2),
          icon = Icons.Default.History,
          iconTint = VictimPrimary,
          title = "Emergency history",
          subtitle = "Your past emergency responses",
          onClick = {
            emergencyHistoryFilter = "All"
            viewModel.openEmergencyHistoryDialog()
          },
        )
        HorizontalDivider(
          modifier = Modifier.padding(start = 72.dp, end = 16.dp),
          thickness = 0.8.dp,
          color = Color(0xFFF1F5F9),
        )
        ProfileSettingItem(
          iconBg = Color(0xFFDBEAFE),
          icon = Icons.Default.BarChart,
          iconTint = Color(0xFF2563EB),
          title = "Impact & statistics",
          subtitle = "Your contribution to a safer community",
          onClick = { showImpactStatsDialog = true },
        )
      }

      Spacer(modifier = Modifier.height(12.dp))
    }

    // Bottom Navigation Bar
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
        },
      )
    }
  }

  // Dialogs
  if (uiState.showEditProfileDialog) {
    EditProfileDialogLight(
      userName = user?.name ?: "",
      userPhone = user?.phone ?: "",
      userBlood = user?.bloodGroup ?: "O+",
      onDismiss = { viewModel.closeEditProfileDialog() },
      onSave = { name, phone, blood ->
        viewModel.updateProfile(name, phone, blood, null, null, null, null, null, null)
      },
    )
  }

  if (uiState.showEditMedicalIdDialog) {
    EditMedicalIdDialogLight(
      blood = user?.bloodGroup ?: "O+",
      pacemaker = user?.hasPacemaker ?: false,
      donor = user?.isOrganDonor ?: false,
      conditions = user?.medicalConditions ?: emptyList(),
      allergies = user?.knownAllergies ?: emptyList(),
      notes = user?.medicalNotes ?: "",
      onDismiss = { viewModel.closeEditMedicalIdDialog() },
      onSave = { b, c, a, p, d, n -> viewModel.updateMedicalId(b, c, a, p, d, n) },
    )
  }

  if (showEmergencyContactsDialog) {
    EmergencyContactsDialogLight(
      contacts = uiState.emergencyContacts,
      context = context,
      onDismiss = { showEmergencyContactsDialog = false },
      onAddContact = { viewModel.openAddContactDialog() },
      onEditContact = { contact -> viewModel.openEditContactDialog(contact) },
      onDeleteContact = { id -> viewModel.deleteEmergencyContact(id) },
    )
  }

  if (uiState.showAddContactDialog) {
    AddEditContactDialogLight(
      contact = null,
      onDismiss = { viewModel.closeAddContactDialog() },
      onSave = { n, p, r, primary -> viewModel.addEmergencyContact(n, p, r, primary) },
    )
  }

  if (uiState.editingContact != null) {
    AddEditContactDialogLight(
      contact = uiState.editingContact,
      onDismiss = { viewModel.closeEditContactDialog() },
      onSave = { n, p, r, primary ->
        uiState.editingContact?.id?.let { viewModel.updateEmergencyContact(it, n, p, r, primary) }
      },
    )
  }

  if (showLanguagesDialog) {
    LanguagesDialogLight(
      selectedLanguages = user?.languages ?: listOf("en"),
      onDismiss = { showLanguagesDialog = false },
      onSave = { languages ->
        viewModel.updateLanguages(languages)
        showLanguagesDialog = false
      },
    )
  }

  if (uiState.showQualificationsDialog) {
    QualificationsDialogLight(
      onDismiss = { viewModel.closeQualificationsDialog() },
      onSave = { title, org, idNum -> viewModel.saveQualification(title, org, idNum) },
    )
  }

  if (showVerificationStatusDialog) {
    VerificationStatusDialogLight(
      trustScore = trustScore,
      onDismiss = { showVerificationStatusDialog = false },
      onRefresh = { viewModel.loadProfile() },
    )
  }

  if (uiState.showEmergencyHistoryDialog) {
    EmergencyHistoryDialogLight(
      initialFilter = emergencyHistoryFilter,
      onDismiss = { viewModel.closeEmergencyHistoryDialog() },
    )
  }

  if (showImpactStatsDialog) {
    ImpactStatisticsDialogLight(
      trustScore = trustScore,
      onDismiss = { showImpactStatsDialog = false },
      onViewHistory = {
        emergencyHistoryFilter = "Helped (2)"
        viewModel.openEmergencyHistoryDialog()
      },
    )
  }
}

/**
 * Reusable Grouped Card for Profile Sections
 */
@Composable
private fun ProfileSectionGroup(
  title: String,
  content: @Composable ColumnScope.() -> Unit,
) {
  Column(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Text(
      text = title,
      fontSize = 18.sp,
      fontWeight = FontWeight.Bold,
      color = VictimTextDark,
    )
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(18.dp))
        .background(Color.White)
        .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(18.dp)),
    ) {
      content()
    }
  }
}

/**
 * Individual Setting Item inside a Grouped Card
 */
@Composable
private fun ProfileSettingItem(
  iconBg: Color,
  icon: ImageVector,
  iconTint: Color,
  title: String,
  subtitle: String,
  onClick: () -> Unit,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .padding(horizontal = 16.dp, vertical = 14.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
      modifier = Modifier
        .size(42.dp)
        .clip(CircleShape)
        .background(iconBg),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = iconTint,
        modifier = Modifier.size(20.dp),
      )
    }
    Spacer(modifier = Modifier.width(14.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        color = VictimTextDark,
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = subtitle,
        fontSize = 12.sp,
        color = VictimTextMuted,
      )
    }
    Icon(
      imageVector = Icons.Default.ChevronRight,
      contentDescription = null,
      tint = Color(0xFF94A3B8),
      modifier = Modifier.size(20.dp),
    )
  }
}

/**
 * Emergency Contacts Management Dialog
 */
@Composable
private fun EmergencyContactsDialogLight(
  contacts: List<EmergencyContact>,
  context: Context,
  onDismiss: () -> Unit,
  onAddContact: () -> Unit,
  onEditContact: (EmergencyContact) -> Unit,
  onDeleteContact: (String) -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color(0xFFDCFCE7)),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.Call,
            contentDescription = null,
            tint = StatusSafeGreen,
            modifier = Modifier.size(20.dp),
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text("Emergency Contacts", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = VictimTextDark)
          Text("${contacts.size} / 5 contacts configured", fontSize = 12.sp, color = VictimTextMuted)
        }
      }
    },
    text = {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        if (contacts.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(Color(0xFFF8FAFC))
              .border(1.dp, VictimBorder, RoundedCornerShape(12.dp))
              .padding(16.dp),
            contentAlignment = Alignment.Center,
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text("No emergency contacts yet", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = VictimTextDark)
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                "Add trusted contacts for 1-tap automated SOS notifications.",
                fontSize = 12.sp,
                color = VictimTextMuted,
                textAlign = TextAlign.Center,
              )
            }
          }
        } else {
          contacts.forEach { contact ->
            EmergencyContactRowLight(
              contact = contact,
              context = context,
              onEdit = { onEditContact(contact) },
              onDelete = { contact.id?.let { onDeleteContact(it) } },
            )
          }
        }

        if (contacts.size < 5) {
          Button(
            onClick = onAddContact,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
            shape = RoundedCornerShape(12.dp),
          ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = VictimPrimary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("+ Add Emergency Contact", color = VictimPrimary, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
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

@Composable
private fun EmergencyContactRowLight(
  contact: EmergencyContact,
  context: Context,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(VictimShapes.Card16)
      .background(Color.White)
      .border(1.dp, VictimBorder, VictimShapes.Card16)
      .padding(12.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(text = contact.name, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = VictimTextDark)
      Text(text = "${contact.relationship} • ${contact.phone}", fontSize = 12.5.sp, color = VictimTextMuted)
    }
    IconButton(
      onClick = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.phone}"))) },
      modifier = Modifier.size(34.dp),
    ) {
      Icon(imageVector = Icons.Default.Call, contentDescription = "Call", tint = StatusSafeGreen, modifier = Modifier.size(18.dp))
    }
    IconButton(
      onClick = {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${contact.phone}")).apply {
          putExtra("sms_body", "EMERGENCY ALERT from NearHelp: I need urgent assistance.")
        }
        context.startActivity(intent)
      },
      modifier = Modifier.size(34.dp),
    ) {
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

/**
 * Spoken Languages Selection Dialog
 */
@Composable
private fun LanguagesDialogLight(
  selectedLanguages: List<String>,
  onDismiss: () -> Unit,
  onSave: (List<String>) -> Unit,
) {
  val available = listOf(
    "en" to "English (Default)",
    "bn" to "বাংলা (Bengali)",
    "hi" to "हिन्दी (Hindi)",
    "es" to "Español (Spanish)",
  )
  val currentSelection = remember { mutableStateListOf<String>().apply { addAll(selectedLanguages) } }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color(0xFFE0F2FE)),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.Language,
            contentDescription = null,
            tint = Color(0xFF0284C7),
            modifier = Modifier.size(20.dp),
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text("Spoken Languages", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = VictimTextDark)
          Text("Languages you can communicate in", fontSize = 12.sp, color = VictimTextMuted)
        }
      }
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        available.forEach { (code, label) ->
          val isSelected = currentSelection.contains(code)
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(if (isSelected) Color(0xFFF0FDF4) else Color(0xFFF8FAFC))
              .border(1.dp, if (isSelected) StatusSafeGreen else VictimBorder, RoundedCornerShape(12.dp))
              .clickable {
                if (isSelected) {
                  if (currentSelection.size > 1) currentSelection.remove(code)
                } else {
                  currentSelection.add(code)
                }
              }
              .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = label,
              fontSize = 14.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
              color = if (isSelected) VictimTextDark else VictimTextMuted,
              modifier = Modifier.weight(1f),
            )
            if (isSelected) {
              Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = StatusSafeGreen,
                modifier = Modifier.size(18.dp),
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { onSave(currentSelection.toList()) },
        colors = ButtonDefaults.buttonColors(containerColor = VictimPrimary),
        shape = RoundedCornerShape(12.dp),
      ) {
        Text("Save Preferences", color = Color.White, fontWeight = FontWeight.Bold)
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

/**
 * Verification Status Modal Dialog
 */
@Composable
private fun VerificationStatusDialogLight(
  trustScore: Int,
  onDismiss: () -> Unit,
  onRefresh: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color(0xFFDCFCE7)),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = StatusSafeGreen,
            modifier = Modifier.size(20.dp),
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text("Verification Status", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = VictimTextDark)
          Text("Identity and background verification", fontSize = 12.sp, color = VictimTextMuted)
        }
      }
    },
    text = {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        // Trust score banner
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFFFF1F2))
            .border(1.dp, Color(0xFFFECDD3), RoundedCornerShape(14.dp))
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(
            imageVector = Icons.Default.Shield,
            contentDescription = null,
            tint = VictimPrimary,
            modifier = Modifier.size(30.dp),
          )
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text("Trust Score: $trustScore / 100", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = VictimTextDark)
            Text("Tier 1 Community First Responder", fontSize = 12.sp, color = VictimTextMuted)
          }
        }

        // Verification checklist
        val checklist = listOf(
          Triple("Government ID Verified", "Aadhaar / National ID verified securely", true),
          Triple("Phone Number Confirmed", "+91 98765 43210 (OTP Verified)", true),
          Triple("CPR / First Aid Certified", "Basic Life Support (BLS) certified", true),
          Triple("Identity Screening", "Clean peer-to-peer verification record", true),
        )

        checklist.forEach { (title, desc, ok) ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .background(Color(0xFFF8FAFC))
              .border(1.dp, VictimBorder, RoundedCornerShape(12.dp))
              .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(
              imageVector = if (ok) Icons.Default.CheckCircle else Icons.Default.Close,
              contentDescription = null,
              tint = if (ok) StatusSafeGreen else VictimPrimary,
              modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(text = title, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = VictimTextDark)
              Text(text = desc, fontSize = 11.5.sp, color = VictimTextMuted)
            }
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
    dismissButton = {
      TextButton(onClick = {
        onRefresh()
        onDismiss()
      }) {
        Text("Re-sync", color = VictimTextDark)
      }
    },
    containerColor = Color.White,
    shape = RoundedCornerShape(20.dp),
  )
}

/**
 * Impact and Statistics Dialog
 */
@Composable
private fun ImpactStatisticsDialogLight(
  trustScore: Int,
  onDismiss: () -> Unit,
  onViewHistory: () -> Unit,
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color(0xFFDBEAFE)),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Default.BarChart,
            contentDescription = null,
            tint = Color(0xFF2563EB),
            modifier = Modifier.size(20.dp),
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
          Text("Impact & Statistics", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = VictimTextDark)
          Text("Your contribution to a safer community", fontSize = 12.sp, color = VictimTextMuted)
        }
      }
    },
    text = {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        // Grid of key statistics
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(14.dp))
              .background(Color(0xFFFFF1F2))
              .border(1.dp, Color(0xFFFECDD3), RoundedCornerShape(14.dp))
              .padding(12.dp),
          ) {
            Column {
              Text("5", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = VictimTextDark)
              Text("People Helped", fontSize = 11.5.sp, color = VictimTextMuted)
            }
          }
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(14.dp))
              .background(Color(0xFFEFF6FF))
              .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(14.dp))
              .padding(12.dp),
          ) {
            Column {
              Text("1m 40s", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = VictimTextDark)
              Text("Avg. Response", fontSize = 11.5.sp, color = VictimTextMuted)
            }
          }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(14.dp))
              .background(Color(0xFFECFDF5))
              .border(1.dp, Color(0xFFA7F3D0), RoundedCornerShape(14.dp))
              .padding(12.dp),
          ) {
            Column {
              Text("98%", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = VictimTextDark)
              Text("Success Rate", fontSize = 11.5.sp, color = VictimTextMuted)
            }
          }
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(14.dp))
              .background(Color(0xFFF5F3FF))
              .border(1.dp, Color(0xFFDDD6FE), RoundedCornerShape(14.dp))
              .padding(12.dp),
          ) {
            Column {
              Text("$trustScore / 100", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = VictimTextDark)
              Text("Trust Score", fontSize = 11.5.sp, color = VictimTextMuted)
            }
          }
        }

        // Community Lifesaver Badge
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFFFFBEB))
            .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(14.dp))
            .padding(12.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = Color(0xFFF59E0B),
            modifier = Modifier.size(24.dp),
          )
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text("Community Lifesaver Badge", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = VictimTextDark)
            Text("Recognized for fast community crisis intervention", fontSize = 11.5.sp, color = VictimTextMuted)
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
    dismissButton = {
      TextButton(onClick = {
        onDismiss()
        onViewHistory()
      }) {
        Text("View History", color = VictimPrimary, fontWeight = FontWeight.SemiBold)
      }
    },
    containerColor = Color.White,
    shape = RoundedCornerShape(20.dp),
  )
}

/**
 * Edit User Profile Dialog
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditProfileDialogLight(
  userName: String,
  userPhone: String,
  userBlood: String,
  onDismiss: () -> Unit,
  onSave: (String, String, String) -> Unit,
) {
  var name by remember { mutableStateOf(userName) }
  var phone by remember { mutableStateOf(userPhone) }
  var blood by remember { mutableStateOf(userBlood) }
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Edit User Profile", fontWeight = FontWeight.Bold, color = VictimTextDark) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Full Name") },
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = VictimPrimary,
            unfocusedBorderColor = VictimBorder,
            focusedTextColor = VictimTextDark,
            unfocusedTextColor = VictimTextDark,
          ),
        )
        OutlinedTextField(
          value = phone,
          onValueChange = { phone = it },
          label = { Text("Phone Number") },
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = VictimPrimary,
            unfocusedBorderColor = VictimBorder,
            focusedTextColor = VictimTextDark,
            unfocusedTextColor = VictimTextDark,
          ),
        )
        Text("Blood Group", fontSize = 12.sp, color = VictimTextMuted)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-").forEach {
            val sel = blood == it
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (sel) VictimPrimary else Color(0xFFF1F5F9))
                .clickable { blood = it }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
              Text(it, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (sel) Color.White else VictimTextDark)
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { onSave(name, phone, blood) },
        colors = ButtonDefaults.buttonColors(containerColor = VictimPrimary),
      ) {
        Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = VictimTextMuted)
      }
    },
    containerColor = Color.White,
  )
}

/**
 * Edit Encrypted Medical ID Dialog
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EditMedicalIdDialogLight(
  blood: String,
  pacemaker: Boolean,
  donor: Boolean,
  conditions: List<String>,
  allergies: List<String>,
  notes: String,
  onDismiss: () -> Unit,
  onSave: (String, List<String>, List<String>, Boolean, Boolean, String) -> Unit,
) {
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
    title = { Text("Edit Medical Information", fontWeight = FontWeight.Bold, color = VictimTextDark) },
    text = {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Text("Blood Group", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VictimTextMuted)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-").forEach {
            val sel = b == it
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (sel) VictimPrimary else Color(0xFFF1F5F9))
                .clickable { b = it }
                .padding(horizontal = 8.dp, vertical = 5.dp),
            ) {
              Text(it, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (sel) Color.White else VictimTextDark)
            }
          }
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text("Cardiac Pacemaker", fontSize = 13.sp, color = VictimTextDark)
          Switch(checked = p, onCheckedChange = { p = it }, colors = SwitchDefaults.colors(checkedThumbColor = VictimPrimary))
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text("Organ Donor", fontSize = 13.sp, color = VictimTextDark)
          Switch(checked = d, onCheckedChange = { d = it }, colors = SwitchDefaults.colors(checkedThumbColor = StatusSafeGreen))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
          OutlinedTextField(
            value = newCond,
            onValueChange = { newCond = it },
            placeholder = { Text("e.g. Asthma", fontSize = 12.sp) },
            modifier = Modifier.weight(1f),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VictimPrimary, unfocusedBorderColor = VictimBorder),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Button(
            onClick = {
              if (newCond.isNotBlank()) {
                condList.add(newCond.trim())
                newCond = ""
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = VictimPrimary),
          ) {
            Text("+", color = Color.White)
          }
        }
        condList.forEach {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(it, fontSize = 12.sp, color = VictimTextDark, modifier = Modifier.weight(1f))
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = null,
              tint = VictimPrimary,
              modifier = Modifier.size(16.dp).clickable { condList.remove(it) },
            )
          }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
          OutlinedTextField(
            value = newAlg,
            onValueChange = { newAlg = it },
            placeholder = { Text("e.g. Penicillin", fontSize = 12.sp) },
            modifier = Modifier.weight(1f),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VictimPrimary, unfocusedBorderColor = VictimBorder),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Button(
            onClick = {
              if (newAlg.isNotBlank()) {
                algList.add(newAlg.trim())
                newAlg = ""
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = VictimPrimary),
          ) {
            Text("+", color = Color.White)
          }
        }
        algList.forEach {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(it, fontSize = 12.sp, color = VictimPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = null,
              tint = VictimPrimary,
              modifier = Modifier.size(16.dp).clickable { algList.remove(it) },
            )
          }
        }
        OutlinedTextField(
          value = n,
          onValueChange = { n = it },
          label = { Text("Physician Notes") },
          modifier = Modifier.fillMaxWidth(),
          minLines = 2,
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VictimPrimary, unfocusedBorderColor = VictimBorder),
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { onSave(b, condList.toList(), algList.toList(), p, d, n) },
        colors = ButtonDefaults.buttonColors(containerColor = VictimPrimary),
      ) {
        Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = VictimTextMuted)
      }
    },
    containerColor = Color.White,
  )
}

/**
 * Add / Edit Emergency Contact Dialog
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddEditContactDialogLight(
  contact: EmergencyContact?,
  onDismiss: () -> Unit,
  onSave: (String, String, String, Boolean) -> Unit,
) {
  var name by remember { mutableStateOf(contact?.name ?: "") }
  var phone by remember { mutableStateOf(contact?.phone ?: "") }
  var rel by remember { mutableStateOf(contact?.relationship ?: "Family") }
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(if (contact != null) "Edit Emergency Contact" else "Add Emergency Contact", fontWeight = FontWeight.Bold, color = VictimTextDark) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Contact Name") },
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VictimPrimary, unfocusedBorderColor = VictimBorder),
        )
        OutlinedTextField(
          value = phone,
          onValueChange = { phone = it },
          label = { Text("Phone Number") },
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = VictimPrimary, unfocusedBorderColor = VictimBorder),
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          listOf("Mother", "Father", "Spouse", "Doctor", "Friend", "Neighbor").forEach {
            val sel = rel.equals(it, ignoreCase = true)
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (sel) VictimPinkCard else Color(0xFFF1F5F9))
                .border(1.dp, if (sel) VictimPinkBorder else VictimBorder, RoundedCornerShape(8.dp))
                .clickable { rel = it }
                .padding(horizontal = 8.dp, vertical = 5.dp),
            ) {
              Text(it, fontSize = 12.sp, color = VictimTextDark, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal)
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank() && phone.isNotBlank()) onSave(name, phone, rel, contact?.isPrimary ?: false)
        },
        colors = ButtonDefaults.buttonColors(containerColor = VictimPrimary),
      ) {
        Text("Save Contact", color = Color.White, fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = VictimTextMuted)
      }
    },
    containerColor = Color.White,
  )
}

/**
 * Qualifications and Certifications Dialog
 */
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

/**
 * Emergency History Dialog
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EmergencyHistoryDialogLight(
  initialFilter: String = "All",
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

  var selectedFilter by remember(initialFilter) { mutableStateOf(initialFilter) }
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
              .background(if (incident.isReceivedHelp) VictimPinkCard.copy(alpha = 0.5f) else Color(0xFFEFF6FF).copy(alpha = 0.5f))
              .border(
                1.dp,
                if (incident.isReceivedHelp) VictimPinkBorder else Color(0xFFBFDBFE),
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
