package com.mugheadgolf.app.screens.tourney

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mugheadgolf.app.data.api.ApiClient
import com.mugheadgolf.app.data.models.TourneySeed
import com.mugheadgolf.app.data.models.TourneyBranch
import com.mugheadgolf.app.viewmodels.SessionViewModel
import kotlinx.coroutines.launch

@Composable
fun TourneySeedsScreen(title: String, type: String, sessionViewModel: SessionViewModel) {
    val session by sessionViewModel.state.collectAsState()
    var seeds by remember { mutableStateOf<List<TourneySeed>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(session.year, type) {
        scope.launch {
            try { seeds = ApiClient.service.getSeeds(session.year, type) } catch (_: Exception) {}
            loading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("$title (${session.year})", style = MaterialTheme.typography.headlineMedium)
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                Text("#", modifier = Modifier.width(32.dp), style = MaterialTheme.typography.labelMedium)
                Text("Name", modifier = Modifier.weight(2f), style = MaterialTheme.typography.labelMedium)
                Text("Score", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
            }
            HorizontalDivider()
            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(seeds.sortedBy { it.seed }) { s ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(8.dp)) {
                            Text("${s.seed}", modifier = Modifier.width(32.dp))
                            Text("${s.idgolfer?.firstname} ${s.idgolfer?.lastname}", modifier = Modifier.weight(2f))
                            Text("${"%.1f".format(s.score)}", modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TourneyLowScreen(sessionViewModel: SessionViewModel) = TourneySeedsScreen("Bracket Low Handicap", "low", sessionViewModel)

@Composable
fun TourneyHighScreen(sessionViewModel: SessionViewModel) = TourneySeedsScreen("Bracket High Handicap", "high", sessionViewModel)

@Composable
fun TourneyNetScreen(sessionViewModel: SessionViewModel) = TourneySeedsScreen("Low Net", "net", sessionViewModel)

@Composable
fun BracketScreen(title: String, type: String, sessionViewModel: SessionViewModel) {
    val session by sessionViewModel.state.collectAsState()
    var branches by remember { mutableStateOf<List<TourneyBranch>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(session.year, type) {
        scope.launch {
            try { branches = ApiClient.service.getBranches(session.year, type) } catch (_: Exception) {}
            loading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("$title Bracket (${session.year})", style = MaterialTheme.typography.headlineMedium)
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            val rounds = branches.groupBy { it.round }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                rounds.keys.sorted().forEach { round ->
                    item {
                        Text("Round $round", style = MaterialTheme.typography.titleMedium)
                        rounds[round]?.forEach { b ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("${b.idgolfer1?.firstname} ${b.idgolfer1?.lastname} (${b.score1 ?: "-"})")
                                    Text("vs")
                                    Text("${b.idgolfer2?.firstname} ${b.idgolfer2?.lastname} (${b.score2 ?: "-"})")
                                    if (b.winner != null) {
                                        Text("Winner: ${b.winner.firstname} ${b.winner.lastname}",
                                            color = MaterialTheme.colorScheme.primary,
                                            style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BracketLowScreen(sessionViewModel: SessionViewModel) = BracketScreen("Low Handicap", "low", sessionViewModel)

@Composable
fun BracketHighScreen(sessionViewModel: SessionViewModel) = BracketScreen("High Handicap", "high", sessionViewModel)
