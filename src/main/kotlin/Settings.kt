import com.uchuhimo.konf.Config
import com.uchuhimo.konf.ConfigSpec

object SettingsSpec : ConfigSpec("bot") {
    val token by required<String>()
}

val settings = Config { addSpec(SettingsSpec) }.from.json.resource("settings.json")