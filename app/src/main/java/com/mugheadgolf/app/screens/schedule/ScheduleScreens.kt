package com.mugheadgolf.app.screens.schedule

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mugheadgolf.app.data.api.ApiClient
import com.mugheadgolf.app.data.models.Schedule
import com.mugheadgolf.app.viewmodels.SessionViewModel
import kotlinx.coroutines.launch

@Composable
fun WeeklyScheduleScreen(sessionViewModel: SessionViewModel, onMatchClick: (Int) -> Unit) {
    val session by sessionViewModel.state.collectAsState()
    var schedules by remember { mutableStateOf<List<Schedule>>(emptyList()) }
    var selectedWeek by remember { mutableIntStateOf(session.currentWeek.coerceAtLeast(1)) }
    var loading by remember { mutableStateOf(true) }
    val scope = kotlinx.coroutines.rememberCoroutineScope()

    LaunchedEffect(session.year) {
        scope.launch {
            try { schedules = ApiClient.service.getSchedule(session.year) } catch (_: Exception) {}
            loading = false
        }
    }

    val weeks = schedules.map { it.week }.distinct().sorted()
    val filtered = schedules.filter { it.week == selectedWeek }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Weekly Schedule (${session.year})", style = MaterialTheme.typography.headlineMedium)

        if (weeks.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Week:")
                weeks.take(10).forEach { w ->
                    FilterChip(selected = selectedWeek == w, onClick = { selectedWeek = w }, label = { Text("$w") })
                }
            }
        }

        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filtered) { s ->
                    Card(modifier = Modifier.fillMaxWidth(), onClick = { onMatchClick(s.idschedule) }) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Week ${s.week}${s.date?.let { " - $it" } ?: ""}", style = MaterialTheme.typography.labelMedium)
                            Text("${s.idgolfer1?.firstname} ${s.idgolfer1?.lastname} vs ${s.idgolfer2?.firstname} ${s.idgolfer2?.lastname}")
                            Text("Points: ${s.points1} - ${s.points2}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GolferScheduleScreen(sessionViewModel: SessionViewModel, onMatchClick: (Int) -> Unit) {
    val session by sessionViewModel.state.collectAsState()
    var schedules by remember { mutableStateOf<List<Schedule>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val scope = kotlinx.coroutines.rememberCoroutineScope()

    LaunchedEffect(session.year, session.idgolfer) {
        scope.launch {
            try { schedules = ApiClient.service.getGolferSchedule(session.year, session.idgolfer) } catch (_: Exception) {}
            loading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("My Schedule (${session.year})", style = MaterialTheme.typography.headlineMedium)
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(schedules) { s ->
                    val opponent = if (s.idgolfer1?.idgolfer == session.idgolfer)
                        "${s.idgolfer2?.firstname} ${s.idgolfer2?.lastname}"
                    else "${s.idgolfer1?.firstname} ${s.idgolfer1?.lastname}"
                    Card(modifier = Modifier.fillMaxWidth(), onClick = { onMatchClick(s.idschedule) }) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Week ${s.week}${s.date?.let { " - $it" } ?: ""}", style = MaterialTheme.typography.labelMedium)
                            Text("vs $opponent")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeeTimesScreen(sessionViewModel: SessionViewModel) {
    val session by sessionViewModel.state.collectAsState()
    var schedules by remember { mutableStateOf<List<Schedule>>(emptyList()) }
    var selectedWeek by remember { mutableIntStateOf(session.currentWeek.coerceAtLeast(1)) }
    var loading by remember { mutableStateOf(true) }
    val scope = kotlinx.coroutines.rememberCoroutineScope()

    LaunchedEffect(session.year) {
        scope.launch {
            try { schedules = ApiClient.service.getSchedule(session.year) } catch (_: Exception) {}
            loading = false
        }
    }

    val weeks = schedules.map { it.week }.distinct().sorted()
    val filtered = schedules.filter { it.week == selectedWeek }
    val foursomes = filtered.groupBy { it.idfoursome }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Weekly Tee Times (${session.year})", style = MaterialTheme.typography.headlineMedium)

        if (weeks.isNotEmpty()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Week:")
                weeks.take(10).forEach { w ->
                    FilterChip(selected = selectedWeek == w, onClick = { selectedWeek = w }, label = { Text("$w") })
                }
            }
        }

        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                foursomes.forEach { (fsId, matches) ->
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Foursome${fsId?.let { " #$it" } ?: ""}", style = MaterialTheme.typography.titleSmall)
                                matches.forEach { s ->
                                    Text("${s.idgolfer1?.firstname} ${s.idgolfer1?.lastname} vs ${s.idgolfer2?.firstname} ${s.idgolfer2?.lastname}",
                                        style = MaterialTheme.typography.bodySmall)
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
fun CreateScheduleScreen(sessionViewModel: SessionViewModel, onCreated: () -> Unit) {
    val session by sessionViewModel.state.collectAsState()
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    val scope = kotlinx.coroutines.rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Create Schedule", style = MaterialTheme.typography.headlineMedium)
        Text("This will create the schedule for year ${session.year}.")
        if (message.isNotEmpty()) {
            Card { Text(message, modifier = Modifier.padding(8.dp)) }
        }
        if (loading) {
            CircularProgressIndicator()
        } else {
            Button(onClick = {
                loading = true
                scope.launch {
                    try {
                        ApiClient.service.createSchedule(session.year)
                        message = "Schedule created successfully."
                    } catch (e: Exception) {
                        message = e.message ?: "Error creating schedule."
                    } finally { loading = false }
                }
            }) { Text("Create Schedule for ${session.year}") }
        }
    }
}
