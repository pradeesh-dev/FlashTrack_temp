package com.flashtrack.app.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.flashtrack.app.domain.model.*
import com.flashtrack.app.data.local.entity.*
import com.flashtrack.app.presentation.theme.*
import java.text.*
import java.util.*
import kotlin.math.*

// ─── Amount Formatter ────────────────────────────────────────────────────────

fun formatAmount(amount: Double): String {
    val df = DecimalFormat("#,##,##0.##")
    return "₹${df.format(amount)}"
}

fun formatAmountCompact(amount: Double): String {
    return when {
        amount >= 100_000 -> "₹${String.format("%.1f", amount / 100_000)}L"
        amount >= 1_000 -> "₹${String.format("%.1f", amount / 1_000)}K"
        else -> formatAmount(amount)
    }
}

fun formatDate(ms: Long): String {
    val today = Calendar.getInstance()
    val txnDay = Calendar.getInstance().also { it.timeInMillis = ms }
    return when {
        today.get(Calendar.DATE) == txnDay.get(Calendar.DATE) &&
        today.get(Calendar.MONTH) == txnDay.get(Calendar.MONTH) -> "Today"
        today.get(Calendar.DATE) - txnDay.get(Calendar.DATE) == 1 &&
        today.get(Calendar.MONTH) == txnDay.get(Calendar.MONTH) -> "Yesterday"
        else -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(ms))
    }
}

fun formatFullDate(ms: Long): String =
    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(ms))

fun formatTime(ms: Long): String =
    SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(ms))

fun formatMonthYear(year: Int, month: Int): String {
    val cal = Calendar.getInstance().also { it.set(year, month - 1, 1) }
    return SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
}

fun greetingText(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "Good Morning,"
        hour < 17 -> "Good Afternoon,"
        else -> "Good Evening,"
    }
}

// ─── Category Color Parser ────────────────────────────────────────────────────

fun parseColor(hex: String): Color = try {
    Color(android.graphics.Color.parseColor(hex))
} catch (e: Exception) { Color(0xFF757575) }

// ─── FlashTrack Card ─────────────────────────────────────────────────────────

@Composable
fun FCard(
    modifier: Modifier = Modifier,
    color: Color = Surface,
    shape: Shape = RoundedCornerShape(16.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = color)
        ) { Column { content() } }
    } else {
        Card(
            modifier = modifier,
            shape = shape,
            colors = CardDefaults.cardColors(containerColor = color)
        ) { Column { content() } }
    }
}

// ─── Section Header ──────────────────────────────────────────────────────────

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        action?.invoke()
    }
}

// ─── See All Button ──────────────────────────────────────────────────────────

@Composable
fun SeeAllButton(onClick: () -> Unit, label: String = "See all") {
    Surface(
        onClick = onClick,
        color = SurfaceVariant,
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = OnSurfaceVariant
        )
    }
}

// ─── Pill Segment Control ────────────────────────────────────────────────────

@Composable
fun PillSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = SurfaceVariant,
        shape = RoundedCornerShape(50.dp)
    ) {
        Row(modifier = Modifier.padding(4.dp)) {
            options.forEachIndexed { index, label ->
                val selected = index == selectedIndex
                Surface(
                    onClick = { onSelect(index) },
                    color = if (selected) Color(0xFF2A2A3A) else Color.Transparent,
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = label,
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (selected) OnSurface else OnSurfaceVariant
                    )
                }
            }
        }
    }
}

// ─── Category Icon ───────────────────────────────────────────────────────────

@Composable
fun CategoryIcon(
    iconName: String,
    colorHex: String,
    size: Dp = 46.dp,
    iconSize: Dp = 22.dp
) {
    val bg = parseColor(colorHex).copy(alpha = 0.18f)
    val fg = parseColor(colorHex)
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = iconFromName(iconName),
            contentDescription = null,
            tint = fg,
            modifier = Modifier.size(iconSize)
        )
    }
}

// ─── Payment Mode Icon ───────────────────────────────────────────────────────

@Composable
fun PaymentModeIcon(iconName: String, size: Dp = 20.dp) {
    Icon(
        imageVector = iconFromName(iconName),
        contentDescription = null,
        tint = OnSurfaceVariant,
        modifier = Modifier.size(size)
    )
}

// ─── Semicircle Budget Gauge ─────────────────────────────────────────────────

@Composable
fun SemicircleGauge(
    spent: Double,
    limit: Double,
    remaining: Double,
    modifier: Modifier = Modifier,
    onEditLimit: (() -> Unit)? = null
) {
    val progress = if (limit > 0) (spent / limit).toFloat().coerceIn(0f, 1f) else 0f
    val trackColor = SurfaceVariant
    val fillColor = if (progress > 0.9f) SpendingRed else Primary
    val dotAngle = (-180f + progress * 180f)  // -180° to 0°

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            val stroke = 18.dp.toPx()
            val padding = stroke / 2 + 8.dp.toPx()
            val arcLeft = padding
            val arcTop = padding
            val arcRight = size.width - padding
            val arcBottom = size.height * 2 - padding

            // Track (grey arc)
            drawArc(
                color = trackColor,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(arcLeft, arcTop),
                size = androidx.compose.ui.geometry.Size(arcRight - arcLeft, arcBottom - arcTop),
                style = Stroke(stroke, cap = StrokeCap.Round)
            )
            // Filled arc
            if (progress > 0f) {
                drawArc(
                    color = fillColor,
                    startAngle = 180f,
                    sweepAngle = 180f * progress,
                    useCenter = false,
                    topLeft = Offset(arcLeft, arcTop),
                    size = androidx.compose.ui.geometry.Size(arcRight - arcLeft, arcBottom - arcTop),
                    style = Stroke(stroke, cap = StrokeCap.Round)
                )
                // Dot at current position
                val cx = (arcLeft + arcRight) / 2
                val cy = (arcTop + arcBottom) / 2
                val r = (arcRight - arcLeft) / 2
                // FIX 13: compute dot position inside Canvas where cx/cy/r are known
                val dotRad = Math.toRadians(dotAngle.toDouble())
                val dotX = cos(dotRad).toFloat()
                val dotY = sin(dotRad).toFloat()
                drawCircle(
                    color = fillColor,
                    radius = stroke / 2 + 2.dp.toPx(),
                    center = Offset(cx + dotX * r, cy + dotY * r)
                )
            }
        }

        // Center content
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("REMAINING", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
            Text(
                formatAmount(remaining),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
        }

        // Left — Spent
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 8.dp, bottom = 4.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text("Spent", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
            Text(formatAmount(spent), style = MaterialTheme.typography.bodyMedium,
                color = if (progress > 0.9f) SpendingRed else Primary,
                fontWeight = FontWeight.SemiBold)
        }

        // Right — Limit
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 8.dp, bottom = 4.dp),
            horizontalAlignment = Alignment.End
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Limit", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                if (onEditLimit != null) {
                    Icon(Icons.Default.Edit, null, tint = OnSurfaceVariant,
                        modifier = Modifier.size(12.dp).clickable { onEditLimit() })
                }
            }
            Text(formatAmount(limit), style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold)
        }
    }
}

// ─── Shimmer Effect ──────────────────────────────────────────────────────────

@Composable
fun ShimmerBox(modifier: Modifier = Modifier, shape: Shape = RoundedCornerShape(8.dp)) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.3f, targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "shimmer_alpha"
    )
    Box(modifier = modifier.clip(shape).background(Shimmer1.copy(alpha = alpha)))
}

@Composable
fun ShimmerTransactionItem() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ShimmerBox(modifier = Modifier.size(46.dp), shape = RoundedCornerShape(12.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            ShimmerBox(modifier = Modifier.fillMaxWidth(0.5f).height(14.dp))
            ShimmerBox(modifier = Modifier.fillMaxWidth(0.35f).height(12.dp))
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            ShimmerBox(modifier = Modifier.width(60.dp).height(14.dp))
            ShimmerBox(modifier = Modifier.width(40.dp).height(12.dp))
        }
    }
}

// ─── Empty State ─────────────────────────────────────────────────────────────

@Composable
fun EmptyState(
    emoji: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(emoji, fontSize = 48.sp)
        Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Text(subtitle, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, color = OnSurfaceVariant)
        action?.invoke()
    }
}

// ─── Icon Mapper ─────────────────────────────────────────────────────────────

fun iconFromName(name: String) = when (name) {
    "restaurant"         -> Icons.Default.Restaurant
    "local_cafe"         -> Icons.Default.LocalCafe
    "directions_bus"     -> Icons.Default.DirectionsBus
    "receipt_long"       -> Icons.Default.ReceiptLong
    "shopping_bag"       -> Icons.Default.ShoppingBag
    "movie"              -> Icons.Default.Movie
    "local_hospital"     -> Icons.Default.LocalHospital
    "flight"             -> Icons.Default.Flight
    "school"             -> Icons.Default.School
    "local_grocery_store"-> Icons.Default.LocalGroceryStore
    "face"               -> Icons.Default.Face
    "more_horiz"         -> Icons.Default.MoreHoriz
    "account_balance_wallet" -> Icons.Default.AccountBalanceWallet
    "work"               -> Icons.Default.Work
    "trending_up"        -> Icons.Default.TrendingUp
    "store"              -> Icons.Default.Store
    "card_giftcard"      -> Icons.Default.CardGiftcard
    "savings"            -> Icons.Default.Savings
    "account_balance"    -> Icons.Default.AccountBalance
    "payments"           -> Icons.Default.Payments
    "payment"            -> Icons.Default.Payment
    "credit_card"        -> Icons.Default.CreditCard
    "wallet"             -> Icons.Default.Wallet
    "attach_money"       -> Icons.Default.AttachMoney
    "arrow_upward"       -> Icons.Default.ArrowUpward
    "arrow_downward"     -> Icons.Default.ArrowDownward
    "sync_alt"           -> Icons.Default.SyncAlt
    "history"            -> Icons.Default.History
    "schedule"           -> Icons.Default.Schedule
    "category"           -> Icons.Default.Category
    "tag"                -> Icons.Default.Tag
    "people"             -> Icons.Default.People
    "bar_chart"          -> Icons.Default.BarChart
    else                 -> Icons.Default.MoreHoriz
}

// ─── Transaction List Item ────────────────────────────────────────────────────

@Composable
fun TransactionListItem(
    transaction: Transaction,
    onClick: (() -> Unit)? = null
) {
    val modifier = if (onClick != null)
        Modifier.clickable { onClick() } else Modifier
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CategoryIcon(
            iconName = transaction.category.iconName,
            colorHex = transaction.category.colorHex
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = transaction.note.ifBlank { transaction.category.name },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                PaymentModeIcon(iconName = transaction.paymentMode.iconName, size = 14.dp)
                Spacer(Modifier.width(4.dp))
                Text(transaction.paymentMode.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            val amtColor = when (transaction.type) {
                com.flashtrack.app.data.local.entity.TransactionType.EXPENSE -> SpendingRed
                com.flashtrack.app.data.local.entity.TransactionType.INCOME -> IncomeGreen
                com.flashtrack.app.data.local.entity.TransactionType.TRANSFER -> TransferBlue
            }
            val prefix = when (transaction.type) {
                com.flashtrack.app.data.local.entity.TransactionType.INCOME -> "+"
                com.flashtrack.app.data.local.entity.TransactionType.TRANSFER -> ""
                else -> ""
            }
            Text(
                text = "$prefix${formatAmount(transaction.amount)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = amtColor
            )
            Text(
                formatDate(transaction.date),
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceVariant
            )
        }
    }
}

// ─── Debt Person Row ──────────────────────────────────────────────────────────

@Composable
fun DebtPersonRow(
    person: com.flashtrack.app.domain.model.DebtPerson,
    onClick: () -> Unit
) {
    FCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isLending = person.type == com.flashtrack.app.data.local.entity.DebtType.LENDING
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isLending) BadgeGreen else Color(0xFF3A1515)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isLending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = if (isLending) BadgeGreenText else SpendingRed,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Text(person.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.End) {
                val amtColor = if (isLending) IncomeGreen else SpendingRed
                val label = if (isLending) "owes you" else "you owe"
                Text(label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                Text(formatAmount(person.displayAmount),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = amtColor)
            }
        }
    }
}

// ─── Summary 2-Column Card ────────────────────────────────────────────────────

@Composable
fun SummaryTwoColumn(
    leftLabel: String, leftValue: String, leftColor: Color = OnSurface,
    rightLabel: String, rightValue: String, rightColor: Color = OnSurface,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FCard(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.padding(14.dp)) {
                Text(leftLabel, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Text(leftValue, style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, color = leftColor)
            }
        }
        FCard(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
            Column(Modifier.padding(14.dp)) {
                Text(rightLabel, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                Text(rightValue, style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold, color = rightColor)
            }
        }
    }
}

// ─── Cash Flow Card ───────────────────────────────────────────────────────────

@Composable
fun CashFlowCard(
    spending: Double,
    income: Double,
    balance: Double,
    periodLabel: String,
    onPeriodClick: () -> Unit,
    balanceLabel: String = "Available Balance",
    modifier: Modifier = Modifier
) {
    FCard(modifier = modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("CASH FLOW", style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant, letterSpacing = 1.5.sp)
                Surface(
                    onClick = onPeriodClick,
                    color = SurfaceVariant, shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(periodLabel, style = MaterialTheme.typography.labelMedium)
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.KeyboardArrowDown, null,
                            modifier = Modifier.size(16.dp), tint = OnSurfaceVariant)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text("SPENDING", style = MaterialTheme.typography.labelSmall,
                        color = SpendingRed, fontWeight = FontWeight.Bold)
                    Text(formatAmount(spending),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold)
                }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text("INCOME", style = MaterialTheme.typography.labelSmall,
                        color = IncomeGreen, fontWeight = FontWeight.Bold)
                    Text(formatAmount(income),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(12.dp))
            Surface(
                color = SurfaceContainer,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(balanceLabel, style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant)
                    Text(formatAmount(balance),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
