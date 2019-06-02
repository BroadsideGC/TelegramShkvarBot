import SettingsSpec.token
import events.Bar
import events.Joppa
import events.Smash
import rocks.waffle.telekt.bot.Bot
import rocks.waffle.telekt.contrib.filters.CommandFilter
import rocks.waffle.telekt.contrib.filters.ContentTypeFilter
import rocks.waffle.telekt.dispatcher.Dispatcher
import rocks.waffle.telekt.types.enums.ContentType


suspend fun main() {
    val token = settings[token]

    val bot = Bot(token = token)
    val dp = Dispatcher(bot)

    dp.messageHandler(CommandFilter("roll"), block = ::rollHandler)
    dp.messageHandler(CommandFilter("doubles"), block = ::doublesHandler)
    dp.messageHandler(CommandFilter("wednesday"), block = ::wednesdayHandler)
    dp.messageHandler(CommandFilter("todayOngoings"), block = ::todayOngoingsHandler)
    dp.messageHandler(CommandFilter("generate2"), block = ::generate2Handler)
    dp.messageHandler(CommandFilter("generate3"), block = ::generate3Handler)
    dp.messageHandler(CommandFilter("generate5"), block = ::generate5Handler)
    dp.messageHandler(ContentTypeFilter(ContentType.TEXT), block = ::randomGenerateHandler)
    dp.messageHandler(CommandFilter("markov"), block = ::markovHandler)
    dp.messageHandler(CommandFilter("toxics"), block = ::toxicsHandler)
    dp.messageHandler(
        CommandFilter("bar", "unbar", "call4bar"),
        block = makeHandler(Bar, "/bar", "/unbar", "/call4bar")
    )
    dp.messageHandler(CommandFilter("tournament"), block = ::tournamentHandler)
    dp.messageHandler(CommandFilter("next"), block = ::nextBracket)
    dp.messageHandler(CommandFilter("win"), block = ::win)
    dp.messageHandler(CommandFilter("ikea"), block = ::ikea)
    dp.messageHandler(
        CommandFilter("smash", "unsmash", "call4smash"),
        block = makeHandler(Smash, "/smash", "/unsmash", "/call4smash")
    )
    dp.messageHandler(
        CommandFilter("jopa", "unjopa", "iditeVjopu", ignoreCase = true),
        block = makeHandler(Joppa, "/jopa", "/unjopa", "/iditeVjopu")
    )
    dp.messageHandler(CommandFilter("broadcast"), block = ::broadcast)
    dp.messageHandler(ContentTypeFilter(ContentType.TEXT), block = ::markovHandler)

    dp.poll()
}

