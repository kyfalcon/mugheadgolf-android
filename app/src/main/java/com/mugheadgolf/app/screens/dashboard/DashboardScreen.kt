package com.mugheadgolf.app.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mugheadgolf.app.viewmodels.SessionViewModel

@Composable
fun DashboardScreen(
    sessionViewModel: SessionViewModel,
    onGoToMatch: (Int) -> Unit
) {
    val session by sessionViewModel.state.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Dashboard", style = MaterialTheme.typography.headlineMedium)

        // This Week's Match
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("This Week's Match", style = MaterialTheme.typography.titleMedium)
                if (session.opponentAbsent) {
                    Text("Opponent ${session.currentOpponent} is not golfing this week.",
                        color = MaterialTheme.colorScheme.error)
                } else if (session.currentOpponent.isNotBlank()) {
                    Text("Opponent: ${session.currentOpponent} (HC: ${session.currentOpponentHC})")
                }
                Text("Your Handicap: ${session.currentHandicap}")
                if (session.currentMatch != 0) {
                    Button(onClick = { onGoToMatch(session.currentMatch) }) {
                        Icon(Icons.Default.Flag, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Go to Scorecard")
                    }
                }
            }
        }

        // Last Week's Results
        if (session.currentWeek > 1 && session.lastOpponent.isNotBlank()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Last Week's Results", style = MaterialTheme.typography.titleMedium)
                    Text("You: ${session.lastPoints} pts vs ${session.lastOpponent}: ${session.lastOpponentPoints} pts")
                    if (session.lastMatch != 0) {
                        OutlinedButton(onClick = { onGoToMatch(session.lastMatch) }) {
                            Icon(Icons.Default.RemoveRedEye, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("View Last Match")
                        }
                    }
                }
            }
        }

        // Division Standings
        if (session.currentWeek > 1) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Division Standings", style = MaterialTheme.typography.titleMedium)
                    Text("Your Points: ${session.totalPoints}")
                    if (session.divisionName.isNotBlank()) Text("Division: ${session.divisionName}")
                    if (session.divisionLeader.isNotBlank()) Text("Leader: ${session.divisionLeader} (${session.divisionLeaderPoints} pts)")
                }
            }

            // Money & Averages
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Money & Averages", style = MaterialTheme.typography.titleMedium)
                    Text("Total Winnings: ${"$"}${session.winnings}")
                    Text("Your Avg: ${"%.1f".format(session.golferAvg)}")
                    Text("League Avg: ${"%.1f".format(session.leagueAvg)}")
                }
            }
        }
    }
}
