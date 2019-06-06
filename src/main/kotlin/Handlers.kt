import `fun`.StringGenerator
import anime365.getTodayOngoingTranslations
import events.EventInChat
import events.handle
import markov.markovChain2
import markov.markovChain3
import markov.markovChain5
import rocks.waffle.telekt.dispatcher.HandlerScope
import rocks.waffle.telekt.network.InputFile
import rocks.waffle.telekt.types.Message
import rocks.waffle.telekt.types.enums.ParseMode
import rocks.waffle.telekt.util.Recipient
import rocks.waffle.telekt.util.replyTo
import tournaments.*
import java.security.SecureRandom
import java.util.*


suspend fun rollHandler(scope: HandlerScope, message: Message) {
    message.from?.id?.let {
        if (checkToxik(it)) {
            scope.bot.replyTo(message, "Exceeded level of toxicity")
            return
        }
    }
    val rnd = SecureRandom()
    val roll = (1..9)
        .map { if (it == 1) rnd.nextInt() % 9 + 1 else rnd.nextInt() % 10 }
        .map { Math.abs(it).toString() }
        .reduce { s, acc -> s + acc }
    scope.bot.replyTo(message, "You rolled $roll")
}


suspend fun doublesHandler(scope: HandlerScope, message: Message) {
    message.from?.id?.let {
        if (checkToxik(it)) {
            scope.bot.replyTo(message, "Exceeded level of toxicity")
            return
        }
    }
    val rnd = SecureRandom()
    val names = message.text?.replace("/doubles", "")?.split(',')?.map { it -> it.trim() } ?: emptyList()
    when {
        names.isEmpty() -> scope.bot.replyTo(message, "No names provided")
        names.size % 2 != 0 -> scope.bot.replyTo(message, "Not even number of names")
        names.size != names.distinct().size -> scope.bot.replyTo(message, "Repeating name")
        else -> {
            val result = names.shuffled(rnd).shuffled(rnd).windowed(2, 2)
            scope.bot.replyTo(message, result.joinToString("\n"))
        }
    }
}

suspend fun wednesdayHandler(scope: HandlerScope, message: Message) {
    message.from?.id?.let {
        if (checkToxik(it)) {
            scope.bot.replyTo(message, "Exceeded level of toxicity")
            return
        }
    }

    val localCalendar = Calendar.getInstance(TimeZone.getDefault())
    if (localCalendar.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY) {
        val fileId = getWednesdayFileId()
        scope.bot.sendPhoto(
            Recipient(message.chat.id),
            InputFile(fileId)
        )
    } else {
        scope.bot.replyTo(message, "Today is not wednesday")
    }
}

suspend fun todayOngoingsHandler(scope: HandlerScope, message: Message) {
    message.from?.id?.let {
        if (checkToxik(it)) {
            scope.bot.replyTo(message, "Exceeded level of toxicity")
            return
        }
    }
    val res = getTodayOngoingTranslations().distinctBy { it.series.title }
        .map { "${it.series.title} ${it.episode.episodeFull}" }
    if (res.isNotEmpty()) {
        scope.bot.replyTo(message, res.joinToString("\n"))
    } else {
        scope.bot.replyTo(message, "Пока нет новых серий")
    }
}

suspend fun toxicsHandler(scope: HandlerScope, message: Message) {
    if (message.chat.type != "private") {
        val toxicsInfo =
            toxiks.mapNotNull {
                try {
                    scope.bot.getChatMember(Recipient(message.chat.id), it.toInt())
                } catch (e: Exception) {
                    null
                }
            }
        val toxicsUsernames = toxicsInfo.map { "@${it.user.username}" }
        scope.bot.replyTo(message, "Current toxics list:\n${toxicsUsernames.joinToString("\n")}")
    }
}

suspend fun markovHandler(scope: HandlerScope, message: Message) {
    val text = message.text ?: ""
    markovChain2.processText(text)
    markovChain3.processText(text)
    markovChain5.processText(text)
}

suspend fun allHandler(scope: HandlerScope, message: Message) {
    markovHandler(scope, message)
    randomGenerateHandler(scope, message)
}

suspend fun randomGenerateHandler(scope: HandlerScope, message: Message) {
    val rnd = SecureRandom()
    if (rnd.nextInt(100) < replyChance) {
        when (rnd.nextInt(3)) {
            0 -> generate5Handler(scope, message)
            1 -> generate3Handler(scope, message)
            else -> generate2Handler(scope, message)
        }
    }
}

suspend fun generate2Handler(scope: HandlerScope, message: Message) {
    val rnd = SecureRandom()
    val text = markovChain2.generate(minsize = rnd.nextInt(20) + 5).trim()
    if (text.isNotEmpty()) {
        scope.bot.replyTo(message, text)
    }

}

suspend fun generate3Handler(scope: HandlerScope, message: Message) {
    val rnd = SecureRandom()
    val text = markovChain3.generate(minsize = rnd.nextInt(20) + 5).trim()
    if (text.isNotEmpty()) {
        scope.bot.replyTo(message, text)
    }

}

suspend fun generate5Handler(scope: HandlerScope, message: Message) {
    val rnd = SecureRandom()
    val text = markovChain5.generate(minsize = rnd.nextInt(20) + 5).trim()
    if (text.isNotEmpty()) {
        scope.bot.replyTo(message, text)
    }

}

fun checkToxik(userId: Long) = userId in toxiks


suspend fun tournamentHandler(scope: HandlerScope, message: Message) {
    val bracket =
        generateBrackets(message.text!!.substring("/tournament ".length).split(',').map(String::trim))
    val name = StringGenerator.randomWord()
    TournamentsHolder.put(message.messageId, name, bracket)
    scope.bot.sendMessage(
        chatId = Recipient.ChatId.new(message.chat.id),
        parseMode = ParseMode.MARKDOWN,
        text = bracket.flatten().joinToString(
            prefix = "**Tournament ${name.capitalize()}!**\n\n",
            separator = "\n",
            transform = { it.pretty() }
        )
    )
}

suspend fun nextBracket(scope: HandlerScope, message: Message) {
    scope.bot.replyTo(message, (TournamentsHolder.next((message.replyToMessage ?: run {
        scope.bot.replyTo(message, "Use `reply` on tournament announce")
        return
    }).messageId) ?: run {
        scope.bot.replyTo(message, "Unknown or finished tournament")
        return
    }).pretty())
}

suspend fun win(scope: HandlerScope, message: Message) {
    val result = when (message.text?.substring("/win".length)?.trim()?.substring(0, 3)) {
        "top" -> BracketResult.WINNER_TOP
        "bot" -> BracketResult.WINNER_BOTTOM
        else -> run {
            scope.bot.replyTo(message, "`/win top` or `/win bottom`")
            return
        }
    }
    val id = (message.replyToMessage ?: run {
        scope.bot.replyTo(message, "Use `reply` on tournament announce")
        return
    }).messageId
    val current = TournamentsHolder.current(id) ?: run {
        scope.bot.replyTo(message, "Unknown or finished tournament")
        return
    }
    TournamentsHolder.next(id)
    current.result = result
    scope.bot.replyTo(message, "Got it!\n${current.pretty()}\n\n$result")
}

suspend fun ikea(scope: HandlerScope, message: Message) {
    scope.bot.replyTo(message, StringGenerator.randomWord())
}

internal fun makeHandler(
    event: EventInChat,
    registerCommand: String,
    unregisterCommand: String,
    callCommand: String
): suspend (HandlerScope, Message) -> Unit = { scope, message ->
    val response = event.handle(message, registerCommand, unregisterCommand, callCommand)
    scope.bot.replyTo(message, response, ParseMode.HTML)
}

suspend fun broadcast(scope: HandlerScope, message: Message) {
    val textToBroadcast = message.text?.removePrefix("/broadcast")?.trim() ?: return
    scope.bot.sendMessage(
        Recipient(message.chat.id),
        barUsers.joinToString(" ", transform = { "@$it" }) + " " + textToBroadcast
    )
}

fun getWednesdayFileId() = "AgADAgADqaoxG-GRwEvOUeREMdsyGK9QXw8ABKj2DhsNSAXkH6cFAAEC"