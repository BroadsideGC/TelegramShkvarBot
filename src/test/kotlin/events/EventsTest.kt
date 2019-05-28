package events

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import rocks.waffle.telekt.types.Chat
import rocks.waffle.telekt.types.Message
import rocks.waffle.telekt.types.User as TgUser

fun Message.Companion.createFake(from: User, text: String) =
    Message(42, 42, Chat(42, "title"), TgUser(42, false, "name", username = from), text = text)

class EventsTest {
    private val testEvent = EventInChat("жопу", 1000)
    private val chatId = 42L
    private val users = listOf("user1", "user2", "user3")

    @Test
    fun canRegisterUsers() {
        testEvent.registerUser(chatId, users[0])
        testEvent.registerUser(chatId, users[1])
        testEvent.registerUser(chatId, users[2])
        assertEquals(users, testEvent.going(chatId))
        assertEquals(listOf<User>(), testEvent.going(chatId + 1))
    }

    @Test
    fun canUnregisterUsers() {
        testEvent.registerUser(chatId, users[0])
        testEvent.registerUser(chatId, users[1])
        testEvent.registerUser(chatId, users[2])
        testEvent.unregisterUser(chatId, users[1])
        assertEquals(listOf(users[0], users[2]), testEvent.going(chatId))
    }

    @Test
    fun stateCanExpire() = runBlocking {
        testEvent.registerUser(chatId, users[0])
        testEvent.registerUser(chatId, users[1])
        delay(1500)
        testEvent.registerUser(chatId, users[2])
        assertEquals(listOf(users[2]), testEvent.going(chatId))
    }

    @Test
    fun canHandleMessages() {
        val reg = "/reg"
        val unreg = "/unreg"
        val fakeMessages =
            users.map { Message.createFake(it, reg) } + listOf(Message.createFake(users[1], unreg))
        fakeMessages.forEach {
            println(testEvent.handle(it, reg, unreg, ""))
        }
        assertEquals(listOf(users[0], users[2]), testEvent.going(chatId))
    }
}
