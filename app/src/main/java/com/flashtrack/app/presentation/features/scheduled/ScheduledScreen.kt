package com.flashtrack.app.presentation.features.scheduled

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.flashtrack.app.domain.repository.ScheduledTransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import com.flashtrack.app.presentation.components.*
import com.flashtrack.app.presentation.theme.*

@HiltViewModel
class ScheduledViewModel @Inject constructor(
    private val repo: ScheduledTransactionRepository
) : ViewModel() {
    val active = repo.getActive()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

@Composable
fun ScheduledScreen(navController: NavController, viewModel: ScheduledViewModel = hiltViewModel()) {
    val scheduled by viewModel.active.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    var query by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().background(Background)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, null, tint = OnSurfaceVariant)
            }
            Text("Scheduled", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
        }
        OutlinedTextField(
            value = query, onValueChange = { query = it },
            placeholder = { Text("Search...", color = OnSurfaceVariant) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = OnSurfaceVariant) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary, unfocusedBorderColor = DividerColor,
                focusedContainerColor = Surface, unfocusedContainerColor = Surface),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp)
        )
        PillSegmentedControl(
            options = listOf("Upcoming", "Completed"),
            selectedIndex = selectedTab,
            onSelect = { selectedTab = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(12.dp))

        val filtered = scheduled.filter {
            it.note.contains(query, true) || it.category.name.contains(query, true)
        }

        if (filtered.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.TopCenter) {
                FCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    Column(Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📅", fontSize = 48.sp)
                        Spacer(Modifier.height(16.dp))
                        Text("Ready to Plan Ahead?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Automate your finances with scheduled transactions. Tap '+' to set up your first one.",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filtered, key = { it.id }) { s ->
                    FCard(Modifier.fillMaxWidth()) {
                        Row(Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            CategoryIcon(s.category.iconName, s.category.colorHex)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(s.note.ifBlank { s.category.name },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium)
                                Text(
                                    "${s.frequency.name.lowercase().replaceFirstChar { it.uppercase() }} · Next: ${formatDate(s.nextDueDate)}",
                                    style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                            }
                            Text(formatAmount(s.amount),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                item { Spacer(Modifier.height(100.dp)) }
            }
        }
    }
}
