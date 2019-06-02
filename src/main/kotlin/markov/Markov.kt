package markov

import com.fasterxml.jackson.databind.ObjectMapper
import io.inbot.eskotlinwrapper.JacksonModelReaderAndWriter
import kotlinx.serialization.Serializable
import org.elasticsearch.ElasticsearchStatusException
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.client.crudDao
import org.elasticsearch.common.lucene.search.function.CombineFunction
import org.elasticsearch.common.lucene.search.function.FieldValueFactorFunction
import org.elasticsearch.common.lucene.search.function.FunctionScoreQuery
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.index.query.QueryBuilders.*
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder.FilterFunctionBuilder
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders.fieldValueFactorFunction
import org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders.randomFunction
import org.elasticsearch.search.builder.SearchSourceBuilder
import settings
import java.util.*


@Serializable
data class NGram(val previousWords: List<String>, val word: String, var count: Int = 1) {
    val pvHash = previousWords.hashCode()
}

const val indexName3 = "markov3"
const val indexName5 = "markov5"
val esClient = RestHighLevelClient(host = settings["elastic.address"], port = settings["elastic.port"])

class MarkovChain(elascticSearchClient: RestHighLevelClient, index: String, private val n: Int = 5) {

    private val dao = elascticSearchClient.crudDao(
        index, type = "_doc", modelReaderAndWriter = JacksonModelReaderAndWriter(
            NGram::class,
            ObjectMapper().findAndRegisterModules()
        )
    )

    init {
        try {
            dao.createIndex {
                val settingsJson = this::class.java.getResource("/markov-settings.json").readText()
                source(settingsJson, XContentType.JSON)
            }
        } catch (e: ElasticsearchStatusException) {
            print(e.message)
        }
    }


    private fun addGram(nGram: NGram) {
        val id = Objects.hash(nGram.previousWords, nGram.word).toString()
        val ng = dao.get(id)
        if (ng != null) {
            dao.update(id) {
                it.count++
                it
            }
        } else {
            dao.index(id, nGram)
        }
    }

    fun processText(text: String) {
        val window = ArrayDeque<String>()
        val preparedText = "${prepareText(text)} "
        val tokens = preparedText.split(" ")
        for (token in tokens) {
            if (window.isNotEmpty()) {
                val ngram = NGram(window.toList(), token)
                this.addGram(ngram)
            }
            window.add(token)
            if (window.size > this.n) {
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

    fun generate(word: String = "", minsize: Int = 15): String {
        val result = mutableListOf("")
        var stop = false
        val window = ArrayDeque<String>()
        window.add("")
        if (word.isNotEmpty()) {
            result.add(word)
            window.add(word)
        }
        while (!stop) {
            val nextWord = getNextWord(window.toList())
            result.add(nextWord)
            window.add(nextWord)
            if (window.size > this.n) {
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

    private fun getNextWord(window: List<String>): String {
        val word = dao.search {
            val functions = if (window.size > 1) arrayOf(
                FilterFunctionBuilder(
                    randomFunction()
                ), FilterFunctionBuilder(
                    fieldValueFactorFunction("count").missing(0.0).modifier(FieldValueFactorFunction.Modifier.NONE)
                )
            ) else arrayOf(
                FilterFunctionBuilder(
                    randomFunction()
                )
            )
            val fquery = functionScoreQuery(functions).scoreMode(FunctionScoreQuery.ScoreMode.MULTIPLY)
                .boostMode(CombineFunction.REPLACE)
            fquery.filterFunctionBuilders()
            val query = SearchSourceBuilder.searchSource()
                .query(boolQuery().must(fquery).filter(termQuery("pvHash", window.hashCode())))

            source(query)
        }.mappedHits.toList()
        return if (word.isNotEmpty()) {
            word.first().word
        } else {
            ""
        }
    }
}

val markovChain3 = MarkovChain(esClient, indexName3, 3)
val markovChain5 = MarkovChain(esClient, indexName5, 5)