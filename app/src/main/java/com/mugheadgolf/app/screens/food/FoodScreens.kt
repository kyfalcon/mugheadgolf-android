package com.mugheadgolf.app.screens.food

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mugheadgolf.app.data.api.ApiClient
import com.mugheadgolf.app.data.models.Menu
import com.mugheadgolf.app.viewmodels.SessionViewModel
import kotlinx.coroutines.launch

@Composable
fun FoodMenuScreen(sessionViewModel: SessionViewModel, onOrder: () -> Unit) {
    var menu by remember { mutableStateOf<List<Menu>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    val scope = kotlinx.coroutines.rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try { menu = ApiClient.service.getMenu() } catch (_: Exception) {}
            loading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Food Menu", style = MaterialTheme.typography.headlineMedium)
            Button(onClick = onOrder) { Text("Place Order") }
        }
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(menu.filter { it.active }) { item ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, style = MaterialTheme.typography.titleSmall)
                                if (item.description.isNotBlank()) Text(item.description, style = MaterialTheme.typography.bodySmall)
                            }
                            Text("${"$"}${"%.2f".format(item.price)}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderScreen(sessionViewModel: SessionViewModel) {
    val session by sessionViewModel.state.collectAsState()
    var menu by remember { mutableStateOf<List<Menu>>(emptyList()) }
    var quantities by remember { mutableStateOf<Map<Int, Int>>(emptyMap()) }
    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    val scope = kotlinx.coroutines.rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            try { menu = ApiClient.service.getMenu() } catch (_: Exception) {}
            loading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Place Order", style = MaterialTheme.typography.headlineMedium)
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Column
        }

        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(menu.filter { it.active }) { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.name, style = MaterialTheme.typography.titleSmall)
                            Text("${"$"}${"%.2f".format(item.price)}", style = MaterialTheme.typography.bodySmall)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            val qty = quantities[item.idmenu] ?: 0
                            OutlinedButton(onClick = { if (qty > 0) quantities = quantities + (item.idmenu to qty - 1) }) { Text("-") }
                            Text("$qty")
                            OutlinedButton(onClick = { quantities = quantities + (item.idmenu to qty + 1) }) { Text("+") }
                        }
                    }
                }
            }
        }

        val total = quantities.entries.sumOf { (id, qty) -> (menu.find { it.idmenu == id }?.price ?: 0.0) * qty }
        Text("Total: ${"$"}${"%.2f".format(total)}", style = MaterialTheme.typography.titleMedium)

        if (message.isNotEmpty()) Card { Text(message, modifier = Modifier.padding(8.dp)) }

        if (saving) CircularProgressIndicator()
        else Button(modifier = Modifier.fillMaxWidth(), onClick = {
            saving = true; message = ""
            scope.launch {
                try {
                    val orderItems = quantities.filter { it.value > 0 }.map { (id, qty) ->
                        mapOf("idmenu" to id, "quantity" to qty, "idgolfer" to session.idgolfer,
                            "year" to session.year, "week" to session.currentWeek)
                    }
                    ApiClient.service.saveOrder(orderItems)
                    message = "Order saved!"
                    quantities = emptyMap()
                } catch (e: Exception) { message = e.message ?: "Error saving order." }
                saving = false
            }
        }) { Text("Submit Order") }
    }
}
