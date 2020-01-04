import SettingsSpec.barUsernames
import SettingsSpec.replyChance
import SettingsSpec.toxics
import com.uchuhimo.konf.Config
import com.uchuhimo.konf.ConfigSpec
import com.zaxxer.hikari.HikariConfig
import javax.sql.DataSource

object SettingsSpec : ConfigSpec("bot") {
    val token by required<String>()
    val toxics by optional(emptyList<Long>())
    val barUsernames by optional(emptyList<String>())
    val replyChance by optional(5) // from 0 to 100
    val eventExpireTime by optional<Long>(1000 * 60 * 60 * 6) // 6hrs in ms
}

object ESSettingsSpec : ConfigSpec("elastic") {
    val address by optional("127.0.0.1")
    val port by optional(9200)
}

object DBSettingsSpec : ConfigSpec("db") {
    val url by required<String>()
    val username by required<String>()
    val password by required<String>()
}

val settings = Config {
    addSpec(SettingsSpec)
    addSpec(DBSettingsSpec)
}.from.json.resource("settings.json")

val toxiks = settings[toxics]
val barUsers = settings[barUsernames]
val replyChance = settings[replyChance]
val hikariConfig = HikariConfig()
lateinit var hikariDataSource : DataSource


