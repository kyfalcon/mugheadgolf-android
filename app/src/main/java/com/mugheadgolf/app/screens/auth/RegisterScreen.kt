package com.mugheadgolf.app.screens.auth

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
fun RegisterScreen(id: String, onSuccess: () -> Unit, onBack: () -> Unit) {
    val isNew = id == "-1" || id == "0"
    var firstname by remember { mutableStateOf("") }
    var lastname by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var zip by remember { mutableStateOf("") }
    var spouse by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(id) {
        if (!isNew) {
            try {
                val g = ApiClient.service.getGolferById(id.toInt())
                firstname = g.firstname; lastname = g.lastname; username = g.username
                email = g.email; phone = g.phone; address = g.address
                city = g.city; state = g.state; zip = g.zip; spouse = g.spouse
            } catch (_: Exception) {}
        }
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(if (isNew) "Register" else "Edit Profile", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(value = firstname, onValueChange = { firstname = it }, label = { Text("First Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = lastname, onValueChange = { lastname = it }, label = { Text("Last Name") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = username, onValueChange = { username = it }, label = { Text("Username") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation())
        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
        OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = state, onValueChange = { state = it }, label = { Text("State") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = zip, onValueChange = { zip = it }, label = { Text("Zip") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
        OutlinedTextField(value = spouse, onValueChange = { spouse = it }, label = { Text("Spouse") }, modifier = Modifier.fillMaxWidth())

        if (errorMsg.isNotEmpty()) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Text(errorMsg, modifier = Modifier.padding(8.dp), color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }

        if (loading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = onBack) { Text("Cancel") }
                Button(onClick = {
                    if (firstname.isBlank() || lastname.isBlank() || username.isBlank() || password.isBlank()) {
                        errorMsg = "First name, last name, username, and password are required."
                        return@Button
                    }
                    loading = true; errorMsg = ""
                    scope.launch {
                        try {
                            val golfer = Golfer(
                                idgolfer = if (isNew) 0 else id.toInt(),
                                firstname = firstname, lastname = lastname, username = username,
                                password = password, email = email, phone = phone,
                                address = address, city = city, state = state, zip = zip, spouse = spouse
                            )
                            if (isNew) ApiClient.service.registerGolfer(golfer)
                            else ApiClient.service.addGolfer(golfer)
                            onSuccess()
                        } catch (e: Exception) {
                            errorMsg = e.message ?: "Error saving profile."
                        } finally { loading = false }
                    }
                }) { Text("Save") }
            }
        }
    }
}
