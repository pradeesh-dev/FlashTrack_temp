package com.flashtrack.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.flashtrack.app.presentation.navigation.*
import com.flashtrack.app.presentation.theme.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FlashTrackTheme {
                FlashTrackApp()
            }
        }
    }
}

@Composable
fun FlashTrackApp() {
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    val bottomNavRoutes = setOf(
        Screen.Home.route, Screen.Analysis.route, Screen.Accounts.route, Screen.More.route
    )
    val showBottomBar = currentRoute in bottomNavRoutes

    // FIX 1: Use Scaffold so WindowInsets are handled automatically for status bar + nav bar
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Background,
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(navController, currentRoute)
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Background)
                // FIX 1: Apply top padding from scaffold so status bar never overlaps content
                .padding(innerPadding)
        ) {
            FlashTrackNavGraph(navController = navController)
        }
    }
}

@Composable
fun BottomNavBar(navController: NavController, currentRoute: String?) {
    val navItems = listOf(
        NavItem("Home",     Icons.Filled.Home,           Icons.Outlined.Home,           Screen.Home.route),
        NavItem("Analysis", Icons.Filled.BarChart,       Icons.Outlined.BarChart,       Screen.Analysis.route),
        NavItem("",         Icons.Default.Add,           Icons.Default.Add,             ""),  // FAB placeholder
        NavItem("Accounts", Icons.Filled.AccountBalance, Icons.Outlined.AccountBalance, Screen.Accounts.route),
        NavItem("More",     Icons.Filled.MoreHoriz,      Icons.Outlined.MoreHoriz,      Screen.More.route),
    )

    // FIX 6: navigationBarsPadding() so gesture bar doesn't overlap nav items
    Surface(
        color = NavBackground,
        tonalElevation = 0.dp
    ) {
        Column {
            HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                navItems.forEach { item ->
                    if (item.route.isEmpty()) {
                        // Centered FAB
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .offset(y = (-8).dp)
                                .clip(CircleShape)
                                .background(FABWhite)
                                .clickable { navController.navigate(Screen.AddTransaction.route()) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, "Add transaction",
                                tint = FABIconBlack,
                                modifier = Modifier.size(26.dp))
                        }
                    } else {
                        val selected = currentRoute == item.route
                        NavBarItem(
                            item = item,
                            selected = selected,
                            onClick = {
                                if (!selected) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NavBarItem(item: NavItem, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
            contentDescription = item.label,
            tint = if (selected) NavActiveItem else NavInactiveItem,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) NavActiveItem else NavInactiveItem
        )
    }
}

data class NavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: String
)
