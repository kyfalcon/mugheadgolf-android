package com.mugheadgolf.app.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mugheadgolf.app.data.api.ApiClient
import kotlinx.coroutines.launch

@Composable
fun RegisterResponseScreen(onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Registration Successful", style = MaterialTheme.typography.titleLarge)
                Text("Your registration has been submitted. You will receive an email to activate your account.")
                Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back to Sign In") }
            }
        }
    }
}

@Composable
fun ActivationNoticeScreen(onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Account Activation", style = MaterialTheme.typography.titleLarge)
                Text("Your account needs to be activated by an administrator before you can sign in.")
                Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back to Sign In") }
            }
        }
    }
}

@Composable
fun AuthenticateScreen(token: String, onAuthenticated: () -> Unit, onError: () -> Unit) {
    var loading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(token) {
        scope.launch {
            try {
                ApiClient.service.confirmEmail(token)
                onAuthenticated()
            } catch (e: Exception) {
                errorMsg = e.message ?: "Authentication failed."
                loading = false
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (loading && errorMsg.isEmpty()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                CircularProgressIndicator()
                Text("Authenticating...")
            }
        } else {
            Card(modifier = Modifier.padding(16.dp)) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Authentication Failed", style = MaterialTheme.typography.titleLarge)
                    Text(errorMsg)
                    Button(onClick = onError) { Text("Back to Sign In") }
                }
            }
        }
    }
}
