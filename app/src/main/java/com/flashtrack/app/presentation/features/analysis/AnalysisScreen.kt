package com.flashtrack.app.presentation.features.analysis

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.flashtrack.app.domain.model.*
import com.flashtrack.app.presentation.components.*
import com.flashtrack.app.presentation.theme.*
import java.util.*

@Composable
fun AnalysisScreen(navController: NavController, viewModel: AnalysisViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        Modifier.fillMaxSize().background(Background).verticalScroll(rememberScrollState()).padding(bottom = 24.dp)
    ) {
        // Top bar
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Analysis", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            IconButton(onClick = {}) {
                Icon(Icons.Default.FileDownload, null, tint = OnSurfaceVariant)
            }
        }

        // Period tabs
        PillSegmentedControl(
            options = listOf("Week", "Month", "Year", "Custom"),
            selectedIndex = state.period.ordinal,
            onSelect = { viewModel.setPeriod(AnalysisPeriod.entries[it]) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(12.dp))

        // Period navigator
        FCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = viewModel::prevPeriod) {
                    Icon(Icons.Default.ChevronLeft, null, tint = OnSurfaceVariant)
                }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(formatMonthYear(state.year, state.month),
                        style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${state.transactionCount} TRANSACTIONS",
                        style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                }
                IconButton(onClick = viewModel::nextPeriod) {
                    Icon(Icons.Default.ChevronRight, null, tint = OnSurfaceVariant)
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        // Spending/Income summary
        CashFlowCard(
            spending = state.totalExpense, income = state.totalIncome,
            balance = state.totalIncome - state.totalExpense,
            periodLabel = formatMonthYear(state.year, state.month),
            onPeriodClick = {},
            balanceLabel = "Net Balance",
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(16.dp))

        // Budget gauge
        Text("Budget", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(8.dp))
        FCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Column(Modifier.padding(16.dp)) {
                if (state.budget != null) {
                    SemicircleGauge(
                        spent = state.budget!!.spent, limit = state.budget!!.amount,
                        remaining = state.budget!!.remaining,
                        modifier = Modifier.fillMaxWidth(), onEditLimit = {}
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        val cal = Calendar.getInstance()
                        val month = java.text.SimpleDateFormat("MMMM", Locale.getDefault()).format(cal.time)
                        Text("Safe to spend: ", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                        Text(formatAmount(state.safeToSpend) + "/day",
                            style = MaterialTheme.typography.bodySmall, color = IncomeGreen, fontWeight = FontWeight.SemiBold)
                        Text(" for rest of $month.", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                    }
                } else {
                    EmptyState("📊", "No budget set", "Go to Budgets to set a monthly limit")
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        // Trends
        Text("Trends", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(8.dp))
        FCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Column(Modifier.padding(16.dp)) {
                PillSegmentedControl(
                    options = listOf("Spending", "Income"),
                    selectedIndex = state.categoryTab,
                    onSelect = viewModel::setCategoryTab,
                    modifier = Modifier.fillMaxWidth(0.55f).align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(16.dp))
                Text("Total", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                TotalLineChart(
                    dailyAmounts = state.dailyExpenses,
                    daysInMonth = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH),
                    modifier = Modifier.fillMaxWidth().height(180.dp)
                )
                Spacer(Modifier.height(16.dp))
                Text("Day-wise", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                DayWiseBarChart(
                    dailyAmounts = state.dailyExpenses,
                    avgAmount = state.avgDailyExpense,
                    modifier = Modifier.fillMaxWidth().height(160.dp)
                )
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Text("Avg: ${formatAmount(state.avgDailyExpense)}",
                        style = MaterialTheme.typography.labelSmall, color = ChartProjected)
                }
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = DividerColor)
                Row(Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CompareArrows, null, tint = OnSurfaceVariant, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    val prevMonth = if (state.month == 1) "December ${state.year - 1}"
                    else "${java.text.SimpleDateFormat("MMMM", Locale.getDefault()).format(Calendar.getInstance().apply { set(state.year, state.month - 2, 1) }.time)} ${state.year}"
                    Text("Compare with ", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant,
                        modifier = Modifier.weight(1f))
                    Text(prevMonth, style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline)
                    Spacer(Modifier.width(8.dp))
                    Switch(checked = state.compareWithPrev, onCheckedChange = { viewModel.toggleCompare() },
                        colors = SwitchDefaults.colors(checkedThumbColor = Primary, checkedTrackColor = PrimaryContainer))
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        // Prediction banner
        if (state.predictedExpense > 0) {
            Surface(
                color = Color(0xFF2A2000), shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TrendingUp, null, tint = GoldAccent, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        buildString {
                            append("Based on your past trends, your predicted total spending for this month is ")
                        },
                        style = MaterialTheme.typography.bodySmall, color = GoldAccent,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            // prediction amount below
            Row(Modifier.padding(horizontal = 32.dp)) {
                Text(formatAmount(state.predictedExpense) + ".",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold, color = GoldAccent)
            }
        }
        Spacer(Modifier.height(16.dp))

        // Categories breakdown
        Text("Categories", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(8.dp))
        FCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Column(Modifier.padding(16.dp)) {
                PillSegmentedControl(
                    options = listOf("Spending", "Income"),
                    selectedIndex = state.categoryTab,
                    onSelect = viewModel::setCategoryTab,
                    modifier = Modifier.fillMaxWidth(0.55f).align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(16.dp))
                if (state.categorySpending.isNotEmpty()) {
                    DonutChart(state.categorySpending, modifier = Modifier.size(200.dp).align(Alignment.CenterHorizontally))
                    Spacer(Modifier.height(16.dp))
                    state.categorySpending.take(5).forEach { cs ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            CategoryIcon(cs.category.iconName, cs.category.colorHex, size = 40.dp, iconSize = 18.dp)
                            Spacer(Modifier.width(12.dp))
                            Text(cs.category.name, style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f))
                            Column(horizontalAlignment = Alignment.End) {
                                Text(formatAmount(cs.amount), style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ArrowDropDown, null, tint = IncomeGreen, modifier = Modifier.size(16.dp))
                                    Text("${String.format("%.1f", cs.percentage)}%",
                                        style = MaterialTheme.typography.labelSmall, color = IncomeGreen)
                                }
                            }
                        }
                        if (cs != state.categorySpending.take(5).last())
                            HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
                    }
                } else {
                    EmptyState("📊", "No data", "Add transactions to see breakdown")
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        // Payment modes
        Text("Payment modes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(8.dp))
        FCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Column(Modifier.padding(16.dp)) {
                PillSegmentedControl(
                    options = listOf("Spending", "Income", "Transfers"),
                    selectedIndex = state.paymentTab,
                    onSelect = viewModel::setPaymentTab,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                if (state.paymentModeSpending.isEmpty()) {
                    Text("No data", style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant, modifier = Modifier.padding(8.dp))
                } else {
                    state.paymentModeSpending.forEach { pms ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(36.dp).clip(RoundedCornerShape(8.dp))
                                .background(SurfaceVariant), contentAlignment = Alignment.Center) {
                                Icon(iconFromName(pms.paymentMode.iconName), null,
                                    tint = Primary, modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(pms.paymentMode.name, style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f))
                            Text(formatAmount(pms.amount), style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold)
                        }
                        HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))

        // Stats
        Text("Stats", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(8.dp))
        FCard(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("AVERAGE SPENDING", style = MaterialTheme.typography.labelSmall,
                    color = SpendingRed, fontWeight = FontWeight.Bold)
                Row(Modifier.fillMaxWidth()) {
                    Column(Modifier.weight(1f)) {
                        Text("Per day", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                        Text(formatAmount(state.avgDailyExpense), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Column(Modifier.weight(1f)) {
                        Text("Per transaction", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                        Text(formatAmount(state.avgTxnExpense), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
                HorizontalDivider(color = DividerColor)
                Text("AVERAGE INCOME", style = MaterialTheme.typography.labelSmall,
                    color = IncomeGreen, fontWeight = FontWeight.Bold)
                Row(Modifier.fillMaxWidth()) {
                    Column(Modifier.weight(1f)) {
                        Text("Per day", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                        Text(formatAmount(state.avgDailyIncome), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Column(Modifier.weight(1f)) {
                        Text("Per transaction", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                        Text(formatAmount(state.avgTxnIncome), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

// ─── Line Chart ──────────────────────────────────────────────────────────────

@Composable
fun TotalLineChart(dailyAmounts: List<DailyAmount>, daysInMonth: Int, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        if (dailyAmounts.isEmpty()) return@Canvas
        val cumulative = DoubleArray(daysInMonth)
        var running = 0.0
        for (d in 1..daysInMonth) {
            running += dailyAmounts.find { it.dayOfMonth == d }?.amount ?: 0.0
            cumulative[d - 1] = running
        }
        val maxCum = cumulative.max().coerceAtLeast(1.0)
        val w = size.width; val h = size.height
        val pad = 40f; val chartW = w - pad * 2; val chartH = h - pad * 2

        // Grid lines
        repeat(5) { i ->
            val y = pad + chartH * i / 4
            drawLine(ChartGrid, Offset(pad, y), Offset(w - pad, y), 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)))
        }
        // Actual line
        val actualDays = (0 until daysInMonth).filter { cumulative[it] > 0 }
        if (actualDays.size >= 2) {
            val path = Path()
            actualDays.forEachIndexed { i, dayIdx ->
                val x = pad + chartW * dayIdx / (daysInMonth - 1)
                val y = (pad + chartH * (1 - cumulative[dayIdx] / maxCum)).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(path, ChartSpending, style = Stroke(3f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            actualDays.forEach { dayIdx ->
                val x = pad + chartW * dayIdx / (daysInMonth - 1)
                val y = (pad + chartH * (1 - cumulative[dayIdx] / maxCum)).toFloat()
                drawCircle(ChartSpending, 5f, Offset(x, y))
            }
        }
        // Projected dashed line
        val lastActual = (0 until daysInMonth).lastOrNull { cumulative[it] > 0 }
        if (lastActual != null && lastActual < daysInMonth - 1) {
            val dailyAvg = cumulative[lastActual] / (lastActual + 1)
            val projEnd = (dailyAvg * daysInMonth).coerceAtMost(maxCum * 1.5)
            val x1 = pad + chartW * lastActual / (daysInMonth - 1)
            val y1 = (pad + chartH * (1 - cumulative[lastActual] / maxCum)).toFloat()
            val x2 = pad + chartW
            val y2 = (pad + chartH * (1 - projEnd / maxCum).toFloat()).coerceIn(pad, pad + chartH)
            drawLine(ChartProjected, Offset(x1, y1), Offset(x2, y2), 2.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f)))
            drawCircle(ChartProjected, 6f, Offset(x2, y2))
        }
    }
}

@Composable
fun DayWiseBarChart(dailyAmounts: List<DailyAmount>, avgAmount: Double, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        if (dailyAmounts.isEmpty()) return@Canvas
        val maxAmt = dailyAmounts.maxOf { it.amount }.coerceAtLeast(1.0)
        val w = size.width; val h = size.height
        val pad = 32f; val chartH = h - pad; val chartW = w - pad
        val barW = (chartW / dailyAmounts.size * 0.55f).coerceAtLeast(2f)
        dailyAmounts.forEachIndexed { idx, day ->
            if (day.amount > 0) {
                val x = pad + chartW * idx / dailyAmounts.size
                val barH = (chartH * day.amount / maxAmt).toFloat()
                drawRoundRect(ChartSpending, Offset(x, h - barH - pad * 0.2f),
                    androidx.compose.ui.geometry.Size(barW, barH),
                    androidx.compose.ui.geometry.CornerRadius(4f))
            }
        }
        if (avgAmount > 0) {
            val avgY = (h - chartH * avgAmount / maxAmt - pad * 0.2f).toFloat()
            drawLine(ChartProjected, Offset(pad, avgY), Offset(w, avgY), 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f)))
        }
    }
}

@Composable
fun DonutChart(categories: List<CategorySpending>, modifier: Modifier = Modifier) {
    val colors = listOf(
        Color(0xFFFF6B9D), Color(0xFFFF9F43), Color(0xFF48DBFB),
        Color(0xFF1DD1A1), Color(0xFFFECA57), Color(0xFFFF6348),
        Color(0xFF7BED9F), Color(0xFFA29BFE)
    )
    Canvas(modifier = modifier) {
        val cx = size.width / 2; val cy = size.height / 2
        val r = minOf(size.width, size.height) / 2 * 0.85f
        val innerR = r * 0.55f
        var startAngle = -90f
        categories.take(8).forEachIndexed { idx, cat ->
            val sweep = cat.percentage / 100f * 360f
            drawArc(color = colors[idx % colors.size], startAngle = startAngle, sweepAngle = sweep,
                useCenter = false,
                topLeft = Offset(cx - r, cy - r),
                size = androidx.compose.ui.geometry.Size(r * 2, r * 2),
                style = Stroke(r - innerR, cap = StrokeCap.Butt))
            startAngle += sweep
        }
    }
}
