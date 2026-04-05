package com.mugheadgolf.app.screens.email

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
import com.mugheadgolf.app.data.models.GroupEmailRequest
import com.mugheadgolf.app.viewmodels.SessionViewModel
import kotlinx.coroutines.launch

@Composable
fun GroupEmailScreen() {
    var golfers by remember { mutableStateOf<List<Golfer>>(emptyList()) }
    var selectedIds by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var subject by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(true) }
    var sending by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    val scope = kotlinx.coroutines.rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try { golfers = ApiClient.service.getAllGolfers() } catch (_: Exception) {}
            loading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Send Group Email (Admin)", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = body, onValueChange = { body = it }, label = { Text("Message Body") }, modifier = Modifier.fillMaxWidth(), minLines = 4)

        Text("Select Recipients:", style = MaterialTheme.typography.titleSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { selectedIds = golfers.map { it.idgolfer }.toSet() }) { Text("Select All") }
            OutlinedButton(onClick = { selectedIds = emptySet() }) { Text("Clear All") }
        }

        if (loading) CircularProgressIndicator()
        else {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                items(golfers) { g ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Checkbox(
                            checked = selectedIds.contains(g.idgolfer),
                            onCheckedChange = { checked ->
                                selectedIds = if (checked) selectedIds + g.idgolfer else selectedIds - g.idgolfer
                            }
                        )
                        Text("${g.firstname} ${g.lastname} <${g.email}>")
                    }
                }
            }
        }

        if (message.isNotEmpty()) Card { Text(message, modifier = Modifier.padding(8.dp)) }

        if (sending) CircularProgressIndicator()
        else Button(modifier = Modifier.fillMaxWidth(), onClick = {
            if (subject.isBlank() || body.isBlank()) { message = "Subject and body are required."; return@Button }
            if (selectedIds.isEmpty()) { message = "Select at least one recipient."; return@Button }
            sending = true; message = ""
            scope.launch {
                try {
                    ApiClient.service.sendGroupEmail(GroupEmailRequest(subject, body, selectedIds.toList()))
                    message = "Email sent to ${selectedIds.size} golfers."
                } catch (e: Exception) { message = e.message ?: "Error sending email." }
                sending = false
            }
        }) { Text("Send Email") }
    }
}

@Composable
fun SendScheduleScreen(sessionViewModel: SessionViewModel) {
    val session by sessionViewModel.state.collectAsState()
    var selectedWeek by remember { mutableIntStateOf(session.currentWeek.coerceAtLeast(1)) }
    var sending by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    val scope = kotlinx.coroutines.rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Send Weekly Schedule Email (Admin)", style = MaterialTheme.typography.headlineMedium)

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Week:")
            (1..session.currentWeek.coerceAtLeast(1)).forEach { w ->
                FilterChip(selected = selectedWeek == w, onClick = { selectedWeek = w }, label = { Text("$w") })
            }
        }

        if (message.isNotEmpty()) Card { Text(message, modifier = Modifier.padding(8.dp)) }

        if (sending) CircularProgressIndicator()
        else Button(modifier = Modifier.fillMaxWidth(), onClick = {
            sending = true; message = ""
            scope.launch {
                try {
                    ApiClient.service.sendScheduleEmail(session.year, selectedWeek)
                    message = "Schedule email sent for week $selectedWeek."
                } catch (e: Exception) { message = e.message ?: "Error sending email." }
                sending = false
            }
        }) { Text("Send Schedule Email for Week $selectedWeek") }
    }
}
