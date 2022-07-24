package pw.switchcraft.text

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.mojang.brigadier.Command.SINGLE_SUCCESS
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import net.minecraft.command.argument.UuidArgumentType
import net.minecraft.command.argument.UuidArgumentType.getUuid
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import java.time.Duration
import java.util.UUID

typealias Callback = (CommandContext<ServerCommandSource>) -> Unit

object CallbackCommand {
  private val callbacks: Cache<UUID, Callback> = CacheBuilder.newBuilder()
    .expireAfterWrite(Duration.ofMinutes(10))
    .build()

  fun register(callback: Callback): UUID {
    val id = UUID.randomUUID()
    callbacks.put(id, callback)
    return id
  }

  fun makeCommand(callback: Callback) = register(callback).let { "/sc-text:callback $it" }

  internal fun register(dispatcher: CommandDispatcher<ServerCommandSource?>) {
    dispatcher.register(literal("sc-text:callback")
      .then(argument("id", UuidArgumentType.uuid())
        .executes {
          val id = getUuid(it, "id")
          callbacks.getIfPresent(id)?.invoke(it).let { callbacks.invalidate(id); SINGLE_SUCCESS }
        }))
  }
}
