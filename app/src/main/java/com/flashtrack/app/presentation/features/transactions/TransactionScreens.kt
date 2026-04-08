package com.flashtrack.app.presentation.features.transactions

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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.flashtrack.app.data.local.entity.TransactionType
import com.flashtrack.app.domain.model.*
import com.flashtrack.app.domain.repository.TransactionRepository
import com.flashtrack.app.presentation.components.*
import com.flashtrack.app.presentation.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

// ─── Add / Edit Transaction Screen ───────────────────────────────────────────

@Composable
fun AddTransactionScreen(
    navController: NavController,
    initialType: String = "EXPENSE",
    initialAccountId: Long = -1L,
    editTransactionId: Long = -1L,           // FIX 3: edit mode param
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        if (editTransactionId > 0) {
            viewModel.loadTransaction(editTransactionId)   // FIX 3: load existing
        } else {
            viewModel.setInitialType(initialType)
        }
    }

    // FIX 3: consume one-shot Channel event
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { navController.popBackStack() }
    }

    Box(Modifier.fillMaxSize().background(Background)) {
        Column(
            // FIX 15: imePadding() so keyboard pushes content up, not over FAB
            Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 90.dp)
        ) {
            // Top bar
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, null, tint = OnSurfaceVariant)
                }
                Text(
                    if (state.isEditMode) "Edit transaction" else "Add transaction",
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold
                )
            }

            // Type selector
            PillSegmentedControl(
                options = listOf("Expense", "Income", "Transfer"),
                selectedIndex = when (state.type) {
                    TransactionType.EXPENSE -> 0; TransactionType.INCOME -> 1; else -> 2
                },
                onSelect = {
                    viewModel.setType(when (it) {
                        1 -> TransactionType.INCOME; 2 -> TransactionType.TRANSFER
                        else -> TransactionType.EXPENSE
                    })
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(20.dp))

            // Date / Time
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Row(Modifier.weight(1f).clickable {}, verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, null, tint = OnSurfaceVariant, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(formatFullDate(state.date), style = MaterialTheme.typography.bodyMedium)
                }
                Row(Modifier.weight(1f).clickable {}, verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccessTime, null, tint = OnSurfaceVariant, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(formatTime(state.date), style = MaterialTheme.typography.bodyMedium)
                }
            }
            HorizontalDivider(Modifier.padding(horizontal = 16.dp, vertical = 16.dp), color = DividerColor)

            // Amount
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CurrencyRupee, null, tint = OnSurfaceVariant, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Amount", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                    TextField(
                        value = state.amount,
                        onValueChange = viewModel::setAmount,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        placeholder = { Text("0", style = MaterialTheme.typography.headlineSmall, color = OnSurfaceVariant) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Box(
                    Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(SurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Calculate, null, tint = OnSurfaceVariant)
                }
            }
            HorizontalDivider(Modifier.padding(horizontal = 16.dp, vertical = 4.dp), color = DividerColor)

            // Category
            Row(
                Modifier.fillMaxWidth().clickable { viewModel.showCategorySheet() }
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (state.selectedCategory != null) {
                    CategoryIcon(state.selectedCategory!!.iconName, state.selectedCategory!!.colorHex, size = 40.dp, iconSize = 18.dp)
                } else {
                    Box(Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(SurfaceVariant),
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.MoreHoriz, null, tint = OnSurfaceVariant)
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Category", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                    Text(state.selectedCategory?.name ?: "Select", style = MaterialTheme.typography.bodyMedium)
                }
                Icon(Icons.Default.ChevronRight, null, tint = OnSurfaceVariant)
            }
            HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = DividerColor)

            // Payment mode
            Row(
                Modifier.fillMaxWidth().clickable { viewModel.showPaymentSheet() }
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(SurfaceVariant),
                    contentAlignment = Alignment.Center) {
                    Icon(iconFromName(state.selectedPaymentMode?.iconName ?: "payment"), null,
                        tint = Primary, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Payment mode", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                    Text(state.selectedPaymentMode?.name ?: "Select", style = MaterialTheme.typography.bodyMedium)
                }
                Icon(Icons.Default.ChevronRight, null, tint = OnSurfaceVariant)
            }

            Spacer(Modifier.height(8.dp))
            Text("Other details", style = MaterialTheme.typography.labelMedium,
                color = Primary, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

            // Note
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.Notes, null, tint = OnSurfaceVariant,
                    modifier = Modifier.size(20.dp).padding(top = 4.dp))
                Spacer(Modifier.width(14.dp))
                TextField(
                    value = state.note,
                    onValueChange = viewModel::setNote,
                    placeholder = { Text("Write a note", color = OnSurfaceVariant) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = DividerColor)

            // Tags
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Tag, null, tint = OnSurfaceVariant, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Add tags", color = OnSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                    if (state.allTags.isNotEmpty()) {
                        Spacer(Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        ) {
                            state.allTags.take(8).forEach { tag ->
                                val selected = state.selectedTagIds.contains(tag.id)
                                Surface(
                                    onClick = { viewModel.toggleTag(tag.id) },
                                    color = if (selected) Primary.copy(alpha = 0.2f) else SurfaceVariant,
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Text(tag.name,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (selected) Primary else OnSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
            HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = DividerColor)

            // Attachment
            Row(Modifier.fillMaxWidth().clickable {}
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AttachFile, null, tint = OnSurfaceVariant, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(14.dp))
                Text("Add attachment", color = OnSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Icon(Icons.Default.ChevronRight, null, tint = OnSurfaceVariant)
            }

            if (state.error != null) {
                Text(state.error!!, color = SpendingRed, style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp))
            }
        }

        // Save FAB
        FloatingActionButton(
            onClick = viewModel::save,
            containerColor = Color.White,
            contentColor = Color.Black,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .navigationBarsPadding()
                .imePadding(),        // FIX 15: FAB floats above keyboard
            shape = androidx.compose.foundation.shape.CircleShape
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.Black, strokeWidth = 2.dp)
            } else {
                Icon(Icons.Default.Save, "Save", modifier = Modifier.size(24.dp))
            }
        }

        // Payment Mode Sheet
        if (state.showPaymentSheet) {
            SelectPaymentModeSheet(
                allAccounts = state.allAccounts,
                allPaymentModes = state.allPaymentModes,
                selectedPaymentMode = state.selectedPaymentMode,
                onSelect = viewModel::setPaymentMode,
                onDismiss = viewModel::hidePaymentSheet
            )
        }

        // Category Sheet
        if (state.showCategorySheet) {
            val cats = state.allCategories.filter {
                // FIX 10: Transfer shows Expense categories as fallback
                if (state.type == TransactionType.TRANSFER) it.type == TransactionType.EXPENSE
                else it.type == state.type
            }
            SelectCategorySheet(
                categories = cats,
                onSelect = viewModel::setCategory,
                onDismiss = viewModel::hideCategorySheet
            )
        }
    }
}

// ─── Select Payment Mode Sheet ────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectPaymentModeSheet(
    allAccounts: List<Account>,
    allPaymentModes: List<PaymentMode>,
    selectedPaymentMode: PaymentMode?,
    onSelect: (PaymentMode) -> Unit,
    onDismiss: () -> Unit
) {
    var showBalance by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceVariant,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Text("Select Payment Mode", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                IconButton(onClick = {}) { Icon(Icons.Default.Edit, null, tint = OnSurfaceVariant) }
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null, tint = OnSurfaceVariant) }
            }

            val bankAccounts = allAccounts.filter { it.type == com.flashtrack.app.data.local.entity.AccountType.BANK }
            val creditCards  = allAccounts.filter { it.type == com.flashtrack.app.data.local.entity.AccountType.CREDIT_CARD }
            val wallets      = allAccounts.filter { it.type == com.flashtrack.app.data.local.entity.AccountType.WALLET }

            LazyColumn(Modifier.fillMaxWidth().heightIn(max = 520.dp)) {
                // ── Bank Accounts ──
                if (bankAccounts.isNotEmpty()) {
                    item {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountBalance, null, tint = Primary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Bank Accounts", style = MaterialTheme.typography.labelLarge,
                                color = Primary, modifier = Modifier.weight(1f))
                            Text("Show balance", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                            Spacer(Modifier.width(8.dp))
                            Switch(checked = showBalance, onCheckedChange = { showBalance = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Primary, checkedTrackColor = PrimaryContainer))
                        }
                        FCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(12.dp)) {
                            Column {
                                bankAccounts.forEach { account ->
                                    val linkedPMs = allPaymentModes.filter { it.linkedAccountId == account.id }
                                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(selected = false, onClick = null,
                                            colors = RadioButtonDefaults.colors(selectedColor = Primary))
                                        Spacer(Modifier.width(8.dp))
                                        Text(account.name, style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.weight(1f))
                                        Text(if (showBalance) formatAmount(account.balance) else "*****",
                                            style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                                    }
                                    if (linkedPMs.isNotEmpty()) {
                                        Text("Linked payment modes", style = MaterialTheme.typography.labelSmall,
                                            color = OnSurfaceVariant, modifier = Modifier.padding(start = 48.dp, bottom = 4.dp))
                                        linkedPMs.forEach { pm ->
                                            Row(Modifier.fillMaxWidth().clickable { onSelect(pm) }
                                                .padding(start = 48.dp, end = 16.dp, top = 6.dp, bottom = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically) {
                                                RadioButton(selected = selectedPaymentMode?.id == pm.id,
                                                    onClick = { onSelect(pm) },
                                                    colors = RadioButtonDefaults.colors(selectedColor = Primary))
                                                Spacer(Modifier.width(8.dp))
                                                Icon(iconFromName(pm.iconName), null, tint = Primary, modifier = Modifier.size(18.dp))
                                                Spacer(Modifier.width(8.dp))
                                                Text(pm.name, style = MaterialTheme.typography.bodyMedium)
                                            }
                                        }
                                    }
                                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = DividerColor)
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }

                // ── Credit Cards ──
                if (creditCards.isNotEmpty()) {
                    item {
                        Row(Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CreditCard, null, tint = Color(0xFFFF9800), modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Credit Cards", style = MaterialTheme.typography.labelLarge, color = Color(0xFFFF9800))
                        }
                        FCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(12.dp)) {
                            Column {
                                creditCards.forEach { card ->
                                    val pm = allPaymentModes.firstOrNull { it.linkedAccountId == card.id }
                                        ?: PaymentMode(id = card.id, name = card.name,
                                            type = com.flashtrack.app.data.local.entity.PaymentModeType.CREDIT_CARD,
                                            linkedAccountId = card.id, iconName = "credit_card")
                                    Row(Modifier.fillMaxWidth().clickable { onSelect(pm) }
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(selected = selectedPaymentMode?.linkedAccountId == card.id,
                                            onClick = { onSelect(pm) },
                                            colors = RadioButtonDefaults.colors(selectedColor = Primary))
                                        Spacer(Modifier.width(8.dp))
                                        Text(card.name, style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.weight(1f))
                                        Text(if (showBalance) formatAmount(card.availableCreditLimit ?: 0.0) else "*****",
                                            style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                                    }
                                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = DividerColor)
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }

                // FIX 11: Wallets — only show payment modes linked to wallets (no duplication)
                if (wallets.isNotEmpty()) {
                    item {
                        Row(Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Wallet, null, tint = Color(0xFF1E88E5), modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Wallets", style = MaterialTheme.typography.labelLarge, color = Color(0xFF1E88E5))
                        }
                        FCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(12.dp)) {
                            Column {
                                wallets.forEach { wallet ->
                                    // FIX 11: Use linked payment mode if exists, else represent wallet directly ONCE
                                    val linkedPMs = allPaymentModes.filter { it.linkedAccountId == wallet.id }
                                    if (linkedPMs.isNotEmpty()) {
                                        linkedPMs.forEach { pm ->
                                            Row(Modifier.fillMaxWidth().clickable { onSelect(pm) }
                                                .padding(horizontal = 16.dp, vertical = 14.dp),
                                                verticalAlignment = Alignment.CenterVertically) {
                                                RadioButton(selected = selectedPaymentMode?.id == pm.id,
                                                    onClick = { onSelect(pm) },
                                                    colors = RadioButtonDefaults.colors(selectedColor = Primary))
                                                Spacer(Modifier.width(8.dp))
                                                Icon(iconFromName(pm.iconName), null, tint = Color(0xFF1E88E5), modifier = Modifier.size(18.dp))
                                                Spacer(Modifier.width(8.dp))
                                                Text(pm.name, style = MaterialTheme.typography.bodyMedium,
                                                    modifier = Modifier.weight(1f))
                                                Text(if (showBalance) formatAmount(wallet.balance) else "*****",
                                                    style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                                            }
                                        }
                                    } else {
                                        // No linked PM → show wallet account directly once
                                        val pm = PaymentMode(id = -wallet.id, name = wallet.name,
                                            type = com.flashtrack.app.data.local.entity.PaymentModeType.WALLET,
                                            linkedAccountId = wallet.id, iconName = "wallet")
                                        Row(Modifier.fillMaxWidth().clickable { onSelect(pm) }
                                            .padding(horizontal = 16.dp, vertical = 14.dp),
                                            verticalAlignment = Alignment.CenterVertically) {
                                            RadioButton(selected = selectedPaymentMode?.linkedAccountId == wallet.id,
                                                onClick = { onSelect(pm) },
                                                colors = RadioButtonDefaults.colors(selectedColor = Primary))
                                            Spacer(Modifier.width(8.dp))
                                            Icon(Icons.Default.Wallet, null, tint = Color(0xFF1E88E5), modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text(wallet.name, style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier.weight(1f))
                                            Text(if (showBalance) formatAmount(wallet.balance) else "*****",
                                                style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                                        }
                                    }
                                    HorizontalDivider(Modifier.padding(horizontal = 16.dp), color = DividerColor)
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

// ─── Select Category Sheet ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectCategorySheet(
    categories: List<Category>,
    onSelect: (Category) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceVariant,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(Modifier.padding(bottom = 32.dp)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Text("Select Category", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null, tint = OnSurfaceVariant) }
            }
            if (categories.isEmpty()) {
                EmptyState("📁", "No categories", "Add categories from More → Categories",
                    modifier = Modifier.padding(32.dp))
            } else {
                androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                    columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(4),
                    contentPadding = PaddingValues(16.dp),
                    modifier = Modifier.heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(categories.size) { idx ->
                        val cat = categories[idx]
                        Column(Modifier.clickable { onSelect(cat) },
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            CategoryIcon(cat.iconName, cat.colorHex, size = 52.dp, iconSize = 22.dp)
                            Spacer(Modifier.height(4.dp))
                            Text(cat.name, style = MaterialTheme.typography.labelSmall,
                                maxLines = 2,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                }
            }
        }
    }
}

// ─── Transactions List Screen ─────────────────────────────────────────────────

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val transactionRepo: TransactionRepository
) : ViewModel() {
    val transactions = transactionRepo.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    fun setSearchQuery(q: String) { _searchQuery.value = q }

    fun filtered(list: List<Transaction>): List<Transaction> {
        val q = _searchQuery.value.trim()
        if (q.isEmpty()) return list
        return list.filter {
            it.note.contains(q, true) ||
            it.category.name.contains(q, true) ||
            it.paymentMode.name.contains(q, true)
        }
    }
}

@Composable
fun TransactionsScreen(navController: NavController, viewModel: TransactionsViewModel = hiltViewModel()) {
    val allTxns by viewModel.transactions.collectAsStateWithLifecycle()
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filtered = viewModel.filtered(allTxns)

    // FIX 5: bottom padding so last item not hidden behind nav bar
    Column(Modifier.fillMaxSize().background(Background)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, null, tint = OnSurfaceVariant)
            }
            Text("Transactions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        OutlinedTextField(
            value = query, onValueChange = viewModel::setSearchQuery,
            placeholder = { Text("Search transactions...", color = OnSurfaceVariant) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = OnSurfaceVariant) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary, unfocusedBorderColor = DividerColor,
                focusedContainerColor = Surface, unfocusedContainerColor = Surface),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp)
        )

        if (filtered.isEmpty()) {
            EmptyState("💸", "No transactions", "Tap + to add your first transaction",
                modifier = Modifier.padding(32.dp))
        } else {
            LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, bottom = 24.dp)) {
                val grouped = filtered.groupBy { formatDate(it.date) }
                grouped.forEach { (dateLabel, txns) ->
                    item {
                        Text(dateLabel, style = MaterialTheme.typography.labelMedium,
                            color = OnSurfaceVariant, modifier = Modifier.padding(vertical = 8.dp))
                    }
                    item {
                        FCard(Modifier.fillMaxWidth()) {
                            Column {
                                txns.forEachIndexed { idx, txn ->
                                    // FIX 3: clicking a transaction opens it in edit mode
                                    TransactionListItem(
                                        transaction = txn,
                                        onClick = {
                                            navController.navigate(
                                                Screen.AddTransaction.route(
                                                    type = txn.type.name,
                                                    editId = txn.id
                                                )
                                            )
                                        }
                                    )
                                    if (idx < txns.lastIndex)
                                        HorizontalDivider(Modifier.padding(horizontal = 16.dp),
                                            color = DividerColor, thickness = 0.5.dp)
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}
