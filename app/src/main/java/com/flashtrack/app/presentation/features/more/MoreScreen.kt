package com.flashtrack.app.presentation.features.more

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.flashtrack.app.presentation.components.*
import com.flashtrack.app.presentation.navigation.Screen
import com.flashtrack.app.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MoreScreen(navController: NavController) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        // Profile card
        FCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Column(Modifier.padding(20.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(56.dp).clip(CircleShape).background(SurfaceVariant),
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Person, null, tint = OnSurfaceVariant,
                            modifier = Modifier.size(32.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("User", style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold)
                        Text("user@email.com", style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant)
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Edit, null, tint = OnSurfaceVariant)
                    }
                    Box(Modifier.size(36.dp).clip(CircleShape).background(SurfaceVariant)
                        .clickable {},
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Shield, null, tint = OnSurfaceVariant,
                            modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(Modifier.height(12.dp))
                HorizontalDivider(color = DividerColor)
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    val now = SimpleDateFormat("dd MMM 'at' hh:mm a", Locale.getDefault()).format(Date())
                    Text("Last backup: G-Drive backup on $now",
                        style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant,
                        modifier = Modifier.weight(1f))
                    Text("Backup now", style = MaterialTheme.typography.labelSmall,
                        color = Primary, fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable {}.padding(start = 8.dp))
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        // Quick access grid 2×3
        Column(Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickCard("Transactions", Icons.Default.ReceiptLong, Primary, Modifier.weight(1f)) {
                    navController.navigate(Screen.Transactions.route)
                }
                QuickCard("Scheduled Txns", Icons.Default.Schedule, Color(0xFF00897B), Modifier.weight(1f)) {
                    navController.navigate(Screen.Scheduled.route)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickCard("Budgets", Icons.Default.Wallet, Color(0xFFE91E63), Modifier.weight(1f)) {
                    navController.navigate(Screen.Budgets.route)
                }
                QuickCard("Categories", Icons.Default.Category, Color(0xFF43A047), Modifier.weight(1f)) {
                    navController.navigate(Screen.Categories.route)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickCard("Tags", Icons.Default.Tag, Color(0xFF00BCD4), Modifier.weight(1f)) {
                    navController.navigate(Screen.Tags.route)
                }
                QuickCard("Debts", Icons.Default.SyncAlt, Color(0xFFFF9800), Modifier.weight(1f)) {
                    navController.navigate(Screen.Debts.route)
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        // Views section
        Text("Views", style = MaterialTheme.typography.titleSmall,
            color = OnSurfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                Triple("Day", Icons.Default.ViewList, {}),
                Triple("Calendar", Icons.Default.CalendarMonth, {}),
                Triple("Custom", Icons.Default.Tune, {})
            ).forEach { (label, icon, onClick) ->
                FCard(Modifier.weight(1f).clickable { onClick() }) {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(icon, null, tint = OnSurfaceVariant, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.height(6.dp))
                        Text(label, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        // More options
        Text("More options", style = MaterialTheme.typography.titleSmall,
            color = OnSurfaceVariant, modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(8.dp))
        FCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Column {
                listOf(
                    Triple(Icons.Default.Settings, "Settings", { navController.navigate("settings") }),
                    Triple(Icons.Default.CardGiftcard, "Gift Premium", {}),
                    Triple(Icons.Default.Star, "Rate app", {}),
                    Triple(Icons.Default.Lightbulb, "Request & Vote on features", {}),
                    Triple(Icons.Default.Feedback, "Query/feedback", {})
                ).forEachIndexed { idx, (icon, label, onClick) ->
                    Row(
                        Modifier.fillMaxWidth().clickable { onClick() }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(icon, null, tint = OnSurfaceVariant, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(14.dp))
                        Text(label, style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ChevronRight, null, tint = OnSurfaceVariant,
                            modifier = Modifier.size(18.dp))
                    }
                    if (idx < 4) HorizontalDivider(Modifier.padding(horizontal = 16.dp),
                        color = DividerColor)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun QuickCard(
    label: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    FCard(modifier = modifier.clickable { onClick() }) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(10.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
        }
    }
}
