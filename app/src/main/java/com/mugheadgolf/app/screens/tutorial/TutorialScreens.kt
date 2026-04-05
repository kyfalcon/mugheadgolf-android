package com.mugheadgolf.app.screens.tutorial

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TutorialScreen(onSignIn: () -> Unit, onTeeTimes: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Welcome to MugheadGolf", style = MaterialTheme.typography.headlineMedium)
        Card {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Getting Started", style = MaterialTheme.typography.titleMedium)
                Text("MugheadGolf is a golf league management app. It helps you track scores, standings, money, and schedules for your golf league.")
            }
        }
        Card {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Features", style = MaterialTheme.typography.titleMedium)
                Text("• Weekly match schedules and tee times")
                Text("• Score tracking with net scores and handicaps")
                Text("• Points-based standings and division tracking")
                Text("• Money winnings (low net, skins)")
                Text("• Tournament brackets (low/high handicap)")
                Text("• Pick-four wager system")
                Text("• Food ordering")
            }
        }
        Button(onClick = onTeeTimes, modifier = Modifier.fillMaxWidth()) { Text("View Tee Times Tutorial") }
        OutlinedButton(onClick = onSignIn, modifier = Modifier.fillMaxWidth()) { Text("Sign In") }
    }
}

@Composable
fun TutorialTeeTimesScreen(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Tee Times Tutorial", style = MaterialTheme.typography.headlineMedium)
        Card {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("How Tee Times Work", style = MaterialTheme.typography.titleMedium)
                Text("Golfers are grouped into foursomes for each week. The schedule shows who you'll play with and against.")
                Text("• Your match opponent is shown on the dashboard")
                Text("• Foursomes are assigned by the administrator")
                Text("• Tee times are listed in the Weekly Tee Times section")
                Text("• You can view your full schedule under Golfer Schedule")
            }
        }
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Back") }
    }
}
