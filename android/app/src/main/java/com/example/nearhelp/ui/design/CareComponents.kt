package com.example.nearhelp.ui.design

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nearhelp.data.location.LocationHelper
import com.example.nearhelp.data.location.UserLocationState
import com.example.nearhelp.theme.ThemeManager
import com.example.nearhelp.ui.map.MapPinItem
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay

@Composable
fun CarePage(title: String? = null, onBack: (() -> Unit)? = null, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding()) {
        if (title != null) CareTopBar(title, onBack)
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).imePadding().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp), content = content)
    }
}

@Composable
fun CareTopBar(title: String, onBack: (() -> Unit)?) {
    Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(start = 10.dp, end = 20.dp), verticalAlignment = Alignment.CenterVertically) {
        if (onBack != null) {
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Go back", tint = MaterialTheme.colorScheme.onBackground) }
            Spacer(Modifier.width(8.dp))
        }
        Text(title, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
fun CareHeading(title: String, subtitle: String? = null, compact: Boolean = false) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = if (compact) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.headlineLarge)
        if (subtitle != null) CareMuted(subtitle)
    }
}

@Composable
fun CareMuted(text: String, modifier: Modifier = Modifier) = Text(text, modifier, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

enum class CareButtonStyle { PRIMARY, SECONDARY, SOS, QUIET }

@Composable
fun CareButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, style: CareButtonStyle = CareButtonStyle.SECONDARY,
               icon: ImageVector? = null, enabled: Boolean = true, subtitle: String? = null) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(12.dp)
    val contents: @Composable RowScope.() -> Unit = {
        if (icon != null) { Icon(icon, null, Modifier.size(21.dp)); Spacer(Modifier.width(10.dp)) }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text, style = if (subtitle == null) MaterialTheme.typography.labelLarge else MaterialTheme.typography.titleMedium)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
        }
    }
    val sizing = modifier.fillMaxWidth().heightIn(min = if (subtitle == null) 56.dp else 94.dp)
    when (style) {
        CareButtonStyle.PRIMARY, CareButtonStyle.SOS -> Button(onClick, sizing, enabled = enabled, shape = shape,
            colors = ButtonDefaults.buttonColors(containerColor = if (style == CareButtonStyle.SOS) colors.error else colors.primary,
                contentColor = if (style == CareButtonStyle.SOS) colors.onError else colors.onPrimary),
            contentPadding = PaddingValues(16.dp), content = contents)
        CareButtonStyle.SECONDARY -> OutlinedButton(onClick, sizing, enabled = enabled, shape = shape,
            border = BorderStroke(1.dp, colors.outline), contentPadding = PaddingValues(16.dp), content = contents)
        CareButtonStyle.QUIET -> TextButton(onClick, sizing, enabled = enabled, shape = shape, contentPadding = PaddingValues(12.dp), content = contents)
    }
}

@Composable
fun CareField(label: String, value: String, onChange: (String) -> Unit, modifier: Modifier = Modifier,
              hint: String? = null, error: String? = null, password: Boolean = false, keyboard: KeyboardType = KeyboardType.Text,
              multiline: Boolean = false, enabled: Boolean = true) {
    var visible by remember { mutableStateOf(false) }
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        OutlinedTextField(value, onChange, Modifier.fillMaxWidth().heightIn(min = 56.dp), enabled = enabled,
            singleLine = !multiline, minLines = if (multiline) 3 else 1, shape = RoundedCornerShape(10.dp),
            textStyle = MaterialTheme.typography.bodyLarge,
            isError = error != null, placeholder = if (hint != null) ({ Text(hint, style = MaterialTheme.typography.bodyLarge) }) else null,
            visualTransformation = if (password && !visible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = if (password) KeyboardType.Password else keyboard),
            trailingIcon = if (password) ({ IconButton(onClick = { visible = !visible }) { Icon(if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, if (visible) "Hide password" else "Show password") } }) else null,
            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedContainerColor = MaterialTheme.colorScheme.background, unfocusedContainerColor = MaterialTheme.colorScheme.background))
        if (error != null) Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun CareRow(title: String, description: String? = null, icon: ImageVector? = null, onClick: (() -> Unit)? = null) {
    Column {
        Row(Modifier.fillMaxWidth().heightIn(min = 66.dp).then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier).padding(vertical = 15.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) Icon(icon, null, Modifier.size(22.dp), tint = MaterialTheme.colorScheme.primary)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                if (!description.isNullOrBlank()) CareMuted(description)
            }
            if (onClick != null) Icon(Icons.Outlined.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
fun CareNote(title: String, description: String? = null, icon: ImageVector = Icons.Outlined.Info, error: Boolean = false) {
    val colors = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(if (error) colors.errorContainer else colors.surfaceVariant).padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, null, tint = if (error) colors.error else colors.primary, modifier = Modifier.size(22.dp))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = if (error) colors.onErrorContainer else colors.onSurface)
            if (!description.isNullOrBlank()) CareMuted(description)
        }
    }
}

@Composable
fun CareCheck(text: String, checked: Boolean, onChange: (Boolean) -> Unit, enabled: Boolean = true) {
    Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).clickable(enabled = enabled) { onChange(!checked) }, verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked, onCheckedChange = onChange, enabled = enabled)
        Text(text, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun CareSelect(label: String, selected: String, options: List<String>, onChange: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Box {
            OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp), shape = RoundedCornerShape(10.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {
                Text(selected, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                Icon(Icons.Outlined.ExpandMore, null)
            }
            DropdownMenu(expanded, onDismissRequest = { expanded = false }) { options.forEach { option -> DropdownMenuItem(text = { Text(option) }, onClick = { onChange(option); expanded = false }) } }
        }
    }
}

@Composable
fun CareBrand() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) {
            Icon(Icons.Outlined.MonitorHeart, null, Modifier.size(29.dp), tint = MaterialTheme.colorScheme.onPrimary)
        }
        Row { Text("Near", style = MaterialTheme.typography.headlineMedium); Text("Help", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary) }
    }
}

@Composable
fun EmergencyCallButton() {
    val context = LocalContext.current
    CareButton("Call emergency services · 112", { dial(context, "112") }, icon = Icons.Outlined.Phone)
}
fun dial(context: Context, number: String) { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number.filter { it.isDigit() || it == '+' }))) }
fun directions(context: Context, point: LatLng, name: String = "") { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${point.latitude},${point.longitude}(${Uri.encode(name)})"))) }

@Composable
fun rememberCareLocation(): Pair<UserLocationState, () -> Unit> {
    val context = LocalContext.current
    val helper = remember { LocationHelper(context.applicationContext) }
    val state by helper.locationState.collectAsStateWithLifecycle()
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { helper.startLocationUpdates() }
    val request = { if (helper.hasLocationPermission()) helper.startLocationUpdates() else permission.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) }
    LifecycleEventEffect(Lifecycle.Event.ON_START) { helper.startLocationUpdates() }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { helper.stopLocationUpdates() }
    DisposableEffect(Unit) { onDispose { helper.close() } }
    return state to request
}

/** Uses actual device/server coordinates; no simulated responders or invented availability. */
@Composable
fun CareMap(location: UserLocationState, modifier: Modifier = Modifier, height: Dp = 270.dp,
            center: LatLng? = null, pins: List<MapPinItem> = emptyList(), route: List<LatLng> = emptyList(),
            status: String? = null, onPinClick: ((MapPinItem) -> Unit)? = null) {
    val target = center ?: LatLng(location.latitude, location.longitude)
    val camera = rememberCameraPositionState { position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(target, 15f) }
    var loaded by remember { mutableStateOf(false) }
    val routeColor = MaterialTheme.colorScheme.primary
    val dark = ThemeManager.isDarkMode
    var now by remember { mutableLongStateOf(android.os.SystemClock.elapsedRealtime()) }
    LaunchedEffect(Unit) { while (true) { delay(5_000); now = android.os.SystemClock.elapsedRealtime() } }
    val fresh = location.hasFix && now - location.fixElapsedMillis in 0 until 120_000
    val centerMarker = remember { MarkerState(target) }
    SideEffect { centerMarker.position = target }
    val style = remember(dark) { if (!dark) null else MapStyleOptions("""[{"elementType":"geometry","stylers":[{"color":"#101817"}]},{"elementType":"labels.text.fill","stylers":[{"color":"#afc3c0"}]},{"elementType":"labels.text.stroke","stylers":[{"color":"#101817"}]},{"featureType":"water","elementType":"geometry","stylers":[{"color":"#000000"}]}]""") }
    LaunchedEffect(target, pins.map { it.position }, loaded) {
        if (loaded) {
            val points = pins.map { it.position } + target
            if (points.distinct().size > 1) {
                val bounds = LatLngBounds.builder().also { b -> points.forEach(b::include) }.build()
                camera.animate(CameraUpdateFactory.newLatLngBounds(bounds, 90))
            } else camera.animate(CameraUpdateFactory.newLatLngZoom(target, 15f))
        }
    }
    Box(modifier.fillMaxWidth().height(height).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
        GoogleMap(modifier = Modifier.fillMaxSize(), cameraPositionState = camera,
            properties = MapProperties(mapStyleOptions = style),
            uiSettings = MapUiSettings(zoomControlsEnabled = false, mapToolbarEnabled = false, myLocationButtonEnabled = false, compassEnabled = false),
            onMapLoaded = { loaded = true }) {
            if (center != null || location.hasFix) Marker(state = centerMarker, title = if (center != null) "Medical SOS location" else if (fresh) "You" else "Your last known location", icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            pins.forEach { pin -> key(pin.id) {
                val marker = remember { MarkerState(pin.position) }
                SideEffect { marker.position = pin.position }
                Marker(state = marker, title = pin.title, snippet = pin.snippet,
                icon = BitmapDescriptorFactory.defaultMarker(pin.hueColor), onClick = { onPinClick?.invoke(pin); true }) } }
            if (route.size > 1) Polyline(points = route, color = routeColor, width = 9f)
        }
        Surface(Modifier.align(Alignment.TopStart).padding(12.dp), shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.background) {
            Row(Modifier.padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Outlined.MyLocation, null, Modifier.size(15.dp), tint = MaterialTheme.colorScheme.primary)
                Text(status ?: if (fresh) "Live location" else if (location.hasFix) "Last known location" else "Waiting for GPS", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
