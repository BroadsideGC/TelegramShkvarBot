import SettingsSpec.barUsernames
import SettingsSpec.toxics
import com.uchuhimo.konf.Config
import com.uchuhimo.konf.ConfigSpec

object SettingsSpec : ConfigSpec("bot") {
    val token by required<String>()
    val toxics by optional(emptyList<Long>())
    val barUsernames by optional(emptyList<String>())
    val eventExpireTime by optional<Long>(1000 * 60 * 60 * 6) // 6hrs in ms
}

object ESSettingsSpec : ConfigSpec("elastic") {
    val address by optional("127.0.0.1")
    val port by optional(9200)
}

val settings = Config {
    addSpec(SettingsSpec)
    addSpec(ESSettingsSpec)
}.from.json.resource("settings.json")

val toxiks = settings[toxics]
val barUsers = settings[barUsernames]
