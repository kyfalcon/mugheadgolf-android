package com.mugheadgolf.app.screens.scores

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mugheadgolf.app.data.api.ApiClient
import com.mugheadgolf.app.data.models.CourseData
import com.mugheadgolf.app.data.models.ScoreData
import com.mugheadgolf.app.data.models.ScoreHole
import com.mugheadgolf.app.viewmodels.SessionViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Score calculation logic mirroring useScoreCalc.ts
object ScoreCalc {
    fun calculateHCHoles(strokes1: Int, strokes2: Int, coursedata: CourseData, useBack: Boolean): Pair<IntArray, IntArray> {
        val r1 = strokes1 % 9; val r2 = strokes2 % 9
        val hcHole1 = IntArray(9); val hcHole2 = IntArray(9)
        val idx = if (useBack) 9 else 0
        for (i in 0 until 9) {
            val hc = coursedata.courseholes?.getOrNull(i + idx)?.handicap ?: 0
            hcHole1[i] = if (r1 >= hc) 1 else 0
            hcHole2[i] = if (r2 >= hc) 1 else 0
        }
        return Pair(hcHole1, hcHole2)
    }

    fun calculateNetHoles(hc1: Int, hc2: Int, coursedata: CourseData, useBack: Boolean): Pair<IntArray, IntArray> {
        val xs1 = hc1 / 9; val xs2 = hc2 / 9
        val net1 = IntArray(9) { -xs1 }; val net2 = IntArray(9) { -xs2 }
        val R1 = hc1 % 9; val R2 = hc2 % 9
        val idx = if (useBack) 9 else 0
        for (i in 0 until 9) {
            val hc = coursedata.courseholes?.getOrNull(i + idx)?.handicap ?: 0
            if (R1 >= hc) net1[i] -= 1
            if (R2 >= hc) net2[i] -= 1
        }
        return Pair(net1, net2)
    }

    fun calculatePoint(hole: Int, holes1: Array<ScoreHole>, holes2: Array<ScoreHole>, xtraStroke1: Int, xtraStroke2: Int, hcHole1: IntArray, hcHole2: IntArray): Triple<Double, Double, Int> {
        val s1 = holes1.getOrNull(hole)?.score; val s2 = holes2.getOrNull(hole)?.score
        if (s1 != null && s2 != null && s1 != 0 && s2 != 0) {
            val a1 = s1 - (xtraStroke1 + hcHole1[hole])
            val a2 = s2 - (xtraStroke2 + hcHole2[hole])
            return when {
                a1 == a2 -> Triple(0.5, 0.5, 0)
                a1 > a2 -> Triple(0.0, 1.0, -1)
                else -> Triple(1.0, 0.0, 1)
            }
        }
        return Triple(0.0, 0.0, 0)
    }

    fun calculateScoreType(score: Int, par: Int): Int {
        var t = (score + 4) - par
        if (t > 7) t = 7
        return t
    }

    fun updateScoreTotal(holes: Array<ScoreHole>, isBye: Boolean, isAbsent: Boolean): Pair<Int, Boolean> {
        var total = 0; var canSave = true
        for (h in holes) {
            if (h.score != null) total += h.score
            else if (!isBye && !isAbsent) canSave = false
        }
        return Pair(total, canSave)
    }

    fun updateNetTotal(holes: Array<ScoreHole>): Int =
        holes.sumOf { if (it.score != null && it.net != null) it.net else 0 }

    fun updatePointTotal(points: DoubleArray): Double = points.sum()
}

@Composable
fun MatchScreen(idschedule: Int, sessionViewModel: SessionViewModel) {
    val session by sessionViewModel.state.collectAsState()
    var loading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf("") }
    var scoredata by remember { mutableStateOf<List<ScoreData>>(emptyList()) }
    var coursedata by remember { mutableStateOf<CourseData?>(null) }
    var backnine by remember { mutableStateOf(false) }
    var holeStart by remember { mutableIntStateOf(0) }
    var name1 by remember { mutableStateOf("") }
    var name2 by remember { mutableStateOf("") }
    var hc1 by remember { mutableIntStateOf(0) }
    var hc2 by remember { mutableIntStateOf(0) }
    var isBye1 by remember { mutableStateOf(false) }
    var isBye2 by remember { mutableStateOf(false) }
    var isAbsent1 by remember { mutableStateOf(false) }
    var isAbsent2 by remember { mutableStateOf(false) }
    var useForHC1 by remember { mutableStateOf(true) }
    var useForHC2 by remember { mutableStateOf(true) }
    var strokes1 by remember { mutableIntStateOf(0) }
    var strokes2 by remember { mutableIntStateOf(0) }
    var xtraStroke1 by remember { mutableIntStateOf(0) }
    var xtraStroke2 by remember { mutableIntStateOf(0) }

    val holes1 = remember { Array(9) { i -> ScoreHole(hole = i + 1) } }
    val holes2 = remember { Array(9) { i -> ScoreHole(hole = i + 1) } }
    val holeScores1 = remember { mutableStateListOf<Int?>(*arrayOfNulls(9)) }
    val holeScores2 = remember { mutableStateListOf<Int?>(*arrayOfNulls(9)) }
    val hcHole1 = remember { IntArray(9) }
    val hcHole2 = remember { IntArray(9) }
    val net1 = remember { IntArray(9) }
    val net2 = remember { IntArray(9) }
    val points1 = remember { DoubleArray(10) }
    val points2 = remember { DoubleArray(10) }
    var total1 by remember { mutableIntStateOf(0) }
    var total2 by remember { mutableIntStateOf(0) }
    var pointTotal1 by remember { mutableDoubleStateOf(0.0) }
    var pointTotal2 by remember { mutableDoubleStateOf(0.0) }
    var canSave by remember { mutableStateOf(false) }
    var refreshTick by remember { mutableIntStateOf(0) }

    val scope = kotlinx.coroutines.rememberCoroutineScope()
    val api = ApiClient.service

    fun recalcAll(cd: CourseData) {
        val (hh1, hh2) = ScoreCalc.calculateHCHoles(strokes1, strokes2, cd, backnine)
        val (n1, n2) = ScoreCalc.calculateNetHoles(hc1, hc2, cd, backnine)
        hh1.copyInto(hcHole1); hh2.copyInto(hcHole2)
        n1.copyInto(net1); n2.copyInto(net2)
        for (i in 0 until 9) {
            val s1 = holeScores1[i]; val s2 = holeScores2[i]
            if (s1 != null) holes1[i] = holes1[i].copy(score = s1, net = s1 + net1[i])
            if (s2 != null) holes2[i] = holes2[i].copy(score = s2, net = s2 + net2[i])
            val (p1, p2, _) = ScoreCalc.calculatePoint(i, holes1, holes2, xtraStroke1, xtraStroke2, hcHole1, hcHole2)
            points1[i] = p1; points2[i] = p2
        }
        val (t1, cs) = ScoreCalc.updateScoreTotal(holes1, isBye1, isAbsent1)
        val (t2, _) = ScoreCalc.updateScoreTotal(holes2, isBye2, isAbsent2)
        total1 = t1; total2 = t2; canSave = cs
        val nt1 = ScoreCalc.updateNetTotal(holes1)
        val nt2 = ScoreCalc.updateNetTotal(holes2)
        if (t1 > 0 && t2 > 0) {
            points1[9] = when { nt1 > nt2 -> 0.0; nt1 < nt2 -> 1.0; else -> 0.5 }
            points2[9] = when { nt2 > nt1 -> 0.0; nt2 < nt1 -> 1.0; else -> 0.5 }
            pointTotal1 = ScoreCalc.updatePointTotal(points1)
            pointTotal2 = ScoreCalc.updatePointTotal(points2)
        }
    }

    fun populateFromScoredata(sd: List<ScoreData>) {
        if (sd.size < 2) return
        val sd0 = sd[0]; val sd1 = sd[1]
        val sch = sd0.score.idschedule as? com.mugheadgolf.app.data.models.Schedule ?: return
        backnine = sch.backnine; holeStart = if (backnine) 9 else 0
        useForHC1 = sd0.score.useforhandicap == 1; useForHC2 = sd1.score.useforhandicap == 1
        isAbsent1 = sch.absent1; isAbsent2 = sch.absent2
        isBye1 = sd0.score.idgolfer?.idgolfer == 0; isBye2 = sd1.score.idgolfer?.idgolfer == 0
        name1 = "${sch.idgolfer1?.firstname} ${sch.idgolfer1?.lastname}"
        name2 = "${sch.idgolfer2?.firstname} ${sch.idgolfer2?.lastname}"
        hc1 = hc1; hc2 = hc2
        strokes1 = maxOf(0, hc1 - hc2); strokes2 = maxOf(0, hc2 - hc1)
        xtraStroke1 = strokes1 / 9; xtraStroke2 = strokes2 / 9
        sd0.scoreholes?.forEachIndexed { i, sh -> if (i < 9) { holes1[i] = sh; holeScores1[i] = sh.score } }
        sd1.scoreholes?.forEachIndexed { i, sh -> if (i < 9) { holes2[i] = sh; holeScores2[i] = sh.score } }
    }

    suspend fun fetchScores() {
        try {
            val sch = api.getScheduleById(idschedule)
            val idg1 = sch.idgolfer1?.idgolfer ?: return
            val idg2 = sch.idgolfer2?.idgolfer ?: return
            val idCourse = (sch as? Any)?.let {
                val f = it.javaClass.getDeclaredField("idcourse")
                f.isAccessible = true
                (f.get(it) as? Map<*, *>)?.get("idcourse") as? Int
            } ?: 0
            val sd0 = api.getMatchScore(idschedule, idg1)
            val sd1 = api.getMatchScore(idschedule, idg2)
            scoredata = listOf(sd0, sd1)
            hc1 = sch.idgolfer1.currenthandicap.toInt()
            hc2 = sch.idgolfer2?.currenthandicap?.toInt() ?: 0
            populateFromScoredata(scoredata)
            val cd = if (idCourse != 0) try { api.getCourseData(idCourse) } catch (_: Exception) { null } else null
            coursedata = cd
            if (cd != null) recalcAll(cd)
        } catch (e: Exception) {
            errorMsg = e.message ?: "Error loading match."
        } finally { loading = false }
    }

    LaunchedEffect(idschedule) {
        fetchScores()
        // Auto-refresh every 10 seconds if enabled
        while (true) {
            delay(10000)
            if (session.autoRefresh) fetchScores()
        }
    }

    if (loading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    if (errorMsg.isNotEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Text(errorMsg, color = MaterialTheme.colorScheme.error)
        }
        return
    }

    val cd = coursedata
    val canEdit = session.isAdmin || (scoredata.isNotEmpty() && (
        scoredata[0].score.idgolfer?.idgolfer == session.idgolfer ||
        scoredata[1].score.idgolfer?.idgolfer == session.idgolfer
    ))

    Column(modifier = Modifier.fillMaxSize().padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Header
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("($hc1) $name1\n${total1} pts: ${"%.1f".format(pointTotal1)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            Text("($hc2) $name2\n${total2} pts: ${"%.1f".format(pointTotal2)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary, textAlign = TextAlign.End)
        }

        // Controls
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = backnine, onCheckedChange = { backnine = it; holeStart = if (it) 9 else 0; if (cd != null) recalcAll(cd) })
                Text("Back 9", fontSize = 13.sp)
            }
            if (!isBye1) Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = useForHC1, onCheckedChange = { useForHC1 = it })
                Text("HC1", fontSize = 12.sp)
            }
            if (!isBye2) Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = useForHC2, onCheckedChange = { useForHC2 = it })
                Text("HC2", fontSize = 12.sp)
            }
        }

        if (canEdit && canSave) {
            Button(onClick = {
                scope.launch {
                    try {
                        api.saveScores(true, listOf(scoredata[0].score, scoredata[1].score))
                    } catch (_: Exception) {}
                }
            }, modifier = Modifier.fillMaxWidth()) { Text("Save Final") }
        }

        // Scorecard table
        val holeNums = (1..9).map { it + holeStart }
        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            // Row labels
            Column {
                ScorecardCell("Hole", isHeader = true)
                if (cd != null) { ScorecardCell("HC"); ScorecardCell("Par") }
                if (!isBye1) ScorecardCell(name1.split(" ").first())
                if (!isBye2) ScorecardCell(name2.split(" ").first())
                ScorecardCell("Pts1")
                ScorecardCell("Pts2")
            }
            // Hole columns
            for (i in 0 until 9) {
                Column {
                    ScorecardCell("${holeNums[i]}", isHeader = true)
                    if (cd != null) {
                        val ch = cd.courseholes?.getOrNull(i + holeStart)
                        ScorecardCell("${ch?.handicap ?: ""}")
                        ScorecardCell("${ch?.par ?: ""}")
                    }
                    if (!isBye1) {
                        var txt by remember { mutableStateOf(holeScores1[i]?.toString() ?: "") }
                        ScorecardInput(txt, onValueChange = { s ->
                            txt = s
                            val v = s.toIntOrNull()
                            holeScores1[i] = v
                            holes1[i] = holes1[i].copy(score = v, net = v?.let { it + net1[i] })
                            if (v != null && cd != null) {
                                holes1[i] = holes1[i].copy(type = ScoreCalc.calculateScoreType(v, cd.courseholes?.getOrNull(i + holeStart)?.par ?: 4))
                                recalcAll(cd)
                                scope.launch {
                                    try { api.saveScoreholeWithPar(cd.courseholes?.getOrNull(i + holeStart)?.par ?: 4, holes1[i]) } catch (_: Exception) {}
                                }
                            }
                        }, win = points1[i] == 1.0, tie = points1[i] == 0.5, lose = points1[i] == 0.0 && holeScores1[i] != null)
                    }
                    if (!isBye2) {
                        var txt by remember { mutableStateOf(holeScores2[i]?.toString() ?: "") }
                        ScorecardInput(txt, onValueChange = { s ->
                            txt = s
                            val v = s.toIntOrNull()
                            holeScores2[i] = v
                            holes2[i] = holes2[i].copy(score = v, net = v?.let { it + net2[i] })
                            if (v != null && cd != null) {
                                holes2[i] = holes2[i].copy(type = ScoreCalc.calculateScoreType(v, cd.courseholes?.getOrNull(i + holeStart)?.par ?: 4))
                                recalcAll(cd)
                                scope.launch {
                                    try { api.saveScoreholeWithPar(cd.courseholes?.getOrNull(i + holeStart)?.par ?: 4, holes2[i]) } catch (_: Exception) {}
                                }
                            }
                        }, win = points2[i] == 1.0, tie = points2[i] == 0.5, lose = points2[i] == 0.0 && holeScores2[i] != null)
                    }
                    ScorecardCell("${"%.1f".format(points1[i])}", win = points1[i] == 1.0)
                    ScorecardCell("${"%.1f".format(points2[i])}", win = points2[i] == 1.0)
                }
            }
            // Total column
            Column {
                ScorecardCell("Total", isHeader = true)
                if (cd != null) { ScorecardCell(""); ScorecardCell("") }
                if (!isBye1) ScorecardCell("$total1", win = pointTotal1 > pointTotal2)
                if (!isBye2) ScorecardCell("$total2", win = pointTotal2 > pointTotal1)
                ScorecardCell("${"%.1f".format(pointTotal1)}", win = pointTotal1 > pointTotal2)
                ScorecardCell("${"%.1f".format(pointTotal2)}", win = pointTotal2 > pointTotal1)
            }
        }
    }
}

@Composable
fun ScorecardCell(text: String, isHeader: Boolean = false, win: Boolean = false, tie: Boolean = false, lose: Boolean = false) {
    val bg = when {
        isHeader -> Color(0xFF005E2C)
        win -> Color(0xFFB2DFDB)
        tie -> Color(0xFFFFF9C4)
        else -> Color.Transparent
    }
    val fg = if (isHeader) Color.White else Color.Black
    Box(
        modifier = Modifier.width(52.dp).height(36.dp).background(bg).border(0.5.dp, Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = fg, fontSize = 12.sp, textAlign = TextAlign.Center)
    }
}

@Composable
fun ScorecardInput(value: String, onValueChange: (String) -> Unit, win: Boolean = false, tie: Boolean = false, lose: Boolean = false) {
    val bg = when { win -> Color(0xFFB2DFDB); tie -> Color(0xFFFFF9C4); else -> Color.Transparent }
    Box(modifier = Modifier.width(52.dp).height(44.dp).background(bg).border(0.5.dp, Color.LightGray), contentAlignment = Alignment.Center) {
        OutlinedTextField(
            value = value,
            onValueChange = { if (it.length <= 2) onValueChange(it) },
            modifier = Modifier.size(48.dp, 40.dp),
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, textAlign = TextAlign.Center),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Transparent)
        )
    }
}
