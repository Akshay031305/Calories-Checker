package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.InsightsScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.HealthifyTheme
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.HealthViewModel
import com.example.ui.viewmodel.NavigationTab
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: HealthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HealthifyTheme {
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val isExpanded = maxWidth >= 600.dp
                    HealthifyApp(
                        viewModel = viewModel,
                        isExpanded = isExpanded
                    )
                }
            }
        }
    }
}

data class NavItem(
    val tab: NavigationTab,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val label: String
)

@Composable
fun HealthifyApp(
    viewModel: HealthViewModel,
    isExpanded: Boolean = false
) {
    val activeTab by viewModel.activeTab.collectAsState()
    val userSettings by viewModel.userSettings.collectAsState()
    val allEntries by viewModel.allEntries.collectAsState()
    val todaySummary by viewModel.todaySummary.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val monthlySummary by viewModel.monthlySummary.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var showOnboarding by remember(userSettings) {
        mutableStateOf(!userSettings.isOnboardingCompleted && allEntries.isEmpty())
    }

    LaunchedEffect(Unit) {
        viewModel.userMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    val navItems = listOf(
        NavItem(NavigationTab.TODAY, Icons.Filled.Home, Icons.Outlined.Home, "Today"),
        NavItem(NavigationTab.HISTORY, Icons.Filled.History, Icons.Outlined.History, "History"),
        NavItem(NavigationTab.CALENDAR, Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth, "Calendar"),
        NavItem(NavigationTab.INSIGHTS, Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, "Insights"),
        NavItem(NavigationTab.SETTINGS, Icons.Filled.Settings, Icons.Outlined.Settings, "Settings")
    )

    if (showOnboarding) {
        OnboardingScreen(
            currentSettings = userSettings,
            onComplete = { updated ->
                viewModel.saveSettings(updated)
                showOnboarding = false
            },
            onSkip = {
                viewModel.saveSettings(userSettings.copy(isOnboardingCompleted = true))
                showOnboarding = false
            }
        )
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (!isExpanded) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                    modifier = Modifier.testTag("bottom_navigation_bar")
                ) {
                    navItems.forEach { item ->
                        val selected = activeTab == item.tab
                        NavigationBarItem(
                            selected = selected,
                            onClick = { viewModel.setActiveTab(item.tab) },
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(text = item.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = EmeraldPrimary,
                                selectedTextColor = EmeraldPrimary,
                                indicatorColor = EmeraldAccent.copy(alpha = 0.15f),
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            ),
                            modifier = Modifier.testTag("nav_tab_${item.tab.name.lowercase()}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Adaptive Navigation Rail for Expanded/Tablet screens
            if (isExpanded) {
                NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    navItems.forEach { item ->
                        val selected = activeTab == item.tab
                        NavigationRailItem(
                            selected = selected,
                            onClick = { viewModel.setActiveTab(item.tab) },
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(text = item.label) },
                            colors = NavigationRailItemDefaults.colors(
                                selectedIconColor = EmeraldPrimary,
                                selectedTextColor = EmeraldPrimary,
                                indicatorColor = EmeraldAccent.copy(alpha = 0.15f),
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            )
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.TopCenter
            ) {
                Box(modifier = Modifier.widthIn(max = 700.dp)) {
                    when (activeTab) {
                        NavigationTab.TODAY -> {
                            DashboardScreen(
                                dailySummary = todaySummary,
                                userSettings = userSettings,
                                isAnalyzing = isAnalyzing,
                                onLogSubmit = { text -> viewModel.submitNaturalLanguageLog(text) },
                                onEditEntry = { entry -> viewModel.updateEntry(entry) },
                                onDeleteEntry = { id -> viewModel.deleteEntry(id) },
                                onLoadSampleData = { viewModel.loadSampleData() },
                                onMicClick = {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Voice logging is not supported in the streaming emulator. Please type your entry.")
                                    }
                                }
                            )
                        }
                        NavigationTab.HISTORY -> {
                            HistoryScreen(
                                allEntries = allEntries,
                                onEditEntry = { entry -> viewModel.updateEntry(entry) },
                                onDeleteEntry = { id -> viewModel.deleteEntry(id) }
                            )
                        }
                        NavigationTab.CALENDAR -> {
                            CalendarScreen(
                                allEntries = allEntries,
                                userSettings = userSettings,
                                selectedDate = selectedDate,
                                onDateSelected = { date -> viewModel.setSelectedDate(date) },
                                onEditEntry = { entry -> viewModel.updateEntry(entry) },
                                onDeleteEntry = { id -> viewModel.deleteEntry(id) }
                            )
                        }
                        NavigationTab.INSIGHTS -> {
                            InsightsScreen(
                                monthlySummary = monthlySummary,
                                allEntries = allEntries,
                                userSettings = userSettings
                            )
                        }
                        NavigationTab.SETTINGS -> {
                            SettingsScreen(
                                userSettings = userSettings,
                                onSaveSettings = { settings -> viewModel.saveSettings(settings) },
                                onLoadSampleData = { viewModel.loadSampleData() },
                                onResetAllData = { viewModel.clearAllData() }
                            )
                        }
                    }
                }
            }
        }
    }
}
