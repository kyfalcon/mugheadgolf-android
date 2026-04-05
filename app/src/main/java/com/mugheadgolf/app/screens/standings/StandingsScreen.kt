package com.mugheadgolf.app.screens.standings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mugheadgolf.app.data.api.ApiClient
import com.mugheadgolf.app.data.models.PointTotal
import com.mugheadgolf.app.viewmodels.SessionViewModel
import kotlinx.coroutines.launch

@Composable
fun StandingsScreen(sessionViewModel: SessionViewModel) {
    val session by sessionViewModel.state.collectAsState()
    var standings by remember { mutableStateOf<List<PointTotal>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val scope = kotlinx.coroutines.rememberCoroutineScope()

    LaunchedEffect(session.year) {
        scope.launch {
            try { standings = ApiClient.service.getPointsByYear(session.year) } catch (_: Exception) {}
            loading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Standings (${session.year})", style = MaterialTheme.typography.headlineMedium)
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            // Header row
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp)) {
                Text("Name", modifier = Modifier.weight(2f), style = MaterialTheme.typography.labelMedium)
                Text("Pts", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
                Text("W", modifier = Modifier.weight(0.5f), style = MaterialTheme.typography.labelMedium)
                Text("L", modifier = Modifier.weight(0.5f), style = MaterialTheme.typography.labelMedium)
                Text("T", modifier = Modifier.weight(0.5f), style = MaterialTheme.typography.labelMedium)
            }
            HorizontalDivider()
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(standings.sortedByDescending { it.totalpoints }) { pt ->
                    val isMe = pt.idgolfer?.idgolfer == session.idgolfer
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = if (isMe) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else CardDefaults.cardColors()
                    ) {
                        Row(modifier = Modifier.padding(8.dp)) {
                            Text(pt.name, modifier = Modifier.weight(2f))
                            Text("${"%.1f".format(pt.totalpoints)}", modifier = Modifier.weight(1f))
                            Text("${pt.wins}", modifier = Modifier.weight(0.5f))
                            Text("${pt.losses}", modifier = Modifier.weight(0.5f))
                            Text("${pt.ties}", modifier = Modifier.weight(0.5f))
                        }
                    }
                }
            }
        }
    }
}
