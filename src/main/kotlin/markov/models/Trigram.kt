package markov.models

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object Trigrams : IntIdTable() {
    val word1 = text("word1").index()
    val word2 = text("word2")
    val nextWord = text("next_word")
    val count = integer("count").default(1)

    init {
        Trigrams.index(
            false,
            word1,
            word2,
            nextWord
        )
        Trigrams.index(
            false,
            word1,
            word2
        )
    }
}

class Trigram(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Trigram>(Trigrams)

    var word1 by Trigrams.word1
    var word2 by Trigrams.word2
    var nextWord by Trigrams.nextWord
    var count by Trigrams.count
}
