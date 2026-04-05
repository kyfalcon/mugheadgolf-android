package com.mugheadgolf.app.screens.stats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mugheadgolf.app.data.api.ApiClient
import com.mugheadgolf.app.data.models.Stats
import com.mugheadgolf.app.data.models.WeeklyStats
import com.mugheadgolf.app.viewmodels.SessionViewModel
import kotlinx.coroutines.launch

@Composable
fun LeagueStatsScreen(sessionViewModel: SessionViewModel) {
    val session by sessionViewModel.state.collectAsState()
    var stats by remember { mutableStateOf<List<Stats>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val scope = kotlinx.coroutines.rememberCoroutineScope()

    LaunchedEffect(session.year) {
        scope.launch {
            try { stats = ApiClient.service.getGolferStats(session.year) } catch (_: Exception) {}
            loading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("League Stats (${session.year})", style = MaterialTheme.typography.headlineMedium)
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                Text("Name", modifier = Modifier.weight(2f), style = MaterialTheme.typography.labelMedium)
                Text("Avg", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
                Text("Low", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
                Text("High", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
                Text("Rds", modifier = Modifier.weight(0.7f), style = MaterialTheme.typography.labelMedium)
            }
            HorizontalDivider()
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(stats.sortedBy { it.avg }) { s ->
                    val isMe = s.idgolfer?.idgolfer == session.idgolfer
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = if (isMe) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else CardDefaults.cardColors()
                    ) {
                        Row(modifier = Modifier.padding(8.dp)) {
                            Text(s.name, modifier = Modifier.weight(2f))
                            Text("${"%.1f".format(s.avg)}", modifier = Modifier.weight(1f))
                            Text("${"%.0f".format(s.low)}", modifier = Modifier.weight(1f))
                            Text("${"%.0f".format(s.high)}", modifier = Modifier.weight(1f))
                            Text("${s.rounds}", modifier = Modifier.weight(0.7f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyStatsScreen(sessionViewModel: SessionViewModel) {
    val session by sessionViewModel.state.collectAsState()
    var stats by remember { mutableStateOf<List<WeeklyStats>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val scope = kotlinx.coroutines.rememberCoroutineScope()

    LaunchedEffect(session.year) {
        scope.launch {
            try { stats = ApiClient.service.getWeeklyStats(session.year) } catch (_: Exception) {}
            loading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Golfer Stats (${session.year})", style = MaterialTheme.typography.headlineMedium)
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                Text("Wk", modifier = Modifier.weight(0.5f), style = MaterialTheme.typography.labelMedium)
                Text("Date", modifier = Modifier.weight(1.5f), style = MaterialTheme.typography.labelMedium)
                Text("Avg", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
                Text("Low", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
                Text("High", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
            }
            HorizontalDivider()
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(stats) { s ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(8.dp)) {
                            Text("${s.week}", modifier = Modifier.weight(0.5f))
                            Text(s.date ?: "", modifier = Modifier.weight(1.5f))
                            Text("${"%.1f".format(s.avg)}", modifier = Modifier.weight(1f))
                            Text("${"%.0f".format(s.low)}", modifier = Modifier.weight(1f))
                            Text("${"%.0f".format(s.high)}", modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
