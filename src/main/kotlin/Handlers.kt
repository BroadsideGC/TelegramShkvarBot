import `fun`.StringGenerator
import rocks.waffle.telekt.types.enums.ParseMode
import rocks.waffle.telekt.types.events.MessageEvent
import rocks.waffle.telekt.types.events.message
import rocks.waffle.telekt.util.Recipient
import rocks.waffle.telekt.util.replyTo
import tournaments.*
import java.security.SecureRandom

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

fun checkToxik(userId: Long): Boolean {
    val toxikList = listOf<Long>(82620713)
    if (userId in toxikList) {
        return true
    }
    return false
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
    //message.bot.replyTo(message, names.size.toString())
    when {
        names.isEmpty() -> message.bot.replyTo(message, "No names provided")
        names.size % 2 != 0 -> message.bot.replyTo(message, "Not even number of names")
        else -> {
            val pairNumsAll = mutableListOf<Int>()
            pairNumsAll.addAll(1..names.size / 2)
            val pairNums = 1..names.size / 2
            pairNumsAll.addAll(1..names.size / 2)
            repeat(5 + rnd.nextInt(15)) { pairNumsAll.shuffle(rnd) }
            val pairs = pairNumsAll.zip(names)
            val result =
                pairNums.map { n -> pairs.filter { it.first == n }.map { it.second } }.map { it.joinToString(" - ") }
            message.bot.replyTo(message, result.joinToString("\n"))
        }
    }
}

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