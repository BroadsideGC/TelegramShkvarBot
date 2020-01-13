package markov.models

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object Bigrams : IntIdTable() {
    val word1 = text("word1")
    val nextWord = text("next_word")
    val count = integer("count").default(1)

    init {
        index(false, word1, nextWord)
        index(false, word1)
    }
}

class Bigram(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Bigram>(Bigrams)

    var word1 by Bigrams.word1
    var nextWord by Bigrams.nextWord
    var count by Bigrams.count
}
