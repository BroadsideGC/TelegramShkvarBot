package tournaments

import java.util.*

typealias Team = Pair<String, String>

fun generateBrackets(names: List<String>): Bracket {
    val n = names.toMutableList()
    var p = 3
    while ((1 shl p) < n.size) ++p
    var botN = 0
    while (n.size < (1 shl p)) n.add("bot ${++botN}")
    n.shuffle()
    val players = ArrayDeque(n)
    // classic
    val base = ArrayDeque<Bracket>()
    var group = 'A'
    while (players.isNotEmpty()) {
        base.add(
            ReadyBracket(
                Pair(players.remove(), players.remove()),
                Pair(players.remove(), players.remove()),
                "Match ${group++}"
            )
        )
    }
    var nextW = ArrayDeque<Bracket>()
    var nextL = ArrayDeque<Bracket>()
    while (base.isNotEmpty()) {
        val top = base.remove()
        val bottom = base.remove()
        nextW.add(PendingBracket(top, bottom, "Match ${group++}"))
        nextL.add(PendingBracket(top, bottom, "Match ${group++}", false, false))
    }
    while (nextW.size != 1) {
        assert(nextL.size == nextW.size) { "$nextW != $nextL" }
        // dropdown
        val tmpL = ArrayDeque<Bracket>()
        val tmpW = ArrayDeque<Bracket>()
        while (nextW.isNotEmpty()) {
            val topW = nextW.remove()
            val bottomW = nextW.remove()
            val topL = nextL.remove()
            val bottomL = nextL.remove()
            val comingTopL = PendingBracket(topW, topL, "Match ${group++}", false, true)
            val comingBottomL = PendingBracket(bottomW, bottomL, "Match ${group++}", false, true)
            tmpW.add(PendingBracket(topW, bottomW, "Match ${group++}"))
            tmpL.add(PendingBracket(comingTopL, comingBottomL, "Match ${group++}", true, true))
        }
        nextW = tmpW
        nextL = tmpL
    }
    val winnersFinal = nextW.remove()
    val losersFinal = nextL.remove()
    return PendingBracket(
        winnersFinal,
        PendingBracket(winnersFinal, losersFinal, "Match $group", false, true),
        "GRAND FINAL"
    )
}

fun Bracket.flatten(): List<Bracket> = when {
    ready -> listOf(this)
    this is PendingBracket -> topFrom.flatten() + bottomFrom.flatten()
    else -> throw AssertionError("unreachable")
}

fun Bracket.pretty(): String = "\t${this.name}\n${this.top.first} & ${this.top.second}\n${this.bottom.first} & ${this.bottom.second}"

enum class BracketResult {
    NOT_PLAYED,
    WINNER_TOP,
    WINNER_BOTTOM
}

interface Bracket {
    val top: Team
    val bottom: Team
    var result: BracketResult
    val name: String
    val ready: Boolean
}

class ReadyBracket(
    override val top: Team,
    override val bottom: Team,
    override val name: String,
    override var result: BracketResult = BracketResult.NOT_PLAYED
) : Bracket {
    override val ready: Boolean
        get() = true
}

class PendingBracket(
    val topFrom: Bracket,
    val bottomFrom: Bracket,
    override val name: String,
    val topWinner: Boolean = true,
    val bottomWinner: Boolean = true
) : Bracket {
    override val top: Team
        get() = player(topFrom, topWinner)
    override val bottom: Team
        get() = player(bottomFrom, bottomWinner)
    override var result: BracketResult = BracketResult.NOT_PLAYED
    override val ready: Boolean
        get() = topFrom.result != BracketResult.NOT_PLAYED && bottomFrom.result != BracketResult.NOT_PLAYED

    private fun player(from: Bracket, win: Boolean) = when (from.result) {
        BracketResult.WINNER_TOP -> from.top
        BracketResult.WINNER_BOTTOM -> from.bottom
        BracketResult.NOT_PLAYED -> Pair(if (win) "(winners" else "(losers", "${from.name})")
    }
}