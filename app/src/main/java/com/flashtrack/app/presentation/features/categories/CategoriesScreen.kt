package com.flashtrack.app.presentation.features.categories

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
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
import com.flashtrack.app.data.local.entity.TransactionType
import com.flashtrack.app.domain.model.Category
import com.flashtrack.app.domain.repository.CategoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import com.flashtrack.app.presentation.components.*
import com.flashtrack.app.presentation.theme.*

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val categoryRepo: CategoryRepository
) : ViewModel() {
    val expenseCategories = categoryRepo.getByType(TransactionType.EXPENSE)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val incomeCategories = categoryRepo.getByType(TransactionType.INCOME)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

@Composable
fun CategoriesScreen(navController: NavController, viewModel: CategoriesViewModel = hiltViewModel()) {
    val expense by viewModel.expenseCategories.collectAsStateWithLifecycle()
    val income by viewModel.incomeCategories.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    val categories = if (selectedTab == 0) expense else income

    Column(Modifier.fillMaxSize().background(Background)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, null, tint = OnSurfaceVariant)
            }
            Text("Categories", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            TextButton(onClick = {}) { Text("Edit order", color = Primary) }
        }
        PillSegmentedControl(
            options = listOf("Expense", "Income"),
            selectedIndex = selectedTab,
            onSelect = { selectedTab = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(12.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(categories) { cat ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CategoryIcon(cat.iconName, cat.colorHex, size = 56.dp, iconSize = 24.dp)
                    Spacer(Modifier.height(4.dp))
                    Text(cat.name, style = MaterialTheme.typography.labelSmall,
                        maxLines = 2,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(56.dp).background(SurfaceVariant,
                        RoundedCornerShape(12.dp)).clickable {},
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Add, null, tint = OnSurfaceVariant)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("Add", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
