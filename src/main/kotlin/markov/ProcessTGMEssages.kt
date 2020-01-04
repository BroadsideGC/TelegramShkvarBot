package markov

import DBSettingsSpec.password
import DBSettingsSpec.url
import DBSettingsSpec.username
import com.zaxxer.hikari.HikariDataSource
import hikariConfig
import hikariDataSource
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.list
import org.jetbrains.exposed.sql.Database
import settings
import javax.xml.ws.LogicalMessage

@Serializable
data class TGMessage(val id: Int, val from_id: Int, val message: String? = null)

class R

@UnstableDefault
fun main() {
    hikariConfig.jdbcUrl = settings[url]
    hikariConfig.username = settings[username]
    hikariConfig.password = settings[password]
    hikariConfig.driverClassName = "org.postgresql.Driver"

    hikariDataSource = HikariDataSource(hikariConfig)

    Database.Companion.connect(hikariDataSource)
    val file = R::class.java.classLoader.getResource("sh_messages_tg_full.json")
    val data = Json.nonstrict.parse(TGMessage.serializer().list, file.readText())
    runBlocking {
        data.filter { it.message != null && it.message.isNotEmpty() }.forEach {
            it.message?.let { it1 -> markovChain.processText(it1) }
        }
    }

}