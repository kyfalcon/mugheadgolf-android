package com.mugheadgolf.app.screens.wager

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mugheadgolf.app.data.api.ApiClient
import com.mugheadgolf.app.data.models.Golfer
import com.mugheadgolf.app.data.models.Wager
import com.mugheadgolf.app.viewmodels.SessionViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PickDropdown(label: String, golfers: List<Golfer>, selected: Golfer?, onSelect: (Golfer?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected?.let { "${it.firstname} ${it.lastname}" } ?: "Select...",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("None") }, onClick = { onSelect(null); expanded = false })
            golfers.forEach { g ->
                DropdownMenuItem(
                    text = { Text("${g.firstname} ${g.lastname}") },
                    onClick = { onSelect(g); expanded = false }
                )
            }
        }
    }
}

@Composable
fun WagerScreen(sessionViewModel: SessionViewModel) {
    val session by sessionViewModel.state.collectAsState()
    var wager by remember { mutableStateOf<Wager?>(null) }
    var golfers by remember { mutableStateOf<List<Golfer>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var pick1 by remember { mutableStateOf<Golfer?>(null) }
    var pick2 by remember { mutableStateOf<Golfer?>(null) }
    var pick3 by remember { mutableStateOf<Golfer?>(null) }
    var pick4 by remember { mutableStateOf<Golfer?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(session.year, session.currentWeek, session.idgolfer) {
        scope.launch {
            try {
                golfers = ApiClient.service.getAllGolfers()
                if (session.currentWeek > 0 && session.idgolfer > 0) {
                    wager = try { ApiClient.service.getWager(session.year, session.currentWeek, session.idgolfer) } catch (_: Exception) { null }
                    pick1 = wager?.pick1; pick2 = wager?.pick2; pick3 = wager?.pick3; pick4 = wager?.pick4
                }
            } catch (_: Exception) {}
            loading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Pick Four Bet (Week ${session.currentWeek})", style = MaterialTheme.typography.headlineMedium)
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Column
        }

        Text("Select 4 golfers you think will have the lowest scores this week:")

        PickDropdown("Pick 1", golfers, pick1) { pick1 = it }
        PickDropdown("Pick 2", golfers, pick2) { pick2 = it }
        PickDropdown("Pick 3", golfers, pick3) { pick3 = it }
        PickDropdown("Pick 4", golfers, pick4) { pick4 = it }

        if (message.isNotEmpty()) Card { Text(message, modifier = Modifier.padding(8.dp)) }

        if (saving) CircularProgressIndicator()
        else Button(modifier = Modifier.fillMaxWidth(), onClick = {
            val picks = listOfNotNull(pick1, pick2, pick3, pick4)
            if (picks.size != 4) { message = "Please select 4 different golfers."; return@Button }
            saving = true; message = ""
            scope.launch {
                try {
                    val w = Wager(
                        idwager = wager?.idwager ?: 0,
                        year = session.year, week = session.currentWeek,
                        idgolfer = golfers.find { it.idgolfer == session.idgolfer },
                        pick1 = pick1, pick2 = pick2, pick3 = pick3, pick4 = pick4
                    )
                    ApiClient.service.saveWager(w)
                    message = "Wager saved!"
                } catch (e: Exception) { message = e.message ?: "Error saving wager." }
                saving = false
            }
        }) { Text("Save Wager") }
    }
}

@Composable
fun WagerResultsScreen(sessionViewModel: SessionViewModel) {
    val session by sessionViewModel.state.collectAsState()
    var results by remember { mutableStateOf<List<Wager>>(emptyList()) }
    var selectedWeek by remember { mutableIntStateOf(session.currentWeek.coerceAtLeast(1)) }
    var loading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(session.year, selectedWeek) {
        loading = true
        scope.launch {
            try { results = ApiClient.service.getWagerResults(session.year, selectedWeek) } catch (_: Exception) {}
            loading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Wager Results (${session.year})", style = MaterialTheme.typography.headlineMedium)
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
                items(results) { r ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("${r.idgolfer?.firstname} ${r.idgolfer?.lastname}", style = MaterialTheme.typography.titleSmall)
                            Text("Picks: ${r.pick1?.firstname}, ${r.pick2?.firstname}, ${r.pick3?.firstname}, ${r.pick4?.firstname}")
                            r.result?.let { Text("Result: ${"%.2f".format(it)}", color = MaterialTheme.colorScheme.primary) }
                        }
                    }
                }
            }
        }
    }
}
