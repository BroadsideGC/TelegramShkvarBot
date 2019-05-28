import `fun`.StringGenerator
import events.Bar
import events.Joppa
import events.Smash
import events.handle
import khronos.Dates
import khronos.isWednesday
import rocks.waffle.telekt.network.InputFile
import rocks.waffle.telekt.types.enums.ParseMode
import rocks.waffle.telekt.types.events.MessageEvent
import rocks.waffle.telekt.types.events.message
import rocks.waffle.telekt.util.Recipient
import rocks.waffle.telekt.util.replyTo
import tournaments.*
import java.io.File
import java.security.SecureRandom


suspend fun rollHandler(messageEvent: MessageEvent) {
    messageEvent.message.from?.id?.let {
        if (checkToxik(it)) {
            messageEvent.bot.replyTo(messageEvent, "Exceeded level of toxicity")
            return
        }
    }
    val rnd = SecureRandom()
    val roll = (1..9)
        .map { if (it == 1) rnd.nextInt() % 9 + 1 else rnd.nextInt() % 10 }
        .map { Math.abs(it).toString() }
        .reduce { s, acc -> s + acc }
    messageEvent.bot.replyTo(messageEvent, "You rolled $roll")
}


suspend fun doublesHandler(messageEvent: MessageEvent) {
    messageEvent.message.from?.id?.let {
        if (checkToxik(it)) {
            messageEvent.bot.replyTo(messageEvent, "Exceeded level of toxicity")
            return
        }
    }
    val rnd = SecureRandom()
    val names = messageEvent.message.text?.replace("/doubles", "")?.split(',')?.map { it -> it.trim() } ?: emptyList()
    when {
        names.isEmpty() -> messageEvent.bot.replyTo(messageEvent, "No names provided")
        names.size % 2 != 0 -> messageEvent.bot.replyTo(messageEvent, "Not even number of names")
        names.size != names.distinct().size -> messageEvent.bot.replyTo(messageEvent, "Repeating name")
        else -> {
            val result = names.shuffled(rnd).shuffled(rnd).windowed(2, 2)
            messageEvent.bot.replyTo(messageEvent, result.joinToString("\n"))
        }
    }
}

suspend fun wednesdayHandler(messageEvent: MessageEvent) {
    messageEvent.message.from?.id?.let {
        if (checkToxik(it)) {
            messageEvent.bot.replyTo(messageEvent, "Exceeded level of toxicity")
            return
        }
    }
    if (Dates.today.isWednesday()) {
        val file = getWednesdayFile()
        messageEvent.bot.sendPhoto(
            Recipient(messageEvent.message.chat.id),
            InputFile(file)
        )
    } else {
        messageEvent.bot.replyTo(messageEvent, "Today is not wednesday")
    }
}

fun checkToxik(userId: Long) = userId in toxiks


suspend fun tournamentHandler(messageEvent: MessageEvent) {
    val bracket =
        generateBrackets(messageEvent.message.text!!.substring("/tournament ".length).split(',').map(String::trim))
    val name = StringGenerator.randomWord()
    TournamentsHolder.put(messageEvent.message.messageId, name, bracket)
    messageEvent.bot.sendMessage(
        chatId = Recipient.ChatId.new(messageEvent.message.chat.id),
        parseMode = ParseMode.MARKDOWN,
        text = bracket.flatten().joinToString(
            prefix = "**Tournament ${name.capitalize()}!**\n\n",
            separator = "\n",
            transform = { it.pretty() }
        )
    )
}

suspend fun nextBracket(messageEvent: MessageEvent) {
    messageEvent.bot.replyTo(messageEvent, (TournamentsHolder.next((messageEvent.message.replyToMessage ?: run {
        messageEvent.bot.replyTo(messageEvent, "Use `reply` on tournament announce")
        return
    }).messageId) ?: run {
        messageEvent.bot.replyTo(messageEvent, "Unknown or finished tournament")
        return
    }).pretty())
}

suspend fun win(messageEvent: MessageEvent) {
    val result = when (messageEvent.message.text?.substring("/win".length)?.trim()?.substring(0, 3)) {
        "top" -> BracketResult.WINNER_TOP
        "bot" -> BracketResult.WINNER_BOTTOM
        else -> run {
            messageEvent.bot.replyTo(messageEvent, "`/win top` or `/win bottom`")
            return
        }
    }
    val id = (messageEvent.message.replyToMessage ?: run {
        messageEvent.bot.replyTo(messageEvent, "Use `reply` on tournament announce")
        return
    }).messageId
    val current = TournamentsHolder.current(id) ?: run {
        messageEvent.bot.replyTo(messageEvent, "Unknown or finished tournament")
        return
    }
    TournamentsHolder.next(id)
    current.result = result
    messageEvent.bot.replyTo(messageEvent, "Got it!\n${current.pretty()}\n\n$result")
}

suspend fun ikea(messageEvent: MessageEvent) {
    messageEvent.bot.replyTo(messageEvent, StringGenerator.randomWord())
}

suspend fun smashHandler(messageEvent: MessageEvent) {
    val response = Smash.handle(messageEvent.message, "/smash", "/unsmash", "/call4smash")
    messageEvent.bot.replyTo(messageEvent, response)
}

suspend fun barHandler(messageEvent: MessageEvent) {
    val response = Bar.handle(messageEvent.message, "/bar", "/unbar", "/call4bar")
    messageEvent.bot.replyTo(messageEvent, response)
}

suspend fun joppaHandler(messageEvent: MessageEvent) {
    val response = Joppa.handle(messageEvent.message, "/jopa", "/unjopa", "/iditeVjopu")
    messageEvent.bot.replyTo(messageEvent, response)
}

class R

fun getWednesdayFile(): File = File(R::class.java.classLoader.getResource("wednesday.jpg").file)