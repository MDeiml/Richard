package com.mdeiml.richard

import org.apache.commons.math3.linear.RealVector

internal val POINT_NAMES = arrayOf("0", "15", "30", "40", "A")

fun Int.toPlayer() = when (this) {
    1 -> Match.Player.PLAYER_A
    2 -> Match.Player.PLAYER_B
    else -> null
}

class Match(
        player1: String?,
        player2: String?,
        p1: Double,
        p2: Double,
        startTime: Long,
        numSets: Int,
        matchTiebreak: Boolean,
        matchId: Long?) {

    enum class Tiebreak {
        NO_TIEBREAK, SET_TIEBREAK, MATCH_TIEBREAK
    }

    enum class Player {
        PLAYER_A {
            override fun invert() = PLAYER_B
            override fun toInt() = 1
        },
        PLAYER_B {
            override fun invert() = PLAYER_A
            override fun toInt() = 2
        };

        abstract fun invert(): Player
        abstract fun toInt(): Int
    }

    var matchId: Long? = matchId

    val matrixSet = MatrixSet(p1, p2, numSets, matchTiebreak)
    val numSets = numSets
    val matchTiebreak = matchTiebreak
    val startTime = startTime
    val sets = arrayListOf(Set(matrixSet, false, Player.PLAYER_A))
    val player1 = player1
    val player2 = player2
    var p1 = p1
        set(p1: Double) {
            field = p1
            matrixSet.updateProb(p1, p2)
        }
    var p2 = p2
        set(p2: Double) {
            field = p1
            matrixSet.updateProb(p1, p2)
        }

    private var _winner: Player? = null
    var winner: Player?
        set(winner: Player?) {
            _winner = winner
        }
        get() {
            if (_winner != null) {
                return _winner
            }
            val (s1, s2) = this.totalSets
            if (s1 == numSets / 2 + 1) {
                _winner = Player.PLAYER_A
            } else if(s2 == numSets / 2 + 1) {
                _winner = Player.PLAYER_B
            }
            return _winner
        }

    val currentSetNr
        get() = sets.size - 1

    val servePoint
        get() = currentSet.currentGame.currentPoint.server == Player.PLAYER_A

    val gameScore: List<Pair<Int, Int>>
        get() = sets.map { it.totalGames }

    val currentSet
        get() = sets[currentSetNr]

    val breaks: Pair<Int, Int>
        get() = sets.fold(Pair(0, 0)) { acc: Pair<Int, Int>, set: Set ->
                val (x1, x2) = set.breaks
                val (b1, b2) = acc
                return Pair(b1 + x1, b2 + x2)
            }

    val totalPoints: List<Int>
        get() = sets.fold(listOf(0, 0, 0, 0, 0, 0)) { acc: List<Int>, set: Set ->
            val p = set.totalPoints
            return acc.zip(p) { x, y -> x + y }
        }

    val totalSets: Pair<Int, Int>
        get() = sets.fold(Pair(0, 0)) { acc: Pair<Int, Int>, set: Set ->
            val (s1, s2) = acc
            return when (set.winner) {
                null -> Pair(s1, s2)
                Player.PLAYER_A -> Pair(s1 + 1, s2)
                Player.PLAYER_B -> Pair(s1, s2 + 1)
            }
        }

    val winProb
        get() = currentSet.currentGame.currentPoint.winProb

    val importance
        get() = currentSet.currentGame.currentPoint.importance

    init {
        val p = currentSet.currentGame.currentPoint
        p.winProb = calcWinProb()
        p.importance = calcImportance()
    }

    constructor (player1: String?, player2: String?, p1: Double, p2: Double) : this(player1, player2, p1, p2, System.currentTimeMillis(), 3, true, null) {}


    fun point(player: Player) = point(player, true)

    fun point(player: Player, calcStatistics: Boolean): Player? {
        var w1 = this.winner
        if(w1 != null) {
            return w1
        }
        val w = this.currentSet.point(player)
        if(w != null) {
            w1 = this.winner
            if (w1 == null) {
                val (s1, s2) = this.totalSets
                val t = matchTiebreak && s1 == numSets / 2 && s2 == numSets / 2
                val server = currentSet.currentGame.currentPoint.server.invert()
                sets.add(Set(matrixSet, t, server))
            }
        }
        if(calcStatistics) {
            val p = currentSet.currentGame.currentPoint
            p.winProb = calcWinProb()
            p.importance = calcImportance()
        }
        return w1
    }

    fun resetWinner() {
        _winner = null
        currentSet.resetWinner()
    }

    fun removePoint(): Boolean {
        if(currentSet.currentGame.currentPoint === sets[0].games[0].points[0]) {
            return false
        }
        if (_winner != null) {
            resetWinner()
        } else if (currentSet.removePoint()) {
            sets.removeAt(sets.size - 1)
            currentSet.resetWinner()
        }
        return true
    }

    private fun calcWinProb(): Float {
        val w = this.winner
        when (w) {
            Player.PLAYER_A -> return 1.0f
            Player.PLAYER_B -> return 0.0f
            else -> {
                val (s1, s2) = this.totalSets
                val pm1 = if (s1 == 1) 1.0 else matrixSet.matM.getEntry(MarkovMatrix.getMatchMatrixIndex(s1+1, s2, numSets))
                val pm2 = if (s2 == 1) 0.0 else matrixSet.matM.getEntry(MarkovMatrix.getMatchMatrixIndex(s1, s2+1, numSets))
                val ps = currentSet.winProb
                return (ps * pm1 + (1 - ps) * pm2).toFloat()
            }
        }
    }

    private fun calcImportance(): Float {
        if(_winner != null) {
            return 0.0f
        }
        point(Player.PLAYER_A, false)
        val p1 = calcWinProb()
        removePoint()
        point(Player.PLAYER_B, false)
        val p2 = calcWinProb()
        removePoint()
        return p1 - p2
    }

    class Set(val matrixSet: MatrixSet, val tiebreak: Boolean) {

        private var _winner: Player? = null
        val games = ArrayList<Game>()

        val currentGame
            get() = games[games.size - 1]

        var winner: Player?
            set(winner: Player?) {
                _winner = winner
            }
            get() {
                if(_winner != null) {
                    return _winner
                }
                if(tiebreak) {
                    _winner = games[0].winner
                } else {
                    val (g1, g2) = this.totalGames
                    if(g1 == 7 || (g1 == 6 && g2 <= 4)) {
                        _winner = Player.PLAYER_A
                    }else if(g2 == 7 || (g2 == 6 && g1 <= 4)) {
                        _winner = Player.PLAYER_B
                    }
                }
                return _winner
            }

        val breaks: Pair<Int, Int>
            get() = games.filter {it.isBreak()}.fold(Pair(0, 0)) { acc: Pair<Int, Int>, game: Game ->
                val (b1, b2) = acc
                return when (game.winner) {
                    null -> Pair(b1, b2)
                    Player.PLAYER_A -> Pair(b1 + 1, b2)
                    Player.PLAYER_B -> Pair(b1, b2 + 1)
                }
            }

        val totalPoints: List<Int>
            get() = games.fold(listOf(0, 0, 0, 0, 0, 0)) { acc: List<Int>, game: Game ->
                val p = game.totalPoints
                return acc.zip(p) { x, y -> x + y }
            }

        val totalGames: Pair<Int, Int>
            get() = games.fold(Pair(0, 0)) { acc: Pair<Int, Int>, game: Game ->
                val (g1, g2) = acc
                when (game.winner) {
                    null -> acc
                    Player.PLAYER_A -> Pair(g1 + 1, g2)
                    Player.PLAYER_B -> Pair(g1, g2 + 1)
                }
            }

        constructor(matrixSet: MatrixSet, tiebreak: Boolean, server: Player) : this(matrixSet, tiebreak) {
            games.add(Game(matrixSet, if (tiebreak) Tiebreak.MATCH_TIEBREAK else Tiebreak.NO_TIEBREAK, server))
        }

        fun point(player: Player): Player? {
            val w = currentGame.point(player)
            if (w != null) {
                val w1 = this.winner
                if(w1 == null) {
                    val (g1, g2) = this.totalGames
                    val t = g1 == 6 && g2 == 6
                    val server = currentGame.currentPoint.server.invert()
                    games.add(Game(matrixSet, if (t) Tiebreak.SET_TIEBREAK else Tiebreak.NO_TIEBREAK, server))
                }
                return w1
            }
            return null
        }

        internal fun resetWinner() {
            _winner = null
            currentGame.resetWinner()
        }

        fun removePoint(): Boolean {
            if(currentGame.removePoint()) {
                games.removeAt(games.size - 1)
                if(games.isEmpty()) {
                    return true
                }else {
                    resetWinner()
                    return false
                }
            }
            return false
        }

        val winProb: Double
            get() {
                val w = this.winner
                when (w) {
                    Player.PLAYER_A -> return 1.0
                    Player.PLAYER_B -> return 0.0
                    null -> {
                        if(tiebreak) {
                            return currentGame.winProb
                        } else {
                            val server = games[0].points[0].server
                            val ms = when (server) {
                                Player.PLAYER_A -> matrixSet.matS1
                                Player.PLAYER_B -> matrixSet.matS2
                            }
                            val (g1, g2) = this.totalGames
                            val (si, sj) = when (server) {
                                Player.PLAYER_A -> Pair(g1, g2)
                                Player.PLAYER_B -> Pair(g2, g1)
                            }
                            val psi = if (si == 6 || (si == 5 && sj < 5)) 1.0 else ms.getEntry(MarkovMatrix.getSetMatrixIndex(si+1, sj))
                            val psj = if (sj == 6 || (sj == 5 && si < 5)) 0.0 else ms.getEntry(MarkovMatrix.getSetMatrixIndex(si, sj+1))
                            val (ps1, ps2) = when (server) {
                                Player.PLAYER_A -> Pair(psi, psj)
                                Player.PLAYER_B -> Pair(1 - psj, 1 - psi)
                            }
                            val pg = currentGame.winProb
                            return pg * ps1 + (1 - pg) * ps2
                        }
                    }
                }
            }

    }

    class Game(val matrixSet: MatrixSet, val tiebreak: Tiebreak) {

        private var _winner: Player? = null
        val points = ArrayList<Point>()

        val currentPoint
            get() = points[points.size - 1]

        var winner: Player?
            set(winner: Player?) {
                _winner = winner
            }
            get() {
                if (_winner != null) {
                    return _winner
                }

                val (p1, p2) = this.totalPoints

                if(Math.abs(p1 - p2) >= 2) {
                    val minWinPoints = when (tiebreak) {
                        Tiebreak.NO_TIEBREAK -> 4
                        Tiebreak.SET_TIEBREAK -> 7
                        Tiebreak.MATCH_TIEBREAK -> 10
                    }
                    if(p1 >= minWinPoints || p2 >= minWinPoints) {
                        if(p1 > p2) {
                            _winner = Player.PLAYER_A
                        }else {
                            _winner = Player.PLAYER_B
                        }
                    }
                }
                return _winner
            }

        val totalPoints: List<Int>
            get() {
                val (p1, p2) = points.fold(Pair(Pair(0, 0), Pair(0, 0))) fold@{ acc: Pair<Pair<Int, Int>, Pair<Int, Int>>, point: Point ->
                    val (p1, p2) = acc
                    val (p1s, p1r) = p1
                    val (p2s, p2r) = p2
                    return@fold when (Pair(point.winner, point.server)) {
                        Pair(Player.PLAYER_A, Player.PLAYER_A) -> Pair(Pair(p1s + 1, p1r), p2)
                        Pair(Player.PLAYER_A, Player.PLAYER_B) -> Pair(Pair(p1s, p1r + 1), p2)
                        Pair(Player.PLAYER_B, Player.PLAYER_A) -> Pair(p1, Pair(p2s, p2r + 1))
                        Pair(Player.PLAYER_B, Player.PLAYER_B) -> Pair(p1, Pair(p2s + 1, p2r))
                        else -> acc
                    }
                }
                val (p1s, p1r) = p1
                val (p2s, p2r) = p2
                return listOf(p1s + p1r, p2s + p2r, p1s, p1r, p2s, p2r)
            }

        val stringScores: Pair<String, String>
            get() {
                val totalPoints = this.totalPoints
                var t1 = totalPoints[0]
                var t2 = totalPoints[1]
                if (tiebreak == Tiebreak.NO_TIEBREAK) {
                    if (t1 >= 4 && t2 >= 4) {
                        val pointsMin = Math.min(t1, t2)
                        t1 += (3 - pointsMin)
                        t2 += (3 - pointsMin)
                    }
                    return Pair(POINT_NAMES[t1], POINT_NAMES[t2])
                } else {
                    return Pair(t1.toString(), t2.toString())
                }
            }

        val winProb: Double
            get() {
                val w = this.winner
                when (w) {
                    Player.PLAYER_A -> return 1.0
                    Player.PLAYER_B -> return 0.0
                    null -> {
                        val server = points[0].server
                        val totalPoints = this.totalPoints
                        val (si, sj) = when (server) {
                            Player.PLAYER_A -> Pair(totalPoints[0], totalPoints[1])
                            Player.PLAYER_B -> Pair(totalPoints[1], totalPoints[0])
                        }
                        val mg = when (tiebreak) {
                            Tiebreak.NO_TIEBREAK -> when (server) {
                                Player.PLAYER_A -> matrixSet.matG1
                                Player.PLAYER_B -> matrixSet.matG2
                            }
                            Tiebreak.SET_TIEBREAK -> when (server) {
                                Player.PLAYER_A -> matrixSet.matT1
                                Player.PLAYER_B -> matrixSet.matT2
                            }
                            Tiebreak.MATCH_TIEBREAK -> when (server) {
                                Player.PLAYER_A -> matrixSet.matMT1
                                Player.PLAYER_B -> matrixSet.matMT2
                            }
                        }

                        val index = when (tiebreak) {
                            Tiebreak.NO_TIEBREAK -> MarkovMatrix.getGameMatrixIndex(si, sj)
                            Tiebreak.SET_TIEBREAK -> MarkovMatrix.getSetTiebreakMatrixIndex(si, sj)
                            Tiebreak.MATCH_TIEBREAK -> MarkovMatrix.getMatchTiebreakMatrixIndex(si, sj)
                        }
                        val pgi = mg.getEntry(index)
                        return when (server) {
                            Player.PLAYER_A -> pgi
                            Player.PLAYER_B -> 1 - pgi
                        }
                    }
                }
            }

        constructor(matrixSet: MatrixSet, tiebreak: Tiebreak, server: Player) : this(matrixSet, tiebreak) {
            points.add(Point(server))
        }

        fun isBreak() = tiebreak == Tiebreak.NO_TIEBREAK && (this.winner?.let { it != points[0].server } ?: false)

        fun point(player: Player): Player? {
            currentPoint.winner = player
            val totalPoints = this.totalPoints
            val server = when (tiebreak) {
                Tiebreak.NO_TIEBREAK -> currentPoint.server
                else -> if ((totalPoints[0] + totalPoints[1]) % 2 == 1) {
                    currentPoint.server.invert()
                } else currentPoint.server
            }
            val w = this.winner
            if(w == null) {
                points.add(Point(server))
            }
            return w
        }

        fun resetWinner() {
            _winner = null
            currentPoint.winner = null
        }

        fun removePoint(): Boolean {
            points.removeAt(points.size - 1)
            if(points.isEmpty()) {
                return true
            } else {
                resetWinner()
                return false
            }
        }

    }

    data class Point(
            val server: Player,
            var winner: Player? = null,
            var winProb: Float = 0.0f,
            var importance: Float = 0.0f
        )

    class MatrixSet(p1: Double, p2: Double, val numSets: Int, val matchTiebreak: Boolean) {
        private lateinit var _matG1: Lazy<RealVector>
        val matG1: RealVector
            get() = _matG1.value

        private lateinit var _matG2: Lazy<RealVector>
        val matG2: RealVector
            get() = _matG2.value

        private lateinit var _matMT1: Lazy<RealVector>
        val matMT1: RealVector
            get() = _matMT1.value

        private lateinit var _matMT2: Lazy<RealVector>
        val matMT2: RealVector
            get() = _matMT2.value

        private lateinit var _matT1: Lazy<RealVector>
        val matT1: RealVector
            get() = _matT1.value

        private lateinit var _matT2: Lazy<RealVector>
        val matT2: RealVector
            get() = _matT2.value

        private lateinit var _matS1: Lazy<RealVector>
        val matS1: RealVector
            get() = _matS1.value

        private lateinit var _matS2: Lazy<RealVector>
        val matS2: RealVector
            get() = _matS2.value

        private lateinit var _matM: Lazy<RealVector>
        val matM: RealVector
            get() = _matM.value

        init {
            updateProb(p1, p2)
        }

        fun updateProb(p1: Double, p2: Double) {
            _matG1 = lazy { MarkovMatrix.getGamePropabilities(p1) }
            _matG2 = lazy { MarkovMatrix.getGamePropabilities(p2) }
            _matMT1 = lazy { MarkovMatrix.getMatchTiebreakPropabilities(p1, p2) }
            _matMT2 = lazy { MarkovMatrix.getMatchTiebreakPropabilities(p2, p1) }
            _matT1 = lazy { MarkovMatrix.getSetTiebreakPropabilities(p1, p2) }
            _matT2 = lazy { MarkovMatrix.getSetTiebreakPropabilities(p2, p1) }
            _matS1 = lazy { MarkovMatrix.getSetPropabilities(matG1.getEntry(0), matG2.getEntry(0), matT1.getEntry(0)) }
            _matS2 = lazy { MarkovMatrix.getSetPropabilities(matG2.getEntry(0), matG1.getEntry(0), matT1.getEntry(0)) }
            _matM = lazy { MarkovMatrix.getMatchPropabilities(matS1.getEntry(0), matMT1.getEntry(0), numSets, matchTiebreak) }
        }
    }

}
