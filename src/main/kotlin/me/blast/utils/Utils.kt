@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package me.blast.utils

import me.blast.core.Cordex
import java.io.File
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.ResolverStyle
import java.util.*

object Utils {
  val DURATION_REGEX = Regex("^(\\d+(\\.\\d+)?)\\s*(\\w+)$")
  val localDatePatterns = listOf(
    "dd.MM[.yyyy][ HH:mm:ss]",
    "dd-MM[-yyyy][ HH:mm:ss]",
    "dd/MM[/yyyy][ HH:mm:ss]",
    "[yyyy.]MM.dd[ HH:mm:ss]",
    "[yyyy-]MM-dd[ HH:mm:ss]",
    "[yyyy/]MM/dd[ HH:mm:ss]",
    "dd MM[ yyyy][ HH:mm:ss]",
    "dd MMM[ yyyy][ HH:mm:ss]",
    "MMM dd[ yyyy][ HH:mm:ss]",
  )
  val lazyEmptyList = emptyList<Any>()
  
  fun loadClasses(packageName: String): List<Class<*>> {
    val classLoader = Thread.currentThread().contextClassLoader
    fun findClasses(dir: File, packageName: String): List<Class<*>> {
      val classes = mutableListOf<Class<*>>()
      if (!dir.exists()) {
        return classes
      }
      val files = dir.listFiles()
      for (file in files!!) {
        if (file.isDirectory) {
          assert(!file.name.contains("."))
          classes.addAll(findClasses(file, "$packageName.${file.name}"))
        } else if (file.name.endsWith(".class")) {
          val clazz = try {
            Class.forName("$packageName.${file.name.substring(0, file.name.length - 6)}", true, classLoader)
          } catch (e: ExceptionInInitializerError) {
            Cordex.logger.trace("$packageName.${file.name}", e)
            continue
          }
          classes.add(clazz)
        }
      }
      return classes
    }
    
    val path = packageName.replace('.', '/')
    val resources = classLoader.getResources(path)
    val dirs = mutableListOf<File>()
    
    while (resources.hasMoreElements()) {
      val resource = resources.nextElement()
      dirs.add(File(resource.file))
    }
    val classes = mutableListOf<Class<*>>()
    for (dir in dirs) {
      classes.addAll(findClasses(dir, packageName))
    }
    
    return classes
  }
  
  fun extractDigits(s: String): String {
    return s.replace("[^0-9]".toRegex(), "")
  }
  
  fun convertCamelToKebab(input: String): String {
    val builder = StringBuilder()
    
    for (char in input) {
      if (char.isUpperCase()) {
        builder.append('-')
        builder.append(char.lowercaseChar())
      } else {
        builder.append(char)
      }
    }
    
    return builder.toString()
  }
  
  fun <T> ListIterator<T>.takeWhileWithIndex(predicate: (index: Int, T) -> Boolean): List<T> {
    val resultList = mutableListOf<T>()
    var currentIndex = 0
    
    while (hasNext()) {
      val item = next()
      if (predicate(currentIndex, item)) {
        resultList.add(item)
        currentIndex++
      } else {
        previous()
        break
      }
    }
    
    return resultList
  }
  
  fun <T> Optional<T>.hasValue() = orElse(null) != null
  
  fun <T> Optional<T>.toNullable(): T? {
    return orElse(null)
  }
  
  fun parseDuration(input: String): Duration? {
    val matchResult = DURATION_REGEX.matchEntire(input) ?: return null
    
    val (floatValueStr, _, timeUnit) = matchResult.destructured
    val floatValue = floatValueStr.toDouble()
    
    return Duration.ofSeconds(
      when (timeUnit.lowercase()) {
        "mo", "month", "months" -> (floatValue * 2592000).toLong()
        "w", "week", "weeks" -> (floatValue * 604800).toLong()
        "d", "day", "days" -> (floatValue * 8640).toLong()
        "h", "hour", "hours" -> (floatValue * 3600).toLong()
        "m", "min", "mins", "minute", "minutes" -> (floatValue * 60).toLong()
        "s", "sec", "secs", "second", "seconds" -> floatValue.toLong()
        else -> throw IllegalArgumentException()
      }
    )
  }
  
  fun parseDate(input: String, locale: Locale): LocalDateTime? {
    for (pattern in localDatePatterns) {
      try {
        val formatter = DateTimeFormatter.ofPattern(pattern, locale).withResolverStyle(ResolverStyle.STRICT)
        return LocalDateTime.parse(input, formatter)
      } catch (_: Exception) {
      }
    }
    return null
  }
}

