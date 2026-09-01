package com.example.nearhelp.ui.assistant

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nearhelp.data.model.AiChatMessageUiModel
import com.example.nearhelp.data.model.ProtocolStepDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiCrisisAssistantScreen(
  onNavigateBack: () -> Unit,
  viewModel: AiCrisisAssistantViewModel,
  conditionId: String = "cardiac_arrest",
  sessionId: String = "DEMO-SESSION-001",
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()
  var inputQuestion by remember { mutableStateOf("") }
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

  LaunchedEffect(conditionId, sessionId) {
    viewModel.initialize(conditionId, sessionId)
  }

  val protocol = uiState.protocol
  val totalSteps = protocol?.steps?.size ?: 4
  val progress = if (totalSteps > 0) uiState.completedSteps.size.toFloat() / totalSteps else 0f

  Scaffold(
    topBar = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(Color(0xFF0D0F14))
          .padding(top = 8.dp, bottom = 12.dp, start = 12.dp, end = 12.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) {
              Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color(0xFF00E5FF)
              )
            }
            Column {
              Text(
                text = protocol?.conditionLabel ?: "Emergency Protocol",
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Level ${protocol?.severityLevel ?: 5} • ${protocol?.authority ?: "AHA / IRC Grounded"}",
                color = Color(0xFF00E5FF),
                fontSize = 11.sp
              )
            }
          }

          // Section 134A Badge
          Surface(
            color = Color(0xFF1E3A2F),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E676))
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = "Legal Shield",
                tint = Color(0xFF00E676),
                modifier = Modifier.size(13.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Sec 134A Protected",
                color = Color(0xFF00E676),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Progress Bar
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
              .weight(1f)
              .height(6.dp)
              .clip(RoundedCornerShape(3.dp)),
            color = Color(0xFF00E5FF),
            trackColor = Color(0xFF1E2430),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "${uiState.completedSteps.size}/$totalSteps Done",
            color = Color.LightGray,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
          )
        }
      }
    },
    bottomBar = {
      Surface(
        color = Color(0xFF0D0F14),
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Button(
            onClick = { viewModel.setChatDrawerOpen(true) },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E2430)),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(
              imageVector = Icons.Default.MedicalServices,
              contentDescription = "AI Assistant",
              tint = Color(0xFF00E5FF),
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Ask AI Assistant", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold)
          }

          Button(
            onClick = { viewModel.requestHandover() },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(
              imageVector = Icons.Default.LocalHospital,
              contentDescription = "Handover",
              tint = Color.White,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("108 Handover", color = Color.White, fontWeight = FontWeight.Bold)
          }
        }
      }
    },
    containerColor = Color(0xFF000000),
    modifier = modifier.fillMaxSize()
  ) { innerPadding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(horizontal = 14.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      contentPadding = PaddingValues(top = 10.dp, bottom = 16.dp)
    ) {
      // 1. CPR Metronome Card (If CPR condition)
      if (protocol?.cprBpm != null && protocol.cprBpm > 0) {
        item {
          CprMetronomeCard(
            isActive = uiState.cprMetronomeActive,
            bpm = protocol.cprBpm,
            beatCount = uiState.cprBeatCount,
            isPulse = uiState.isHeartPulse,
            onToggle = { viewModel.toggleCprMetronome() }
          )
        }
      }

      // 2. Active Contraindication Alert Banner (if triggered)
      if (uiState.activeContraindication != null) {
        item {
          val alert = uiState.activeContraindication!!
          Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF3E1212)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF5252)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.Top
            ) {
              Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Warning",
                tint = Color(0xFFFF5252),
                modifier = Modifier.size(24.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = alert.warningTitle,
                  color = Color(0xFFFF5252),
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = alert.warningMessage,
                  color = Color.White,
                  fontSize = 11.5.sp,
                  lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = alert.actionDirective,
                  color = Color(0xFFFFD54F),
                  fontSize = 11.5.sp,
                  fontWeight = FontWeight.SemiBold
                )
              }
              IconButton(
                onClick = { viewModel.dismissContraindication() },
                modifier = Modifier.size(20.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "Dismiss",
                  tint = Color.LightGray
                )
              }
            }
          }
        }
      }

      // 3. Protocol Steps Section Header
      item {
        Text(
          text = "EVIDENCE-BASED ACTION PROTOCOL",
          color = Color.Gray,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.5.sp,
          modifier = Modifier.padding(top = 4.dp)
        )
      }

      // 4. Protocol Steps List
      items(protocol?.steps ?: emptyList()) { step ->
        val isCompleted = uiState.completedSteps.contains(step.stepNumber)
        ProtocolStepCard(
          step = step,
          isCompleted = isCompleted,
          onToggle = { viewModel.toggleStep(step.stepNumber) }
        )
      }

      // 5. Statutory Immunity Footer Card
      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = Color(0xFF0F141C)),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E2838)),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = Color(0xFF00E5FF),
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Section 134A Good Samaritan Legal Immunity",
                color = Color(0xFF00E5FF),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Under the Motor Vehicles (Amendment) Act 2019 and Supreme Court WP(Civil) 235/2012, any bystander providing emergency first-aid holds absolute civil and criminal immunity.",
              color = Color.LightGray,
              fontSize = 10.5.sp,
              lineHeight = 15.sp
            )
          }
        }
      }
    }
  }

  // ==============================================================================
  // BYSTANDER AI ASSISTANT BOTTOM SHEET
  // ==============================================================================
  if (uiState.isChatDrawerOpen) {
    ModalBottomSheet(
      onDismissRequest = { viewModel.setChatDrawerOpen(false) },
      sheetState = sheetState,
      containerColor = Color(0xFF12141A)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .height(520.dp)
          .padding(horizontal = 14.dp, vertical = 8.dp)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .background(Color(0xFF00E676), CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Bystander AI Clinical Assistant",
              color = Color.White,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold
            )
          }
          IconButton(onClick = { viewModel.setChatDrawerOpen(false) }) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.LightGray)
          }
        }

        // Quick Suggestion Chips
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          contentPadding = PaddingValues(vertical = 6.dp)
        ) {
          items(uiState.quickQuestions) { q ->
            Surface(
              color = Color(0xFF1E2430),
              shape = RoundedCornerShape(14.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E384A)),
              modifier = Modifier.clickable { viewModel.sendChatMessage(q) }
            ) {
              Text(
                text = q,
                color = Color(0xFF00E5FF),
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
              )
            }
          }
        }

        // Chat Message Stream
        LazyColumn(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .padding(vertical = 6.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          if (uiState.chatMessages.isEmpty()) {
            item {
              Text(
                text = "💡 Tap a suggestion above or ask any first-aid question.\nAnswers are clinically grounded with AHA/IRC citations.",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 16.dp)
              )
            }
          }
          items(uiState.chatMessages) { msg ->
            ChatMessageBubble(msg = msg)
          }
        }

        // Input Field
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedTextField(
            value = inputQuestion,
            onValueChange = { inputQuestion = it },
            placeholder = { Text("Ask emergency guidance...", color = Color.Gray, fontSize = 12.sp) },
            modifier = Modifier.weight(1f),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = Color(0xFF00E5FF),
              unfocusedBorderColor = Color(0xFF2E384A),
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White,
              focusedContainerColor = Color(0xFF181C24),
              unfocusedContainerColor = Color(0xFF181C24)
            ),
            shape = RoundedCornerShape(12.dp),
            maxLines = 2
          )
          Spacer(modifier = Modifier.width(6.dp))
          IconButton(
            onClick = {
              if (inputQuestion.isNotBlank()) {
                viewModel.sendChatMessage(inputQuestion.trim())
                inputQuestion = ""
              }
            },
            modifier = Modifier
              .background(Color(0xFF00E5FF), RoundedCornerShape(12.dp))
              .size(48.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Send,
              contentDescription = "Send",
              tint = Color.Black
            )
          }
        }
      }
    }
  }

  // ==============================================================================
  // CLINICAL HANDOVER SUMMARY DIALOG
  // ==============================================================================
  if (uiState.isHandoverModalOpen && uiState.handoverSummary != null) {
    val report = uiState.handoverSummary!!
    AlertDialog(
      onDismissRequest = { viewModel.dismissHandoverModal() },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.LocalHospital, contentDescription = null, tint = Color(0xFF4CAF50))
          Spacer(modifier = Modifier.width(8.dp))
          Text("108 ALS Paramedic Handover", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
      },
      text = {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text("Report ID: ${report.reportId}", color = Color.Gray, fontSize = 11.sp)
          Text("Incident Code: ${report.incidentCode}", color = Color.Gray, fontSize = 11.sp)
          Spacer(modifier = Modifier.height(6.dp))
          Text("Diagnostic: ${report.diagnosticSummary}", color = Color(0xFF00E5FF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
          Text("Estimated CPR: ~${report.cprCompressionsEstimated} compressions (110 BPM)", color = Color.LightGray, fontSize = 11.5.sp)
          Text("AED Deployed: ${if (report.aedDeployed) "Yes (1 Shock Delivered)" else "No"}", color = Color.LightGray, fontSize = 11.5.sp)
          Text("Destination: ${report.destinationHospital}", color = Color.LightGray, fontSize = 11.5.sp)
          Spacer(modifier = Modifier.height(6.dp))
          Text("Audit Signature: ${report.digitalSignatureHash.take(28)}...", color = Color.Gray, fontSize = 9.5.sp)
        }
      },
      confirmButton = {
        TextButton(onClick = { viewModel.dismissHandoverModal() }) {
          Text("Close", color = Color(0xFF00E5FF), fontWeight = FontWeight.Bold)
        }
      },
      containerColor = Color(0xFF181C24)
    )
  }
}

@Composable
fun CprMetronomeCard(
  isActive: Boolean,
  bpm: Int,
  beatCount: Long,
  isPulse: Boolean,
  onToggle: () -> Unit
) {
  val scale by animateFloatAsState(
    targetValue = if (isPulse) 1.25f else 1.0f,
    animationSpec = tween(durationMillis = 100),
    label = "heartPulse"
  )

  Card(
    colors = CardDefaults.cardColors(containerColor = Color(0xFF180A0A)),
    border = androidx.compose.foundation.BorderStroke(1.dp, if (isActive) Color(0xFFFF1744) else Color(0xFF3E1A1A)),
    shape = RoundedCornerShape(14.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .padding(12.dp)
        .fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Favorite,
          contentDescription = "Heartbeat",
          tint = if (isActive) Color(0xFFFF1744) else Color.Gray,
          modifier = Modifier
            .size(32.dp)
            .scale(scale)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text = "AHA / ERC 110 BPM Metronome",
            color = Color.White,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = if (isActive) "Beats: $beatCount (~545ms Cadence)" else "Tap to activate continuous audio cadence",
            color = if (isActive) Color(0xFFFF5252) else Color.Gray,
            fontSize = 11.sp
          )
        }
      }

      Button(
        onClick = onToggle,
        colors = ButtonDefaults.buttonColors(
          containerColor = if (isActive) Color(0xFFFF1744) else Color(0xFF2C1010)
        ),
        shape = RoundedCornerShape(10.dp)
      ) {
        Text(
          text = if (isActive) "STOP" else "START",
          color = Color.White,
          fontSize = 11.5.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }
  }
}

@Composable
fun ProtocolStepCard(
  step: ProtocolStepDto,
  isCompleted: Boolean,
  onToggle: () -> Unit
) {
  Card(
    colors = CardDefaults.cardColors(
      containerColor = if (isCompleted) Color(0xFF0D1E16) else Color(0xFF11141C)
    ),
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      if (isCompleted) Color(0xFF00E676) else Color(0xFF202634)
    ),
    shape = RoundedCornerShape(12.dp),
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onToggle() }
  ) {
    Row(
      modifier = Modifier.padding(12.dp),
      verticalAlignment = Alignment.Top
    ) {
      Box(
        modifier = Modifier
          .size(24.dp)
          .background(
            if (isCompleted) Color(0xFF00E676) else Color(0xFF1E2430),
            CircleShape
          ),
        contentAlignment = Alignment.Center
      ) {
        if (isCompleted) {
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Completed",
            tint = Color.Black,
            modifier = Modifier.size(16.dp)
          )
        } else {
          Text(
            text = "${step.stepNumber}",
            color = Color(0xFF00E5FF),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }

      Spacer(modifier = Modifier.width(10.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = step.title,
          color = if (isCompleted) Color(0xFF00E676) else Color.White,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = step.actionInstruction,
          color = Color.LightGray,
          fontSize = 11.5.sp,
          lineHeight = 16.sp
        )
        if (!step.warningNote.isNullOrBlank()) {
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "⚠️ ${step.warningNote}",
            color = Color(0xFFFFB74D),
            fontSize = 10.5.sp,
            fontWeight = FontWeight.SemiBold
          )
        }
      }
    }
  }
}

@Composable
fun ChatMessageBubble(msg: AiChatMessageUiModel) {
  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = if (msg.isUser) Alignment.End else Alignment.Start
  ) {
    Surface(
      color = if (msg.isUser) Color(0xFF00E5FF) else Color(0xFF1A1F2B),
      shape = RoundedCornerShape(12.dp),
      border = if (!msg.isUser) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2B3448)) else null,
      modifier = Modifier.fillMaxWidth(0.9f)
    ) {
      Column(modifier = Modifier.padding(10.dp)) {
        if (!msg.isUser && msg.highlightBadge != null) {
          Text(
            text = msg.highlightBadge,
            color = Color(0xFF00E5FF),
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 2.dp)
          )
        }
        Text(
          text = msg.text,
          color = if (msg.isUser) Color.Black else Color.White,
          fontSize = 12.sp,
          lineHeight = 17.sp
        )
        Text(
          text = msg.timestamp,
          color = if (msg.isUser) Color.DarkGray else Color.Gray,
          fontSize = 9.sp,
          modifier = Modifier.align(Alignment.End).padding(top = 2.dp)
        )
      }
    }
  }
}
