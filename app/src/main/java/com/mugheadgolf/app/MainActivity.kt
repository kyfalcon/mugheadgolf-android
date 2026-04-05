package com.mugheadgolf.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.mugheadgolf.app.data.TokenManager
import com.mugheadgolf.app.data.api.ApiClient
import com.mugheadgolf.app.navigation.Routes
import com.mugheadgolf.app.screens.admin.*
import com.mugheadgolf.app.screens.auth.*
import com.mugheadgolf.app.screens.dashboard.DashboardScreen
import com.mugheadgolf.app.screens.email.*
import com.mugheadgolf.app.screens.food.*
import com.mugheadgolf.app.screens.golfers.*
import com.mugheadgolf.app.screens.money.*
import com.mugheadgolf.app.screens.schedule.*
import com.mugheadgolf.app.screens.scores.*
import com.mugheadgolf.app.screens.standings.StandingsScreen
import com.mugheadgolf.app.screens.stats.*
import com.mugheadgolf.app.screens.tourney.*
import com.mugheadgolf.app.screens.tutorial.*
import com.mugheadgolf.app.screens.wager.*
import com.mugheadgolf.app.ui.theme.MugheadGolfTheme
import com.mugheadgolf.app.viewmodels.SessionViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ApiClient.init(this)
        enableEdgeToEdge()
        setContent {
            MugheadGolfTheme {
                MugheadGolfApp(TokenManager(this))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MugheadGolfApp(tokenManager: TokenManager) {
    val navController = rememberNavController()
    val sessionViewModel: SessionViewModel = viewModel()
    val session by sessionViewModel.state.collectAsState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    val isAuthRoute = currentRoute in listOf(
        Routes.SIGN_IN, Routes.FORGOT, Routes.REGISTER, Routes.REGISTER_RESPONSE,
        Routes.ACTIVATION_NOTICE, Routes.AUTHENTICATE, Routes.TUTORIAL, Routes.TUTORIAL_TEE_TIMES
    )

    if (isAuthRoute || !session.isAuthenticated) {
        NavHost(navController = navController, startDestination = Routes.SIGN_IN) {
            composable(Routes.SIGN_IN) {
                SignInScreen(
                    sessionViewModel = sessionViewModel,
                    onSignedIn = { navController.navigate(Routes.DASHBOARD) { popUpTo(Routes.SIGN_IN) { inclusive = true } } },
                    onRegister = { navController.navigate(Routes.register("-1")) },
                    onForgot = { navController.navigate(Routes.FORGOT) }
                )
            }
            composable(Routes.FORGOT) {
                ForgotScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.REGISTER) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id") ?: "-1"
                RegisterScreen(
                    id = id,
                    onSuccess = { navController.navigate(Routes.REGISTER_RESPONSE) { popUpTo(Routes.SIGN_IN) } },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Routes.REGISTER_RESPONSE) {
                RegisterResponseScreen(onBack = { navController.navigate(Routes.SIGN_IN) { popUpTo(0) { inclusive = true } } })
            }
            composable(Routes.ACTIVATION_NOTICE) {
                ActivationNoticeScreen(onBack = { navController.navigate(Routes.SIGN_IN) { popUpTo(0) { inclusive = true } } })
            }
            composable(Routes.AUTHENTICATE) { backStackEntry ->
                val token = backStackEntry.arguments?.getString("token") ?: ""
                AuthenticateScreen(
                    token = token,
                    onAuthenticated = { navController.navigate(Routes.SIGN_IN) { popUpTo(0) { inclusive = true } } },
                    onError = { navController.navigate(Routes.SIGN_IN) { popUpTo(0) { inclusive = true } } }
                )
            }
            composable(Routes.TUTORIAL) {
                TutorialScreen(
                    onSignIn = { navController.navigate(Routes.SIGN_IN) },
                    onTeeTimes = { navController.navigate(Routes.TUTORIAL_TEE_TIMES) }
                )
            }
            composable(Routes.TUTORIAL_TEE_TIMES) {
                TutorialTeeTimesScreen(onBack = { navController.popBackStack() })
            }
            // Authenticated routes accessible even outside drawer
            composable(Routes.DASHBOARD) {
                MainScaffold(navController, sessionViewModel, drawerState, scope) {
                    DashboardScreen(sessionViewModel = sessionViewModel, onGoToMatch = { id -> navController.navigate(Routes.match(id)) })
                }
            }
        }
    } else {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                AppDrawer(
                    session = session,
                    navController = navController,
                    drawerState = drawerState,
                    scope = scope,
                    onLogout = {
                        sessionViewModel.logout(tokenManager)
                        scope.launch { drawerState.close() }
                        navController.navigate(Routes.SIGN_IN) { popUpTo(0) { inclusive = true } }
                    }
                )
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("MugheadGolf") },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            ) { paddingValues ->
                NavHost(
                    navController = navController,
                    startDestination = Routes.DASHBOARD,
                    modifier = Modifier.padding(paddingValues)
                ) {
                    composable(Routes.DASHBOARD) {
                        DashboardScreen(sessionViewModel = sessionViewModel, onGoToMatch = { id -> navController.navigate(Routes.match(id)) })
                    }
                    composable(Routes.GOLFERS) { GolferListScreen() }
                    composable(Routes.HANDICAPS) { HandicapsScreen(sessionViewModel) }
                    composable(Routes.WEEKLY_SCHEDULE) { WeeklyScheduleScreen(sessionViewModel, onMatchClick = { id -> navController.navigate(Routes.match(id)) }) }
                    composable(Routes.GOLFER_SCHEDULE) { GolferScheduleScreen(sessionViewModel, onMatchClick = { id -> navController.navigate(Routes.match(id)) }) }
                    composable(Routes.FOURSOMES) { TeeTimesScreen(sessionViewModel) }
                    composable(Routes.CREATE_SCHEDULE) { CreateScheduleScreen(sessionViewModel, onCreated = { navController.popBackStack() }) }
                    composable(Routes.MATCH) { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("idschedule")?.toIntOrNull() ?: 0
                        MatchScreen(id, sessionViewModel)
                    }
                    composable(Routes.FOURSOME_SCORE) { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("idfoursome")?.toIntOrNull() ?: 0
                        FoursomeScoreScreen(id, sessionViewModel)
                    }
                    composable(Routes.WEEKLY_SCORES) { WeeklyScoresScreen(sessionViewModel, onMatchClick = { id -> navController.navigate(Routes.match(id)) }) }
                    composable(Routes.GOLFER_SCORES) { GolferScoresScreen(sessionViewModel) }
                    composable(Routes.GOLFER_NET_SCORES) { GolferNetScoresScreen(sessionViewModel) }
                    composable(Routes.EDIT_SCORE) { EditScoreScreen(sessionViewModel) }
                    composable(Routes.STANDINGS) { StandingsScreen(sessionViewModel) }
                    composable(Routes.STATS) { LeagueStatsScreen(sessionViewModel) }
                    composable(Routes.WEEKLY_STATS) { WeeklyStatsScreen(sessionViewModel) }
                    composable(Routes.GOLFER_MONEY) { GolferMoneyScreen(sessionViewModel) }
                    composable(Routes.WEEKLY_MONEY) { WeeklyMoneyScreen(sessionViewModel) }
                    composable(Routes.ADD_WINNERS) { AddMoneyScreen(sessionViewModel) }
                    composable(Routes.TOURNEY_LOW) { TourneyLowScreen(sessionViewModel) }
                    composable(Routes.TOURNEY_HIGH) { TourneyHighScreen(sessionViewModel) }
                    composable(Routes.TOURNEY_NET) { TourneyNetScreen(sessionViewModel) }
                    composable(Routes.BRACKET_LOW) { BracketLowScreen(sessionViewModel) }
                    composable(Routes.BRACKET_HIGH) { BracketHighScreen(sessionViewModel) }
                    composable(Routes.WAGER) { WagerScreen(sessionViewModel) }
                    composable(Routes.WAGER_RESULTS) { WagerResultsScreen(sessionViewModel) }
                    composable(Routes.FOOD) { FoodMenuScreen(sessionViewModel, onOrder = { navController.navigate(Routes.ORDER) }) }
                    composable(Routes.ORDER) { OrderScreen(sessionViewModel) }
                    composable(Routes.YEAR) { SelectYearScreen(sessionViewModel) }
                    composable(Routes.CALC_HANDICAPS) { CalcHandicapsScreen(sessionViewModel) }
                    composable(Routes.ADD_GOLFER) { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("id") ?: "0"
                        GolferFormScreen(id, onSaved = { navController.popBackStack() }, onBack = { navController.popBackStack() })
                    }
                    composable(Routes.EDIT_GOLFER) { EditGolferScreen(onEditGolfer = { id -> navController.navigate(Routes.addGolfer(id)) }) }
                    composable(Routes.DIVISION_SETUP) { DivisionSetupScreen(sessionViewModel) }
                    composable(Routes.DIVISION) { DivisionsScreen(sessionViewModel) }
                    composable(Routes.SETTINGS) { SettingsScreen(sessionViewModel) }
                    composable(Routes.EMAIL) { GroupEmailScreen() }
                    composable(Routes.SEND_SCHEDULE) { SendScheduleScreen(sessionViewModel) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    navController: NavHostController,
    sessionViewModel: SessionViewModel,
    drawerState: DrawerState,
    scope: kotlinx.coroutines.CoroutineScope,
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MugheadGolf") },
                navigationIcon = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) { content() }
    }
}

@Composable
fun AppDrawer(
    session: com.mugheadgolf.app.viewmodels.SessionState,
    navController: NavHostController,
    drawerState: DrawerState,
    scope: kotlinx.coroutines.CoroutineScope,
    onLogout: () -> Unit
) {
    fun nav(route: String) { scope.launch { drawerState.close(); navController.navigate(route) } }

    ModalDrawerSheet(modifier = Modifier.width(280.dp)) {
        Column(modifier = Modifier.fillMaxSize().padding(vertical = 8.dp)) {
            // Header
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column {
                    Text("MugheadGolf", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                    Text(session.fullName, style = MaterialTheme.typography.bodyMedium)
                    Text("Year: ${session.year}", style = MaterialTheme.typography.bodySmall)
                }
            }
            HorizontalDivider()

            // Menu items
            val items = buildList {
                add(Triple(Icons.Default.Home, "Dashboard", Routes.DASHBOARD))
                add(Triple(Icons.Default.Person, "Golfers", Routes.GOLFERS))
                add(Triple(Icons.Default.BarChart, "Handicaps", Routes.HANDICAPS))
                add(null) // separator
                add(Triple(Icons.Default.CalendarMonth, "Weekly Schedule", Routes.WEEKLY_SCHEDULE))
                add(Triple(Icons.Default.CalendarMonth, "Golfer Schedule", Routes.GOLFER_SCHEDULE))
                add(Triple(Icons.Default.Schedule, "Tee Times", Routes.FOURSOMES))
                add(Triple(Icons.Default.Flag, "Weekly Scores", Routes.WEEKLY_SCORES))
                add(Triple(Icons.Default.Flag, "Golfer Scores", Routes.GOLFER_SCORES))
                add(Triple(Icons.Default.Flag, "Net Scores", Routes.GOLFER_NET_SCORES))
                add(null)
                add(Triple(Icons.Default.List, "Standings", Routes.STANDINGS))
                add(Triple(Icons.Default.AttachMoney, "Golfer Money", Routes.GOLFER_MONEY))
                add(Triple(Icons.Default.AttachMoney, "Weekly Money", Routes.WEEKLY_MONEY))
                add(Triple(Icons.Default.BarChart, "Golfer Stats", Routes.WEEKLY_STATS))
                add(Triple(Icons.Default.PieChart, "League Stats", Routes.STATS))
                add(null)
                add(Triple(Icons.Default.EmojiEvents, "Bracket Low HC", Routes.TOURNEY_LOW))
                add(Triple(Icons.Default.EmojiEvents, "Bracket High HC", Routes.TOURNEY_HIGH))
                add(Triple(Icons.Default.EmojiEvents, "Low Net Tourney", Routes.TOURNEY_NET))
                add(null)
                add(Triple(Icons.Default.LocalOffer, "Pick Four Bet", Routes.WAGER))
                add(Triple(Icons.Default.AttachMoney, "Wager Results", Routes.WAGER_RESULTS))
                add(null)
                add(Triple(Icons.Default.Restaurant, "Food Menu", Routes.FOOD))
                add(null)
                if (session.isAdmin) {
                    add(Triple(Icons.Default.Calculate, "Calc Handicaps", Routes.CALC_HANDICAPS))
                    add(Triple(Icons.Default.CalendarToday, "Select Year", Routes.YEAR))
                    add(Triple(Icons.Default.AddCircle, "Add Golfer", Routes.addGolfer(0)))
                    add(Triple(Icons.Default.Edit, "Edit Golfer", Routes.EDIT_GOLFER))
                    add(Triple(Icons.Default.Edit, "Edit Score", Routes.EDIT_SCORE))
                    add(Triple(Icons.Default.Add, "Create Schedule", Routes.CREATE_SCHEDULE))
                    add(Triple(Icons.Default.Add, "Add Weekly Winners", Routes.ADD_WINNERS))
                    add(Triple(Icons.Default.Group, "Division Setup", Routes.DIVISION_SETUP))
                    add(Triple(Icons.Default.Group, "Divisions", Routes.DIVISION))
                    add(Triple(Icons.Default.Settings, "League Settings", Routes.SETTINGS))
                    add(Triple(Icons.Default.Email, "Group Email", Routes.EMAIL))
                    add(Triple(Icons.Default.Email, "Send Schedule Email", Routes.SEND_SCHEDULE))
                    add(null)
                }
            }

            androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.weight(1f)) {
                items.forEach { item ->
                    if (item == null) {
                        item { HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) }
                    } else {
                        item {
                            NavigationDrawerItem(
                                icon = { Icon(item.first, contentDescription = null) },
                                label = { Text(item.second) },
                                selected = false,
                                onClick = { nav(item.third) },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider()
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.ExitToApp, contentDescription = null) },
                label = { Text("Sign Out") },
                selected = false,
                onClick = onLogout,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}
