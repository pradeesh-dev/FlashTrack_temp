package com.flashtrack.app.presentation.features.accounts

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.flashtrack.app.data.local.entity.AccountType
import com.flashtrack.app.domain.model.*
import com.flashtrack.app.presentation.components.*
import com.flashtrack.app.presentation.navigation.Screen
import com.flashtrack.app.presentation.theme.*

// ─── Accounts Screen ──────────────────────────────────────────────────────────

@Composable
fun AccountsScreen(
    navController: NavController,
    viewModel: AccountsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showAddSheet by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(Background)) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                // Top bar
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("All Accounts", style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Default.Info, null, tint = OnSurfaceVariant,
                            modifier = Modifier.size(16.dp))
                    }
                    Surface(
                        onClick = { showAddSheet = true },
                        color = SurfaceVariant, shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, null, tint = Primary,
                                modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Add account", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
            item {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Transactions based balance, actual may vary.",
                        style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant,
                        modifier = Modifier.weight(1f))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Show balance", style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant)
                        Spacer(Modifier.width(8.dp))
                        Switch(checked = state.showBalance,
                            onCheckedChange = { viewModel.toggleShowBalance() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Primary,
                                checkedTrackColor = PrimaryContainer
                            ))
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
            item {
                // Summary cards
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FCard(Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Available Balance", style = MaterialTheme.typography.labelSmall,
                                    color = OnSurfaceVariant)
                                Spacer(Modifier.width(4.dp))
                                Icon(Icons.Default.Info, null, tint = OnSurfaceVariant,
                                    modifier = Modifier.size(12.dp))
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(if (state.showBalance) formatAmount(state.totalBalance) else "*****",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold)
                        }
                    }
                    FCard(Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Available Credit", style = MaterialTheme.typography.labelSmall,
                                    color = OnSurfaceVariant)
                                Spacer(Modifier.width(4.dp))
                                Icon(Icons.Default.Info, null, tint = OnSurfaceVariant,
                                    modifier = Modifier.size(12.dp))
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(if (state.showBalance) formatAmount(state.totalAvailableCredit) else "*****",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // Bank Accounts
            if (state.bankAccounts.isNotEmpty()) {
                item { AccountSectionHeader("Bank Accounts", Icons.Default.AccountBalance,
                    Color(0xFF4CAF50)) }
                item { AccountGroup(state.bankAccounts, state.showBalance, navController) }
                item { Spacer(Modifier.height(16.dp)) }
            }

            // Credit Cards
            if (state.creditCards.isNotEmpty()) {
                item {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CreditCard, null, tint = Color(0xFFFF7043),
                            modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Credit Cards", style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        PillSegmentedControl(
                            options = listOf("Available", "Outstanding"),
                            selectedIndex = state.creditView,
                            onSelect = viewModel::setCreditView,
                            modifier = Modifier.width(200.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                }
                item {
                    FCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                        Column {
                            state.creditCards.forEachIndexed { idx, acc ->
                                val balance = if (state.creditView == 0)
                                    acc.availableCreditLimit ?: 0.0
                                else (acc.totalCreditLimit ?: 0.0) - (acc.availableCreditLimit ?: 0.0)
                                AccountRow(acc, balance, state.showBalance) {
                                    navController.navigate(Screen.AccountDetail.route(acc.id))
                                }
                                if (idx < state.creditCards.lastIndex)
                                    HorizontalDivider(Modifier.padding(horizontal = 16.dp),
                                        color = DividerColor)
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            // Wallets
            if (state.wallets.isNotEmpty()) {
                item { AccountSectionHeader("Wallets", Icons.Default.Wallet, Color(0xFF1E88E5)) }
                item { AccountGroup(state.wallets, state.showBalance, navController) }
                item { Spacer(Modifier.height(16.dp)) }
            }

            // Cash
            if (state.cashAccounts.isNotEmpty()) {
                item { AccountSectionHeader("Cash", Icons.Default.Payments, Color(0xFF43A047)) }
                item { AccountGroup(state.cashAccounts, state.showBalance, navController) }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }

    if (showAddSheet) {
        AddAccountSheet(onDismiss = { showAddSheet = false })
    }
}

@Composable
private fun AccountSectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color) {
    Row(Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun AccountGroup(accounts: List<Account>, showBalance: Boolean, navController: NavController) {
    FCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Column {
            accounts.forEachIndexed { idx, acc ->
                AccountRow(acc, acc.balance, showBalance) {
                    navController.navigate(Screen.AccountDetail.route(acc.id))
                }
                if (idx < accounts.lastIndex)
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = DividerColor)
            }
        }
    }
}

@Composable
private fun AccountRow(account: Account, balance: Double, showBalance: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(account.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (showBalance) formatAmount(balance) else "*****",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Default.ChevronRight, null, tint = OnSurfaceVariant,
                modifier = Modifier.size(18.dp))
        }
    }
}

// ─── Add Account Sheet ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountSheet(
    onDismiss: () -> Unit,
    viewModel: AddAccountViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // FIX 4: Channel-based one-shot event — sheet can be opened again immediately after save
    LaunchedEffect(Unit) { viewModel.navigationEvent.collect { onDismiss() } }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceVariant,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 32.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("Add account", style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, null, tint = OnSurfaceVariant)
                }
            }
            Spacer(Modifier.height(16.dp))
            PillSegmentedControl(
                options = listOf("Bank Account", "Wallet", "Credit Card"),
                selectedIndex = state.tabIndex,
                onSelect = viewModel::setTab,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(24.dp))

            // Name field
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("A", style = MaterialTheme.typography.titleLarge,
                    color = OnSurfaceVariant, modifier = Modifier.padding(end = 12.dp))
                TextField(
                    value = state.name,
                    onValueChange = viewModel::setName,
                    placeholder = { Text("Name", color = OnSurfaceVariant) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Primary,
                        unfocusedIndicatorColor = DividerColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.height(16.dp))

            if (state.tabIndex < 2) {
                // Bank / Wallet
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                    Icon(Icons.Default.CurrencyRupee, null, tint = OnSurfaceVariant,
                        modifier = Modifier.padding(end = 12.dp, bottom = 8.dp))
                    Column {
                        Text("Current Balance", style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant)
                        TextField(
                            value = state.balance,
                            onValueChange = viewModel::setBalance,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Primary,
                                unfocusedIndicatorColor = DividerColor
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
                // Linked payment modes placeholder
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("Linked payment modes", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                    Surface(onClick = {}, color = SurfaceContainer,
                        shape = RoundedCornerShape(20.dp)) {
                        Row(Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, null, tint = Primary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Add", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
                Spacer(Modifier.height(40.dp))
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Link, null, tint = OnSurfaceVariant,
                        modifier = Modifier.size(40.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("Link your payment modes",
                        style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text("You can add a Debit Card, UPI or other payment modes to use with this bank account.",
                        style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            } else {
                // Credit Card
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                    Icon(Icons.Default.CurrencyRupee, null, tint = OnSurfaceVariant,
                        modifier = Modifier.padding(end = 12.dp, bottom = 8.dp))
                    Column {
                        Text("Current Available Limit", style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant)
                        TextField(value = state.availableLimit,
                            onValueChange = viewModel::setAvailableLimit,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Primary,
                                unfocusedIndicatorColor = DividerColor
                            ), modifier = Modifier.fillMaxWidth())
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                    Icon(Icons.Default.CurrencyRupee, null, tint = OnSurfaceVariant,
                        modifier = Modifier.padding(end = 12.dp, bottom = 8.dp))
                    Column {
                        Text("Total Credit Limit", style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant)
                        TextField(value = state.totalLimit, onValueChange = viewModel::setTotalLimit,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Primary,
                                unfocusedIndicatorColor = DividerColor
                            ), modifier = Modifier.fillMaxWidth())
                    }
                }
                Spacer(Modifier.height(16.dp))
                // Billing Cycle Day
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, null, tint = OnSurfaceVariant,
                        modifier = Modifier.size(20.dp).padding(end = 0.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Billing Cycle Start Date", style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant)
                        Text("%02d of every month".format(state.billingCycleDay),
                            style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, null, tint = OnSurfaceVariant,
                        modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Payment Due Date", style = MaterialTheme.typography.labelSmall,
                            color = OnSurfaceVariant)
                        Text("%02d of every month".format(state.paymentDueDay),
                            style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                Button(
                    onClick = viewModel::save,
                    shape = RoundedCornerShape(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White, contentColor = Color.Black)
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ─── Account Detail Screen ────────────────────────────────────────────────────

@Composable
fun AccountDetailScreen(
    navController: NavController,
    accountId: Long,
    viewModel: AccountDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(accountId) { viewModel.loadAccount(accountId) }

    val isCreditCard = state.account?.type == AccountType.CREDIT_CARD

    Column(Modifier.fillMaxSize().background(Background)) {
        // Top bar
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, null, tint = OnSurfaceVariant)
            }
            Text(state.account?.name ?: "", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            IconButton(onClick = {}) { Icon(Icons.Default.Edit, null, tint = OnSurfaceVariant) }
            IconButton(onClick = {}) { Icon(Icons.Default.Add, null, tint = OnSurfaceVariant) }
        }

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
            return@Column
        }

        LazyColumn(contentPadding = PaddingValues(bottom = 100.dp)) {
            item {
                FCard(Modifier.fillMaxWidth().padding(16.dp)) {
                    Column(Modifier.padding(20.dp)) {
                        if (isCreditCard) {
                            // Credit card header
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top) {
                                Column {
                                    Text("Available Credit Limit",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OnSurfaceVariant)
                                    Text(formatAmount(state.account?.availableCreditLimit ?: 0.0),
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold)
                                }
                                Surface(
                                    color = BadgeGreen,
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, null, tint = BadgeGreenText,
                                            modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("On Track", style = MaterialTheme.typography.labelSmall,
                                            color = BadgeGreenText, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text("Incorrect? Edit", style = MaterialTheme.typography.labelSmall,
                                color = Primary)
                            Spacer(Modifier.height(12.dp))
                            // Progress bar
                            val spent = (state.account?.totalCreditLimit ?: 0.0) - (state.account?.availableCreditLimit ?: 0.0)
                            val total = state.account?.totalCreditLimit ?: 1.0
                            LinearProgressIndicator(
                                progress = { (spent / total).toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = Primary, trackColor = SurfaceVariant
                            )
                            Spacer(Modifier.height(12.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Current Spends", style = MaterialTheme.typography.labelSmall,
                                        color = OnSurfaceVariant)
                                    Text(formatAmount(spent),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Total Credit Limit",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OnSurfaceVariant)
                                    Text(formatAmount(total),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Next Bill expected in ${state.daysUntilNextBill} days.",
                                style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant
                            )
                            Spacer(Modifier.height(14.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { navController.navigate(Screen.BillPayment.route(accountId)) },
                                    shape = RoundedCornerShape(50.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White, contentColor = Color.Black),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Record Payment", fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(onClick = {},
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OnSurface),
                                    border = BorderStroke(1.dp, DividerColor)) {
                                    Icon(Icons.Default.Notifications, null,
                                        modifier = Modifier.size(18.dp))
                                }
                            }
                        } else {
                            // Bank account header
                            Text("Available Balance",
                                style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                            Text(formatAmount(state.account?.balance ?: 0.0),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text("Incorrect balance? Edit",
                                style = MaterialTheme.typography.labelSmall, color = Primary)
                            Spacer(Modifier.height(12.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically) {
                                Text("${state.account?.linkedPaymentModes?.size ?: 0} Linked payment modes",
                                    style = MaterialTheme.typography.bodySmall)
                                Surface(onClick = {}, color = SurfaceVariant,
                                    shape = RoundedCornerShape(20.dp)) {
                                    Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Add, null, tint = Primary,
                                            modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Link", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Tabs
            item {
                TabRow(selectedTabIndex = state.tabIndex,
                    containerColor = Background,
                    contentColor = Primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[state.tabIndex]),
                            color = Primary
                        )
                    }
                ) {
                    listOf("All", "Credit", "Debit", "Adjustments").forEachIndexed { idx, label ->
                        Tab(selected = state.tabIndex == idx, onClick = { viewModel.setTab(idx) }) {
                            Text(label, modifier = Modifier.padding(vertical = 12.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = if (state.tabIndex == idx) Primary else OnSurfaceVariant)
                        }
                    }
                }
            }

            // Transactions
            val filtered = viewModel.filteredTransactions()
            if (filtered.isEmpty()) {
                item {
                    EmptyState("📋", "No transactions", "No transactions for this period",
                        modifier = Modifier.padding(32.dp))
                }
            } else {
                items(filtered) { txn ->
                    Row(Modifier.fillMaxWidth().clickable {}.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text(formatDate(txn.date), style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant, modifier = Modifier.width(50.dp))
                        Text(txn.note.ifBlank { txn.category.name },
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f))
                        val amtColor = when (txn.type) {
                            com.flashtrack.app.data.local.entity.TransactionType.EXPENSE -> SpendingRed
                            com.flashtrack.app.data.local.entity.TransactionType.INCOME -> IncomeGreen
                            else -> OnSurface
                        }
                        Text(formatAmount(txn.amount), style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold, color = amtColor)
                    }
                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = DividerColor)
                }
            }
        }
    }
}

// ─── Bill Payment Screen ──────────────────────────────────────────────────────

@Composable
fun BillPaymentScreen(
    navController: NavController,
    accountId: Long
) {
    var paymentType by remember { mutableIntStateOf(0) }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    Box(Modifier.fillMaxSize().background(Background)) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            // Top bar
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, null, tint = OnSurfaceVariant)
                }
                Text("Bill Payment", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
            }

            Text(
                "Check your credit card bill for accurate amount, and pay the bill in full.",
                style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            PillSegmentedControl(
                options = listOf("Full Payment", "Partial Payment"),
                selectedIndex = paymentType,
                onSelect = { paymentType = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Spacer(Modifier.height(16.dp))

            // Date / Time row
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, null, tint = Primary,
                        modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(formatFullDate(System.currentTimeMillis()),
                        style = MaterialTheme.typography.bodyMedium)
                }
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccessTime, null, tint = OnSurfaceVariant,
                        modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(formatTime(System.currentTimeMillis()),
                        style = MaterialTheme.typography.bodyMedium)
                }
            }
            HorizontalDivider(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), color = DividerColor)

            // Amount
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CurrencyRupee, null, tint = Primary,
                    modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Amount", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                    TextField(value = amount, onValueChange = { amount = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        placeholder = { Text("0", style = MaterialTheme.typography.headlineSmall) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ), textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Calculate, null, tint = OnSurfaceVariant)
                }
            }
            HorizontalDivider(Modifier.padding(horizontal = 16.dp, vertical = 4.dp), color = DividerColor)

            // Payment mode
            Row(Modifier.fillMaxWidth().clickable {}.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Payment, null, tint = Primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Payment mode", style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant)
                    Text("Select", style = MaterialTheme.typography.bodyMedium)
                }
                Icon(Icons.Default.ChevronRight, null, tint = OnSurfaceVariant)
            }
            HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = DividerColor)

            Text("Other details", style = MaterialTheme.typography.labelMedium,
                color = Primary, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))

            // Note
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.Notes, null, tint = OnSurfaceVariant,
                    modifier = Modifier.size(20.dp).padding(top = 4.dp))
                Spacer(Modifier.width(12.dp))
                TextField(value = note, onValueChange = { note = it },
                    placeholder = { Text("Write a note", color = OnSurfaceVariant) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ), modifier = Modifier.fillMaxWidth())
            }
        }

        // Save FAB
        FloatingActionButton(
            onClick = { navController.popBackStack() },
            containerColor = Color.White,
            contentColor = Color.Black,
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp)
        ) {
            Icon(Icons.Default.Save, "Save")
        }
    }
}
