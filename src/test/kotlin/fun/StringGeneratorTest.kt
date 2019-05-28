package `fun`

import `fun`.StringGenerator.randomSyllable
import `fun`.StringGenerator.randomWord
import org.junit.Test

import org.junit.Assert.*

class StringGeneratorTest {

    @Test
    fun randomWords() {
        println("\tWords:")
        for (i in 1..16)
            println(randomWord())
    }

    @Test
    fun randomSyllables() {
        println("\tSyllables:")
        for (i in 1..16)
            println(randomSyllable())
    }
}