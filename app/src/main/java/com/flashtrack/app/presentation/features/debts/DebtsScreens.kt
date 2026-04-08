package com.flashtrack.app.presentation.features.debts

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
import com.flashtrack.app.data.local.entity.*
import com.flashtrack.app.domain.model.*
import com.flashtrack.app.presentation.components.*
import com.flashtrack.app.presentation.navigation.Screen
import com.flashtrack.app.presentation.theme.*

// ─── Debts Screen ─────────────────────────────────────────────────────────────

@Composable
fun DebtsScreen(navController: NavController, viewModel: DebtsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showAddSheet by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(Background)) {
        Column(Modifier.fillMaxSize()) {
            // Top bar
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, null, tint = OnSurfaceVariant)
                }
                Text("Debts", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Help, null, tint = OnSurfaceVariant)
                }
            }

            // Tabs
            PillSegmentedControl(
                options = listOf("All", "Lending", "Borrowing"),
                selectedIndex = state.tabIndex,
                onSelect = viewModel::setTab,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(12.dp))

            // Summary cards
            SummaryTwoColumn(
                leftLabel = "Payable", leftValue = formatAmount(state.totalPayable),
                leftColor = SpendingRed,
                rightLabel = "Receivable", rightValue = formatAmount(state.totalReceivable),
                rightColor = IncomeGreen,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(12.dp))

            if (state.isLoading) {
                repeat(3) {
                    ShimmerBox(Modifier.fillMaxWidth().height(72.dp).padding(horizontal = 16.dp, vertical = 4.dp),
                        RoundedCornerShape(12.dp))
                }
            } else if (state.filteredPersons.isEmpty()) {
                EmptyState("🤝", "No debts yet", "Track money you lend or borrow",
                    modifier = Modifier.padding(32.dp))
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.filteredPersons, key = { it.id }) { person ->
                        DebtPersonRow(person = person,
                            onClick = { navController.navigate(Screen.DebtDetail.route(person.id)) })
                    }
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = { showAddSheet = true },
            containerColor = Color.White, contentColor = Color.Black,
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            shape = androidx.compose.foundation.shape.CircleShape
        ) {
            Icon(Icons.Default.Add, "Add debt")
        }
    }

    if (showAddSheet) {
        AddDebtTypeSheet(
            onSelectType = { type ->
                showAddSheet = false
                navController.navigate(Screen.AddDebt.route(type.name))
            },
            onDismiss = { showAddSheet = false }
        )
    }
}

// ─── Add Debt Type Sheet ──────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDebtTypeSheet(onSelectType: (DebtType) -> Unit, onDismiss: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceVariant,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 40.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Add debt", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Select what you want to track:", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, null, tint = OnSurfaceVariant)
                }
            }
            Spacer(Modifier.height(16.dp))

            // Lend Money option
            Surface(
                onClick = { onSelectType(DebtType.LENDING) },
                color = Surface, shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(BadgeGreen),
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.ArrowUpward, null, tint = BadgeGreenText, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Lend Money", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        Text("Record money you've lent to someone.", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                    }
                    Icon(Icons.Default.ChevronRight, null, tint = OnSurfaceVariant)
                }
            }
            Spacer(Modifier.height(8.dp))

            // Borrow Money option
            Surface(
                onClick = { onSelectType(DebtType.BORROWING) },
                color = Surface, shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF3A1515)),
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.ArrowDownward, null, tint = SpendingRed, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Borrow Money", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        Text("Track money you owe to someone.", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                    }
                    Icon(Icons.Default.ChevronRight, null, tint = OnSurfaceVariant)
                }
            }
        }
    }
}

// ─── Debt Detail Screen ───────────────────────────────────────────────────────

@Composable
fun DebtDetailScreen(
    navController: NavController,
    personId: Long,
    viewModel: DebtDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showAddRecord by remember { mutableStateOf(false) }

    LaunchedEffect(personId) { viewModel.loadPerson(personId) }

    Box(Modifier.fillMaxSize().background(Background)) {
        Column(Modifier.fillMaxSize()) {
            // Top bar
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, null, tint = OnSurfaceVariant)
                }
                Text(state.person?.name ?: "", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                IconButton(onClick = {}) { Icon(Icons.Default.Edit, null, tint = OnSurfaceVariant) }
                IconButton(onClick = {
                    state.person?.let { p ->
                        viewModel.deletePerson(DebtPersonEntity(
                            id = p.id, name = p.name, type = p.type,
                            initialAmount = p.initialAmount,
                            dueDate = p.dueDate, note = p.note, isSettled = p.isSettled
                        )) { navController.popBackStack() }
                    }
                }) { Icon(Icons.Default.Delete, null, tint = SpendingRed) }
            }

            LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, bottom = 24.dp)) {
                // Header card
                item {
                    val person = state.person
                    if (person != null) {
                        FCard(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(20.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        if (person.type == DebtType.LENDING) "Total Receivable" else "Total Payable",
                                        style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant
                                    )
                                    Icon(Icons.Default.MoreVert, null, tint = OnSurfaceVariant)
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(formatAmount(person.displayAmount),
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (person.type == DebtType.LENDING) IncomeGreen else SpendingRed)
                                Row {
                                    Text("Incorrect? ", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                                    Text("Edit", style = MaterialTheme.typography.bodySmall, color = Primary,
                                        modifier = Modifier.clickable {})
                                }
                                Spacer(Modifier.height(16.dp))
                                HorizontalDivider(color = DividerColor)
                                Spacer(Modifier.height(16.dp))
                                Row(Modifier.fillMaxWidth()) {
                                    // Total Paid
                                    Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                        Box(Modifier.size(36.dp).clip(androidx.compose.foundation.shape.CircleShape)
                                            .background(Color(0xFF3A1515)), contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Remove, null, tint = SpendingRed, modifier = Modifier.size(18.dp))
                                        }
                                        Spacer(Modifier.width(10.dp))
                                        Column {
                                            Text("Total Paid", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                                            Text(formatAmount(person.totalPaid),
                                                style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                    // Total Received
                                    Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                        Box(Modifier.size(36.dp).clip(androidx.compose.foundation.shape.CircleShape)
                                            .background(BadgeGreen), contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Add, null, tint = BadgeGreenText, modifier = Modifier.size(18.dp))
                                        }
                                        Spacer(Modifier.width(10.dp))
                                        Column {
                                            Text("Total Received", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                                            Text(formatAmount(person.totalReceived),
                                                style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Due Date ${if (person.dueDate == null) "Not Set" else formatFullDate(person.dueDate)}",
                                        style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                                    Text("${person.recordCount} records",
                                        style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }

                // Tabs
                item {
                    PillSegmentedControl(
                        options = listOf("All", "Paid", "Received", "Adjustments"),
                        selectedIndex = state.tabIndex,
                        onSelect = viewModel::setTab,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                }

                // Records
                val records = viewModel.filteredRecords()
                if (records.isEmpty()) {
                    item {
                        EmptyState("📋", "No records yet", "Add a record to track this debt",
                            modifier = Modifier.padding(top = 32.dp))
                    }
                } else {
                    items(records, key = { it.id }) { record ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Text(formatDate(record.date), style = MaterialTheme.typography.labelSmall,
                                color = OnSurfaceVariant, modifier = Modifier.width(60.dp))
                            Text(record.note.ifBlank { record.recordType.name.lowercase().replaceFirstChar { it.uppercase() } },
                                style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            Text(formatAmount(record.amount),
                                style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold,
                                color = if (record.recordType == DebtRecordType.PAID) SpendingRed else IncomeGreen)
                        }
                        HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddRecord = true },
            containerColor = Color.White, contentColor = Color.Black,
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            shape = androidx.compose.foundation.shape.CircleShape
        ) { Icon(Icons.Default.Add, null) }
    }

    if (showAddRecord && state.person != null) {
        AddRecordSheet(
            personName = state.person!!.name,
            personType = state.person!!.type,
            onAdd = { amount, type, note ->
                viewModel.addRecord(personId, amount, type, note)
                showAddRecord = false
            },
            onDismiss = { showAddRecord = false }
        )
    }
}

// ─── Add Record Sheet ─────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecordSheet(
    personName: String,
    personType: DebtType,
    onAdd: (Double, DebtRecordType, String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedType by remember {
        mutableStateOf(if (personType == DebtType.LENDING) DebtRecordType.RECEIVED else DebtRecordType.PAID)
    }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceVariant,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 32.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Add record", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Select what you want to track:", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                }
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null, tint = OnSurfaceVariant) }
            }
            Spacer(Modifier.height(16.dp))

            // Money Received option
            Surface(
                onClick = { selectedType = DebtRecordType.RECEIVED },
                color = if (selectedType == DebtRecordType.RECEIVED) BadgeGreen.copy(alpha = 0.3f) else Surface,
                shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(44.dp).clip(androidx.compose.foundation.shape.CircleShape).background(BadgeGreen),
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Add, null, tint = BadgeGreenText, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Money received", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        Text("Track money you've received from $personName",
                            style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                    }
                    Icon(Icons.Default.ChevronRight, null, tint = OnSurfaceVariant)
                }
            }
            Spacer(Modifier.height(8.dp))

            // Money Paid option
            Surface(
                onClick = { selectedType = DebtRecordType.PAID },
                color = if (selectedType == DebtRecordType.PAID) Color(0xFF3A1515).copy(alpha = 0.3f) else Surface,
                shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(44.dp).clip(androidx.compose.foundation.shape.CircleShape).background(Color(0xFF3A1515)),
                        contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Remove, null, tint = SpendingRed, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Money paid", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        Text("Track money you've paid to $personName",
                            style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                    }
                    Icon(Icons.Default.ChevronRight, null, tint = OnSurfaceVariant)
                }
            }
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = amount, onValueChange = { amount = it },
                label = { Text("Amount") },
                leadingIcon = { Text("₹", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 12.dp)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = DividerColor),
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)
            )
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = note, onValueChange = { note = it },
                label = { Text("Note (optional)") },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = DividerColor),
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull()
                    if (amt != null && amt > 0) onAdd(amt, selectedType, note)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(50.dp)
            ) { Text("Save Record", modifier = Modifier.padding(vertical = 6.dp)) }
        }
    }
}

// ─── Add Debt Screen ──────────────────────────────────────────────────────────

@Composable
fun AddDebtScreen(
    navController: NavController,
    debtType: String = "LENDING",
    viewModel: AddDebtViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.setInitialType(debtType) }
    // FIX 2: Consume one-shot Channel event — never fires spuriously on recomposition
    LaunchedEffect(Unit) { viewModel.navigationEvent.collect { navController.popBackStack() } }

    Box(Modifier.fillMaxSize().background(Background)) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth().padding(start = (-8).dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, null, tint = OnSurfaceVariant)
                }
                Text("Add debt", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }

            // Name card
            FCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Name of the person", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = state.name, onValueChange = viewModel::setName,
                        placeholder = { Text("Name", color = OnSurfaceVariant) },
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = IncomeGreen, modifier = Modifier.size(20.dp)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = DividerColor,
                            focusedContainerColor = SurfaceVariant, unfocusedContainerColor = SurfaceVariant),
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Due date card
            FCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Due date for repayment", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = "Not set", onValueChange = {},
                        leadingIcon = { Icon(Icons.Default.CalendarMonth, null, tint = IncomeGreen, modifier = Modifier.size(20.dp)) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = DividerColor,
                            focusedContainerColor = SurfaceVariant, unfocusedContainerColor = SurfaceVariant),
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), readOnly = true
                    )
                }
            }

            // Note card
            FCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Additional details", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = state.note, onValueChange = viewModel::setNote,
                        placeholder = { Text("Write a note", color = OnSurfaceVariant) },
                        minLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Primary, unfocusedBorderColor = DividerColor,
                            focusedContainerColor = SurfaceVariant, unfocusedContainerColor = SurfaceVariant),
                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            if (state.error != null) {
                Text(state.error!!, color = SpendingRed, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(80.dp))
        }

        // Next button
        Button(
            onClick = viewModel::save,
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            shape = RoundedCornerShape(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
        ) {
            Text("Next", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 6.dp))
        }
    }
}
