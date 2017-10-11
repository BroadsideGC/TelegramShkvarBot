package TelegramShkvarBot

import com.natpryce.konfig.PropertyGroup
import com.natpryce.konfig.getValue
import com.natpryce.konfig.stringType

object shkvarBot : PropertyGroup() {
    val botToken by stringType;
    val botUsername by stringType;
}