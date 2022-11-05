package pw.switchcraft.text

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.mojang.brigadier.Command.SINGLE_SUCCESS
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.command.argument.UuidArgumentType.getUuid
import net.minecraft.command.argument.UuidArgumentType.uuid
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import java.time.Duration
import java.util.*

typealias CallbackFn = (CommandContext<ServerCommandSource>) -> Unit
data class Callback(
  val fn: CallbackFn,
  val owner: UUID? = null,
  val name: String? = null
)

object CallbackCommand {
  private val EXPIRED_EXCEPTION = SimpleCommandExceptionType(of("This callback has expired."))
  private val INVALID_OWNER_EXCEPTION = SimpleCommandExceptionType(of("This callback cannot be executed by you."))

  private val callbacks: Cache<UUID, Callback> = CacheBuilder.newBuilder()
    .expireAfterWrite(Duration.ofMinutes(10))
    .build()

  fun lookupCallback(id: UUID): Callback? = callbacks.getIfPresent(id)

  fun register(owner: UUID? = null, name: String? = null, callback: CallbackFn): UUID {
    val id = UUID.randomUUID()
    callbacks.put(id, Callback(callback, owner, name))
    return id
  }

  fun makeCommand(owner: UUID? = null, name: String? = null, callback: CallbackFn) =
    register(owner, name, callback).let { "/sc-text:callback $it" }

  internal fun register(dispatcher: CommandDispatcher<ServerCommandSource?>) {
    dispatcher.register(literal("sc-text:callback")
      .then(argument("id", uuid())
        .executes { ctx ->
          val id = getUuid(ctx, "id")
          val callback = callbacks.getIfPresent(id) ?: throw EXPIRED_EXCEPTION.create()

          if (callback.owner != null && callback.owner != ctx.source.player?.uuid) {
            throw INVALID_OWNER_EXCEPTION.create()
          }

          callback.fn.invoke(ctx)

          callbacks.invalidate(id)
          SINGLE_SUCCESS
        }))
  }
}
