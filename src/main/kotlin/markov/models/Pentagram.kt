package markov.models

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object Pentagrams : IntIdTable() {
    val word1 = text("word1")
    val word2 = text("word2")
    val word3 = text("word3")
    val word4 = text("word4")
    val nextWord = text("next_word")
    val count = integer("count").default(1)

    init {
        Pentagrams.index(
            false,
            word1,
            word2,
            word3,
            word4,
            nextWord
        )
        Pentagrams.index(
            false,
            word1,
            word2,
            word3,
            word4
        )
    }
}

class Pentagram(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Pentagram>(Pentagrams)

    var word1 by Pentagrams.word1
    var word2 by Pentagrams.word2
    var word3 by Pentagrams.word3
    var word4 by Pentagrams.word4
    var nextWord by Pentagrams.nextWord
    var count by Pentagrams.count
}
