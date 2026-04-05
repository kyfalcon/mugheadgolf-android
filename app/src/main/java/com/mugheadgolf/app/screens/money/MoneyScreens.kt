package com.mugheadgolf.app.screens.money

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mugheadgolf.app.data.api.ApiClient
import com.mugheadgolf.app.data.models.GolferMoney
import com.mugheadgolf.app.viewmodels.SessionViewModel
import kotlinx.coroutines.launch

@Composable
fun GolferMoneyScreen(sessionViewModel: SessionViewModel) {
    val session by sessionViewModel.state.collectAsState()
    var moneyList by remember { mutableStateOf<List<GolferMoney>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val scope = kotlinx.coroutines.rememberCoroutineScope()

    LaunchedEffect(session.year) {
        scope.launch {
            try { moneyList = ApiClient.service.getGolfersMoney(session.year) } catch (_: Exception) {}
            loading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Golfer Money (${session.year})", style = MaterialTheme.typography.headlineMedium)
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                Text("Name", modifier = Modifier.weight(2f), style = MaterialTheme.typography.labelMedium)
                Text("Total", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
            }
            HorizontalDivider()
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(moneyList.sortedByDescending { it.total }) { m ->
                    val isMe = m.idgolfer?.idgolfer == session.idgolfer
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = if (isMe) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else CardDefaults.cardColors()
                    ) {
                        Row(modifier = Modifier.padding(8.dp)) {
                            Text(m.name, modifier = Modifier.weight(2f))
                            Text("${"$"}${"%.2f".format(m.total)}", modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyMoneyScreen(sessionViewModel: SessionViewModel) {
    val session by sessionViewModel.state.collectAsState()
    var weeklyWinners by remember { mutableStateOf<List<Any>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val scope = kotlinx.coroutines.rememberCoroutineScope()

    LaunchedEffect(session.year) {
        scope.launch {
            try { weeklyWinners = ApiClient.service.getWeeklyWinners(session.year) } catch (_: Exception) {}
            loading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Weekly Money (${session.year})", style = MaterialTheme.typography.headlineMedium)
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(weeklyWinners) { w ->
                    Card(modifier = Modifier.fillMaxWidth()) { Text(w.toString(), modifier = Modifier.padding(12.dp)) }
                }
            }
        }
    }
}

@Composable
fun AddMoneyScreen(sessionViewModel: SessionViewModel) {
    val session by sessionViewModel.state.collectAsState()
    var selectedWeek by remember { mutableIntStateOf(session.currentWeek.coerceAtLeast(1)) }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    val scope = kotlinx.coroutines.rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Add Weekly Winners (Admin)", style = MaterialTheme.typography.headlineMedium)

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Week:")
            (1..session.currentWeek.coerceAtLeast(1)).forEach { w ->
                FilterChip(selected = selectedWeek == w, onClick = { selectedWeek = w }, label = { Text("$w") })
            }
        }

        if (message.isNotEmpty()) Card { Text(message, modifier = Modifier.padding(8.dp)) }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = {
                loading = true
                scope.launch {
                    try {
                        ApiClient.service.calculateLowNet(session.year, selectedWeek)
                        message = "Low net calculated."
                    } catch (e: Exception) { message = e.message ?: "Error." }
                    loading = false
                }
            }) { Text("Calc Low Net") }
            OutlinedButton(onClick = {
                loading = true
                scope.launch {
                    try {
                        ApiClient.service.calculateSkins(session.year, selectedWeek)
                        message = "Skins calculated."
                    } catch (e: Exception) { message = e.message ?: "Error." }
                    loading = false
                }
            }) { Text("Calc Skins") }
        }

        if (loading) CircularProgressIndicator()
    }
}
