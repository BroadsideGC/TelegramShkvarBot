package TelegramShkvarBot


import com.natpryce.konfig.ConfigurationProperties
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import org.telegram.telegrambots.api.methods.send.SendMessage
import org.telegram.telegrambots.api.objects.Update
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.exceptions.TelegramApiException
import java.io.File
import java.security.SecureRandom


class ShkvarBot : TelegramLongPollingBot() {
    private val CONFIG_FILE = "shkvar.properties";


    private val config = ConfigurationProperties.fromFile(File(CONFIG_FILE));



    override fun getBotUsername() = config[shkvarBot.botUsername]

    override fun onUpdateReceived(update: Update?) {
        async(CommonPool) {
            val chatId = update?.message?.chatId.toString()
            val text = update?.message!!.text
            val message = SendMessage()
            message.chatId = chatId
            when  {
                text.matches("^/roll(@$botUsername)?.*".toRegex()) -> {
                    val rnd = SecureRandom()
                    val roll = (1..9)
                            .map { if (it == 1) rnd.nextInt() % 9 + 1 else rnd.nextInt() % 10 }
                            .map { Math.abs(it).toString() }
                            .reduce { s, acc -> s + acc }
                    message.text = "You rolled $roll"
                    message.replyToMessageId = update.message.messageId
                    try {
                        sendMessage(message)
                    } catch (e: TelegramApiException) {
                        e.printStackTrace()
                    }
                }
                else -> println(text)
            }

        }
    }


    override fun getBotToken(): String = config[shkvarBot.botToken]

}