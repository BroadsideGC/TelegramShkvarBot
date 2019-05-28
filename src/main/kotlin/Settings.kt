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

val settings = Config { addSpec(SettingsSpec) }.from.json.resource("settings.json")

val toxiks = settings[toxics]
val barUsers = settings[barUsernames]
