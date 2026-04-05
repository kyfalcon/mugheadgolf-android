package com.mugheadgolf.app.screens.scores

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mugheadgolf.app.data.api.ApiClient
import com.mugheadgolf.app.viewmodels.SessionViewModel
import kotlinx.coroutines.launch

@Composable
fun WeeklyScoresScreen(sessionViewModel: SessionViewModel, onMatchClick: (Int) -> Unit) {
    val session by sessionViewModel.state.collectAsState()
    var scores by remember { mutableStateOf<List<Any>>(emptyList()) }
    var selectedWeek by remember { mutableIntStateOf(session.currentWeek.coerceAtLeast(1)) }
    var loading by remember { mutableStateOf(true) }
    val scope = kotlinx.coroutines.rememberCoroutineScope()

    LaunchedEffect(session.year, selectedWeek) {
        loading = true
        scope.launch {
            try { scores = ApiClient.service.getWeeklyScores(session.year, selectedWeek) } catch (_: Exception) {}
            loading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Weekly Scores (${session.year})", style = MaterialTheme.typography.headlineMedium)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Week:")
            (1..session.currentWeek.coerceAtLeast(1)).forEach { w ->
                FilterChip(selected = selectedWeek == w, onClick = { selectedWeek = w }, label = { Text("$w") })
            }
        }
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(scores) { s ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(s.toString(), modifier = Modifier.padding(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun GolferScoresScreen(sessionViewModel: SessionViewModel) {
    val session by sessionViewModel.state.collectAsState()
    var scores by remember { mutableStateOf<List<Any>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val scope = kotlinx.coroutines.rememberCoroutineScope()

    LaunchedEffect(session.year) {
        scope.launch {
            try { scores = ApiClient.service.getGolfersScores(session.year) } catch (_: Exception) {}
            loading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Golfer Scores (${session.year})", style = MaterialTheme.typography.headlineMedium)
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(scores) { s ->
                    Card(modifier = Modifier.fillMaxWidth()) { Text(s.toString(), modifier = Modifier.padding(12.dp)) }
                }
            }
        }
    }
}

@Composable
fun GolferNetScoresScreen(sessionViewModel: SessionViewModel) {
    val session by sessionViewModel.state.collectAsState()
    // Re-use same API as golfer scores but display "net" label
    var scores by remember { mutableStateOf<List<Any>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val scope = kotlinx.coroutines.rememberCoroutineScope()

    LaunchedEffect(session.year) {
        scope.launch {
            try { scores = ApiClient.service.getGolfersScores(session.year) } catch (_: Exception) {}
            loading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Golfer Net Scores (${session.year})", style = MaterialTheme.typography.headlineMedium)
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(scores) { s ->
                    Card(modifier = Modifier.fillMaxWidth()) { Text(s.toString(), modifier = Modifier.padding(12.dp)) }
                }
            }
        }
    }
}

@Composable
fun FoursomeScoreScreen(idfoursome: Int, sessionViewModel: SessionViewModel) {
    val session by sessionViewModel.state.collectAsState()
    var loading by remember { mutableStateOf(true) }
    var scoredatas by remember { mutableStateOf<List<com.mugheadgolf.app.data.models.ScoreData>>(emptyList()) }
    val scope = kotlinx.coroutines.rememberCoroutineScope()

    LaunchedEffect(idfoursome) {
        scope.launch {
            try { scoredatas = ApiClient.service.getFoursomeScores(idfoursome) } catch (_: Exception) {}
            loading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Foursome Scorecard", style = MaterialTheme.typography.headlineMedium)
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(scoredatas) { sd ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            val g = sd.score.idgolfer
                            Text("${g?.firstname} ${g?.lastname}", style = MaterialTheme.typography.titleSmall)
                            Text("Score: ${sd.score.score} | Net: ${sd.score.net}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditScoreScreen(sessionViewModel: SessionViewModel) {
    val session by sessionViewModel.state.collectAsState()
    var selectedWeek by remember { mutableIntStateOf(session.currentWeek.coerceAtLeast(1)) }
    var scores by remember { mutableStateOf<List<Any>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val scope = kotlinx.coroutines.rememberCoroutineScope()

    LaunchedEffect(session.year, selectedWeek) {
        loading = true
        scope.launch {
            try { scores = ApiClient.service.getWeeklyScores(session.year, selectedWeek) } catch (_: Exception) {}
            loading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Edit Score (Admin)", style = MaterialTheme.typography.headlineMedium)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Week:")
            (1..session.currentWeek.coerceAtLeast(1)).forEach { w ->
                FilterChip(selected = selectedWeek == w, onClick = { selectedWeek = w }, label = { Text("$w") })
            }
        }
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(scores) { s ->
                    Card(modifier = Modifier.fillMaxWidth()) { Text(s.toString(), modifier = Modifier.padding(12.dp)) }
                }
            }
        }
    }
}
