import SettingsSpec.token
import events.Bar
import events.Joppa
import events.Smash
import rocks.waffle.telekt.bot.Bot
import rocks.waffle.telekt.contrib.filters.CommandFilter
import rocks.waffle.telekt.dispatcher.Dispatcher


suspend fun main() {
    val token = settings[token]

    val bot = Bot(token = token)
    val dp = Dispatcher(bot)

    dp.messageHandler(CommandFilter("roll"), block = ::rollHandler)
    dp.messageHandler(CommandFilter("doubles"), block = ::doublesHandler)
    dp.messageHandler(CommandFilter("wednesday"), block = ::wednesdayHandler)
    dp.messageHandler(CommandFilter("todayOngoings"), block = ::todayOngoingsHandler)
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

    dp.poll()
}

