package com.example.nearhelp.ui.assistant

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.nearhelp.data.model.AiChatMessageUiModel
import com.example.nearhelp.data.model.GroundedProtocolDto
import com.example.nearhelp.data.model.ProtocolStepDto
import kotlinx.coroutines.launch
import com.example.nearhelp.theme.StatusSafeGreen
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
import com.example.nearhelp.theme.VictimPurpleBorder
import com.example.nearhelp.theme.VictimPurpleCard
import com.example.nearhelp.theme.VictimTextDark
import com.example.nearhelp.theme.VictimTextMuted
import com.example.nearhelp.ui.victim.NearHelpTopBar
import com.example.nearhelp.ui.victim.VictimBottomNavBar
import com.example.nearhelp.ui.victim.VictimNavTab
import com.example.nearhelp.ui.victim.VictimShapes

private data class EmergencyTopicItem(
  val conditionId: String,
  val shortLabel: String,
  val fullName: String,
  val category: String,
  val bg: Color,
  val border: Color,
  val icon: ImageVector,
  val isFeatured: Boolean = false,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiCrisisAssistantScreen(
  onNavigateBack: () -> Unit,
  onNavigateToHome: () -> Unit = onNavigateBack,
  onNavigateToMap: () -> Unit = {},
  onNavigateToProfile: () -> Unit = {},
  viewModel: AiCrisisAssistantViewModel,
  conditionId: String = "cardiac_arrest",
  sessionId: String = "DEMO-SESSION-001",
  modifier: Modifier = Modifier,
  showBottomBar: Boolean = true,
) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsState()
  var inputQuestion by remember { mutableStateOf("") }
  var isTopicsExpanded by remember { mutableStateOf(false) }
  var attachedMediaName by remember { mutableStateOf<String?>(null) }
  var attachedMediaUri by remember { mutableStateOf<Uri?>(null) }
  var attachedMediaBitmap by remember { mutableStateOf<Bitmap?>(null) }

  val filePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    if (uri != null) {
      attachedMediaUri = uri
      attachedMediaBitmap = null
      val resolved = resolveFileName(context, uri) ?: "attached_document"
      attachedMediaName = resolved
      viewModel.setChatDrawerOpen(true)
    }
  }

  val cameraLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicturePreview()
  ) { bitmap: Bitmap? ->
    if (bitmap != null) {
      attachedMediaBitmap = bitmap
      attachedMediaUri = null
      val timeStamp = SimpleDateFormat("HHmmss", Locale.getDefault()).format(Date())
      attachedMediaName = "camera_capture_$timeStamp.jpg"
      viewModel.setChatDrawerOpen(true)
    }
  }

  val cameraPermissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    if (isGranted) {
      cameraLauncher.launch(null)
    } else {
      Toast.makeText(context, "Camera permission needed to capture photos", Toast.LENGTH_SHORT).show()
    }
  }

  val triggerCamera: () -> Unit = {
    val hasCamera = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    if (hasCamera) {
      cameraLauncher.launch(null)
    } else {
      cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }
  }

  val triggerFilePicker: () -> Unit = {
    filePickerLauncher.launch("*/*")
  }

  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val coroutineScope = rememberCoroutineScope()
  val listState = rememberLazyListState()

  LaunchedEffect(conditionId, sessionId) { viewModel.initialize(conditionId, sessionId) }

  val protocols = uiState.protocols.ifEmpty { listOfNotNull(uiState.protocol) }
  val initialPageIndex = remember(protocols, conditionId) {
    val idx = protocols.indexOfFirst { it.conditionId == conditionId }
    if (idx >= 0) idx else 0
  }
  val pagerState = rememberPagerState(
    initialPage = initialPageIndex,
    pageCount = { protocols.size }
  )

  val allEmergencyTopics = remember {
    listOf(
      EmergencyTopicItem("cardiac_arrest", "CPR", "Cardiac Arrest (CPR)", "Resuscitation", VictimPinkCard, VictimPinkBorder, Icons.Default.Favorite, isFeatured = true),
      EmergencyTopicItem("leg_fracture", "Fractures", "Fractures & Trauma", "Trauma", VictimBlueCard, VictimBlueBorder, Icons.Default.Add, isFeatured = true),
      EmergencyTopicItem("severe_bleeding", "Bleeding", "Severe Bleeding", "Trauma", VictimPinkCard, VictimPinkBorder, Icons.Default.Warning, isFeatured = true),
      EmergencyTopicItem("choking", "Choking", "Choking & Airway", "Airway", VictimOrangeCard, VictimOrangeBorder, Icons.Default.Person, isFeatured = true),
      EmergencyTopicItem("burns", "Burns", "Severe Burns & Scalds", "Trauma", VictimPurpleCard, VictimPurpleBorder, Icons.Default.Star, isFeatured = true),
      EmergencyTopicItem("seizures", "Seizures", "Seizures & Convulsions", "Neurological", VictimGreenCard, VictimGreenBorder, Icons.Default.Shield, isFeatured = true),
      EmergencyTopicItem("stroke", "Stroke", "Stroke / FAST Signs", "Neurological", VictimPinkCard, VictimPinkBorder, Icons.Default.Warning),
      EmergencyTopicItem("asthma", "Asthma", "Asthma & Wheezing", "Respiratory", VictimBlueCard, VictimBlueBorder, Icons.Default.Info),
      EmergencyTopicItem("anaphylaxis", "Allergy", "Severe Allergy / EpiPen", "Allergic", VictimOrangeCard, VictimOrangeBorder, Icons.Default.Warning),
      EmergencyTopicItem("poisoning", "Poisoning", "Toxin Ingestion", "Toxicology", VictimPurpleCard, VictimPurpleBorder, Icons.Default.Shield),
      EmergencyTopicItem("heatstroke", "Heatstroke", "Heat Exhaustion", "Environmental", VictimOrangeCard, VictimOrangeBorder, Icons.Default.Star),
      EmergencyTopicItem("hypothermia", "Cold / Hypo", "Hypothermia & Freezing", "Environmental", VictimBlueCard, VictimBlueBorder, Icons.Default.Info),
      EmergencyTopicItem("snakebite", "Snakebite", "Snakebite Envenomation", "Toxicology", VictimGreenCard, VictimGreenBorder, Icons.Default.Warning),
      EmergencyTopicItem("head_injury", "Head Trauma", "Head & Spine Injury", "Trauma", VictimPinkCard, VictimPinkBorder, Icons.Default.Shield),
      EmergencyTopicItem("diabetic_emergency", "Diabetic", "Diabetic Hypoglycemia", "Metabolic", VictimPurpleCard, VictimPurpleBorder, Icons.Default.Favorite),
      EmergencyTopicItem("electric_shock", "Electrocution", "Electric Shock Trauma", "Environmental", VictimOrangeCard, VictimOrangeBorder, Icons.Default.Warning),
      EmergencyTopicItem("drowning", "Drowning", "Drowning & Water Rescue", "Resuscitation", VictimBlueCard, VictimBlueBorder, Icons.Default.Favorite),
      EmergencyTopicItem("shock", "Shock", "Circulatory Collapse", "Cardiovascular", VictimPinkCard, VictimPinkBorder, Icons.Default.Warning)
    )
  }

  val featuredTopics = remember(allEmergencyTopics) {
    allEmergencyTopics.filter { it.isFeatured }
  }

  val selectEmergencyTopic: (String) -> Unit = { targetCond ->
    val pageIdx = protocols.indexOfFirst { it.conditionId == targetCond }
    if (pageIdx >= 0) {
      coroutineScope.launch {
        pagerState.animateScrollToPage(
          page = pageIdx,
          animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
        )
        val targetItemIndex = if (uiState.activeContraindication != null) 4 else 3
        listState.animateScrollToItem(targetItemIndex)
      }
    }
  }

  Scaffold(
    topBar = {
      NearHelpTopBar(
        onAvatarClick = onNavigateToProfile,
        modifier = Modifier
          .fillMaxWidth()
          .background(VictimBackground)
          .statusBarsPadding()
          .padding(horizontal = 16.dp, vertical = 10.dp),
      )
    },
    bottomBar = {
      androidx.compose.foundation.layout.Column(modifier = Modifier.fillMaxWidth()) {
        if (attachedMediaName != null && !uiState.isChatDrawerOpen) {
          Row(
            modifier = Modifier
              .padding(horizontal = 14.dp, vertical = 2.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(VictimBlueCard)
              .border(1.dp, VictimBlueBorder, RoundedCornerShape(8.dp))
              .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            if (attachedMediaBitmap != null) {
              Image(
                bitmap = attachedMediaBitmap!!.asImageBitmap(),
                contentDescription = "Attached photo preview",
                modifier = Modifier
                  .size(22.dp)
                  .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop
              )
              Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
              text = if (attachedMediaBitmap != null) "📷 $attachedMediaName" else "📄 $attachedMediaName",
              fontSize = 11.5.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFF2563EB)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Remove attachment",
              tint = Color(0xFF2563EB),
              modifier = Modifier
                .size(13.dp)
                .clickable {
                  attachedMediaName = null
                  attachedMediaBitmap = null
                  attachedMediaUri = null
                }
            )
          }
        }
        Surface(color = Color.White, shadowElevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color(0xFFF1F5F9))
                .clickable { viewModel.setHistorySidePanelOpen(true) },
              contentAlignment = Alignment.Center
            ) {
              Icon(imageVector = Icons.Default.Menu, contentDescription = "Chat History", tint = VictimTextDark, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
              value = inputQuestion, onValueChange = { inputQuestion = it },
              placeholder = { Text("Ask something...", color = VictimTextMuted, fontSize = 14.sp) },
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(24.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VictimBorder, unfocusedBorderColor = VictimBorder,
                focusedContainerColor = Color(0xFFF8FAFC), unfocusedContainerColor = Color(0xFFF8FAFC),
                focusedTextColor = VictimTextDark, unfocusedTextColor = VictimTextDark,
              ),
              maxLines = 1,
              trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 4.dp)) {
                  IconButton(
                    onClick = { triggerFilePicker() },
                    modifier = Modifier.size(30.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.AttachFile,
                      contentDescription = "Attach Document",
                      tint = if (attachedMediaUri != null) VictimPrimary else VictimTextMuted,
                      modifier = Modifier.size(17.dp)
                    )
                  }
                  IconButton(
                    onClick = { triggerCamera() },
                    modifier = Modifier.size(30.dp)
                  ) {
                    Icon(
                      imageVector = Icons.Default.CameraAlt,
                      contentDescription = "Camera / Scan",
                      tint = if (attachedMediaBitmap != null) VictimPrimary else VictimTextMuted,
                      modifier = Modifier.size(17.dp)
                    )
                  }
                }
              }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
              modifier = Modifier.size(42.dp).clip(CircleShape).background(VictimPinkCard)
                .clickable {
                  val fullQuery = buildString {
                    if (!attachedMediaName.isNullOrBlank()) {
                      append("[Attached: $attachedMediaName] ")
                    }
                    append(inputQuestion.trim())
                  }.trim()
                  if (fullQuery.isNotBlank()) {
                    viewModel.setChatDrawerOpen(true)
                    viewModel.sendChatMessage(fullQuery)
                    inputQuestion = ""
                    attachedMediaName = null
                    attachedMediaBitmap = null
                    attachedMediaUri = null
                  } else {
                    viewModel.setChatDrawerOpen(true)
                  }
                },
              contentAlignment = Alignment.Center,
            ) {
              Icon(imageVector = Icons.Default.Mic, contentDescription = "Voice", tint = VictimPrimary, modifier = Modifier.size(20.dp))
            }
          }
        }
        if (showBottomBar) {
          VictimBottomNavBar(
            selected = VictimNavTab.CHAT,
            onSelect = { tab ->
              when (tab) {
                VictimNavTab.HOME -> onNavigateToHome()
                VictimNavTab.CHAT -> Unit
                VictimNavTab.MAP -> onNavigateToMap()
                VictimNavTab.PROFILE -> onNavigateToProfile()
              }
            }
          )
        }
      }
    },
    containerColor = VictimBackground,
    modifier = modifier.fillMaxSize(),
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 6.dp, bottom = 52.dp),
      ) {
      // Medical Assistant unified card
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(VictimShapes.Card20)
            .background(VictimPinkCard)
            .border(1.dp, VictimPinkBorder, VictimShapes.Card20)
            .clickable { viewModel.setChatDrawerOpen(true) }
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Medical Assistant",
              fontSize = 20.sp,
              fontWeight = FontWeight.Black,
              color = VictimTextDark,
              letterSpacing = (-0.3).sp,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Ask about first aid, symptoms, or emergencies.",
              fontSize = 13.sp,
              color = VictimTextMuted,
              lineHeight = 18.sp,
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Box(
            modifier = Modifier
              .size(54.dp)
              .clip(CircleShape)
              .background(Color.White)
              .border(1.dp, VictimPinkBorder, CircleShape),
            contentAlignment = Alignment.Center,
          ) {
            Text(text = "🧑‍⚕️", fontSize = 26.sp)
            Box(
              modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(12.dp)
                .clip(CircleShape)
                .background(StatusSafeGreen)
                .border(2.dp, Color.White, CircleShape),
            )
          }
        }
      }
      // Quick Topics Header
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(text = "Quick Topics", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = VictimTextDark)
          Text(
            text = if (isTopicsExpanded) "Show top 6 ‹" else "View all (18) ›",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = VictimPrimary,
            modifier = Modifier.clickable { isTopicsExpanded = !isTopicsExpanded }
          )
        }
      }
      // Quick Topics Expandable Content
      item {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          if (!isTopicsExpanded) {
            featuredTopics.chunked(3).forEach { row ->
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { topic ->
                  Row(
                    modifier = Modifier
                      .weight(1f)
                      .clip(RoundedCornerShape(14.dp))
                      .background(topic.bg)
                      .border(1.dp, topic.border, RoundedCornerShape(14.dp))
                      .clickable { selectEmergencyTopic(topic.conditionId) }
                      .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Icon(imageVector = topic.icon, contentDescription = null, tint = VictimTextDark, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(text = topic.shortLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = VictimTextDark, maxLines = 1, overflow = TextOverflow.Ellipsis)
                  }
                }
              }
            }
          } else {
            allEmergencyTopics.chunked(2).forEach { row ->
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { topic ->
                  Row(
                    modifier = Modifier
                      .weight(1f)
                      .clip(RoundedCornerShape(14.dp))
                      .background(topic.bg)
                      .border(1.dp, topic.border, RoundedCornerShape(14.dp))
                      .clickable { selectEmergencyTopic(topic.conditionId) }
                      .padding(horizontal = 10.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Box(
                      modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.85f)),
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(imageVector = topic.icon, contentDescription = null, tint = VictimTextDark, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                      Text(text = topic.shortLabel, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = VictimTextDark, maxLines = 1, overflow = TextOverflow.Ellipsis)
                      Text(text = topic.category, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = VictimTextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                  }
                }
                if (row.size == 1) {
                  Spacer(modifier = Modifier.weight(1f))
                }
              }
            }
          }
        }
      }
      // Active Contraindication Alert Banner (if triggered)
      if (uiState.activeContraindication != null) {
        item {
          val alert = uiState.activeContraindication!!
          Card(
            colors = CardDefaults.cardColors(containerColor = VictimPinkCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, VictimPinkBorder),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
              Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = VictimPrimary, modifier = Modifier.size(24.dp))
              Spacer(modifier = Modifier.width(10.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(text = alert.warningTitle, color = VictimPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(text = alert.warningMessage, color = VictimTextDark, fontSize = 12.sp)
                Text(text = alert.actionDirective, color = VictimTextDark, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
              }
              IconButton(onClick = { viewModel.dismissContraindication() }, modifier = Modifier.size(20.dp)) {
                Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = VictimTextMuted)
              }
            }
          }
        }
      }

      // Emergency Protocols Carousel Header
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 4.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Emergency Protocols",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = VictimTextDark
          )
          if (protocols.isNotEmpty()) {
            if (protocols.size <= 8) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                protocols.indices.forEach { index ->
                  val isSelected = pagerState.currentPage == index
                  val indicatorWidth by animateDpAsState(
                    targetValue = if (isSelected) 18.dp else 6.dp,
                    animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
                    label = "indicatorWidth"
                  )
                  val indicatorColor by animateColorAsState(
                    targetValue = if (isSelected) VictimPrimary else Color(0xFFCBD5E1),
                    animationSpec = tween(durationMillis = 250),
                    label = "indicatorColor"
                  )
                  Box(
                    modifier = Modifier
                      .height(6.dp)
                      .width(indicatorWidth)
                      .clip(RoundedCornerShape(3.dp))
                      .background(indicatorColor)
                      .clickable {
                        coroutineScope.launch {
                          pagerState.animateScrollToPage(
                            page = index,
                            animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
                          )
                        }
                      }
                  )
                }
              }
            } else {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(100.dp))
                  .background(Color(0xFFF1F5F9))
                  .padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "${pagerState.currentPage + 1} / ${protocols.size}",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = VictimTextMuted
                )
              }
            }
          }
        }
      }

      // Swipeable Carousel of Unified Problem + Solution Cards (Uniform height to eliminate jerking)
      if (protocols.isNotEmpty()) {
        item {
          HorizontalPager(
            state = pagerState,
            key = { pageIndex -> protocols.getOrNull(pageIndex)?.conditionId ?: pageIndex },
            beyondViewportPageCount = 2,
            modifier = Modifier
              .fillMaxWidth()
              .height(590.dp),
            pageSpacing = 12.dp,
            flingBehavior = PagerDefaults.flingBehavior(
              state = pagerState,
              pagerSnapDistance = PagerSnapDistance.atMost(1),
              snapAnimationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMediumLow
              )
            )
          ) { pageIndex ->
            val currentProto = protocols.getOrNull(pageIndex)
            if (currentProto != null) {
              val completed = uiState.completedStepsMap[currentProto.conditionId] ?: emptySet()
              UnifiedEmergencyProtocolCard(
                protocol = currentProto,
                completedSteps = completed,
                onToggleStep = { stepNum -> viewModel.toggleStep(currentProto.conditionId, stepNum) },
                modifier = Modifier.fillMaxSize()
              )
            }
          }
        }
      }
      // Recent Conversations
      item {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
          Text(text = "Recent Conversations", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = VictimTextDark)
          Text(text = "View all  ›", fontSize = 13.sp, color = VictimTextMuted, modifier = Modifier.clip(RoundedCornerShape(100.dp)).background(Color(0xFFF1F5F9)).padding(horizontal = 12.dp, vertical = 6.dp).clickable { viewModel.setChatDrawerOpen(true) })
        }
      }
      val recents = if (uiState.chatMessages.isNotEmpty()) uiState.chatMessages.takeLast(3).map { it.text.take(32) to it.timestamp } else listOf("Steps for adult CPR..." to "Yesterday", "How to manage heavy bleeding..." to "Sep 5", "Simple daily habits..." to "Aug 28")
      items(recents) { (title, date) ->
        Row(modifier = Modifier.fillMaxWidth().clickable { viewModel.setChatDrawerOpen(true) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
          Box(modifier = Modifier.size(46.dp).clip(CircleShape).background(VictimPinkCard), contentAlignment = Alignment.Center) {
            Icon(imageVector = Icons.Default.Favorite, contentDescription = null, tint = VictimPrimary, modifier = Modifier.size(22.dp))
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = VictimTextDark)
            Text(text = title, fontSize = 12.5.sp, color = VictimTextMuted)
          }
          Text(text = date, fontSize = 12.sp, color = VictimTextMuted)
          Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = VictimTextMuted, modifier = Modifier.size(16.dp))
        }
      }
    }

    Surface(
      shape = RoundedCornerShape(100.dp),
      color = Color(0xFF2563EB),
      shadowElevation = 6.dp,
      modifier = Modifier
        .align(Alignment.BottomStart)
        .padding(start = 12.dp, bottom = 10.dp)
        .height(42.dp)
        .clickable {
          viewModel.setChatDrawerOpen(true)
          viewModel.sendChatMessage("Provide clinical guidance on today's health tip: Stay hydrated and get enough sleep.")
        }
    ) {
      Row(
        modifier = Modifier.padding(start = 14.dp, end = 7.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(text = "💡", fontSize = 15.sp)
        Spacer(modifier = Modifier.width(7.dp))
        Text(
          text = "Daily Tip",
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = Color.White
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
          modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.22f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "Daily tip guidance",
            tint = Color.White,
            modifier = Modifier.size(15.dp)
          )
        }
      }
    }
  }
}

  if (uiState.isChatDrawerOpen) {
    ModalBottomSheet(onDismissRequest = { viewModel.setChatDrawerOpen(false) }, sheetState = sheetState, containerColor = Color.White) {
      Column(modifier = Modifier.fillMaxWidth().height(520.dp).padding(horizontal = 14.dp, vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(8.dp).background(StatusSafeGreen, CircleShape))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "Bystander AI Clinical Assistant", color = VictimTextDark, fontSize = 15.sp, fontWeight = FontWeight.Bold)
          }
          IconButton(onClick = { viewModel.setChatDrawerOpen(false) }) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = VictimTextMuted)
          }
        }
        androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), contentPadding = PaddingValues(vertical = 6.dp)) {
          items(uiState.quickQuestions) { q ->
            Surface(color = VictimBlueCard, shape = RoundedCornerShape(14.dp), border = androidx.compose.foundation.BorderStroke(1.dp, VictimBlueBorder), modifier = Modifier.clickable { viewModel.sendChatMessage(q) }) {
              Text(text = q, color = VictimTextDark, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
            }
          }
        }
        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          if (uiState.chatMessages.isEmpty()) {
            item { Text(text = "Tap a suggestion above or ask any first-aid question.", color = VictimTextMuted, fontSize = 12.sp, modifier = Modifier.padding(vertical = 16.dp)) }
          }
          items(uiState.chatMessages) { msg -> ChatMessageBubble(msg = msg) }
        }
        if (attachedMediaName != null) {
          Row(
            modifier = Modifier
              .padding(bottom = 6.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(VictimBlueCard)
              .border(1.dp, VictimBlueBorder, RoundedCornerShape(8.dp))
              .padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            if (attachedMediaBitmap != null) {
              Image(
                bitmap = attachedMediaBitmap!!.asImageBitmap(),
                contentDescription = "Attached photo preview",
                modifier = Modifier
                  .size(24.dp)
                  .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop
              )
              Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
              text = if (attachedMediaBitmap != null) "📷 $attachedMediaName" else "📄 $attachedMediaName",
              fontSize = 11.5.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFF2563EB)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Remove attachment",
              tint = Color(0xFF2563EB),
              modifier = Modifier
                .size(13.dp)
                .clickable {
                  attachedMediaName = null
                  attachedMediaBitmap = null
                  attachedMediaUri = null
                }
            )
          }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
          OutlinedTextField(
            value = inputQuestion,
            onValueChange = { inputQuestion = it },
            placeholder = { Text("Ask emergency guidance...", color = VictimTextMuted, fontSize = 13.sp) },
            modifier = Modifier.weight(1f),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = VictimPrimary,
              unfocusedBorderColor = VictimBorder,
              focusedTextColor = VictimTextDark,
              unfocusedTextColor = VictimTextDark,
              focusedContainerColor = Color(0xFFF8FAFC),
              unfocusedContainerColor = Color(0xFFF8FAFC)
            ),
            shape = RoundedCornerShape(22.dp),
            maxLines = 3,
            trailingIcon = {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 4.dp)
              ) {
                IconButton(
                  onClick = { triggerFilePicker() },
                  modifier = Modifier.size(32.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.AttachFile,
                    contentDescription = "Attach Document",
                    tint = if (attachedMediaUri != null) VictimPrimary else VictimTextMuted,
                    modifier = Modifier.size(18.dp)
                  )
                }
                IconButton(
                  onClick = { triggerCamera() },
                  modifier = Modifier.size(32.dp)
                ) {
                  Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Camera / Scan",
                    tint = if (attachedMediaBitmap != null) VictimPrimary else VictimTextMuted,
                    modifier = Modifier.size(18.dp)
                  )
                }
              }
            }
          )
          Spacer(modifier = Modifier.width(8.dp))
          Box(
            modifier = Modifier
              .size(44.dp)
              .clip(CircleShape)
              .background(VictimPrimary)
              .clickable {
                val fullQuery = buildString {
                  if (!attachedMediaName.isNullOrBlank()) {
                    append("[Attached: $attachedMediaName] ")
                  }
                  append(inputQuestion.trim())
                }.trim()
                if (fullQuery.isNotBlank()) {
                  viewModel.sendChatMessage(fullQuery)
                  inputQuestion = ""
                  attachedMediaName = null
                  attachedMediaBitmap = null
                  attachedMediaUri = null
                }
              },
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = "Send",
              tint = Color.White,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }
    }
  }

  if (uiState.isHandoverModalOpen && uiState.handoverSummary != null) {
    val report = uiState.handoverSummary!!
    androidx.compose.material3.AlertDialog(
      onDismissRequest = { viewModel.dismissHandoverModal() },
      title = { Text("108 ALS Paramedic Handover", color = VictimTextDark, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
      text = {
        Column {
          Text("Report ID: ${report.reportId}", color = VictimTextMuted, fontSize = 11.sp)
          Text("Diagnostic: ${report.diagnosticSummary}", color = VictimPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
          Text("Destination: ${report.destinationHospital}", color = VictimTextMuted, fontSize = 11.5.sp)
        }
      },
      confirmButton = {
        androidx.compose.material3.TextButton(onClick = { viewModel.dismissHandoverModal() }) {
          Text("Close", color = VictimPrimary, fontWeight = FontWeight.Bold)
        }
      },
      containerColor = Color.White,
    )
  }

  // Side Panel Drawer for Previous Chat History
  AnimatedVisibility(
    visible = uiState.isHistorySidePanelOpen,
    enter = fadeIn(animationSpec = tween(200)),
    exit = fadeOut(animationSpec = tween(200))
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.45f))
        .clickable { viewModel.setHistorySidePanelOpen(false) }
    )
  }

  AnimatedVisibility(
    visible = uiState.isHistorySidePanelOpen,
    enter = slideInHorizontally(
      initialOffsetX = { -it },
      animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
    ),
    exit = slideOutHorizontally(
      targetOffsetX = { -it },
      animationSpec = tween(durationMillis = 240, easing = FastOutSlowInEasing)
    )
  ) {
    Surface(
      modifier = Modifier
        .fillMaxHeight()
        .widthIn(max = 320.dp)
        .fillMaxWidth(0.84f)
        .statusBarsPadding()
        .navigationBarsPadding(),
      color = Color.White,
      shadowElevation = 16.dp,
      shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(vertical = 14.dp)
      ) {
        // Top Header
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(VictimBlueCard),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = null,
                tint = Color(0xFF2563EB),
                modifier = Modifier.size(18.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Chat History",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = VictimTextDark
              )
              Text(
                text = "Past AI Consultations",
                fontSize = 11.5.sp,
                color = VictimTextMuted
              )
            }
          }
          IconButton(
            onClick = { viewModel.setHistorySidePanelOpen(false) },
            modifier = Modifier.size(32.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = VictimTextMuted,
              modifier = Modifier.size(18.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // New Chat Button
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = VictimPinkCard,
          border = androidx.compose.foundation.BorderStroke(1.dp, VictimPinkBorder),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { viewModel.startNewChat() }
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(VictimPrimary),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "New Consultation",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "Start New Consultation",
              fontSize = 13.5.sp,
              fontWeight = FontWeight.Bold,
              color = VictimTextDark
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "PREVIOUS CHATS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = VictimTextMuted,
            letterSpacing = 0.5.sp
          )
          Text(
            text = "${uiState.chatHistory.size} sessions",
            fontSize = 11.sp,
            color = VictimPrimary,
            fontWeight = FontWeight.SemiBold
          )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LazyColumn(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          val grouped = uiState.chatHistory.groupBy { it.group }
          grouped.forEach { (groupLabel, sessions) ->
            item {
              Text(
                text = groupLabel.uppercase(),
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = VictimTextMuted,
                modifier = Modifier.padding(start = 6.dp, top = 8.dp, bottom = 2.dp)
              )
            }
            items(sessions) { session ->
              Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFF8FAFC),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { viewModel.loadChatSession(session) }
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Box(
                    modifier = Modifier
                      .size(36.dp)
                      .clip(CircleShape)
                      .background(VictimBlueCard),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(text = "💬", fontSize = 16.sp)
                  }
                  Spacer(modifier = Modifier.width(10.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = session.title,
                      fontSize = 13.sp,
                      fontWeight = FontWeight.Bold,
                      color = VictimTextDark,
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                      text = session.snippet,
                      fontSize = 11.5.sp,
                      color = VictimTextMuted,
                      maxLines = 2,
                      overflow = TextOverflow.Ellipsis,
                      lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                      text = session.timestamp,
                      fontSize = 10.5.sp,
                      fontWeight = FontWeight.Medium,
                      color = Color(0xFF2563EB)
                    )
                  }
                  Spacer(modifier = Modifier.width(6.dp))
                  Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Open session",
                    tint = VictimTextMuted,
                    modifier = Modifier.size(14.dp)
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

private data class ProtocolCardTheme(
  val icon: ImageVector,
  val tintColor: Color,
  val bgTint: Color,
  val severityBadge: String
)

@Composable
fun UnifiedEmergencyProtocolCard(
  protocol: GroundedProtocolDto,
  completedSteps: Set<Int>,
  onToggleStep: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  val totalSteps = protocol.steps.size
  val progress = remember(completedSteps.size, totalSteps) {
    if (totalSteps > 0) completedSteps.size.toFloat() / totalSteps else 0f
  }

  val theme = remember(protocol.conditionId) {
    when (protocol.conditionId) {
      "leg_fracture" -> ProtocolCardTheme(Icons.Default.Add, Color(0xFF2563EB), VictimBlueCard, "Level 4 • Urgent Trauma")
      "severe_bleeding" -> ProtocolCardTheme(Icons.Default.Warning, Color(0xFFDC2626), VictimPinkCard, "Level 5 • Critical Hemorrhage")
      "choking" -> ProtocolCardTheme(Icons.Default.Person, Color(0xFFD97706), VictimOrangeCard, "Level 5 • Airway Obstruction")
      "burns" -> ProtocolCardTheme(Icons.Default.Star, Color(0xFF9333EA), VictimPurpleCard, "Level 3 • Thermal Injury")
      "seizures" -> ProtocolCardTheme(Icons.Default.Shield, Color(0xFF059669), VictimGreenCard, "Level 4 • Neurological Emergency")
      "stroke" -> ProtocolCardTheme(Icons.Default.Warning, Color(0xFFDC2626), VictimPinkCard, "Level 5 • Acute Brain Attack")
      "asthma" -> ProtocolCardTheme(Icons.Default.Info, Color(0xFF2563EB), VictimBlueCard, "Level 4 • Respiratory Distress")
      "anaphylaxis" -> ProtocolCardTheme(Icons.Default.Warning, Color(0xFFDC2626), VictimPinkCard, "Level 5 • Severe Allergic Shock")
      "poisoning" -> ProtocolCardTheme(Icons.Default.Shield, Color(0xFF9333EA), VictimPurpleCard, "Level 4 • Toxic Ingestion")
      "heatstroke" -> ProtocolCardTheme(Icons.Default.Star, Color(0xFFD97706), VictimOrangeCard, "Level 4 • Hyperthermia Crisis")
      "hypothermia" -> ProtocolCardTheme(Icons.Default.Info, Color(0xFF2563EB), VictimBlueCard, "Level 4 • Severe Cold Exposure")
      "snakebite" -> ProtocolCardTheme(Icons.Default.Warning, Color(0xFFDC2626), VictimPinkCard, "Level 5 • Venomous Envenomation")
      "head_injury" -> ProtocolCardTheme(Icons.Default.Shield, Color(0xFFDC2626), VictimPinkCard, "Level 5 • Head & Spine Trauma")
      "diabetic_emergency" -> ProtocolCardTheme(Icons.Default.Favorite, Color(0xFF9333EA), VictimPurpleCard, "Level 4 • Metabolic Emergency")
      "electric_shock" -> ProtocolCardTheme(Icons.Default.Warning, Color(0xFFD97706), VictimOrangeCard, "Level 5 • Electrical Trauma")
      "drowning" -> ProtocolCardTheme(Icons.Default.Favorite, Color(0xFF2563EB), VictimBlueCard, "Level 5 • Hypoxic Submersion")
      "shock" -> ProtocolCardTheme(Icons.Default.Warning, Color(0xFFDC2626), VictimPinkCard, "Level 5 • Systemic Collapse")
      else -> ProtocolCardTheme(Icons.Default.Favorite, VictimPrimary, VictimPinkCard, "Level ${protocol.severityLevel} • Critical Life Threat")
    }
  }

  Card(
    colors = CardDefaults.cardColors(containerColor = Color.White),
    border = androidx.compose.foundation.BorderStroke(1.dp, VictimBorder),
    shape = RoundedCornerShape(18.dp),
    modifier = modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(14.dp)
    ) {
      // Header Row: Condition Icon + Title + Good Samaritan Badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(theme.bgTint),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = theme.icon,
              contentDescription = null,
              tint = theme.tintColor,
              modifier = Modifier.size(20.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = protocol.conditionLabel,
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold,
              color = VictimTextDark,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(modifier = Modifier.size(6.dp).background(theme.tintColor, CircleShape))
              Spacer(modifier = Modifier.width(5.dp))
              Text(
                text = theme.severityBadge,
                fontSize = 12.sp,
                color = theme.tintColor,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Surface(
          color = VictimGreenCard,
          shape = RoundedCornerShape(100.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, VictimGreenBorder)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Shield,
              contentDescription = null,
              tint = StatusSafeGreen,
              modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Sec 134A Protected",
              fontSize = 10.5.sp,
              fontWeight = FontWeight.Bold,
              color = VictimTextDark,
              maxLines = 1,
              softWrap = false
            )
          }
        }
      }

      // Clinical authority and guidelines source
      val authorityText = protocol.authority
      Spacer(modifier = Modifier.height(8.dp))
      Surface(
        color = Color(0xFFF8FAFC),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = VictimTextMuted,
            modifier = Modifier.size(14.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = authorityText,
            fontSize = 11.sp,
            color = VictimTextMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Progress bar and completed step counter
      Row(verticalAlignment = Alignment.CenterVertically) {
        LinearProgressIndicator(
          progress = { progress },
          modifier = Modifier.weight(1f).height(6.dp).clip(CircleShape),
          color = theme.tintColor,
          trackColor = VictimBorder
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = "${completedSteps.size}/$totalSteps Done",
          fontSize = 11.5.sp,
          fontWeight = FontWeight.Bold,
          color = VictimTextMuted,
          maxLines = 1,
          softWrap = false
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Action Steps Header inside the same card
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "ACTION PROTOCOL STEPS",
          color = VictimTextMuted,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.5.sp
        )
        Text(
          text = if (completedSteps.size == totalSteps && totalSteps > 0) "All Steps Completed" else "Tap step to mark done",
          fontSize = 10.5.sp,
          color = if (completedSteps.size == totalSteps && totalSteps > 0) StatusSafeGreen else VictimTextMuted,
          fontWeight = FontWeight.SemiBold
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Integrated Action Steps inside the same card (Each takes exactly 1/4 of remaining card height)
      Column(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        protocol.steps.forEach { step ->
          val isDone = completedSteps.contains(step.stepNumber)
          IntegratedStepRow(
            step = step,
            isCompleted = isDone,
            tintColor = theme.tintColor,
            onToggle = { onToggleStep(step.stepNumber) },
            modifier = Modifier
              .weight(1f)
              .fillMaxWidth()
          )
        }
      }
    }
  }
}

@Composable
fun IntegratedStepRow(
  step: ProtocolStepDto,
  isCompleted: Boolean,
  tintColor: Color,
  onToggle: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    color = if (isCompleted) VictimGreenCard else Color(0xFFF8FAFC),
    shape = RoundedCornerShape(12.dp),
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      if (isCompleted) VictimGreenBorder else Color(0xFFE2E8F0)
    ),
    modifier = modifier
      .fillMaxWidth()
      .clickable { onToggle() }
  ) {
    Row(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 10.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Box(
        modifier = Modifier
          .size(24.dp)
          .clip(CircleShape)
          .background(if (isCompleted) Color(0xFF22C55E) else Color.White)
          .border(
            1.dp,
            if (isCompleted) Color(0xFF22C55E) else Color(0xFFCBD5E1),
            CircleShape
          ),
        contentAlignment = Alignment.Center
      ) {
        if (isCompleted) {
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(16.dp)
          )
        } else {
          Text(
            text = "${step.stepNumber}",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = VictimTextDark
          )
        }
      }
      Spacer(modifier = Modifier.width(10.dp))
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.Center
      ) {
        Text(
          text = step.title,
          fontSize = 12.5.sp,
          fontWeight = FontWeight.Bold,
          color = VictimTextDark,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = step.actionInstruction,
          fontSize = 11.5.sp,
          color = VictimTextMuted,
          lineHeight = 15.sp,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )
        if (!step.warningNote.isNullOrBlank()) {
          Spacer(modifier = Modifier.height(3.dp))
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(6.dp))
              .background(VictimPinkCard.copy(alpha = 0.7f))
              .padding(horizontal = 7.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "⚠️ ${step.warningNote}",
              color = VictimPrimary,
              fontSize = 10.5.sp,
              fontWeight = FontWeight.SemiBold,
              lineHeight = 13.sp,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }
    }
  }
}

@Composable
fun ChatMessageBubble(msg: AiChatMessageUiModel) {
  Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = if (msg.isUser) Alignment.End else Alignment.Start) {
    Surface(
      color = if (msg.isUser) VictimPrimary else Color(0xFFF1F5F9),
      shape = RoundedCornerShape(12.dp),
      border = if (!msg.isUser) androidx.compose.foundation.BorderStroke(1.dp, VictimBorder) else null,
      modifier = Modifier.fillMaxWidth(0.9f),
    ) {
      Column(modifier = Modifier.padding(10.dp)) {
        if (!msg.isUser && msg.highlightBadge != null) {
          Text(text = msg.highlightBadge, color = VictimPrimary, fontSize = 9.5.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 2.dp))
        }
        Text(text = msg.text, color = if (msg.isUser) Color.White else VictimTextDark, fontSize = 12.5.sp, lineHeight = 18.sp)
        Text(text = msg.timestamp, color = if (msg.isUser) Color.White.copy(alpha = 0.8f) else VictimTextMuted, fontSize = 9.sp, modifier = Modifier.align(Alignment.End).padding(top = 2.dp))
      }
    }
  }
}

private fun resolveFileName(context: Context, uri: Uri): String? {
  return try {
    var name: String? = null
    if (uri.scheme == "content") {
      val cursor = context.contentResolver.query(uri, null, null, null, null)
      cursor?.use {
        if (it.moveToFirst()) {
          val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
          if (nameIndex != -1) {
            name = it.getString(nameIndex)
          }
        }
      }
    }
    name ?: uri.lastPathSegment ?: "document"
  } catch (e: Exception) {
    uri.lastPathSegment ?: "document"
  }
}
