package markov

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.list

@Serializable
data class TGMessage(val id: Int, val from_id: Int, val body: String?)

class R

@UnstableDefault
fun main() {
    val file = R::class.java.classLoader.getResource("sh_messages_tg.json")
    val data = Json.nonstrict.parse(TGMessage.serializer().list, file.readText())
    runBlocking {
        data.filter { it.body != null && it.body.isNotEmpty() }.forEach {
            markovChain2.processText(it.body!!)
            markovChain3.processText(it.body)
            markovChain5.processText(it.body)
        }
    }

}