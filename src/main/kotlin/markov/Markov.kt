package markov

import markov.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*


class MarkovChain {

    init {
        transaction {
            SchemaUtils.createMissingTablesAndColumns(
                Bigrams,
                Trigrams,
                Quadgrams,
                Pentagrams,
                Hexagrams,
                Geptagrams,
                Octograms
            )
        }
    }


    private suspend fun addGram(window: Array<String>, token: String) {
        newSuspendedTransaction {
            if (window.size > 0) {
                val tmp = window.takeLast(1)
                val bigram = Bigram.find {
                    (Bigrams.word1 eq tmp[0]) and (Bigrams.nextWord eq token)
                }.firstOrNull()

                if (bigram != null) {
                    bigram.count++
                } else {
                    Bigram.new {
                        word1 = tmp[0]
                        nextWord = token
                    }
                }
            }

            if (window.size > 1) {
                val tmp = window.takeLast(2)
                val trigram = Trigram.find {
                    (Trigrams.word1 eq tmp[0]) and (Trigrams.word2 eq tmp[1]) and (Trigrams.nextWord eq token)
                }.firstOrNull()

                if (trigram != null) {
                    trigram.count++
                } else {
                    Trigram.new {
                        word1 = tmp[0]
                        word2 = tmp[1]
                        nextWord = token
                    }
                }
            }

            if (window.size > 2) {
                val tmp = window.takeLast(3)
                val quadgram = Quadgram.find {
                    (Quadgrams.word1 eq tmp[0]) and (Quadgrams.word2 eq tmp[1]) and (Quadgrams.word3 eq tmp[2]) and (Quadgrams.nextWord eq token)
                }.firstOrNull()

                if (quadgram != null) {
                    quadgram.count++
                } else {
                    Quadgram.new {
                        word1 = tmp[0]
                        word2 = tmp[1]
                        word3 = tmp[2]
                        nextWord = token
                    }
                }
            }

            if (window.size > 3) {
                val tmp = window.takeLast(4)
                val pentagram = Pentagram.find {
                    (Pentagrams.word1 eq tmp[0]) and (Pentagrams.word2 eq tmp[1]) and (Pentagrams.word3 eq tmp[2]) and (Pentagrams.word4 eq tmp[3]) and (Pentagrams.nextWord eq token)
                }.firstOrNull()

                if (pentagram != null) {
                    pentagram.count++
                } else {
                    Pentagram.new {
                        word1 = tmp[0]
                        word2 = tmp[1]
                        word3 = tmp[2]
                        word4 = tmp[3]
                        nextWord = token
                    }
                }
            }

            if (window.size > 4) {
                val tmp = window.takeLast(5)
                val hexagram = Hexagram.find {
                    (Hexagrams.word1 eq tmp[0]) and (Hexagrams.word2 eq tmp[1]) and (Hexagrams.word3 eq tmp[2]) and (Hexagrams.word4 eq tmp[3]) and (Hexagrams.word5 eq tmp[4]) and (Hexagrams.nextWord eq token)
                }.firstOrNull()

                if (hexagram != null) {
                    hexagram.count++
                } else {
                    Hexagram.new {
                        word1 = tmp[0]
                        word2 = tmp[1]
                        word3 = tmp[2]
                        word4 = tmp[3]
                        word5 = tmp[4]
                        nextWord = token
                    }
                }
            }

            if (window.size > 5) {
                val tmp = window.takeLast(6)
                val geptagram = Geptagram.find {
                    (Geptagrams.word1 eq tmp[0]) and (Geptagrams.word2 eq tmp[1]) and (Geptagrams.word3 eq tmp[2]) and (Geptagrams.word4 eq tmp[3]) and (Geptagrams.word5 eq tmp[4]) and (Geptagrams.word6 eq tmp[5]) and (Geptagrams.nextWord eq token)
                }.firstOrNull()

                if (geptagram != null) {
                    geptagram.count++
                } else {
                    Geptagram.new {
                        word1 = tmp[0]
                        word2 = tmp[1]
                        word3 = tmp[2]
                        word4 = tmp[3]
                        word5 = tmp[4]
                        word6 = tmp[5]
                        nextWord = token
                    }
                }
            }

            if (window.size > 6) {
                val tmp = window.takeLast(7)
                val octogram = Octogram.find {
                    (Octograms.word1 eq tmp[0]) and (Octograms.word2 eq tmp[1]) and (Octograms.word3 eq tmp[2]) and (Octograms.word4 eq tmp[3]) and (Octograms.word5 eq tmp[4]) and (Octograms.word6 eq tmp[5]) and (Octograms.word7 eq tmp[6]) and (Octograms.nextWord eq token)
                }.firstOrNull()

                if (octogram != null) {
                    octogram.count++
                } else {
                    Octogram.new {
                        word1 = tmp[0]
                        word2 = tmp[1]
                        word3 = tmp[2]
                        word4 = tmp[3]
                        word5 = tmp[4]
                        word6 = tmp[5]
                        word7 = tmp[6]
                        nextWord = token
                    }
                }
            }
        }
    }

    suspend fun processText(text: String) {
        val window = ArrayDeque<String>()
        val preparedText = "${prepareText(text)} "
        val tokens = preparedText.split(" ")
        for (token in tokens) {
            if (window.isNotEmpty()) {
                this.addGram(window.toTypedArray(), token)
            }
            window.add(token)
            if (window.size > 7) {
                window.removeFirst()
            }
        }
    }

    private fun prepareText(text: String): String {
        return " ${text.replace("""/\w+ """.toRegex(), "")}"
            .replace("""\s+""".toRegex(), " ")
            .replace("([()\\[\\]{}])".toRegex(), "")
            .replace("(\\.\\.\\.|\\.|\\?|!|,)".toRegex()) { m ->
                " ${m.value}"
            }
    }

    suspend fun generate(word: String = "", minsize: Int = 15, n: Int = 2): String {
        val result = mutableListOf("")
        var stop = false
        val window = ArrayDeque<String>()
        window.add("")
        if (word.isNotEmpty()) {
            result.add(word)
            window.add(word)
        }
        while (!stop) {
            val nextWord = getNextWord(window.toList(), n = n)
            result.add(nextWord)
            window.add(nextWord)
            if (window.size > n) {
                window.removeFirst()
            }
            if (nextWord == "") {
                if (result.size > minsize) {
                    stop = true
                } else {
                    result.removeAt(result.size - 1)
                    result.add(".")
                    window.clear()
                    window.add("")
                }
            }
        }
        return result.joinToString(" ").replace(""" (\.\.\.|\?|!|,|\.)""".toRegex()) { m ->
            m.value.drop(1)
        }
    }

    private suspend fun getNextWord(window: List<String>, n: Int = 1): String = newSuspendedTransaction {
        when (window.size) {
            1 -> {
                val result = Bigram.find { (Bigrams.word1 eq window[0]) }
                    .orderBy(WeightedRandom(Bigrams.count) to SortOrder.DESC).limit(1).firstOrNull()
                result?.nextWord ?: ""
            }
            2 -> {
                val result = Trigram.find { (Trigrams.word1 eq window[0]) and (Trigrams.word2 eq window[1]) }
                    .orderBy(WeightedRandom(Trigrams.count) to SortOrder.DESC).limit(1).firstOrNull()
                result?.nextWord ?: ""
            }
            3 -> {
                val result =
                    Quadgram.find { (Quadgrams.word1 eq window[0]) and (Quadgrams.word2 eq window[1]) and (Quadgrams.word3 eq window[2]) }
                        .orderBy(WeightedRandom(Quadgrams.count) to SortOrder.DESC).limit(1).firstOrNull()
                result?.nextWord ?: ""
            }
            4 -> {
                val result =
                    Pentagram.find { (Pentagrams.word1 eq window[0]) and (Pentagrams.word2 eq window[1]) and (Pentagrams.word3 eq window[2]) and (Pentagrams.word4 eq window[3]) }
                        .orderBy(WeightedRandom(Pentagrams.count) to SortOrder.DESC).limit(1).firstOrNull()
                result?.nextWord ?: ""
            }
            5 -> {
                val result =
                    Hexagram.find { (Hexagrams.word1 eq window[0]) and (Hexagrams.word2 eq window[1]) and (Hexagrams.word3 eq window[2]) and (Hexagrams.word4 eq window[3]) and (Hexagrams.word5 eq window[4]) }
                        .orderBy(WeightedRandom(Hexagrams.count) to SortOrder.DESC).limit(1).firstOrNull()
                result?.nextWord ?: ""
            }
            6 -> {
                val result =
                    Geptagram.find { (Geptagrams.word1 eq window[0]) and (Geptagrams.word2 eq window[1]) and (Geptagrams.word3 eq window[2]) and (Geptagrams.word4 eq window[3]) and (Geptagrams.word5 eq window[4]) and (Geptagrams.word6 eq window[5]) }
                        .orderBy(WeightedRandom(Geptagrams.count) to SortOrder.DESC).limit(1).firstOrNull()
                result?.nextWord ?: ""
            }
            7 -> {
                val result =
                    Octogram.find { (Octograms.word1 eq window[0]) and (Octograms.word2 eq window[1]) and (Octograms.word3 eq window[2]) and (Octograms.word4 eq window[3]) and (Octograms.word5 eq window[4]) and (Octograms.word6 eq window[5]) and (Octograms.word7 eq window[6]) }
                        .orderBy(WeightedRandom(Octograms.count) to SortOrder.DESC).limit(1).firstOrNull()
                result?.nextWord ?: ""
            }
            else -> ""

        }
    }
}

val markovChain = MarkovChain()
