package com.mugheadgolf.app.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mugheadgolf.app.data.api.ApiClient
import com.mugheadgolf.app.data.models.Division
import com.mugheadgolf.app.data.models.DivisionGolfer
import com.mugheadgolf.app.data.models.Golfer
import com.mugheadgolf.app.viewmodels.SessionViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun CalcHandicapsScreen(sessionViewModel: SessionViewModel) {
    val session by sessionViewModel.state.collectAsState()
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Calculate Handicaps (Admin)", style = MaterialTheme.typography.headlineMedium)
        Text("Current Week: ${session.currentWeek}")
        if (message.isNotEmpty()) Card { Text(message, modifier = Modifier.padding(8.dp)) }
        if (loading) CircularProgressIndicator()
        else Button(onClick = {
            loading = true; message = ""
            scope.launch {
                try {
                    val result = ApiClient.service.calcHandicaps(session.currentWeek)
                    message = result.ifBlank { "Handicaps calculated successfully." }
                } catch (e: Exception) { message = e.message ?: "Error calculating handicaps." }
                loading = false
            }
        }) { Text("Calculate Handicaps for Week ${session.currentWeek}") }
    }
}

@Composable
fun SelectYearScreen(sessionViewModel: SessionViewModel) {
    val session by sessionViewModel.state.collectAsState()
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val years = (currentYear downTo currentYear - 5).toList()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Select Year", style = MaterialTheme.typography.headlineMedium)
        Text("Current Year: ${session.year}")
        years.forEach { y ->
            Card(modifier = Modifier.fillMaxWidth(), onClick = { sessionViewModel.setYear(y) },
                colors = if (y == session.year) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else CardDefaults.cardColors()
            ) {
                Text("$y", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
fun EditGolferScreen(onEditGolfer: (Int) -> Unit) {
    var golfers by remember { mutableStateOf<List<Golfer>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try { golfers = ApiClient.service.getAllGolfers() } catch (_: Exception) {}
            loading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Edit Golfer (Admin)", style = MaterialTheme.typography.headlineMedium)
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(golfers) { g ->
                    Card(modifier = Modifier.fillMaxWidth(), onClick = { onEditGolfer(g.idgolfer) }) {
                        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("${g.firstname} ${g.lastname}", style = MaterialTheme.typography.titleSmall)
                                Text("HC: ${g.currenthandicap} | Admin: ${g.admin}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DivisionSetupScreen(sessionViewModel: SessionViewModel) {
    val session by sessionViewModel.state.collectAsState()
    var divisions by remember { mutableStateOf<List<Division>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var newName by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun load() {
        scope.launch {
            try { divisions = ApiClient.service.getDivisionsByYear(session.year) } catch (_: Exception) {}
            loading = false
        }
    }

    LaunchedEffect(session.year) { load() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Division Setup (${session.year})", style = MaterialTheme.typography.headlineMedium)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("New Division Name") }, modifier = Modifier.weight(1f))
            IconButton(onClick = {
                if (newName.isBlank()) return@IconButton
                saving = true
                scope.launch {
                    try {
                        ApiClient.service.saveDivision(Division(iddivision = 0, name = newName, year = session.year))
                        newName = ""
                        load()
                    } catch (_: Exception) {}
                    saving = false
                }
            }) { Icon(Icons.Default.Add, contentDescription = "Add") }
        }

        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(divisions) { div ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(div.name, style = MaterialTheme.typography.titleSmall)
                            IconButton(onClick = {
                                scope.launch {
                                    try { ApiClient.service.deleteDivision(div.iddivision); load() } catch (_: Exception) {}
                                }
                            }) { Icon(Icons.Default.Delete, contentDescription = "Delete") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DivisionsScreen(sessionViewModel: SessionViewModel) {
    val session by sessionViewModel.state.collectAsState()
    var divisions by remember { mutableStateOf<List<Division>>(emptyList()) }
    var divisionGolfers by remember { mutableStateOf<Map<Int, List<DivisionGolfer>>>(emptyMap()) }
    var allGolfers by remember { mutableStateOf<List<Golfer>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(session.year) {
        scope.launch {
            try {
                divisions = ApiClient.service.getDivisionsByYear(session.year)
                allGolfers = ApiClient.service.getAllGolfers()
                val dgMap = mutableMapOf<Int, List<DivisionGolfer>>()
                divisions.forEach { div ->
                    try { dgMap[div.iddivision] = ApiClient.service.getDivisionGolfersByDivision(div.iddivision) } catch (_: Exception) {}
                }
                divisionGolfers = dgMap
            } catch (_: Exception) {}
            loading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Divisions (${session.year})", style = MaterialTheme.typography.headlineMedium)
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(divisions) { div ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(div.name, style = MaterialTheme.typography.titleMedium)
                            divisionGolfers[div.iddivision]?.forEach { dg ->
                                Text("• ${dg.idgolfer?.firstname} ${dg.idgolfer?.lastname}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(sessionViewModel: SessionViewModel) {
    val session by sessionViewModel.state.collectAsState()
    var loading by remember { mutableStateOf(true) }
    var settings by remember { mutableStateOf<com.mugheadgolf.app.data.models.Settings?>(null) }
    var message by remember { mutableStateOf("") }
    var weeks by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(session.year) {
        scope.launch {
            try {
                settings = ApiClient.service.getSettings(session.year)
                weeks = settings?.weeks?.toString() ?: ""
                startDate = settings?.startdate ?: ""
            } catch (_: Exception) {}
            loading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("League Settings (Admin) - ${session.year}", style = MaterialTheme.typography.headlineMedium)
        if (loading) {
            CircularProgressIndicator()
        } else {
            OutlinedTextField(value = weeks, onValueChange = { weeks = it }, label = { Text("Number of Weeks") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            OutlinedTextField(value = startDate, onValueChange = { startDate = it }, label = { Text("Start Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())

            if (message.isNotEmpty()) Card { Text(message, modifier = Modifier.padding(8.dp)) }

            Button(modifier = Modifier.fillMaxWidth(), onClick = {
                scope.launch {
                    try {
                        val s = settings ?: com.mugheadgolf.app.data.models.Settings()
                        ApiClient.service.saveSettings(s.copy(
                            year = session.year,
                            weeks = weeks.toIntOrNull() ?: s.weeks,
                            startdate = startDate.ifBlank { s.startdate }
                        ))
                        message = "Settings saved."
                    } catch (e: Exception) { message = e.message ?: "Error saving settings." }
                }
            }) { Text("Save Settings") }
        }
    }
}
