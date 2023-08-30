package me.blast.command

import me.blast.command.argument.Argument
import me.blast.utils.Utils.lazyEmptyList

class Arguments(@PublishedApi internal val args: Map<String, Any>) {
  inline operator fun <reified T> get(key: Argument<T>): T {
    return if(T::class == List::class) {
      args[key.argumentName] as? T ?: lazyEmptyList as T
    } else {
      args[key.argumentName] as T
    }
  }
}