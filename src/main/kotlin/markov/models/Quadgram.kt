package markov.models

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object Quadgrams : IntIdTable() {
    val word1 = text("word1")
    val word2 = text("word2")
    val word3 = text("word3")
    val nextWord = text("next_word")
    val count = integer("count").default(1)

    init {
        Quadgrams.index(
            false,
            word1,
            word2,
            word3,
            nextWord
        )
        Quadgrams.index(
            false,
            word1,
            word2,
            word3
        )
    }
}

class Quadgram(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Quadgram>(Quadgrams)

    var word1 by Quadgrams.word1
    var word2 by Quadgrams.word2
    var word3 by Quadgrams.word3
    var nextWord by Quadgrams.nextWord
    var count by Quadgrams.count
}
