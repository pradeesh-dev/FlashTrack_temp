package com.flashtrack.app.presentation.features.home

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.flashtrack.app.domain.model.*
import com.flashtrack.app.presentation.components.*
import com.flashtrack.app.presentation.navigation.Screen
import com.flashtrack.app.presentation.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        HomeTopBar(
            greeting = state.greeting,
            userName = state.userName,
            onSearch = {}
        )

        Spacer(Modifier.height(8.dp))

        // Cash Flow Card
        CashFlowCard(
            spending = state.spending,
            income = state.income,
            balance = state.availableBalance,
            periodLabel = "This Month",
            onPeriodClick = {},
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(20.dp))

        // Recent Transactions
        SectionHeader(
            title = "Recent Transactions",
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            SeeAllButton(onClick = { navController.navigate(Screen.Transactions.route) })
        }
        Spacer(Modifier.height(8.dp))

        if (state.isLoading) {
            FCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                repeat(3) { ShimmerTransactionItem() }
            }
        } else if (state.recentTransactions.isEmpty()) {
            FCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                EmptyState("💸", "No transactions yet", "Tap + to add your first transaction")
            }
        } else {
            FCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                state.recentTransactions.forEach { txn ->
                    TransactionListItem(transaction = txn,
                        onClick = {
                            navController.navigate(
                                Screen.AddTransaction.route(type = txn.type.name, editId = txn.id)
                            )
                        })
                    if (txn != state.recentTransactions.last())
                        HorizontalDivider(color = DividerColor, thickness = 0.5.dp,
                            modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Budgets Section
        SectionHeader("Budgets", modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(8.dp))
        BudgetCard(
            budget = state.budget,
            safeToSpend = state.safeToSpendPerDay,
            onEditLimit = {},
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(20.dp))

        // Scheduled Section
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Scheduled", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(6.dp))
            Icon(Icons.Default.AddCircle, null, tint = OnSurfaceVariant, modifier = Modifier.size(20.dp)
                .clickable { navController.navigate(Screen.Scheduled.route) })
        }
        Spacer(Modifier.height(8.dp))
        FCard(modifier = Modifier.padding(horizontal = 16.dp)) {
            if (state.scheduledTransactions.isEmpty()) {
                EmptyState("📅", "Ready to Plan Ahead?",
                    "Automate your finances with scheduled transactions. Tap '+' to set up your first one.")
            } else {
                state.scheduledTransactions.forEach { s ->
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        CategoryIcon(s.category.iconName, s.category.colorHex)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(s.note.ifBlank { s.category.name }, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text(s.frequency.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                        }
                        Text(formatAmount(s.amount), style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold, color = SpendingRed)
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Unsettled Debts
        SectionHeader(
            title = "Unsettled Debts",
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.AddCircle, null, tint = OnSurfaceVariant, modifier = Modifier.size(20.dp)
                    .clickable { navController.navigate(Screen.Debts.route) })
                SeeAllButton(onClick = { navController.navigate(Screen.Debts.route) })
            }
        }
        Spacer(Modifier.height(8.dp))

        if (state.unsettledDebts.isEmpty()) {
            FCard(modifier = Modifier.padding(horizontal = 16.dp)) {
                EmptyState("🤝", "No unsettled debts", "Track money you lend or borrow")
            }
        } else {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                state.unsettledDebts.forEach { person ->
                    DebtPersonRow(person = person,
                        onClick = { navController.navigate(Screen.DebtDetail.route(person.id)) })
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Safe to spend banner
        if (state.budget != null && state.safeToSpendPerDay > 0) {
            SafeToSpendBanner(
                safePerDay = state.safeToSpendPerDay,
                monthName = SimpleDateFormat("MMMM", Locale.getDefault()).format(Date()),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun HomeTopBar(greeting: String, userName: String, onSearch: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(greeting, style = MaterialTheme.typography.bodyLarge, color = OnSurfaceVariant)
            Text(userName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(onClick = onSearch, color = SurfaceVariant, shape = CircleShape,
                modifier = Modifier.size(42.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Search, null, tint = OnSurfaceVariant, modifier = Modifier.size(20.dp))
                }
            }
            Box(
                modifier = Modifier.size(42.dp).clip(CircleShape).background(SurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, null, tint = OnSurfaceVariant, modifier = Modifier.size(22.dp))
            }
        }
    }
}

@Composable
fun BudgetCard(
    budget: Budget?,
    safeToSpend: Double,
    onEditLimit: () -> Unit,
    modifier: Modifier = Modifier
) {
    var tab by remember { mutableIntStateOf(0) }
    FCard(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            PillSegmentedControl(
                options = listOf("Monthly", "Annual"),
                selectedIndex = tab,
                onSelect = { tab = it },
                modifier = Modifier.fillMaxWidth(0.6f).align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(12.dp))
            if (budget == null) {
                EmptyState("📊", "No budget set",
                    "Set a monthly budget to track your spending")
            } else {
                SemicircleGauge(
                    spent = budget.spent,
                    limit = budget.amount,
                    remaining = budget.remaining,
                    onEditLimit = onEditLimit,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun SafeToSpendBanner(safePerDay: Double, monthName: String, modifier: Modifier = Modifier) {
    FCard(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Safe to spend: ", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
            Text(formatAmount(safePerDay) + "/day", style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold, color = IncomeGreen)
            Text(" for rest of $monthName.", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
        }
    }
}
