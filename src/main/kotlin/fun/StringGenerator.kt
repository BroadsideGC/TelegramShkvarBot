package `fun`

import kotlin.math.roundToInt
import kotlin.random.Random

object StringGenerator {
    private const val vowels = "euioa"
    private const val consonants = "wrtpsdfgklzcvbnm"
    private const val strange = "yhqjx"

    private val random = Random.Default

    fun randomWord(): String = Array(random.nextInt(1, 3) + random.nextInt(0, 3)) {
        randomSyllable()
    }.joinToString(separator = "") { it }

    internal fun randomSyllable(): String {
        val res = CharArray(random.nextInt(1, 3) + random.nextInt(2) + random.nextInt(2)) {
            consonants[random.nextInt(consonants.length)]
        }
        if (random.nextInt(strange.length * 3 - res.size) == 0)
            res[random.nextInt(res.size)] = strange[random.nextInt(strange.length)]
        res[((random.nextInt(res.size) + random.nextInt(res.size) + random.nextInt(res.size)).toDouble() / 3.0).roundToInt()] =
            vowels[random.nextInt(vowels.length)]
        return String(res)
    }
}