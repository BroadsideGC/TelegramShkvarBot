import SettingsSpec.toxics
import rocks.waffle.telekt.types.events.MessageEvent
import rocks.waffle.telekt.types.events.message
import rocks.waffle.telekt.util.replyTo
import java.security.SecureRandom

val toxiks = settings[toxics]

suspend fun rollHandler(message: MessageEvent) {
    message.message.from?.id?.let {
        if (checkToxik(it)) {
            message.bot.replyTo(message, "Exceeded level of toxicity")
            return
        }
    }
    val rnd = SecureRandom()
    val roll = (1..9)
        .map { if (it == 1) rnd.nextInt() % 9 + 1 else rnd.nextInt() % 10 }
        .map { Math.abs(it).toString() }
        .reduce { s, acc -> s + acc }
    message.bot.replyTo(message, "You rolled $roll")
}


suspend fun doublesHandler(message: MessageEvent) {
    message.message.from?.id?.let {
        if (checkToxik(it)) {
            message.bot.replyTo(message, "Exceeded level of toxicity")
            return
        }
    }
    val rnd = SecureRandom()
    val names = message.message.text?.replace("/doubles", "")?.split(',')?.map { it -> it.trim() } ?: emptyList()
    when {
        names.isEmpty() -> message.bot.replyTo(message, "No names provided")
        names.size % 2 != 0 -> message.bot.replyTo(message, "Not even number of names")
        names.size != names.distinct().size -> message.bot.replyTo(message, "Repeating name")
        else -> {
            val result = names.shuffled(rnd).shuffled(rnd).windowed(2, 2)
            message.bot.replyTo(message, result.joinToString("\n"))
        }
    }
}

fun checkToxik(userId: Long) = userId in toxiks
