package tournaments

import org.junit.Test

import kotlin.random.Random

class DoubleEliminationTournamentKtTest {

    @Test
    fun generateBrackets() {
        for (i in 0..32) {
            val bracket = generateBrackets(generateNames(i))
            pretty(bracket)
            println("==================")
        }
    }

    private fun pretty(bracket: Bracket): Unit = if (bracket is PendingBracket) {
        pretty(bracket.topFrom)
        pretty(bracket.bottomFrom)
        println("-----")
        println(bracket.name)
        println(bracket.top.toString())
        println(bracket.bottom.toString())
        println("-----")
    } else {
        println(bracket.name)
        println("${bracket.top.first} & ${bracket.top.second}")
        println("${bracket.bottom.first} & ${bracket.bottom.second}")
        println()
    }

    private fun generateNames(amount: Int) : List<String> {
        val rnd = Random(System.currentTimeMillis())
        val res = ArrayList<String>(amount)
        for (i in 0 until amount) {
            res.add(name(rnd))
        }
        return res
    }

    private fun name(r: Random) = String(charArrayOf(r.nextChar(), r.nextChar(), r.nextChar(), r.nextChar(), r.nextChar()))

    private fun Random.nextChar() = nextInt('a'.toInt(), 'z'.toInt()).toChar()
}