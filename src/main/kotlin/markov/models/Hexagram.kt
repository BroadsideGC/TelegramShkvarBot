package markov.models

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object Hexagrams : IntIdTable() {
    val word1 = text("word1")
    val word2 = text("word2")
    val word3 = text("word3")
    val word4 = text("word4")
    val word5 = text("word5")
    val nextWord = text("next_word")
    val count = integer("count").default(1)

    init {
        Hexagrams.index(
            false,
            word1,
            word2,
            word3,
            word4,
            word5,
            nextWord
        )
        Hexagrams.index(
            false,
            word1,
            word2,
            word3,
            word4,
            word5
        )
    }
}

class Hexagram(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Hexagram>(Hexagrams)

    var word1 by Hexagrams.word1
    var word2 by Hexagrams.word2
    var word3 by Hexagrams.word3
    var word4 by Hexagrams.word4
    var word5 by Hexagrams.word5
    var nextWord by Hexagrams.nextWord
    var count by Hexagrams.count
}