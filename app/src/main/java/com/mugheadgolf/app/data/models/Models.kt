package com.mugheadgolf.app.data.models

import com.google.gson.annotations.SerializedName

data class Golfer(
    val idgolfer: Int = 0,
    val firstname: String = "",
    val lastname: String = "",
    val username: String = "",
    val password: String = "",
    val address: String = "",
    val city: String = "",
    val state: String = "",
    val zip: String = "",
    val email: String = "",
    val spouse: String = "",
    val active: Int = 1,
    val phone: String = "",
    val currenthandicap: Double = 0.0,
    val admin: Boolean = false,
    val autorefresh: Boolean = true
) {
    val fullName get() = "${firstname.uppercase()} ${lastname.uppercase()}"
}

data class LoginRequest(
    val username: String,
    val password: String,
    val idgolfer: Int = 0,
    val firstname: String? = null,
    val lastname: String? = null,
    val address: String? = null,
    val city: String? = null,
    val state: String? = null,
    val zip: String? = null,
    val email: String? = null,
    val spouse: String? = null,
    val active: Int? = null,
    val phone: String? = null,
    val currenthandicap: Double? = null,
    val admin: Boolean? = null,
    val autorefresh: Boolean = true
)

data class Schedule(
    val idschedule: Int = 0,
    val year: Int = 0,
    val week: Int = 0,
    val date: String? = null,
    val idgolfer1: Golfer? = null,
    val idgolfer2: Golfer? = null,
    val points1: Double = 0.0,
    val points2: Double = 0.0,
    val absent1: Boolean = false,
    val absent2: Boolean = false,
    val backnine: Boolean = false,
    val idfoursome: Int? = null
)

data class Score(
    val idscore: Int = 0,
    val score: Double = 0.0,
    val net: Double = 0.0,
    val adjustedscore: Double = 0.0,
    val useforhandicap: Int = 1,
    val handicapdifferential: Double = 0.0,
    val idgolfer: Golfer? = null,
    val idschedule: Schedule? = null
)

data class ScoreHole(
    val idscorehole: Int = 0,
    val idscore: Any? = null,
    val hole: Int = 0,
    val score: Int? = null,
    val net: Int? = null,
    val adjustedscore: Int? = null,
    val type: Int? = null
)

data class ScoreData(
    val score: Score,
    val scoreholes: List<ScoreHole>? = null
)

data class PointTotal(
    val idgolfer: Golfer? = null,
    val name: String = "",
    val totalpoints: Double = 0.0,
    val wins: Int = 0,
    val losses: Int = 0,
    val ties: Int = 0
)

data class GolferMoney(
    val idgolfer: Golfer? = null,
    val name: String = "",
    val total: Double = 0.0,
    val weeks: List<Any>? = null
)

data class Money(
    val idmoney: Int = 0,
    val year: Int = 0,
    val week: Int = 0,
    val amount: Double = 0.0,
    val description: String = "",
    val idgolfer: Golfer? = null
)

data class Wager(
    val idwager: Int = 0,
    val year: Int = 0,
    val week: Int = 0,
    val idgolfer: Golfer? = null,
    val pick1: Golfer? = null,
    val pick2: Golfer? = null,
    val pick3: Golfer? = null,
    val pick4: Golfer? = null,
    val result: Double? = null
)

data class Menu(
    val idmenu: Int = 0,
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val active: Boolean = true
)

data class Division(
    val iddivision: Int = 0,
    val name: String = "",
    val year: Int = 0
)

data class DivisionGolfer(
    val iddivisiongolfer: Int = 0,
    val iddivision: Division? = null,
    val idgolfer: Golfer? = null
)

data class TourneySeed(
    val idtourneyseed: Int = 0,
    val year: Int = 0,
    val type: String = "",
    val seed: Int = 0,
    val idgolfer: Golfer? = null,
    val score: Double = 0.0
)

data class TourneyBranch(
    val idtourneybranch: Int = 0,
    val year: Int = 0,
    val type: String = "",
    val round: Int = 0,
    val position: Int = 0,
    val idgolfer1: Golfer? = null,
    val idgolfer2: Golfer? = null,
    val winner: Golfer? = null,
    val score1: Double? = null,
    val score2: Double? = null
)

data class Stats(
    val idgolfer: Golfer? = null,
    val name: String = "",
    val avg: Double = 0.0,
    val low: Double = 0.0,
    val high: Double = 0.0,
    val rounds: Int = 0,
    val birdies: Int = 0,
    val pars: Int = 0,
    val bogeys: Int = 0,
    val doubles: Int = 0,
    val others: Int = 0
)

data class WeeklyStats(
    val week: Int = 0,
    val date: String? = null,
    val avg: Double = 0.0,
    val low: Double = 0.0,
    val high: Double = 0.0
)

data class StatsPie(
    @SerializedName("GolferData") val golferData: List<List<Any>>? = null,
    @SerializedName("LeagueData") val leagueData: List<List<Any>>? = null
)

data class Settings(
    val idsettings: Int = 0,
    val year: Int = 0,
    val weeks: Int = 0,
    val startdate: String? = null,
    val idcourse: Any? = null
)

data class Course(
    val idcourse: Int = 0,
    val name: String = "",
    val holes: Int = 18
)

data class CourseHole(
    val idcoursehole: Int = 0,
    val hole: Int = 0,
    val par: Int = 0,
    val handicap: Int = 0,
    val idcourse: Course? = null
)

data class CourseData(
    val course: Course? = null,
    val courseholes: List<CourseHole>? = null
)

data class HandicapRow(
    val idgolfer: Golfer? = null,
    val name: String = "",
    val currenthandicap: Double = 0.0,
    val differentials: List<Double>? = null
)

data class GroupEmailRequest(
    val subject: String,
    val body: String,
    val golferIds: List<Int>
)
