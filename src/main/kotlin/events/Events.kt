package events

import SettingsSpec
import barUsers
import rocks.waffle.telekt.types.Message
import rocks.waffle.telekt.util.fullName
import settings
import java.util.concurrent.ConcurrentHashMap

typealias User = String
typealias ChatId = Long
typealias LastUpdated = Long

data class Event(val going: Set<User>) {
    fun merge(other: Event): Event {
        return Event(going + other.going)
    }
}

class EventInChat(
    val eventPostfix: String,
    val timeToExpireMs: Long,
    internal val map: ConcurrentHashMap<ChatId, Pair<LastUpdated, Event>> = ConcurrentHashMap()
) {
    fun registerUser(chatId: ChatId, user: User) {
        map.merge(
            chatId,
            System.currentTimeMillis() to Event(setOf(user))
        ) { (oldTime, oldEvent), (newTime, newEvent) ->
            if (newTime - oldTime > timeToExpireMs)
                newTime to newEvent
            else
                newTime to oldEvent.merge(newEvent)
        }
    }

    fun unregisterUser(chatId: ChatId, user: User) {
        map.merge(
            chatId,
            System.currentTimeMillis() to Event(setOf())
        ) { (oldTime, oldEvent), (newTime, newEvent) ->
            if (newTime - oldTime > timeToExpireMs)
                newTime to newEvent
            else
                newTime to oldEvent.copy(going = oldEvent.going - user)
        }
    }

    fun going(chatId: ChatId): List<User> = map[chatId]?.second?.going?.toList() ?: emptyList()
}

val defaultEventExpireTime = settings[SettingsSpec.eventExpireTime]

fun EventInChat.handle(message: Message, registerCommand: String, unregisterCommand: String, callCommand: String): String {
    val tgUser = message.from ?: return "Ты даже не пользователь"
    val chat = message.chat.id
    val user = tgUser.username ?: tgUser.fullName

    fun formatGoing(current: User, prefix: String) = (going(chat) - current).let { users ->
        when(users.size) {
            0 -> ""
            else -> "\n\n$prefix: ${users.joinToString(prefix = "`", postfix = "`", transform = {"@$it"})}"
        }
    }

    return when(message.text) {
        registerCommand -> {
            registerUser(chat, user)
            "@$user идёт в $eventPostfix!${formatGoing(user, "Тоже идут в $eventPostfix")}"
        }
        unregisterCommand -> {
            unregisterUser(chat, user)
            "@$user не пойдёт в $eventPostfix, вот пидор!${formatGoing(user, "Всё ещё идут в $eventPostfix")}"
        }
        callCommand -> {
            val users = (barUsers - going(chat))
            "${users.joinToString(" ", transform = {"@$it"})} го в $eventPostfix"
        }
        else -> "Ты втираешь мне какую-то дичь"
    }
}

val Smash = EventInChat("смеш", defaultEventExpireTime)
val Bar = EventInChat("бар", defaultEventExpireTime)
val Joppa = EventInChat("жопу", 5 * 60 * 1000) // 5 mins
