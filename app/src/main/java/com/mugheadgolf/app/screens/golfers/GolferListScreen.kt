package com.mugheadgolf.app.screens.golfers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mugheadgolf.app.data.api.ApiClient
import com.mugheadgolf.app.data.models.Golfer
import kotlinx.coroutines.launch

@Composable
fun GolferListScreen() {
    var golfers by remember { mutableStateOf<List<Golfer>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf("") }
    val scope = kotlinx.coroutines.rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                golfers = ApiClient.service.getAllGolfers()
            } catch (e: Exception) {
                error = e.message ?: "Error loading golfers."
            } finally { loading = false }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Golfers", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))

        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (error.isNotEmpty()) {
            Text(error, color = MaterialTheme.colorScheme.error)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(golfers) { golfer ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(40.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("${golfer.firstname} ${golfer.lastname}", style = MaterialTheme.typography.titleMedium)
                                Text("Handicap: ${golfer.currenthandicap}", style = MaterialTheme.typography.bodySmall)
                                Text("Phone: ${golfer.phone}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
