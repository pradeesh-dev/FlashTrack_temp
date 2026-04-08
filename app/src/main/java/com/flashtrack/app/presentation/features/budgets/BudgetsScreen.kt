package com.flashtrack.app.presentation.features.budgets

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.flashtrack.app.data.local.entity.*
import com.flashtrack.app.domain.model.Budget
import com.flashtrack.app.domain.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.flashtrack.app.presentation.components.*
import com.flashtrack.app.presentation.theme.*
import java.util.Calendar

@HiltViewModel
class BudgetsViewModel @Inject constructor(
    private val budgetRepo: BudgetRepository
) : ViewModel() {
    private val cal = Calendar.getInstance()
    val currentYear = cal.get(Calendar.YEAR)
    val currentMonth = cal.get(Calendar.MONTH) + 1

    val monthlyBudget = budgetRepo.getMonthlyBudget(currentYear, currentMonth)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val allMonthly = budgetRepo.getAllMonthly()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setBudget(amount: Double) {
        viewModelScope.launch {
            val existing = monthlyBudget.value
            if (existing != null) {
                budgetRepo.updateBudget(BudgetEntity(
                    id = existing.id, amount = amount,
                    period = BudgetPeriod.MONTHLY, year = currentYear, month = currentMonth))
            } else {
                budgetRepo.addBudget(BudgetEntity(
                    amount = amount, period = BudgetPeriod.MONTHLY,
                    year = currentYear, month = currentMonth))
            }
        }
    }
}

@Composable
fun BudgetsScreen(navController: NavController, viewModel: BudgetsViewModel = hiltViewModel()) {
    val monthlyBudget by viewModel.monthlyBudget.collectAsStateWithLifecycle()
    val allMonthly by viewModel.allMonthly.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showSetBudget by remember { mutableStateOf(false) }
    var budgetInput by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().background(Background)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, null, tint = OnSurfaceVariant)
            }
            Text("Budgets", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
        }
        PillSegmentedControl(
            options = listOf("Monthly", "Yearly"),
            selectedIndex = selectedTab,
            onSelect = { selectedTab = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(12.dp))
        LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Active budget
            item {
                if (monthlyBudget != null) {
                    FCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            SemicircleGauge(
                                spent = monthlyBudget!!.spent,
                                limit = monthlyBudget!!.amount,
                                remaining = monthlyBudget!!.remaining,
                                modifier = Modifier.fillMaxWidth(),
                                onEditLimit = { showSetBudget = true }
                            )
                            Spacer(Modifier.height(12.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                val m = java.text.SimpleDateFormat("MMMM",
                                    java.util.Locale.getDefault()).format(java.util.Date())
                                Text("Safe to spend: ", style = MaterialTheme.typography.bodySmall,
                                    color = OnSurfaceVariant)
                                val days = Calendar.getInstance().getActualMaximum(
                                    Calendar.DAY_OF_MONTH) - Calendar.getInstance().get(
                                    Calendar.DAY_OF_MONTH) + 1
                                val safe = if (days > 0) monthlyBudget!!.remaining / days else 0.0
                                Text(formatAmount(safe) + "/day",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = IncomeGreen, fontWeight = FontWeight.SemiBold)
                                Text(" for rest of $m.", style = MaterialTheme.typography.bodySmall,
                                    color = OnSurfaceVariant)
                            }
                        }
                    }
                } else {
                    FCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📊", style = MaterialTheme.typography.headlineLarge)
                            Spacer(Modifier.height(12.dp))
                            Text("Plan Ahead", style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            Text("Set a budget to track your spending",
                                style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { showSetBudget = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Primary, contentColor = Color.White)) {
                                Text("Set Budget")
                            }
                        }
                    }
                }
            }

            // Past budgets
            val pastBudgets = allMonthly.drop(1)
            if (pastBudgets.isNotEmpty()) {
                item {
                    Text("Past Budgets", style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold, color = OnSurfaceVariant)
                }
                items(pastBudgets) { budget ->
                    PastBudgetRow(budget)
                }
            }
        }
    }

    if (showSetBudget) {
        AlertDialog(
            onDismissRequest = { showSetBudget = false },
            containerColor = SurfaceVariant,
            title = { Text("Set Monthly Budget") },
            text = {
                OutlinedTextField(
                    value = budgetInput, onValueChange = { budgetInput = it },
                    placeholder = { Text("Budget amount") },
                    leadingIcon = { Text("₹", style = MaterialTheme.typography.titleMedium) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary, unfocusedBorderColor = DividerColor))
            },
            confirmButton = {
                TextButton(onClick = {
                    val amt = budgetInput.toDoubleOrNull()
                    if (amt != null && amt > 0) { viewModel.setBudget(amt); showSetBudget = false }
                }) { Text("Set", color = Primary) }
            },
            dismissButton = { TextButton(onClick = { showSetBudget = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun PastBudgetRow(budget: Budget) {
    FCard(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                val label = budget.month?.let { m ->
                    val cal = Calendar.getInstance().also { it.set(budget.year, m - 1, 1) }
                    java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault()).format(cal.time)
                } ?: "${budget.year}"
                Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text("Limit: ${formatAmount(budget.amount)}",
                    style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(formatAmount(budget.spent), style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (budget.isOverBudget) SpendingRed else IncomeGreen)
                if (budget.isOverBudget) {
                    val overPct = ((budget.spent - budget.amount) / budget.amount * 100).toInt()
                    Text("Overspent $overPct%", style = MaterialTheme.typography.labelSmall,
                        color = SpendingRed)
                }
            }
        }
    }
}
