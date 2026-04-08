package com.flashtrack.app.presentation.features.settings

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.flashtrack.app.presentation.components.*
import com.flashtrack.app.presentation.theme.*

@Composable
fun SettingsScreen(navController: NavController) {
    Column(
        Modifier.fillMaxSize().background(Background).verticalScroll(rememberScrollState()).padding(bottom = 24.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, null, tint = OnSurfaceVariant)
            }
            Text("Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }

        SettingsGroup("General") {
            SettingsRow(Icons.Default.Person, "Profile", "Name, email, photo")
            SettingsRow(Icons.Default.CurrencyRupee, "Currency", "₹ Indian Rupee")
            SettingsRow(Icons.Default.DateRange, "Date Format", "dd MMM yyyy")
        }
        Spacer(Modifier.height(12.dp))
        SettingsGroup("Display") {
            SettingsRow(icon = Icons.Default.Visibility, title = "Show Balance", subtitle = "Toggle account balances",
                trailing = {
                    var on by remember { mutableStateOf(true) }
                    Switch(checked = on, onCheckedChange = { on = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Primary, checkedTrackColor = PrimaryContainer))
                })
            SettingsRow(Icons.Default.Notifications, "Notifications", "Reminders & alerts")
        }
        Spacer(Modifier.height(12.dp))
        SettingsGroup("Data") {
            SettingsRow(Icons.Default.CloudUpload, "Backup", "Google Drive backup")
            SettingsRow(Icons.Default.CloudDownload, "Restore", "Restore from backup")
            SettingsRow(Icons.Default.FileDownload, "Export", "Export as CSV or PDF")
        }
        Spacer(Modifier.height(12.dp))
        SettingsGroup("About") {
            SettingsRow(Icons.Default.Info, "App Version", "1.0.0")
            SettingsRow(Icons.Default.PrivacyTip, "Privacy Policy", "")
        }
    }
}

@Composable
private fun SettingsGroup(title: String, content: @Composable ColumnScope.() -> Unit) {
    Text(title, style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp))
    FCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) { Column { content() } }
}

@Composable
private fun SettingsRow(icon: ImageVector, title: String, subtitle: String,
    trailing: (@Composable () -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().clickable {}.padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = OnSurfaceVariant, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium)
            if (subtitle.isNotEmpty())
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
        }
        trailing?.invoke() ?: Icon(Icons.Default.ChevronRight, null,
            tint = OnSurfaceVariant, modifier = Modifier.size(18.dp))
    }
    HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = DividerColor, thickness = 0.5.dp)
}
