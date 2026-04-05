package com.mugheadgolf.app.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mugheadgolf.app.data.TokenManager
import com.mugheadgolf.app.data.api.ApiClient
import com.mugheadgolf.app.data.models.LoginRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class SessionState(
    val isAuthenticated: Boolean = false,
    val idgolfer: Int = 0,
    val firstname: String = "",
    val lastname: String = "",
    val isAdmin: Boolean = false,
    val autoRefresh: Boolean = false,
    val year: Int = Calendar.getInstance().get(Calendar.YEAR),
    val currentWeek: Int = 0,
    val winnings: Double = 0.0,
    val totalPoints: Double = 0.0,
    val golferAvg: Double = 0.0,
    val leagueAvg: Double = 0.0,
    val currentHandicap: Double = 0.0,
    val currentMatch: Int = 0,
    val currentOpponent: String = "",
    val currentOpponentHC: Double = 0.0,
    val opponentAbsent: Boolean = false,
    val lastMatch: Int = 0,
    val lastOpponent: String = "",
    val lastPoints: Double = 0.0,
    val lastOpponentPoints: Double = 0.0,
    val divisionId: Int = 0,
    val divisionName: String = "",
    val divisionLeader: String = "",
    val divisionLeaderPoints: Double = 0.0
) {
    val fullName get() = "${firstname} ${lastname}"
}

class SessionViewModel : ViewModel() {
    private val _state = MutableStateFlow(SessionState())
    val state: StateFlow<SessionState> = _state.asStateFlow()

    private val api get() = ApiClient.service

    fun login(username: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val golfer = api.login(
                    LoginRequest(username = username, password = password)
                )
                if (golfer.idgolfer == 0) {
                    onError("Invalid username and/or password")
                    return@launch
                }
                _state.value = _state.value.copy(
                    isAuthenticated = true,
                    idgolfer = golfer.idgolfer,
                    firstname = golfer.firstname.uppercase(),
                    lastname = golfer.lastname.uppercase(),
                    isAdmin = golfer.admin,
                    autoRefresh = golfer.autorefresh
                )
                bootstrapSession()
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Invalid username and/or password")
            }
        }
    }

    fun bootstrapSession() {
        viewModelScope.launch {
            try {
                val idg = _state.value.idgolfer
                val yr = _state.value.year

                var divId = 0
                try {
                    val dg = api.getDivisionGolferByGolfer(idg)
                    divId = dg.iddivision?.iddivision ?: 0
                    _state.value = _state.value.copy(divisionId = divId)
                } catch (_: Exception) {}

                val week = try { api.getCurrentWeek(yr) } catch (_: Exception) { 0 }
                _state.value = _state.value.copy(currentWeek = week)

                val money = try { api.getGolferMoney(yr, idg) } catch (_: Exception) { null }
                val points = try { api.getGolferTotalPoints(idg) } catch (_: Exception) { 0.0 }
                val golferAvg = try { api.getGolferAvg(yr, idg) } catch (_: Exception) { 0.0 }
                val leagueAvg = try { api.getLeagueAvg(yr) } catch (_: Exception) { 0.0 }
                val schedules = try { api.getGolferSchedule(yr, idg) } catch (_: Exception) { emptyList() }

                _state.value = _state.value.copy(
                    winnings = money?.total ?: 0.0,
                    totalPoints = points,
                    golferAvg = golferAvg,
                    leagueAvg = leagueAvg
                )

                setOpponentAndResults(schedules, week)

                if (divId != 0) {
                    try {
                        val div = api.getDivision(divId)
                        _state.value = _state.value.copy(divisionName = div.name)
                    } catch (_: Exception) {}
                    try {
                        val leader = api.getDivisionLeaderPoints(divId)
                        _state.value = _state.value.copy(
                            divisionLeader = leader.name,
                            divisionLeaderPoints = leader.totalpoints
                        )
                    } catch (_: Exception) {}
                }
            } catch (e: Exception) {
                // silent
            }
        }
    }

    private fun setOpponentAndResults(schedules: List<com.mugheadgolf.app.data.models.Schedule>, week: Int) {
        val idg = _state.value.idgolfer
        if (schedules.isEmpty() || week == 0) return

        val cur = schedules.getOrNull(week - 1)
        if (cur != null) {
            _state.value = _state.value.copy(currentMatch = cur.idschedule)
            when (idg) {
                cur.idgolfer1?.idgolfer -> {
                    val opp = cur.idgolfer2
                    _state.value = _state.value.copy(
                        currentOpponent = "${opp?.firstname} ${opp?.lastname}",
                        currentOpponentHC = opp?.currenthandicap ?: 0.0,
                        currentHandicap = cur.idgolfer1.currenthandicap,
                        opponentAbsent = cur.absent2
                    )
                }
                cur.idgolfer2?.idgolfer -> {
                    val opp = cur.idgolfer1
                    _state.value = _state.value.copy(
                        currentOpponent = "${opp?.firstname} ${opp?.lastname}",
                        currentOpponentHC = opp?.currenthandicap ?: 0.0,
                        currentHandicap = cur.idgolfer2.currenthandicap,
                        opponentAbsent = cur.absent1
                    )
                }
            }
        }

        if (week > 1) {
            val last = schedules.getOrNull(week - 2)
            if (last != null) {
                _state.value = _state.value.copy(lastMatch = last.idschedule)
                when (idg) {
                    last.idgolfer1?.idgolfer -> _state.value = _state.value.copy(
                        lastOpponent = "${last.idgolfer2?.firstname} ${last.idgolfer2?.lastname}",
                        lastOpponentPoints = last.points2, lastPoints = last.points1
                    )
                    last.idgolfer2?.idgolfer -> _state.value = _state.value.copy(
                        lastOpponent = "${last.idgolfer1?.firstname} ${last.idgolfer1?.lastname}",
                        lastOpponentPoints = last.points1, lastPoints = last.points2
                    )
                }
            }
        }
    }

    fun logout(tokenManager: TokenManager) {
        tokenManager.clearToken()
        _state.value = SessionState()
    }

    fun setYear(year: Int) {
        _state.value = _state.value.copy(year = year)
        bootstrapSession()
    }

    fun toggleAutoRefresh() {
        viewModelScope.launch {
            try {
                api.toggleAutoRefresh(_state.value.idgolfer)
                _state.value = _state.value.copy(autoRefresh = !_state.value.autoRefresh)
            } catch (_: Exception) {}
        }
    }
}
