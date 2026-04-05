package com.mugheadgolf.app.screens.golfers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.mugheadgolf.app.data.api.ApiClient
import com.mugheadgolf.app.data.models.Golfer
import kotlinx.coroutines.launch

@Composable
fun GolferFormScreen(id: String, onSaved: () -> Unit, onBack: () -> Unit) {
    val isNew = id == "0"
    var golfer by remember { mutableStateOf(Golfer()) }
    var loading by remember { mutableStateOf(!isNew) }
    var saving by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }
    var adminChecked by remember { mutableStateOf(false) }
    val scope = kotlinx.coroutines.rememberCoroutineScope()

    LaunchedEffect(id) {
        if (!isNew) {
            try {
                golfer = ApiClient.service.getGolferById(id.toInt())
                adminChecked = golfer.admin
            } catch (e: Exception) { errorMsg = e.message ?: "Error loading golfer." }
            loading = false
        }
    }

    if (loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(if (isNew) "Add Golfer" else "Edit Golfer", style = MaterialTheme.typography.titleLarge)

        fun field(label: String, value: String, onChange: (String) -> Unit, keyboard: KeyboardOptions = KeyboardOptions.Default) {
            OutlinedTextField(value = value, onValueChange = onChange, label = { Text(label) }, modifier = Modifier.fillMaxWidth(), keyboardOptions = keyboard)
        }

        field("First Name", golfer.firstname) { golfer = golfer.copy(firstname = it) }
        field("Last Name", golfer.lastname) { golfer = golfer.copy(lastname = it) }
        field("Username", golfer.username) { golfer = golfer.copy(username = it) }
        OutlinedTextField(value = golfer.password, onValueChange = { golfer = golfer.copy(password = it) }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation())
        field("Email", golfer.email, { golfer = golfer.copy(email = it) }, KeyboardOptions(keyboardType = KeyboardType.Email))
        field("Phone", golfer.phone, { golfer = golfer.copy(phone = it) }, KeyboardOptions(keyboardType = KeyboardType.Phone))
        field("Address", golfer.address) { golfer = golfer.copy(address = it) }
        field("City", golfer.city) { golfer = golfer.copy(city = it) }
        field("State", golfer.state) { golfer = golfer.copy(state = it) }
        field("Zip", golfer.zip, { golfer = golfer.copy(zip = it) }, KeyboardOptions(keyboardType = KeyboardType.Number))
        field("Spouse", golfer.spouse) { golfer = golfer.copy(spouse = it) }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = adminChecked, onCheckedChange = { adminChecked = it; golfer = golfer.copy(admin = it) })
            Text("Admin")
        }

        if (errorMsg.isNotEmpty()) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Text(errorMsg, modifier = Modifier.padding(8.dp))
            }
        }

        if (saving) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = onBack) { Text("Cancel") }
                Button(onClick = {
                    saving = true; errorMsg = ""
                    scope.launch {
                        try {
                            ApiClient.service.addGolfer(golfer)
                            onSaved()
                        } catch (e: Exception) {
                            errorMsg = e.message ?: "Error saving golfer."
                        } finally { saving = false }
                    }
                }) { Text("Save") }
            }
        }
    }
}
