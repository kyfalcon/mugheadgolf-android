package com.mugheadgolf.app.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mugheadgolf.app.data.api.ApiClient
import kotlinx.coroutines.launch

@Composable
fun ForgotScreen(onBack: () -> Unit) {
    var phone by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    fun submit() {
        if (phone.isBlank()) { message = "Phone number is required."; isError = true; return }
        loading = true; message = ""; isError = false
        scope.launch {
            try {
                ApiClient.service.forgot(phone)
                message = "An email has been sent with your login information."
                isError = false
            } catch (e: Exception) {
                message = e.message ?: "Error sending forgot request."
                isError = true
            } finally { loading = false }
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Forgot Login Information", style = MaterialTheme.typography.titleLarge)
                Text("Enter your phone number and we will email your login information.")

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    singleLine = true,
                    enabled = !loading,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus(); submit() })
                )

                if (message.isNotEmpty()) {
                    Card(colors = CardDefaults.cardColors(
                        containerColor = if (isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                    )) {
                        Text(message, modifier = Modifier.padding(8.dp))
                    }
                }

                if (loading) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                } else {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        TextButton(onClick = onBack) { Text("Back to Sign In") }
                        Button(onClick = ::submit) { Text("Submit") }
                    }
                }
            }
        }
    }
}
