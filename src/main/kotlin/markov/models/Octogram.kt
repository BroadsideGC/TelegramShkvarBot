package markov.models

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object Octograms : IntIdTable() {
    val word1 = text("word1")
    val word2 = text("word2")
    val word3 = text("word3")
    val word4 = text("word4")
    val word5 = text("word5")
    val word6 = text("word6")
    val word7 = text("word7")
    val nextWord = text("next_word")
    val count = integer("count").default(1)

    init {
        Octograms.index(
            false,
            word1,
            word2,
            word3,
            word4,
            word5,
            word6,
            word7,
            nextWord
        )
        Octograms.index(
            false,
            word1,
            word2,
            word3,
            word4,
            word5,
            word6,
            word7
        )
    }
}

class Octogram(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Octogram>(Octograms)

    var word1 by Octograms.word1
    var word2 by Octograms.word2
    var word3 by Octograms.word3
    var word4 by Octograms.word4
    var word5 by Octograms.word5
    var word6 by Octograms.word6
    var word7 by Octograms.word7
    var nextWord by Octograms.nextWord
    var count by Octograms.count
}
