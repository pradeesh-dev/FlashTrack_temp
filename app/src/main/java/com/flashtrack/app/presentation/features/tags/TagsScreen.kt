package com.flashtrack.app.presentation.features.tags

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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.flashtrack.app.data.local.entity.TagEntity
import com.flashtrack.app.domain.model.Tag
import com.flashtrack.app.domain.repository.TagRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.flashtrack.app.presentation.components.*
import com.flashtrack.app.presentation.theme.*

@HiltViewModel
class TagsViewModel @Inject constructor(
    private val tagRepo: TagRepository
) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query
    val tags = _query.flatMapLatest { q ->
        if (q.isBlank()) tagRepo.getAll() else tagRepo.search(q)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setQuery(q: String) = _query.update { q }

    fun addTag(name: String) {
        viewModelScope.launch { tagRepo.addTag(TagEntity(name = name)) }
    }
}

@Composable
fun TagsScreen(navController: NavController, viewModel: TagsViewModel = hiltViewModel()) {
    val tags by viewModel.tags.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(Background)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, null, tint = OnSurfaceVariant)
            }
            Text("Tags", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
        }
        OutlinedTextField(
            value = query, onValueChange = viewModel::setQuery,
            placeholder = { Text("Search tags...", color = OnSurfaceVariant) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = OnSurfaceVariant) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary, unfocusedBorderColor = DividerColor,
                focusedContainerColor = Surface, unfocusedContainerColor = Surface),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp)
        )
        if (tags.isEmpty()) {
            EmptyState("#️⃣", "No tags yet", "Add a tag to organize your transactions",
                modifier = Modifier.padding(32.dp))
        } else {
            LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp)) {
                item {
                    FCard(Modifier.fillMaxWidth()) {
                        Column {
                            tags.forEachIndexed { idx, tag ->
                                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Tag, null, tint = Primary,
                                        modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(12.dp))
                                    Text(tag.name, style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f))
                                    Text("${tag.transactionCount} transactions",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OnSurfaceVariant)
                                }
                                if (idx < tags.lastIndex)
                                    HorizontalDivider(Modifier.padding(horizontal = 16.dp),
                                        color = DividerColor)
                            }
                            // Add tag row
                            Row(
                                Modifier.fillMaxWidth().clickable { showAddDialog = true }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Add, null, tint = Primary,
                                    modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(12.dp))
                                Text("Add tag", style = MaterialTheme.typography.bodyMedium,
                                    color = Primary)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var newTag by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = SurfaceVariant,
            title = { Text("Add Tag") },
            text = {
                OutlinedTextField(value = newTag, onValueChange = { newTag = it },
                    placeholder = { Text("Tag name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary, unfocusedBorderColor = DividerColor))
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newTag.isNotBlank()) { viewModel.addTag(newTag); showAddDialog = false }
                }) { Text("Add", color = Primary) }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}
