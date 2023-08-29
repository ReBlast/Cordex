@file:Suppress("UNCHECKED_CAST", "unused")

package me.blast.command.argument.extensions

import me.blast.command.argument.Argument
import me.blast.command.argument.MultiValueArgument
import me.blast.utils.Snowflake
import me.blast.utils.Utils
import me.blast.utils.Utils.hasValue
import me.blast.utils.throwUnless
import net.fellbaum.jemoji.Emoji
import net.fellbaum.jemoji.EmojiManager
import org.javacord.api.entity.channel.ChannelCategory
import org.javacord.api.entity.channel.ServerChannel
import org.javacord.api.entity.emoji.CustomEmoji
import org.javacord.api.entity.message.Message
import org.javacord.api.entity.permission.Role
import org.javacord.api.entity.user.User
import org.javacord.api.util.DiscordRegexPattern
import java.awt.Color
import java.net.URL
import java.time.Duration
import java.time.LocalDate
import java.util.*
import kotlin.reflect.KClass

/**
 * Converts the argument values to integers.
 *
 * Use [int] to convert multiple values into a single one.
 *
 * @return An Argument containing a list with retrieved [Int] values.
 */
fun MultiValueArgument<*>.ints(): Argument<List<Int>> {
  return (this as Argument<List<Int>>).apply {
    argumentListValidator = {
      map {
        it.toInt()
      }
    }
  }
}

/**
 * Converts the argument values to long values.
 *
 * Use [long] to convert multiple values into a single one.
 *
 * @return An Argument containing a list with retrieved [Long] values.
 */
fun MultiValueArgument<*>.longs(): Argument<List<Long>> {
  return (this as Argument<List<Long>>).apply {
    argumentListValidator = {
      map {
        it.toLong()
      }
    }
  }
}

/**
 * Converts the argument values to float values.
 *
 * Use [float] to convert multiple values into a single one.
 *
 * @return An Argument containing a list with retrieved [Float] values.
 */
fun MultiValueArgument<*>.floats(): Argument<List<Float>> {
  return (this as Argument<List<Float>>).apply {
    argumentListValidator = {
      map {
        it.toFloat()
      }
    }
  }
}

/**
 * Converts the argument values to double values.
 *
 * Use [double] to convert multiple values into a single one.
 *
 * @return An Argument containing a list with retrieved [Double] values.
 */
fun MultiValueArgument<*>.doubles(): Argument<List<Double>> {
  return (this as Argument<List<Double>>).apply {
    argumentListValidator = {
      map {
        it.toDouble()
      }
    }
  }
}

/**
 * Converts the argument values to [URL]s.
 *
 * Use [url] to convert multiple values into a single one.
 *
 * @return An Argument containing a list with retrieved [URL] values.
 */
fun MultiValueArgument<*>.urls(): Argument<List<URL>> {
  return (this as Argument<List<URL>>).apply {
    argumentListValidator = {
      map {
        URL(it)
      }
    }
  }
}

/**
 * Retrieves [User]s based on the argument values.
 *
 * Use [user] to convert multiple values into a single one.
 *
 * @param searchMutualGuilds Whether to search mutual guilds of the user if not found in the current guild (only in DMs). Defaults to false.
 * @return An Argument containing a list with retrieved [User] values.
 */
fun MultiValueArgument<*>.users(searchMutualGuilds: Boolean = false): Argument<List<User>> {
  return (this as Argument<List<User>>).apply {
    argumentListValidator = {
      map {
        if (guildOnly) {
          argumentEvent.server.get().let { server ->
            server.getMembersByDisplayNameIgnoreCase(it).firstOrNull() ?: server.getMemberByDiscriminatedName(it).orElse(
              server.getMemberById(Utils.extractDigits(it)).orElse(
                server.getMembersByNameIgnoreCase(it).first()
              )
            )
          }
        } else {
          throwUnless(searchMutualGuilds && argumentEvent.channel.asPrivateChannel().hasValue()) {
            argumentEvent.messageAuthor.asUser().get().mutualServers.firstNotNullOf { server ->
              server.getMembersByDisplayNameIgnoreCase(it).firstOrNull() ?: server.getMemberByDiscriminatedName(it).orElse(
                server.getMemberById(Utils.extractDigits(it)).orElse(
                  server.getMembersByNameIgnoreCase(it).firstOrNull()
                )
              )
            }
          }
        }
      }
    }
  }
}

/**
 * Retrieves [ServerChannel]s based on the argument values.
 *
 * Use [channel] to convert multiple values into a single one.
 *
 * @param searchMutualGuilds Whether to search mutual guilds of the user if not found in the current guild (only in DMs). Defaults to false.
 * @return An Argument containing a list with retrieved [ServerChannel] values.
 */
fun MultiValueArgument<*>.channels(searchMutualGuilds: Boolean = false): Argument<List<ServerChannel>> {
  return (this as Argument<List<ServerChannel>>).apply {
    argumentListValidator = {
      map {
        if (guildOnly) {
          argumentEvent.server.get().let { server ->
            server.getChannelsByNameIgnoreCase(it).firstOrNull() ?: server.getChannelById(Utils.extractDigits(it)).get()
          }
        } else {
          throwUnless(searchMutualGuilds && argumentEvent.channel.asPrivateChannel().hasValue()) {
            argumentEvent.messageAuthor.asUser().get().mutualServers.firstNotNullOf { server ->
              server.getChannelsByNameIgnoreCase(it).firstOrNull() ?: server.getChannelById(Utils.extractDigits(it)).orElse(null)
            }
          }
        }
      }
    }
  }
}

/**
 * Retrieves [ServerChannel]s of the specified `types` based on the argument values.
 *
 * Use [channel] to convert multiple values into a single one.
 *
 * @param types types An array of classes extending [ServerChannel] representing the supported channel types.
 * @param searchMutualGuilds Whether to search mutual guilds of the user if not found in the current guild (only in DMs). Defaults to false.
 * @return An Argument containing a list with retrieved [ServerChannel] values.
 */
inline fun <reified R : ServerChannel> MultiValueArgument<*>.channels(vararg types: KClass<out R>, searchMutualGuilds: Boolean = false): Argument<R> {
  return (this as Argument<R>).apply {
    argumentListValidator = {
      map {
        val channel = if (guildOnly) {
          argumentEvent.server.get().let { server ->
            server.getChannelsByNameIgnoreCase(it).firstOrNull() ?: server.getChannelById(Utils.extractDigits(it)).get()
          }
        } else {
          throwUnless(searchMutualGuilds && argumentEvent.channel.asPrivateChannel().hasValue()) {
            argumentEvent.messageAuthor.asUser().get().mutualServers.firstNotNullOf { server ->
              server.getChannelsByNameIgnoreCase(it).firstOrNull() ?: server.getChannelById(Utils.extractDigits(it)).orElse(null)
            }
          }
        }
        if (types.any { it.isInstance(channel) }) {
          channel as R
        } else if (R::class.isInstance(channel)) {
          channel as R
        } else {
          throw IllegalArgumentException()
        }
      }
    }
  }
}

/**
 * Retrieves [ChannelCategory]s based on the argument values.
 *
 * Use [category] to convert multiple values into a single one.
 *
 * @param searchMutualGuilds Whether to search mutual guilds of the user if not found in the current guild (only in DMs). Defaults to false.
 * @return An Argument containing a list with retrieved [ChannelCategory] values.
 */
fun MultiValueArgument<*>.categories(searchMutualGuilds: Boolean = false): Argument<List<ChannelCategory>> {
  return (this as Argument<List<ChannelCategory>>).apply {
    argumentListValidator = {
      map {
        if (guildOnly) {
          argumentEvent.server.get().let { server ->
            server.getChannelCategoriesByNameIgnoreCase(it).firstOrNull() ?: server.getChannelCategoryById(Utils.extractDigits(it)).get()
          }
        } else {
          throwUnless(searchMutualGuilds && argumentEvent.channel.asPrivateChannel().hasValue()) {
            argumentEvent.messageAuthor.asUser().get().mutualServers.firstNotNullOf { server ->
              server.getChannelCategoriesByNameIgnoreCase(it).firstOrNull() ?: server.getChannelCategoryById(Utils.extractDigits(it)).orElse(null)
            }
          }
        }
      }
    }
  }
}

/**
 * Retrieves [Role]s based on the argument values.
 *
 * Use [role] to convert multiple values into a single one.
 *
 * @param searchMutualGuilds Whether to search mutual guilds of the user if not found in the current guild (only in DMs). Defaults to false.
 * @return An Argument containing a list with retrieved [Role] values.
 */
fun MultiValueArgument<*>.roles(searchMutualGuilds: Boolean = false): Argument<List<Role>> {
  return (this as Argument<List<Role>>).apply {
    argumentListValidator = {
      map {
        if (guildOnly) {
          argumentEvent.server.get().let { server ->
            server.getRolesByNameIgnoreCase(it).firstOrNull() ?: server.getRoleById(Utils.extractDigits(it)).get()
          }
        } else {
          throwUnless(searchMutualGuilds && argumentEvent.channel.asPrivateChannel().hasValue()) {
            argumentEvent.messageAuthor.asUser().get().mutualServers.firstNotNullOf { server ->
              server.getRolesByNameIgnoreCase(it).firstOrNull() ?: server.getRoleById(Utils.extractDigits(it)).orElse(null)
            }
          }
        }
      }
    }
  }
}

/**
 * Retrieves [Message]s based on the argument values.
 *
 * Use [message] to convert multiple values into a single one.
 *
 * @param searchMutualGuilds Whether to search mutual guilds of the user if not found in the current guild (only in DMs). Defaults to false.
 * @param includePrivateChannels Whether to include messages in the private channel between the user and the bot in search. Defaults to false.
 * @return An Argument containing a list with retrieved [Message] values.
 */
fun MultiValueArgument<*>.messages(searchMutualGuilds: Boolean = false, includePrivateChannels: Boolean = false): Argument<List<Message>> {
  return (this as Argument<List<Message>>).apply {
    argumentListValidator = {
      map {
        val matchResult = DiscordRegexPattern.MESSAGE_LINK.toRegex().matchEntire(it)
        if(matchResult == null) {
          argumentEvent.channel.getMessageById(Utils.extractDigits(it)).get()
        } else {
          if (matchResult.groups["server"] == null) {
            require(includePrivateChannels)
            argumentEvent.messageAuthor.asUser().get().openPrivateChannel().get()
              .getMessageById(matchResult.groups["message"]!!.value).get()
          } else {
            try {
              argumentEvent.server.get().getTextChannelById(matchResult.groups["channel"]!!.value).get().takeIf { it.canSee(argumentEvent.messageAuthor.asUser().get()) }!!
                .getMessageById(matchResult.groups["message"]!!.value).get()
            } catch (_: NullPointerException) {
              throw IllegalAccessException()
            } catch (_: Exception) {
              throwUnless(searchMutualGuilds) {
                argumentEvent.messageAuthor.asUser().get().mutualServers.find { it.idAsString == matchResult.groups["server"]!!.value }!!
                  .getTextChannelById(matchResult.groups["channel"]!!.value).get().takeIf { it.canSee(argumentEvent.messageAuthor.asUser().get()) }!!
                  .getMessageById(matchResult.groups["message"]!!.value).get()
              }
            }
          }
        }
      }
    }
  }
}

/**
 * Retrieves [CustomEmoji]s based on the argument values.
 *
 * Use [customEmoji] to convert multiple values into a single one.
 *
 * @param searchMutualGuilds Whether to search mutual guilds of the user if not found in the current guild (only in DMs). Defaults to false.
 * @return An Argument containing a list with retrieved [CustomEmoji] values.
 */
fun MultiValueArgument<*>.customEmojis(searchMutualGuilds: Boolean = false): Argument<List<CustomEmoji>> {
  return (this as Argument<List<CustomEmoji>>).apply {
    argumentListValidator = {
      map {
        val matchResult = DiscordRegexPattern.CUSTOM_EMOJI.toRegex().matchEntire(it) ?: throw IllegalArgumentException()
        if (guildOnly) {
          argumentEvent.server.get().getCustomEmojiById(matchResult.groups["id"]!!.value).get()
        } else {
          throwUnless(searchMutualGuilds) {
            argumentEvent.messageAuthor.asUser().get().mutualServers.firstNotNullOf {
              it.getCustomEmojiById(matchResult.groups["id"]!!.value).get()
            }
          }
        }
      }
    }
  }
}

/**
 * Converts the argument values to [Snowflake]s.
 *
 * Use [snowflake] to convert multiple values into a single one.
 *
 * @return An Argument containing a list with retrieved [Snowflake] values.
 */
fun MultiValueArgument<*>.snowflakes(): Argument<List<Snowflake>> {
  return (this as Argument<List<Snowflake>>).apply {
    argumentValidator = {
      map {
        Snowflake(toULong())
      }
    }
  }
}

/**
 * Converts the argument values to [Duration]s.
 *
 * Use [duration] to convert multiple values into a single one.
 *
 * @return An Argument containing a list with retrieved [Duration] values.
 */
fun MultiValueArgument<*>.durations(): Argument<List<Duration>> {
  return (this as Argument<List<Duration>>).apply {
    argumentListValidator = {
      map {
        Utils.parseDuration(it) ?: throw IllegalArgumentException()
      }
    }
  }
}

/**
 * Converts the argument values to [LocalDate]s.
 *
 * Use [date] to convert multiple values into a single one.
 *
 * @param locale The locale used for date parsing. Defaults to [Locale.ENGLISH].
 * @return An Argument containing a list with retrieved [LocalDate] values.
 */
fun MultiValueArgument<*>.dates(locale: Locale = Locale.ENGLISH): Argument<List<LocalDate>> {
  return (this as Argument<List<LocalDate>>).apply {
    argumentListValidator = {
      map {
        Utils.parseDate(it, locale) ?: throw IllegalArgumentException()
      }
    }
  }
}

/**
 * Converts the argument values to [Color]s.
 *
 * Use [color] to convert multiple values into a single one.
 *
 * @returnAn Argument containing a list with retrieved [Color] values.
 */
fun MultiValueArgument<*>.colors(): Argument<List<Color>> {
  return (this as Argument<List<Color>>).apply {
    argumentListValidator = {
      map {
        Color::class.java.getField(it)[null] as? Color ?: Color.decode(it)
      }
    }
  }
}

/**
 * Converts the argument values to [Emoji]s.
 *
 * Use [unicodeEmoji] to convert multiple values into a single one.
 *
 * @return An Argument containing a list with retrieved [Emoji] values.
 */
fun MultiValueArgument<*>.unicodeEmojis(): Argument<List<Emoji>> {
  return (this as Argument<List<Emoji>>).apply {
    argumentListValidator = {
      map {
        EmojiManager.getEmoji(it).get()
      }
    }
  }
}

/**
 * Converts the argument values to values of the given enum class.
 *
 * Use [enum] to convert multiple values into a single one.
 *
 * @return An Argument containing a list with retrieved enum values.
 */
inline fun <reified T : Enum<T>> MultiValueArgument<*>.enums(): Argument<T> {
  return (this as Argument<T>).apply {
    argumentListValidator = {
      map {
        try {
          enumValueOf<T>(it.uppercase().replace(" ", "_"))
        } catch (e: IllegalArgumentException) {
          throw IllegalArgumentException()
        }
      }
    }
  }
}