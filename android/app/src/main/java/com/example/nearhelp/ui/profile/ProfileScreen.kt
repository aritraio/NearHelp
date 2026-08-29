package com.example.nearhelp.ui.profile

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nearhelp.data.api.models.EmergencyContact
import com.example.nearhelp.theme.ActionAmber
import com.example.nearhelp.theme.AiCyan
import com.example.nearhelp.theme.CardSurface
import com.example.nearhelp.theme.CardSurfaceVariant
import com.example.nearhelp.theme.DarkBackground
import com.example.nearhelp.theme.EmergencyRed
import com.example.nearhelp.theme.EmergencyRedContainer
import com.example.nearhelp.theme.SafeGreen
import com.example.nearhelp.theme.SurfaceBorder
import com.example.nearhelp.theme.TextHighContrast
import com.example.nearhelp.theme.TextMediumContrast
import com.example.nearhelp.theme.TextMuted

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
  onNavigateBack: () -> Unit,
  viewModel: ProfileViewModel,
  modifier: Modifier = Modifier,
) {
  val uiState by viewModel.uiState.collectAsState()
  val context = LocalContext.current

  LaunchedEffect(uiState.error) {
    uiState.error?.let { err ->
      Toast.makeText(context, err, Toast.LENGTH_LONG).show()
      viewModel.clearError()
    }
  }

  LaunchedEffect(uiState.successMessage) {
    uiState.successMessage?.let { msg ->
      Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
      viewModel.clearSuccessMessage()
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DarkBackground)
  ) {
    // 1. Top App Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onNavigateBack) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = TextHighContrast,
          )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "Profile & Medical ID",
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
          color = TextHighContrast,
        )
      }

      IconButton(onClick = { viewModel.loadProfile() }) {
        if (uiState.isLoading) {
          CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            color = AiCyan,
            strokeWidth = 2.dp,
          )
        } else {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Refresh",
            tint = TextMediumContrast,
          )
        }
      }
    }

    // 2. Scrollable Body
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 16.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      val user = uiState.user

      // -----------------------------------------------------------------------
      // A. User Identity & Reputation Card
      // -----------------------------------------------------------------------
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, SurfaceBorder),
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Box(
              modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(0xFF1E3A5F)),
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text = (user?.name?.take(2) ?: "NH").uppercase(),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = AiCyan,
              )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                  text = user?.name ?: "User Profile",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = TextHighContrast,
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = "Verified",
                  tint = SafeGreen,
                  modifier = Modifier.size(16.dp),
                )
              }

              Text(
                text = user?.email ?: "No email registered",
                style = MaterialTheme.typography.bodySmall,
                color = TextMediumContrast,
              )

              val phone = user?.phone
              if (!phone.isNullOrBlank()) {
                Text(
                  text = phone,
                  style = MaterialTheme.typography.bodySmall,
                  color = TextMuted,
                )
              }
            }

            IconButton(onClick = { viewModel.openEditProfileDialog() }) {
              Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit Profile",
                tint = AiCyan,
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Reputation Trust Score Bar
          val trustScore = user?.trustScore ?: 50.0
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "Reputation Trust Score",
              style = MaterialTheme.typography.labelSmall,
              color = TextMediumContrast,
            )
            Text(
              text = "${trustScore.toInt()} / 100",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = if (trustScore >= 75) SafeGreen else if (trustScore >= 50) AiCyan else ActionAmber,
            )
          }

          Spacer(modifier = Modifier.height(6.dp))

          LinearProgressIndicator(
            progress = { (trustScore / 100.0).toFloat().coerceIn(0f, 1f) },
            modifier = Modifier
              .fillMaxWidth()
              .height(6.dp)
              .clip(RoundedCornerShape(3.dp)),
            color = if (trustScore >= 75) SafeGreen else if (trustScore >= 50) AiCyan else ActionAmber,
            trackColor = CardSurfaceVariant,
          )

          // Achievement Badges
          val badges = user?.badges ?: emptyList()
          if (badges.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              badges.forEach { badge ->
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF2E3B2E))
                    .border(1.dp, SafeGreen.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                  Text(
                    text = badge.replace("_", " "),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                    color = SafeGreen,
                  )
                }
              }
            }
          }
        }
      }

      // -----------------------------------------------------------------------
      // B. Encrypted Medical ID Card
      // -----------------------------------------------------------------------
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFF00E5FF).copy(alpha = 0.4f)),
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          // Encryption Banner Header
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF00363A))
                .border(1.dp, AiCyan.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
              Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Encrypted",
                tint = AiCyan,
                modifier = Modifier.size(12.dp),
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "AES-256 ENCRYPTED AT REST",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  letterSpacing = 0.5.sp,
                ),
                color = AiCyan,
              )
            }

            IconButton(
              onClick = { viewModel.openEditMedicalIdDialog() },
              modifier = Modifier.size(32.dp),
            ) {
              Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit Medical ID",
                tint = AiCyan,
                modifier = Modifier.size(18.dp),
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Blood Group & Special Implants Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            // Blood Group Badge
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(EmergencyRedContainer)
                .border(1.dp, EmergencyRed.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .padding(12.dp),
              contentAlignment = Alignment.Center,
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.Bloodtype,
                    contentDescription = "Blood Group",
                    tint = EmergencyRed,
                    modifier = Modifier.size(16.dp),
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "BLOOD TYPE",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                    color = EmergencyRed,
                  )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = user?.bloodGroup?.ifBlank { "Unknown" } ?: "Unknown",
                  style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                  color = TextHighContrast,
                )
              }
            }

            // Pacemaker / Critical Implant Status
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(if (user?.hasPacemaker == true) Color(0xFF3E2723) else CardSurfaceVariant)
                .border(
                  1.dp,
                  if (user?.hasPacemaker == true) ActionAmber else SurfaceBorder,
                  RoundedCornerShape(12.dp)
                )
                .padding(12.dp),
              contentAlignment = Alignment.Center,
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Pacemaker",
                    tint = if (user?.hasPacemaker == true) ActionAmber else TextMuted,
                    modifier = Modifier.size(14.dp),
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "PACEMAKER",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                    color = if (user?.hasPacemaker == true) ActionAmber else TextMuted,
                  )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = if (user?.hasPacemaker == true) "FITTED" else "NONE",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = if (user?.hasPacemaker == true) ActionAmber else TextMediumContrast,
                )
              }
            }

            // Organ Donor Status
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(if (user?.isOrganDonor == true) Color(0xFF1B3B22) else CardSurfaceVariant)
                .border(
                  1.dp,
                  if (user?.isOrganDonor == true) SafeGreen else SurfaceBorder,
                  RoundedCornerShape(12.dp)
                )
                .padding(12.dp),
              contentAlignment = Alignment.Center,
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Organ Donor",
                    tint = if (user?.isOrganDonor == true) SafeGreen else TextMuted,
                    modifier = Modifier.size(14.dp),
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "ORGAN DONOR",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                    color = if (user?.isOrganDonor == true) SafeGreen else TextMuted,
                  )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = if (user?.isOrganDonor == true) "YES" else "NO",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = if (user?.isOrganDonor == true) SafeGreen else TextMediumContrast,
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Medical Conditions Chips
          Text(
            text = "MEDICAL CONDITIONS",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
            color = TextMuted,
          )
          Spacer(modifier = Modifier.height(6.dp))
          val conditions = user?.medicalConditions ?: emptyList()
          if (conditions.isNotEmpty()) {
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              conditions.forEach { condition ->
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1E293B))
                    .border(1.dp, AiCyan.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                ) {
                  Text(
                    text = condition,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),
                    color = Color.White,
                  )
                }
              }
            }
          } else {
            Text(
              text = "No chronic medical conditions declared",
              style = MaterialTheme.typography.bodySmall,
              color = TextMuted,
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Known Allergies Chips
          Text(
            text = "KNOWN ALLERGIES",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
            color = EmergencyRed,
          )
          Spacer(modifier = Modifier.height(6.dp))
          val allergies = user?.knownAllergies ?: emptyList()
          if (allergies.isNotEmpty()) {
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              allergies.forEach { allergy ->
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(EmergencyRedContainer)
                    .border(1.dp, EmergencyRed.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                ) {
                  Text(
                    text = allergy,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                    color = EmergencyRed,
                  )
                }
              }
            }
          } else {
            Text(
              text = "No known allergies declared",
              style = MaterialTheme.typography.bodySmall,
              color = TextMuted,
            )
          }

          // Medical Notes / Directives
          if (!user?.medicalNotes.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
              text = "PHYSICIAN / EMERGENCY DIRECTIVES",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
              color = TextMuted,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(CardSurfaceVariant)
                .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
                .padding(10.dp),
            ) {
              Text(
                text = user.medicalNotes,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = TextHighContrast,
              )
            }
          }
        }
      }

      // -----------------------------------------------------------------------
      // C. Emergency Contacts Section (Max 5)
      // -----------------------------------------------------------------------
      val contacts = uiState.emergencyContacts
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, SurfaceBorder),
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column {
              Text(
                text = "EMERGENCY KIN CONTACTS",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                color = TextHighContrast,
              )
              Text(
                text = "${contacts.size} / 5 Contacts Configured",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = TextMediumContrast,
              )
            }

            if (contacts.size < 5) {
              Button(
                onClick = { viewModel.openAddContactDialog() },
                colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                shape = RoundedCornerShape(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
              ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          if (contacts.isEmpty()) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(CardSurfaceVariant)
                .padding(16.dp),
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text = "No emergency contacts added yet.\nAdd up to 5 contacts for 1-tap SOS notifications.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
              )
            }
          } else {
            contacts.forEach { contact ->
              EmergencyContactRow(
                contact = contact,
                context = context,
                onEdit = { viewModel.openEditContactDialog(contact) },
                onDelete = { contact.id?.let { viewModel.deleteEmergencyContact(it) } },
              )
              Spacer(modifier = Modifier.height(8.dp))
            }
          }
        }
      }

      // -----------------------------------------------------------------------
      // D. Language Preferences
      // -----------------------------------------------------------------------
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, SurfaceBorder),
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "SPOKEN LANGUAGES",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
            color = TextMuted,
          )
          Spacer(modifier = Modifier.height(8.dp))

          val currentLanguages = user?.languages ?: listOf("en")
          val availableLanguages = listOf(
            "en" to "English",
            "bn" to "বাংলা (Bengali)",
            "hi" to "हिन्दी (Hindi)",
            "es" to "Español",
          )

          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            availableLanguages.forEach { (code, label) ->
              val isSelected = currentLanguages.contains(code)
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(20.dp))
                  .background(if (isSelected) Color(0xFF004D40) else CardSurfaceVariant)
                  .border(
                    1.dp,
                    if (isSelected) SafeGreen else SurfaceBorder,
                    RoundedCornerShape(20.dp)
                  )
                  .clickable {
                    val updated = if (isSelected) {
                      if (currentLanguages.size > 1) currentLanguages.filter { it != code } else currentLanguages
                    } else {
                      currentLanguages + code
                    }
                    viewModel.updateLanguages(updated)
                  }
                  .padding(horizontal = 12.dp, vertical = 6.dp),
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  if (isSelected) {
                    Icon(
                      imageVector = Icons.Default.Check,
                      contentDescription = null,
                      tint = SafeGreen,
                      modifier = Modifier.size(14.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                  }
                  Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall.copy(
                      fontSize = 12.sp,
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    ),
                    color = if (isSelected) SafeGreen else TextMediumContrast,
                  )
                }
              }
            }
          }
        }
      }

      // -----------------------------------------------------------------------
      // E. Mandatory Good Samaritan Legal Protection Card
      // -----------------------------------------------------------------------
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, ActionAmber.copy(alpha = 0.5f)),
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.Top,
        ) {
          Icon(
            imageVector = Icons.Default.Security,
            contentDescription = "Legal Shield",
            tint = ActionAmber,
            modifier = Modifier.size(20.dp),
          )
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "Good Samaritan Legal Protection (Section 134A)",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = ActionAmber,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = "Medical ID data is strictly shared with verified responders solely during active emergency rescue. Protected under the Supreme Court Good Samaritan Guidelines (2016).",
              style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 15.sp),
              color = TextMediumContrast,
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }

  // ===========================================================================
  // Dialogs & Sheets
  // ===========================================================================

  // 1. Edit Profile Dialog
  if (uiState.showEditProfileDialog) {
    EditProfileDialog(
      user = uiState.user,
      onDismiss = { viewModel.closeEditProfileDialog() },
      onSave = { name, phone, bloodGroup ->
        viewModel.updateProfile(
          name = name,
          phone = phone,
          bloodGroup = bloodGroup,
          languages = null,
          hasPacemaker = null,
          isOrganDonor = null,
          medicalNotes = null,
          medicalConditions = null,
          knownAllergies = null,
        )
      },
    )
  }

  // 2. Edit Medical ID Dialog
  if (uiState.showEditMedicalIdDialog) {
    EditMedicalIdDialog(
      user = uiState.user,
      onDismiss = { viewModel.closeEditMedicalIdDialog() },
      onSave = { bloodGroup, conditions, allergies, pacemaker, organDonor, notes ->
        viewModel.updateMedicalId(
          bloodGroup = bloodGroup,
          conditions = conditions,
          allergies = allergies,
          hasPacemaker = pacemaker,
          isOrganDonor = organDonor,
          notes = notes,
        )
      },
    )
  }

  // 3. Add Contact Dialog
  if (uiState.showAddContactDialog) {
    AddEditContactDialog(
      contact = null,
      onDismiss = { viewModel.closeAddContactDialog() },
      onSave = { name, phone, relationship, isPrimary ->
        viewModel.addEmergencyContact(name, phone, relationship, isPrimary)
      },
    )
  }

  // 4. Edit Contact Dialog
  if (uiState.editingContact != null) {
    AddEditContactDialog(
      contact = uiState.editingContact,
      onDismiss = { viewModel.closeEditContactDialog() },
      onSave = { name, phone, relationship, isPrimary ->
        uiState.editingContact?.id?.let { id ->
          viewModel.updateEmergencyContact(id, name, phone, relationship, isPrimary)
        }
      },
    )
  }
}

// =============================================================================
// Helper Component: Emergency Contact Item Row
// =============================================================================
@Composable
private fun EmergencyContactRow(
  contact: EmergencyContact,
  context: Context,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
) {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(CardSurfaceVariant)
      .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
      .padding(12.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = contact.name,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = TextHighContrast,
          )
          if (contact.isPrimary) {
            Spacer(modifier = Modifier.width(6.dp))
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF4A3800))
                .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Star,
                  contentDescription = null,
                  tint = ActionAmber,
                  modifier = Modifier.size(10.dp),
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                  text = "PRIMARY",
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                  color = ActionAmber,
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = contact.relationship,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = AiCyan,
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "•  ${contact.phone}",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = TextMediumContrast,
          )
        }
      }

      // Quick Call, Quick SMS, and Menu Actions
      Row(verticalAlignment = Alignment.CenterVertically) {
        // Quick Call Button
        IconButton(
          onClick = {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contact.phone}"))
            context.startActivity(intent)
          },
          modifier = Modifier.size(32.dp),
        ) {
          Icon(
            imageVector = Icons.Default.Call,
            contentDescription = "Call",
            tint = SafeGreen,
            modifier = Modifier.size(18.dp),
          )
        }

        // Quick SMS Button
        IconButton(
          onClick = {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${contact.phone}")).apply {
              putExtra("sms_body", "🚨 EMERGENCY ALERT from NearHelp AI: I have triggered an SOS response and need urgent assistance.")
            }
            context.startActivity(intent)
          },
          modifier = Modifier.size(32.dp),
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.Message,
            contentDescription = "SMS",
            tint = AiCyan,
            modifier = Modifier.size(18.dp),
          )
        }

        // Edit Button
        IconButton(
          onClick = onEdit,
          modifier = Modifier.size(32.dp),
        ) {
          Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Edit",
            tint = TextMediumContrast,
            modifier = Modifier.size(16.dp),
          )
        }

        // Delete Button
        IconButton(
          onClick = onDelete,
          modifier = Modifier.size(32.dp),
        ) {
          Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete",
            tint = EmergencyRed,
            modifier = Modifier.size(16.dp),
          )
        }
      }
    }
  }
}

// =============================================================================
// Dialog 1: Edit Profile Dialog
// =============================================================================
@Composable
private fun EditProfileDialog(
  user: com.example.nearhelp.data.api.models.UserResponse?,
  onDismiss: () -> Unit,
  onSave: (name: String, phone: String, bloodGroup: String) -> Unit,
) {
  var name by remember { mutableStateOf(user?.name ?: "") }
  var phone by remember { mutableStateOf(user?.phone ?: "") }
  var bloodGroup by remember { mutableStateOf(user?.bloodGroup ?: "O+") }

  val bloodGroups = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "Edit User Profile",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = TextHighContrast,
      )
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Full Name") },
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextHighContrast,
            unfocusedTextColor = TextHighContrast,
            focusedBorderColor = AiCyan,
            unfocusedBorderColor = SurfaceBorder,
          ),
        )

        OutlinedTextField(
          value = phone,
          onValueChange = { phone = it },
          label = { Text("Phone Number") },
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextHighContrast,
            unfocusedTextColor = TextHighContrast,
            focusedBorderColor = AiCyan,
            unfocusedBorderColor = SurfaceBorder,
          ),
        )

        Text(
          text = "Blood Group",
          style = MaterialTheme.typography.labelSmall,
          color = TextMediumContrast,
        )

        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          bloodGroups.forEach { bg ->
            val selected = bloodGroup == bg
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (selected) EmergencyRed else CardSurfaceVariant)
                .clickable { bloodGroup = bg }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
              Text(
                text = bg,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (selected) Color.White else TextMediumContrast,
              )
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = { onSave(name, phone, bloodGroup) },
        colors = ButtonDefaults.buttonColors(containerColor = AiCyan),
      ) {
        Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = TextMediumContrast)
      }
    },
    containerColor = CardSurface,
  )
}

// =============================================================================
// Dialog 2: Edit Medical ID Dialog (AES-256 Encrypted)
// =============================================================================
@Composable
private fun EditMedicalIdDialog(
  user: com.example.nearhelp.data.api.models.UserResponse?,
  onDismiss: () -> Unit,
  onSave: (
    bloodGroup: String,
    conditions: List<String>,
    allergies: List<String>,
    hasPacemaker: Boolean,
    isOrganDonor: Boolean,
    notes: String,
  ) -> Unit,
) {
  var bloodGroup by remember { mutableStateOf(user?.bloodGroup ?: "O+") }
  var hasPacemaker by remember { mutableStateOf(user?.hasPacemaker ?: false) }
  var isOrganDonor by remember { mutableStateOf(user?.isOrganDonor ?: false) }
  var notes by remember { mutableStateOf(user?.medicalNotes ?: "") }

  val conditionsList = remember { mutableStateListOf<String>().apply { addAll(user?.medicalConditions ?: emptyList()) } }
  val allergiesList = remember { mutableStateListOf<String>().apply { addAll(user?.knownAllergies ?: emptyList()) } }

  var newConditionText by remember { mutableStateOf("") }
  var newAllergyText by remember { mutableStateOf("") }

  val bloodGroups = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Lock, contentDescription = null, tint = AiCyan, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Edit Encrypted Medical ID",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = TextHighContrast,
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        // Blood Group Chips
        Text(
          text = "Blood Group",
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
          color = TextMediumContrast,
        )
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          bloodGroups.forEach { bg ->
            val selected = bloodGroup == bg
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (selected) EmergencyRed else CardSurfaceVariant)
                .clickable { bloodGroup = bg }
                .padding(horizontal = 8.dp, vertical = 5.dp),
            ) {
              Text(
                text = bg,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (selected) Color.White else TextMediumContrast,
              )
            }
          }
        }

        // Pacemaker & Organ Donor Switches
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(text = "Cardiac Pacemaker Fitted", color = TextHighContrast, style = MaterialTheme.typography.bodySmall)
          Switch(
            checked = hasPacemaker,
            onCheckedChange = { hasPacemaker = it },
            colors = SwitchDefaults.colors(checkedThumbColor = ActionAmber, checkedTrackColor = Color(0xFF4E342E)),
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(text = "Registered Organ Donor", color = TextHighContrast, style = MaterialTheme.typography.bodySmall)
          Switch(
            checked = isOrganDonor,
            onCheckedChange = { isOrganDonor = it },
            colors = SwitchDefaults.colors(checkedThumbColor = SafeGreen, checkedTrackColor = Color(0xFF1B5E20)),
          )
        }

        // Add Medical Condition
        Text(
          text = "Medical Conditions",
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
          color = AiCyan,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
          OutlinedTextField(
            value = newConditionText,
            onValueChange = { newConditionText = it },
            placeholder = { Text("e.g. Asthma, Diabetes", fontSize = 12.sp) },
            modifier = Modifier.weight(1f),
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = TextHighContrast,
              unfocusedTextColor = TextHighContrast,
              focusedBorderColor = AiCyan,
              unfocusedBorderColor = SurfaceBorder,
            ),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Button(
            onClick = {
              if (newConditionText.isNotBlank()) {
                conditionsList.add(newConditionText.trim())
                newConditionText = ""
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = AiCyan),
            shape = RoundedCornerShape(8.dp),
          ) {
            Text("+", color = Color.Black, fontWeight = FontWeight.Bold)
          }
        }
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          conditionsList.forEach { item ->
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(CardSurfaceVariant)
                .border(1.dp, AiCyan.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = item, color = TextHighContrast, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp))
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "Remove",
                  tint = EmergencyRed,
                  modifier = Modifier
                    .size(14.dp)
                    .clickable { conditionsList.remove(item) },
                )
              }
            }
          }
        }

        // Add Known Allergy
        Text(
          text = "Known Allergies",
          style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
          color = EmergencyRed,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
          OutlinedTextField(
            value = newAllergyText,
            onValueChange = { newAllergyText = it },
            placeholder = { Text("e.g. Penicillin, Peanuts", fontSize = 12.sp) },
            modifier = Modifier.weight(1f),
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = TextHighContrast,
              unfocusedTextColor = TextHighContrast,
              focusedBorderColor = EmergencyRed,
              unfocusedBorderColor = SurfaceBorder,
            ),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Button(
            onClick = {
              if (newAllergyText.isNotBlank()) {
                allergiesList.add(newAllergyText.trim())
                newAllergyText = ""
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
            shape = RoundedCornerShape(8.dp),
          ) {
            Text("+", color = Color.White, fontWeight = FontWeight.Bold)
          }
        }
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          allergiesList.forEach { item ->
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(EmergencyRedContainer)
                .border(1.dp, EmergencyRed.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = item, color = EmergencyRed, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "Remove",
                  tint = EmergencyRed,
                  modifier = Modifier
                    .size(14.dp)
                    .clickable { allergiesList.remove(item) },
                )
              }
            }
          }
        }

        // Directives & Medical Notes
        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text("Physician Notes / Directives") },
          placeholder = { Text("e.g. Patient carries EpiPen; avoid NSAIDs.") },
          modifier = Modifier.fillMaxWidth(),
          minLines = 3,
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextHighContrast,
            unfocusedTextColor = TextHighContrast,
            focusedBorderColor = AiCyan,
            unfocusedBorderColor = SurfaceBorder,
          ),
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onSave(
            bloodGroup,
            conditionsList.toList(),
            allergiesList.toList(),
            hasPacemaker,
            isOrganDonor,
            notes,
          )
        },
        colors = ButtonDefaults.buttonColors(containerColor = AiCyan),
      ) {
        Text("Save & Encrypt", color = Color.Black, fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = TextMediumContrast)
      }
    },
    containerColor = CardSurface,
  )
}

// =============================================================================
// Dialog 3: Add / Edit Emergency Contact Dialog
// =============================================================================
@Composable
private fun AddEditContactDialog(
  contact: EmergencyContact?,
  onDismiss: () -> Unit,
  onSave: (name: String, phone: String, relationship: String, isPrimary: Boolean) -> Unit,
) {
  var name by remember { mutableStateOf(contact?.name ?: "") }
  var phone by remember { mutableStateOf(contact?.phone ?: "") }
  var relationship by remember { mutableStateOf(contact?.relationship ?: "Family") }
  var isPrimary by remember { mutableStateOf(contact?.isPrimary ?: false) }

  val relationshipPresets = listOf("Mother", "Father", "Spouse", "Doctor", "Friend", "Neighbor")

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = if (contact != null) "Edit Emergency Contact" else "Add Emergency Contact",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = TextHighContrast,
      )
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Contact Name") },
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextHighContrast,
            unfocusedTextColor = TextHighContrast,
            focusedBorderColor = EmergencyRed,
            unfocusedBorderColor = SurfaceBorder,
          ),
        )

        OutlinedTextField(
          value = phone,
          onValueChange = { phone = it },
          label = { Text("Phone Number (+91...)") },
          modifier = Modifier.fillMaxWidth(),
          colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextHighContrast,
            unfocusedTextColor = TextHighContrast,
            focusedBorderColor = EmergencyRed,
            unfocusedBorderColor = SurfaceBorder,
          ),
        )

        Text(
          text = "Relationship",
          style = MaterialTheme.typography.labelSmall,
          color = TextMediumContrast,
        )

        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          relationshipPresets.forEach { rel ->
            val selected = relationship.equals(rel, ignoreCase = true)
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (selected) Color(0xFF00363A) else CardSurfaceVariant)
                .border(1.dp, if (selected) AiCyan else SurfaceBorder, RoundedCornerShape(6.dp))
                .clickable { relationship = rel }
                .padding(horizontal = 8.dp, vertical = 5.dp),
            ) {
              Text(
                text = rel,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal),
                color = if (selected) AiCyan else TextMediumContrast,
              )
            }
          }
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.clickable { isPrimary = !isPrimary },
        ) {
          Checkbox(
            checked = isPrimary,
            onCheckedChange = { isPrimary = it },
            colors = CheckboxDefaults.colors(checkedColor = ActionAmber),
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Set as Primary Emergency Contact",
            style = MaterialTheme.typography.bodySmall,
            color = TextHighContrast,
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank() && phone.isNotBlank()) {
            onSave(name, phone, relationship, isPrimary)
          }
        },
        colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
      ) {
        Text("Save Contact", color = Color.White, fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = TextMediumContrast)
      }
    },
    containerColor = CardSurface,
  )
}
