import SettingsSpec.toxics
import `fun`.StringGenerator
import rocks.waffle.telekt.types.enums.ParseMode
import rocks.waffle.telekt.types.events.MessageEvent
import rocks.waffle.telekt.types.events.message
import rocks.waffle.telekt.util.Recipient
import rocks.waffle.telekt.util.replyTo
import tournaments.*
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


suspend fun tournamentHandler(message: MessageEvent) {
    val bracket = generateBrackets(message.message.text!!.substring("/tournament ".length).split(',').map(String::trim))
    val name = StringGenerator.randomWord()
    TournamentsHolder.put(message.message.messageId, name, bracket)
    message.bot.sendMessage(
        chatId = Recipient.ChatId.new(message.message.chat.id),
        parseMode = ParseMode.MARKDOWN,
        text = bracket.flatten().joinToString(
            prefix = "**Tournament ${name.capitalize()}!**\n\n",
            separator = "\n",
            transform = { it.pretty() }
        )
    )
}

suspend fun nextBracket(message: MessageEvent) {
    message.bot.replyTo(message, (TournamentsHolder.next((message.message.replyToMessage ?: run {
        message.bot.replyTo(message, "Use `reply` on tournament announce")
        return
    }).messageId) ?: run {
        message.bot.replyTo(message, "Unknown or finished tournament")
        return
    }).pretty())
}

suspend fun win(message: MessageEvent) {
    val result = when (message.message.text?.substring("/win".length)?.trim()?.substring(0, 3)) {
        "top" -> BracketResult.WINNER_TOP
        "bot" -> BracketResult.WINNER_BOTTOM
        else -> run {
            message.bot.replyTo(message, "`/win top` or `/win bottom`")
            return
        }
    }
    val id = (message.message.replyToMessage ?: run {
        message.bot.replyTo(message, "Use `reply` on tournament announce")
        return
    }).messageId
    val current = TournamentsHolder.current(id) ?: run {
        message.bot.replyTo(message, "Unknown or finished tournament")
        return
    }
    TournamentsHolder.next(id)
    current.result = result
    message.bot.replyTo(message, "Got it!\n${current.pretty()}\n\n$result")
}

suspend fun ikea(message: MessageEvent) {
    message.bot.replyTo(message, StringGenerator.randomWord())
}