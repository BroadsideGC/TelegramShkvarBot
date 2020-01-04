package markov.models

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object Geptagrams : IntIdTable() {
    val word1 = text("word1")
    val word2 = text("word2")
    val word3 = text("word3")
    val word4 = text("word4")
    val word5 = text("word5")
    val word6 = text("word6")
    val nextWord = text("next_word")
    val count = integer("count").default(1)

    init {
        Geptagrams.index(
            false,
            word1,
            word2,
            word3,
            word4,
            word5,
            word6,
            nextWord
        )
        Geptagrams.index(
            false,
            word1,
            word2,
            word3,
            word4,
            word5,
            word6
        )
    }
}

class Geptagram(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Geptagram>(Geptagrams)

    var word1 by Geptagrams.word1
    var word2 by Geptagrams.word2
    var word3 by Geptagrams.word3
    var word4 by Geptagrams.word4
    var word5 by Geptagrams.word5
    var word6 by Geptagrams.word6
    var nextWord by Geptagrams.nextWord
    var count by Geptagrams.count
}
