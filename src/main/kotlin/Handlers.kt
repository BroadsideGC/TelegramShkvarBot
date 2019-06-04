import `fun`.StringGenerator
import anime365.getTodayOngoingTranslations
import events.EventInChat
import events.handle
import markov.markovChain2
import markov.markovChain3
import markov.markovChain5
import rocks.waffle.telekt.network.InputFile
import rocks.waffle.telekt.types.enums.ParseMode
import rocks.waffle.telekt.types.events.MessageEvent
import rocks.waffle.telekt.types.events.message
import rocks.waffle.telekt.util.Recipient
import rocks.waffle.telekt.util.replyTo
import tournaments.*
import java.security.SecureRandom
import java.util.*


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
    val localCalendar = Calendar.getInstance(TimeZone.getDefault())
    if (localCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY) {
        val file = getWednesdayFile()
        messageEvent.bot.sendPhoto(
            Recipient(messageEvent.message.chat.id),
            InputFile(file.file)
        )
    } else {
        messageEvent.bot.replyTo(messageEvent, "Today is not wednesday")
    }
}

suspend fun todayOngoingsHandler(messageEvent: MessageEvent) {
    messageEvent.message.from?.id?.let {
        if (checkToxik(it)) {
            messageEvent.bot.replyTo(messageEvent, "Exceeded level of toxicity")
            return
        }
    }
    val res = getTodayOngoingTranslations().distinctBy { it.series.title }
        .map { "${it.series.title} ${it.episode.episodeFull}" }
    if (res.isNotEmpty()) {
        messageEvent.bot.replyTo(messageEvent, res.joinToString("\n"))
    } else {
        messageEvent.bot.replyTo(messageEvent, "Пока нет новых серий")
    }
}

suspend fun toxicsHandler(messageEvent: MessageEvent) {
    if (messageEvent.message.chat.type != "private") {
        val toxicsInfo =
            toxiks.mapNotNull {
                try {
                    messageEvent.bot.getChatMember(Recipient(messageEvent.message.chat.id), it.toInt())
                } catch (e: Exception) {
                    null
                }
            }
        val toxicsUsernames = toxicsInfo.map { "@${it.user.username}" }
        messageEvent.bot.replyTo(messageEvent, "Current toxics list:\n${toxicsUsernames.joinToString("\n")}")
    }
}

suspend fun markovHandler(messageEvent: MessageEvent) {
    val text = messageEvent.message.text ?: ""
    markovChain2.processText(text)
    markovChain3.processText(text)
    markovChain5.processText(text)
}

suspend fun allHandler(messageEvent: MessageEvent) {
    markovHandler(messageEvent)
    randomGenerateHandler(messageEvent)
}

suspend fun randomGenerateHandler(messageEvent: MessageEvent) {
    val rnd = SecureRandom()
    if (rnd.nextInt(100) < replyChance) {
        when (rnd.nextInt(3)) {
            0 -> generate5Handler(messageEvent)
            1 -> generate3Handler(messageEvent)
            else -> generate2Handler(messageEvent)
        }
    }
}

suspend fun generate2Handler(messageEvent: MessageEvent) {
    val rnd = SecureRandom()
    val text = markovChain2.generate(minsize = rnd.nextInt(20) + 5).trim()
    if (text.isNotEmpty()) {
        messageEvent.bot.replyTo(messageEvent, text)
    }

}

suspend fun generate3Handler(messageEvent: MessageEvent) {
    val rnd = SecureRandom()
    val text = markovChain3.generate(minsize = rnd.nextInt(20) + 5).trim()
    if (text.isNotEmpty()) {
        messageEvent.bot.replyTo(messageEvent, text)
    }

}

suspend fun generate5Handler(messageEvent: MessageEvent) {
    val rnd = SecureRandom()
    val text = markovChain5.generate(minsize = rnd.nextInt(20) + 5).trim()
    if (text.isNotEmpty()) {
        messageEvent.bot.replyTo(messageEvent, text)
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

internal fun makeHandler(
    event: EventInChat,
    registerCommand: String,
    unregisterCommand: String,
    callCommand: String
): suspend (MessageEvent) -> Unit = { messageEvent ->
    val response = event.handle(messageEvent.message, registerCommand, unregisterCommand, callCommand)
    messageEvent.bot.replyTo(messageEvent, response, ParseMode.HTML)
}

suspend fun broadcast(messageEvent: MessageEvent) {
    val textToBroadcast = messageEvent.message.text?.removePrefix("/broadcast")?.trim() ?: return
    messageEvent.bot.sendMessage(
        Recipient(messageEvent.message.chat.id),
        barUsers.joinToString(" ", transform = { "@$it" }) + " " + textToBroadcast
    )
}

class R

fun getWednesdayFile() = R::class.java.classLoader.getResource("wednesday.jpg")