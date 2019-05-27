import rocks.waffle.telekt.types.events.MessageEvent
import rocks.waffle.telekt.types.events.message
import rocks.waffle.telekt.util.replyTo
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