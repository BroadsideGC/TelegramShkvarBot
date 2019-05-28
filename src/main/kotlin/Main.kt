import SettingsSpec.token
import rocks.waffle.telekt.bot.Bot
import rocks.waffle.telekt.contrib.filters.CommandFilter
import rocks.waffle.telekt.dispatcher.Dispatcher


suspend fun main() {
    val token = settings[token]

    val bot = Bot(token = token)
    val dp = Dispatcher(bot)

    dp.messageHandler(CommandFilter("roll"), block = ::rollHandler)
    dp.messageHandler(CommandFilter("doubles"), block = ::doublesHandler)
    dp.messageHandler(CommandFilter("bar"), block = ::barHandler)
    dp.messageHandler(CommandFilter("tournament"), block = ::tournamentHandler)
    dp.messageHandler(CommandFilter("next"), block = ::nextBracket)
    dp.messageHandler(CommandFilter("win"), block = ::win)
    dp.messageHandler(CommandFilter("ikea"), block = ::ikea)

    dp.poll()
}

