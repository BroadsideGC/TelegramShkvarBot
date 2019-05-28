package `fun`

import kotlin.random.Random

object StringGenerator {
    private const val vowels = "euioa"
    private const val consonants = "qwrtpsdfgjklzxcvbnm"
    private const val strange = "yh"

    private val random = Random.Default

    fun randomWord(): String = Array(random.nextInt(1, 3)) {
        randomSyllable()
    }.joinToString { it }

    private fun randomSyllable(): String {
        val res = CharArray(random.nextInt(1, 4)) {
            consonants[random.nextInt(consonants.length)]
        }
        res[Math.round(Math.sqrt((random.nextInt(res.size) * random.nextInt(res.size)).toDouble())).toInt()] =
            vowels[random.nextInt(vowels.length)]
        return String(res)
    }
}