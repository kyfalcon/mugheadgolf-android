package com.mugheadgolf.app.data.api

import com.mugheadgolf.app.data.models.*
import retrofit2.http.*

interface ApiService {
    // Golfer
    @GET("golfer/findall")
    suspend fun getAllGolfers(): List<Golfer>

    @GET("golfer/find/{id}")
    suspend fun getGolferById(@Path("id") id: Int): Golfer

    @POST("golfer/add")
    suspend fun addGolfer(@Body golfer: Golfer): Golfer

    @POST("golfer/register")
    suspend fun registerGolfer(@Body golfer: Golfer): Golfer

    @DELETE("golfer/{id}")
    suspend fun deleteGolfer(@Path("id") id: Int)

    @GET("golfer/confirmemail/{token}")
    suspend fun confirmEmail(@Path("token") token: String): String

    @POST("golfer/login")
    suspend fun login(@Body request: LoginRequest): Golfer

    @GET("golfer/forgot/{phone}")
    suspend fun forgot(@Path("phone") phone: String)

    @GET("golfer/autoRefresh/{idgolfer}")
    suspend fun toggleAutoRefresh(@Path("idgolfer") idgolfer: Int)

    // Handicap
    @GET("handicap/year/{year}")
    suspend fun getHandicaps(@Path("year") year: Int): List<Any>

    @GET("handicap/calculate/{currentWeek}")
    suspend fun calcHandicaps(@Path("currentWeek") currentWeek: Int): String

    // Schedule
    @GET("schedule/year/{year}")
    suspend fun getSchedule(@Path("year") year: Int): List<Schedule>

    @GET("schedule/find/{id}")
    suspend fun getScheduleById(@Path("id") id: Int): Schedule

    @GET("schedule/create/{year}")
    suspend fun createSchedule(@Path("year") year: Int)

    @GET("schedule/golfer/{year}/{idgolfer}")
    suspend fun getGolferSchedule(@Path("year") year: Int, @Path("idgolfer") idgolfer: Int): List<Schedule>

    @GET("schedule/currentweek/{year}")
    suspend fun getCurrentWeek(@Path("year") year: Int): Int

    @POST("schedule/backnine")
    suspend fun saveBacknine(@Body schedule: Schedule)

    @POST("schedule/save")
    suspend fun saveSchedule(@Body schedule: Schedule): Schedule

    @GET("schedule/swap/{year}/{week}/{golfer1}/{golfer2}")
    suspend fun swapGolfers(@Path("year") year: Int, @Path("week") week: Int, @Path("golfer1") g1: Int, @Path("golfer2") g2: Int)

    @GET("schedule/absent/{idschedule}/{golfer}")
    suspend fun toggleAbsent(@Path("idschedule") idschedule: Int, @Path("golfer") golfer: Int)

    // Score
    @GET("score/match/{idschedule}/{idgolfer}")
    suspend fun getMatchScore(@Path("idschedule") idschedule: Int, @Path("idgolfer") idgolfer: Int): ScoreData

    @GET("score/foursome/{idfoursome}")
    suspend fun getFoursomeScores(@Path("idfoursome") idfoursome: Int): List<ScoreData>

    @GET("score/matchscores/{idschedule}")
    suspend fun getMatchScores(@Path("idschedule") idschedule: Int): List<ScoreData>

    @GET("score/golfers/{year}")
    suspend fun getGolfersScores(@Path("year") year: Int): List<Any>

    @GET("score/leagueavg/{year}")
    suspend fun getLeagueAvg(@Path("year") year: Int): Double

    @GET("score/golferavg/{year}/{idgolfer}")
    suspend fun getGolferAvg(@Path("year") year: Int, @Path("idgolfer") idgolfer: Int): Double

    @GET("score/hasstarted/{year}/{week}")
    suspend fun hasGolfWeekStarted(@Path("year") year: Int, @Path("week") week: Int): Boolean

    @GET("score/weekly/{year}/{week}")
    suspend fun getWeeklyScores(@Path("year") year: Int, @Path("week") week: Int): List<Any>

    @POST("scorehole/save")
    suspend fun saveScorehole(@Body scorehole: ScoreHole): ScoreHole

    @POST("scorehole/savewithpar/{par}")
    suspend fun saveScoreholeWithPar(@Path("par") par: Int, @Body scorehole: ScoreHole): ScoreHole

    @POST("score/save/{calcHC}")
    suspend fun saveScores(@Path("calcHC") calcHC: Boolean, @Body scores: List<Score>)

    @GET("score/calcpoints/{week}/{year}")
    suspend fun calcPoints(@Path("week") week: Int, @Path("year") year: Int)

    // Points/Standings
    @GET("points/golfer/{idgolfer}")
    suspend fun getGolferTotalPoints(@Path("idgolfer") idgolfer: Int): Double

    @GET("points/divisionleader/{iddivision}")
    suspend fun getDivisionLeaderPoints(@Path("iddivision") iddivision: Int): PointTotal

    @GET("points/division/{iddivision}")
    suspend fun getPointsByDivision(@Path("iddivision") iddivision: Int): List<PointTotal>

    @GET("points/year/{year}")
    suspend fun getPointsByYear(@Path("year") year: Int): List<PointTotal>

    // Money
    @GET("money/golfers/{year}")
    suspend fun getGolfersMoney(@Path("year") year: Int): List<GolferMoney>

    @GET("money/golfer/{year}/{idgolfer}")
    suspend fun getGolferMoney(@Path("year") year: Int, @Path("idgolfer") idgolfer: Int): GolferMoney

    @GET("money/weekly/{year}")
    suspend fun getWeeklyWinners(@Path("year") year: Int): List<Any>

    @GET("money/lownet/{year}/{week}")
    suspend fun calculateLowNet(@Path("year") year: Int, @Path("week") week: Int)

    @GET("money/skins/{year}/{week}")
    suspend fun calculateSkins(@Path("year") year: Int, @Path("week") week: Int)

    @POST("money/savewinners/{week}")
    suspend fun saveWinners(@Path("week") week: Int, @Body winners: List<Money>)

    @GET("money/week/{week}")
    suspend fun getMoneyByWeek(@Path("week") week: Int): List<Any>

    // Stats
    @GET("stats/pie/{idgolfer}/{year}")
    suspend fun getPieChartData(@Path("idgolfer") idgolfer: Int, @Path("year") year: Int): StatsPie

    @GET("stats/golfers/{year}")
    suspend fun getGolferStats(@Path("year") year: Int): List<Stats>

    @GET("stats/weekly/{year}")
    suspend fun getWeeklyStats(@Path("year") year: Int): List<WeeklyStats>

    // Wager
    @GET("wager/find/{year}/{week}/{idgolfer}")
    suspend fun getWager(@Path("year") year: Int, @Path("week") week: Int, @Path("idgolfer") idgolfer: Int): Wager

    @GET("wager/results/{year}/{week}")
    suspend fun getWagerResults(@Path("year") year: Int, @Path("week") week: Int): List<Wager>

    @POST("wager/save")
    suspend fun saveWager(@Body wager: Wager): Wager

    // Food
    @GET("menu/findall")
    suspend fun getMenu(): List<Menu>

    @POST("order/save")
    suspend fun saveOrder(@Body order: Any)

    @GET("order/week/{year}/{week}")
    suspend fun getOrders(@Path("year") year: Int, @Path("week") week: Int): List<Any>

    // Tourney
    @GET("tourney/seeds/{year}/{type}")
    suspend fun getSeeds(@Path("year") year: Int, @Path("type") type: String): List<TourneySeed>

    @GET("tourney/branches/{year}/{type}")
    suspend fun getBranches(@Path("year") year: Int, @Path("type") type: String): List<TourneyBranch>

    @POST("tourney/branch/save")
    suspend fun saveBranch(@Body branch: TourneyBranch): TourneyBranch

    @GET("tourney/create/{year}/{type}")
    suspend fun createBracket(@Path("year") year: Int, @Path("type") type: String)

    @GET("tourney/winner/{branch1}/{branch2}")
    suspend fun calculateWinner(@Path("branch1") branch1: Int, @Path("branch2") branch2: Int): TourneyBranch

    // Division
    @GET("division/findall")
    suspend fun getDivisions(): List<Division>

    @GET("division/year/{year}")
    suspend fun getDivisionsByYear(@Path("year") year: Int): List<Division>

    @GET("division/find/{id}")
    suspend fun getDivision(@Path("id") id: Int): Division

    @POST("division/save")
    suspend fun saveDivision(@Body division: Division): Division

    @DELETE("division/{id}")
    suspend fun deleteDivision(@Path("id") id: Int)

    @GET("divisiongolfer/golfer/{idgolfer}")
    suspend fun getDivisionGolferByGolfer(@Path("idgolfer") idgolfer: Int): DivisionGolfer

    @GET("divisiongolfer/division/{iddivision}")
    suspend fun getDivisionGolfersByDivision(@Path("iddivision") iddivision: Int): List<DivisionGolfer>

    @POST("divisiongolfer/save")
    suspend fun saveDivisionGolfer(@Body dg: DivisionGolfer): DivisionGolfer

    @DELETE("divisiongolfer/{id}")
    suspend fun deleteDivisionGolfer(@Path("id") id: Int)

    // Settings
    @GET("settings/year/{year}")
    suspend fun getSettings(@Path("year") year: Int): Settings

    @GET("course/findall")
    suspend fun getCourses(): List<Course>

    @GET("game/findall")
    suspend fun getGames(): List<Any>

    @POST("settings/save")
    suspend fun saveSettings(@Body settings: Settings): Settings

    @POST("game/save")
    suspend fun saveGames(@Body games: List<Any>)

    @POST("course/save")
    suspend fun saveCourse(@Body course: Course): Course

    @DELETE("course/{id}")
    suspend fun deleteCourse(@Path("id") id: Int)

    @GET("coursedata/{idcourse}")
    suspend fun getCourseData(@Path("idcourse") idcourse: Int): CourseData

    @POST("coursehole/save")
    suspend fun saveCoursehole(@Body hole: Any): Any

    // Email
    @POST("email/group")
    suspend fun sendGroupEmail(@Body request: GroupEmailRequest)

    @GET("email/schedule/{year}/{week}")
    suspend fun sendScheduleEmail(@Path("year") year: Int, @Path("week") week: Int)
}
