package com.example.nearhelp.ui.profile

import com.example.nearhelp.i18n.LocalizedText as Text
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nearhelp.NearHelpApplication
import com.example.nearhelp.data.api.models.EmergencyContact
import com.example.nearhelp.theme.AppTheme
import com.example.nearhelp.theme.ThemeManager
import com.example.nearhelp.theme.appColorScheme
import com.example.nearhelp.ui.design.*
import com.example.nearhelp.ui.victim.VictimBottomNavBar
import com.example.nearhelp.ui.victim.VictimNavTab
import com.example.nearhelp.i18n.AppLanguages
import com.example.nearhelp.i18n.activity
import com.example.nearhelp.data.api.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch

private val bloodGroups = listOf("A+", "A−", "B+", "B−", "AB+", "AB−", "O+", "O−")

@Composable
fun ProfileScreen(onNavigateBack: () -> Unit, onLogout: () -> Unit, onNavigateToHome: () -> Unit = onNavigateBack,
    onNavigateToAssistant: () -> Unit = {}, onNavigateToMap: () -> Unit = {},
    modifier: Modifier = Modifier, viewModel: ProfileViewModel = viewModel { ProfileViewModel(NearHelpApplication.instance.userRepository) },
    showBottomBar: Boolean = true, loggingOut: Boolean = false) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var page by rememberSaveable { mutableStateOf("profile") }
    var contactId by rememberSaveable { mutableStateOf<String?>(null) }
    var deleteContact by remember { mutableStateOf<EmergencyContact?>(null) }
    var historyFilter by rememberSaveable { mutableStateOf("All incidents") }
    val user = state.user
    val medical = state.medicalId
    val contact = state.emergencyContacts.firstOrNull { it.id == contactId }
    var name by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var blood by rememberSaveable { mutableStateOf("") }
    var conditions by rememberSaveable { mutableStateOf("") }
    var allergies by rememberSaveable { mutableStateOf("") }
    var conditionDraft by rememberSaveable { mutableStateOf("") }
    var allergyDraft by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var pacemaker by rememberSaveable { mutableStateOf(false) }
    var donor by rememberSaveable { mutableStateOf(false) }
    var relationship by rememberSaveable { mutableStateOf("Parent") }
    var selectedLanguages by rememberSaveable { mutableStateOf(listOf("en")) }
    var qualification by rememberSaveable { mutableStateOf("CPR / AED certified") }
    var organization by rememberSaveable { mutableStateOf("") }
    var certificateId by rememberSaveable { mutableStateOf("") }
    var certificateUri by rememberSaveable { mutableStateOf<String?>(null) }
    var serverUrlInput by rememberSaveable { mutableStateOf(RetrofitClient.getEffectiveBaseUrl()) }
    var pingResult by remember { mutableStateOf<String?>(null) }
    var pingLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val certificatePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> certificateUri = uri?.toString() }
    val history = remember(page, user?.id) { NearHelpApplication.instance.tokenStorage.getIncidentHistory() }
    fun goBack() { if (!state.isSaving) { page = when (page) { "add-contact", "edit-contact" -> "contacts"; "app-language", "languages" -> "communication"; else -> "profile" }; viewModel.clearError() } }
    fun editDetails() { name = user?.name.orEmpty(); phone = user?.phone.orEmpty(); blood = user?.bloodGroup.orEmpty(); page = "edit-profile" }
    fun editMedical() {
        blood = medical?.bloodGroup.orEmpty(); conditions = medical?.medicalConditions.orEmpty().joinToString("\n")
        allergies = medical?.knownAllergies.orEmpty().joinToString("\n"); notes = medical?.medicalNotes.orEmpty()
        pacemaker = medical?.hasPacemaker ?: false; donor = medical?.isOrganDonor ?: false; page = "medical"
    }
    fun editContact(item: EmergencyContact?) {
        contactId = item?.id; name = item?.name.orEmpty(); phone = item?.phone.orEmpty(); relationship = item?.relationship ?: "Parent"
        page = if (item == null) "add-contact" else "edit-contact"
    }
    fun testServer(url: String) {
        pingLoading = true
        pingResult = null
        coroutineScope.launch(Dispatchers.IO) {
            val start = System.currentTimeMillis()
            try {
                val cleanUrl = url.trim().trimEnd('/') + "/health"
                val request = okhttp3.Request.Builder().url(cleanUrl).build()
                val client = okhttp3.OkHttpClient.Builder()
                    .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .build()
                val response = client.newCall(request).execute()
                val elapsed = System.currentTimeMillis() - start
                val body = response.body?.string().orEmpty()
                withContext(Dispatchers.Main) {
                    pingLoading = false
                    if (response.isSuccessful && body.contains("healthy")) {
                        val dbConnected = body.contains("\"database\":\"connected\"") || body.contains("connected")
                        val engine = if (body.contains("PostGIS")) "PostGIS" else if (body.contains("SQLite")) "SQLite" else "Active"
                        pingResult = "Database: " + (if (dbConnected) "Connected ($engine)" else "Disconnected") + " · Latency: ${elapsed}ms"
                    } else {
                        pingResult = "Server status ${response.code} · ${elapsed}ms"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    pingLoading = false
                    pingResult = "Connection error: ${e.message ?: "Server unreachable"}"
                }
            }
        }
    }
    LaunchedEffect(state.showQualificationsDialog, state.showEmergencyHistoryDialog) {
        if (state.showQualificationsDialog) { page = "qualifications"; viewModel.closeQualificationsDialog() }
        if (state.showEmergencyHistoryDialog) { page = "history"; viewModel.closeEmergencyHistoryDialog() }
    }
    LaunchedEffect(state.successMessage) {
        if (state.successMessage != null) {
            page = when (page) { "add-contact", "edit-contact" -> "contacts"; "contacts" -> "contacts"; "languages" -> "communication"; "qualifications" -> "verification"; else -> "profile" }
            viewModel.clearSuccessMessage()
        }
    }
    BackHandler(enabled = page != "profile") { goBack() }
    Column(modifier.fillMaxSize()) {
        CarePage(if (page == "profile") null else when (page) { "appearance" -> "Appearance"; "server" -> "Server & DB connection"; "edit-profile" -> "Edit personal details"; "medical" -> "Medical information"; "contacts" -> "Emergency contacts"; "add-contact" -> "Add emergency contact"; "edit-contact" -> "Edit emergency contact"; "languages" -> "Languages you speak"; "communication" -> "Communication"; "app-language" -> "App language"; "qualifications" -> "Responder qualifications"; "verification" -> "Verification status"; "history" -> "Emergency history"; else -> "Impact and statistics" },
            if (page == "profile") null else ({ goBack() }), Modifier.weight(1f)) {
            if (state.isLoading || state.isSaving) LinearProgressIndicator(Modifier.fillMaxWidth())
            state.error?.let { CareNote("Could not complete that action", it, error = true); if (page == "profile") CareButton("Retry profile", viewModel::loadProfile) }
            when (page) {
                "server" -> {
                    CareHeading("Server & Database Connection", "Verify connectivity, latency, and database status.")
                    CareField("Server Base URL", serverUrlInput, { serverUrlInput = it })
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        CareButton("Cloud (Render)", {
                            serverUrlInput = "https://nearhelp-backend-7sfj.onrender.com/"
                            testServer(serverUrlInput)
                        }, Modifier.weight(1f))
                        CareButton("Local (10.0.2.2)", {
                            serverUrlInput = "http://10.0.2.2:8000/"
                            testServer(serverUrlInput)
                        }, Modifier.weight(1f))
                    }
                    CareButton(if (pingLoading) "Testing connection…" else "Test connection & check DB", {
                        testServer(serverUrlInput)
                    }, icon = Icons.Outlined.Speed, enabled = !pingLoading && serverUrlInput.isNotBlank())
                    pingResult?.let {
                        val isSuccess = it.contains("Connected") || it.contains("status 200")
                        CareNote(if (isSuccess) "Connection verified" else "Connection result", it, icon = if (isSuccess) Icons.Outlined.CheckCircle else Icons.Outlined.ErrorOutline, error = !isSuccess)
                    }
                    CareButton("Save & Switch Server", {
                        val target = serverUrlInput.trim().trimEnd('/') + "/"
                        NearHelpApplication.instance.tokenStorage.saveServerBaseUrl(target)
                        RetrofitClient.setBaseUrl(target)
                        viewModel.loadProfile()
                        page = "profile"
                    }, style = CareButtonStyle.PRIMARY, enabled = serverUrlInput.isNotBlank())
                    CareButton("Cancel", { goBack() }, style = CareButtonStyle.QUIET)
                }
                "appearance" -> {
                    CareHeading("Choose your theme", "Find the look that feels comfortable to read.")
                    AppTheme.entries.chunked(2).forEach { pair -> Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        pair.forEach { theme -> ThemeChoice(theme, ThemeManager.selectedTheme == theme, { ThemeManager.select(theme) }, Modifier.weight(1f)) }
                    } }
                    CareMuted("${ThemeManager.selectedTheme.title} is selected. Applies throughout NearHelp.")
                    CareMuted("OLED themes use a true-black background.")
                    CareButton("Done", { page = "profile" }, style = CareButtonStyle.PRIMARY)
                }
                "edit-profile" -> {
                    CareField("Full name", name, { name = it }, enabled = !state.isSaving)
                    CareField("Phone number", phone, { phone = it }, keyboard = KeyboardType.Phone, enabled = !state.isSaving)
                    CareSelect("Blood group", blood.ifBlank { "Choose blood group" }, bloodGroups, { blood = it })
                    CareButton("Save personal details", { viewModel.updateProfile(name.trim(), phone.trim(), blood.takeIf { it.isNotBlank() }, null, null, null, null, null, null) }, style = CareButtonStyle.PRIMARY, enabled = !state.isSaving && name.isNotBlank())
                    CareButton("Cancel", { goBack() }, style = CareButtonStyle.QUIET, enabled = !state.isSaving)
                }
                "medical" -> {
                    CareSelect("Blood group", blood.ifBlank { "Choose blood group" }, bloodGroups, { blood = it })
                    CareCheck("I have a cardiac pacemaker", pacemaker, { pacemaker = it }, !state.isSaving)
                    CareCheck("I am an organ donor", donor, { donor = it }, !state.isSaving)
                    Text("Medical conditions", style = MaterialTheme.typography.titleMedium)
                    CareField("Add a condition", conditionDraft, { conditionDraft = it }, hint = "Example: asthma", enabled = !state.isSaving)
                    CareButton("Add condition", { conditions = (conditions.lines().filter(String::isNotBlank) + conditionDraft.trim()).distinct().joinToString("\n"); conditionDraft = "" }, icon = Icons.Outlined.Add, enabled = !state.isSaving && conditionDraft.isNotBlank())
                    conditions.lines().filter(String::isNotBlank).forEach { value -> CareRow(value, "Remove condition", Icons.Outlined.Close) { if (!state.isSaving) conditions = conditions.lines().filterNot { it == value }.joinToString("\n") } }
                    Text("Allergies", style = MaterialTheme.typography.titleMedium)
                    CareField("Add an allergy", allergyDraft, { allergyDraft = it }, hint = "Example: penicillin", enabled = !state.isSaving)
                    CareButton("Add allergy", { allergies = (allergies.lines().filter(String::isNotBlank) + allergyDraft.trim()).distinct().joinToString("\n"); allergyDraft = "" }, icon = Icons.Outlined.Add, enabled = !state.isSaving && allergyDraft.isNotBlank())
                    allergies.lines().filter(String::isNotBlank).forEach { value -> CareRow(value, "Remove allergy", Icons.Outlined.Close) { if (!state.isSaving) allergies = allergies.lines().filterNot { it == value }.joinToString("\n") } }
                    CareField("Physician notes · optional", notes, { notes = it }, multiline = true, enabled = !state.isSaving)
                    CareButton("Save medical information", { viewModel.updateMedicalId(blood.takeIf { it.isNotBlank() }, conditions.lines().map(String::trim).filter(String::isNotBlank), allergies.lines().map(String::trim).filter(String::isNotBlank), pacemaker, donor, notes) }, style = CareButtonStyle.PRIMARY, enabled = !state.isSaving)
                    CareButton("Cancel", { goBack() }, style = CareButtonStyle.QUIET, enabled = !state.isSaving)
                }
                "contacts" -> {
                    CareHeading("Emergency contacts", "${state.emergencyContacts.size} of 5 contacts added")
                    state.emergencyContacts.forEach { item ->
                        CareNote(item.name, "${item.relationship} · ${item.phone}", Icons.Outlined.PersonOutline)
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            CareButton("Call", { dial(context, item.phone) }, Modifier.weight(1f), icon = Icons.Outlined.Phone)
                            CareButton("Message", { context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${item.phone}"))) }, Modifier.weight(1f), icon = Icons.Outlined.ChatBubbleOutline)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            CareButton("Edit", { editContact(item) }, Modifier.weight(1f), icon = Icons.Outlined.Edit, enabled = !state.isSaving)
                            CareButton("Delete", { deleteContact = item }, Modifier.weight(1f), icon = Icons.Outlined.DeleteOutline, enabled = !state.isSaving && item.id != null)
                        }
                    }
                    if (state.emergencyContacts.isEmpty()) CareNote("No emergency contacts yet", "Add someone you can reach in an emergency.", Icons.Outlined.Contacts)
                    CareButton("Add emergency contact", { editContact(null) }, style = CareButtonStyle.PRIMARY, icon = Icons.Outlined.Add, enabled = state.emergencyContacts.size < 5 && !state.isSaving)
                    CareButton("Done", { page = "profile" }, style = CareButtonStyle.QUIET)
                }
                "add-contact", "edit-contact" -> {
                    CareField("Contact name", name, { name = it }, enabled = !state.isSaving)
                    CareField("Phone number", phone, { phone = it }, keyboard = KeyboardType.Phone, enabled = !state.isSaving)
                    CareSelect("Relationship", relationship, listOf("Parent", "Spouse", "Sibling", "Friend", "Other"), { relationship = it })
                    CareButton("Save contact", {
                        if (page == "add-contact") viewModel.addEmergencyContact(name.trim(), phone.trim(), relationship)
                        else contactId?.let { viewModel.updateEmergencyContact(it, name.trim(), phone.trim(), relationship, contact?.isPrimary) }
                    }, style = CareButtonStyle.PRIMARY, enabled = !state.isSaving && name.isNotBlank() && phone.count(Char::isDigit) >= 8)
                    CareButton("Cancel", { goBack() }, style = CareButtonStyle.QUIET, enabled = !state.isSaving)
                }
                "communication" -> {
                    CareHeading("Communication settings", "Choose how you read the app and communicate with others.")
                    CareRow("App language", AppLanguages.interfaceLanguages.first { it.code == AppLanguages.selected(context) }.label, Icons.Outlined.Translate) { page = "app-language" }
                    CareRow("Languages you speak", user?.languages?.mapNotNull { code -> AppLanguages.communicationLanguages.firstOrNull { it.code == code }?.label }?.joinToString(", ") ?: "Choose communication languages", Icons.Outlined.Language) {
                        selectedLanguages = user?.languages.orEmpty().filter { code -> AppLanguages.communicationLanguages.any { it.code == code } }.ifEmpty { listOf(AppLanguages.selected(context)) }; page = "languages"
                    }
                    CareMuted("App language changes the interface. Spoken languages help others communicate with you.")
                }
                "app-language" -> {
                    CareHeading("App language", "Choose a language. The interface changes immediately.")
                    AppLanguages.interfaceLanguages.forEach { option ->
                        Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).selectable(selected = AppLanguages.selected(context) == option.code, role = Role.RadioButton,
                            onClick = { if (AppLanguages.selected(context) != option.code) context.activity()?.let { AppLanguages.select(it, option.code) } }).padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(AppLanguages.selected(context) == option.code, onClick = null)
                            Text(option.label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        }
                    }
                }
                "languages" -> {
                    CareHeading("Your languages", "Select the languages you can communicate in.")
                    AppLanguages.communicationLanguages.forEach { (code, label) ->
                        CareCheck(label, code in selectedLanguages, { checked -> selectedLanguages = if (checked) selectedLanguages + code else selectedLanguages - code }, !state.isSaving)
                    }
                    CareButton("Save language preferences", { viewModel.updateLanguages(selectedLanguages) }, style = CareButtonStyle.PRIMARY, enabled = selectedLanguages.isNotEmpty() && !state.isSaving)
                    CareButton("Cancel", { goBack() }, style = CareButtonStyle.QUIET, enabled = !state.isSaving)
                }
                "qualifications" -> {
                    CareHeading("Become a responder", "Add your training and qualifications.")
                    CareSelect("Primary qualification", qualification, listOf("CPR / AED certified", "First-aid provider", "EMT / Paramedic", "Doctor / Physician", "Registered nurse", "Community first responder"), { qualification = it })
                    CareField("Issuing organization", organization, { organization = it }, enabled = !state.isSaving)
                    CareField("License or certificate ID", certificateId, { certificateId = it }, enabled = !state.isSaving)
                    CareButton(if (certificateUri != null) "Certificate attached · change" else "Attach certificate or ID", { certificatePicker.launch("*/*") }, icon = Icons.Outlined.AttachFile, enabled = !state.isSaving)
                    CareMuted("PDF, PNG, JPG or WebP · up to 10 MB")
                    CareButton("Submit for verification", { viewModel.submitQualification(qualification, organization, certificateId, certificateUri?.let(Uri::parse), context.applicationContext) }, style = CareButtonStyle.PRIMARY, enabled = !state.isSaving && certificateUri != null && organization.isNotBlank() && certificateId.isNotBlank())
                    CareButton("Cancel", { goBack() }, style = CareButtonStyle.QUIET, enabled = !state.isSaving)
                }
                "verification" -> {
                    CareHeading("Verification status")
                    CareNote("Trust score", user?.trustScore?.toInt()?.let { "$it / 100" } ?: "Not available", Icons.Outlined.VerifiedUser)
                    CareRow("Contact phone", if (user?.phone.isNullOrBlank()) "Not added" else "Saved for emergency contact purposes", Icons.Outlined.Phone)
                    if (user?.skills.isNullOrEmpty()) CareNote("No verified qualifications yet", "Add your training to apply as a responder.")
                    user?.skills.orEmpty().forEach { CareRow(it.skillType.replace('_', ' '), if (it.verified) "Verified" else "Pending verification", if (it.verified) Icons.Outlined.Verified else Icons.Outlined.Schedule) }
                    state.skillClaims.forEach { CareRow(it.skillType.replace('_', ' '), it.status.replace('_', ' '), Icons.Outlined.Description) }
                    CareButton("Refresh verification", { viewModel.loadProfile(); viewModel.loadSkillClaims() }, icon = Icons.Outlined.Refresh)
                    CareButton("Done", { page = "profile" }, style = CareButtonStyle.PRIMARY)
                }
                "history" -> {
                    CareHeading("Emergency history")
                    CareSelect("Show", historyFilter, listOf("All incidents", "Help I received", "People I helped"), { historyFilter = it })
                    val records = history.filter { historyFilter == "All incidents" || if (historyFilter == "Help I received") it.receivedHelp else !it.receivedHelp }
                    if (records.isEmpty()) CareNote("No recorded incidents yet", "Requests and responses recorded on this device will appear here.", Icons.Outlined.History)
                    records.forEach { CareRow("Medical emergency · ${it.status.lowercase().replaceFirstChar(Char::uppercase)}", listOf(it.address, it.timestamp).filter(String::isNotBlank).joinToString(" · "), Icons.Outlined.MonitorHeart) }
                    CareButton("Done", { page = "profile" }, style = CareButtonStyle.PRIMARY)
                }
                "impact" -> {
                    CareHeading("Your impact", "Your contribution to the community.")
                    CareRow("${history.count { !it.receivedHelp }} responses", "Recorded on this device", Icons.Outlined.VolunteerActivism)
                    CareRow("${history.count { it.receivedHelp }} requests", "Recorded on this device", Icons.Outlined.MonitorHeart)
                    CareRow(user?.trustScore?.toInt()?.let { "$it / 100" } ?: "—", "Trust score", Icons.Outlined.VerifiedUser)
                    user?.badges.orEmpty().forEach { CareNote(it, icon = Icons.Outlined.WorkspacePremium) }
                    CareButton("View emergency history", { page = "history" }, style = CareButtonStyle.PRIMARY)
                    CareButton("Done", { page = "profile" }, style = CareButtonStyle.QUIET)
                }
                else -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.PersonOutline, null, Modifier.size(42.dp), tint = MaterialTheme.colorScheme.primary)
                        CareHeading(user?.name ?: viewModel.getStoredUserName()?.takeIf { it.isNotBlank() } ?: "Your profile", "Your health and safety profile")
                    }
                    CareButton("Edit personal details", { editDetails() }, icon = Icons.Outlined.Edit)
                    ProfileSection("Personal") {
                        CareRow("Medical information", "Blood group, allergies and conditions", Icons.Outlined.MonitorHeart) { editMedical() }
                        CareRow("Emergency contacts", "People to reach in an emergency", Icons.Outlined.Phone) { page = "contacts" }
                    }
                    ProfileSection("Settings") {
                        CareRow("Server connection", RetrofitClient.getEffectiveBaseUrl().removePrefix("https://").removePrefix("http://").trimEnd('/'), Icons.Outlined.Dns) {
                            serverUrlInput = RetrofitClient.getEffectiveBaseUrl()
                            page = "server"
                        }
                        CareRow("Communication", "App language and languages you speak", Icons.Outlined.Language) { page = "communication" }
                        CareRow("Appearance", "${ThemeManager.selectedTheme.title} · 4 themes available", Icons.Outlined.Palette) { page = "appearance" }
                        CareButton(if (loggingOut) "Logging out…" else "Log out", onLogout, style = CareButtonStyle.QUIET,
                            icon = Icons.AutoMirrored.Outlined.Logout, enabled = !loggingOut && !state.isSaving)
                    }
                    ProfileSection("Responder") {
                        CareRow("Qualifications", "Training and certifications", Icons.Outlined.VerifiedUser) { page = "qualifications" }
                        CareRow("Verification status", "View identity and training status", Icons.Outlined.Verified) { viewModel.loadSkillClaims(); page = "verification" }
                    }
                    ProfileSection("Activity") {
                        CareRow("Emergency history", "Requests and responses", Icons.Outlined.History) { page = "history" }
                        CareRow("Impact and statistics", "Your community contribution", Icons.Outlined.BarChart) { page = "impact" }
                    }
                }
            }
        }
        if (showBottomBar) VictimBottomNavBar(VictimNavTab.PROFILE, { when (it) { VictimNavTab.HOME -> onNavigateToHome(); VictimNavTab.CHAT -> onNavigateToAssistant(); VictimNavTab.MAP -> onNavigateToMap(); VictimNavTab.PROFILE -> page = "profile" } })
    }
    deleteContact?.let { item -> AlertDialog(onDismissRequest = { deleteContact = null }, title = { Text("Remove ${item.name}?") }, text = { Text("They will be removed from your emergency contacts.") },
        confirmButton = { TextButton(onClick = { deleteContact = null; item.id?.let(viewModel::deleteEmergencyContact) }) { Text("Remove contact") } },
        dismissButton = { TextButton(onClick = { deleteContact = null }) { Text("Keep contact") } }) }
}

@Composable
private fun ProfileSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column { Text(title, style = MaterialTheme.typography.titleMedium); content() }
}

@Composable
fun ThemeChoice(theme: AppTheme, selected: Boolean, onSelect: () -> Unit, modifier: Modifier = Modifier) {
    val palette = appColorScheme(theme)
    Surface(modifier.clickable(onClick = onSelect), shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.background,
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(Modifier.fillMaxWidth().height(96.dp).background(palette.background, RoundedCornerShape(10.dp)).padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("NearHelp", style = MaterialTheme.typography.labelSmall, color = palette.onBackground)
                Box(Modifier.fillMaxWidth().weight(1f).background(palette.surfaceVariant, RoundedCornerShape(5.dp)), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.LocationOn, null, Modifier.size(17.dp), tint = palette.primary) }
                Box(Modifier.fillMaxWidth().background(palette.error, RoundedCornerShape(4.dp)).padding(2.dp), contentAlignment = Alignment.Center) { Text("Medical SOS", style = MaterialTheme.typography.labelSmall, color = palette.onError) }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected, onClick = onSelect, modifier = Modifier.size(32.dp))
                Text(theme.title, style = MaterialTheme.typography.titleSmall)
            }
            CareMuted(theme.description)
        }
    }
}
