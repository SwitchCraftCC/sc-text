package pw.switchcraft.text.pagination

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.google.common.collect.MapMaker
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.IntegerArgumentType.getInteger
import com.mojang.brigadier.arguments.IntegerArgumentType.integer
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.command.CommandSource.suggestMatching
import net.minecraft.command.argument.UuidArgumentType
import net.minecraft.command.argument.UuidArgumentType.uuid
import net.minecraft.server.command.CommandManager.argument
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import pw.switchcraft.text.of
import java.time.Duration
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

object PaginationHandler {
  private val NO_PAGINATIONS_EXCEPTION = SimpleCommandExceptionType(of("No active paginations found."))
  private val PAGINATION_NOT_FOUND_EXCEPTION = DynamicCommandExceptionType { of("Paginations $it not found.") }

  private val activePaginations: ConcurrentMap<ServerCommandSource, SourcePaginations> = MapMaker().weakKeys().makeMap()

  /** Since Player entities disappear after a player dies, they can't be used as a key in the activePaginations map. */
  private val playerPaginations: Cache<UUID, SourcePaginations> = CacheBuilder.newBuilder()
    .expireAfterAccess(Duration.ofMinutes(10))
    .build()

  fun paginationState(src: ServerCommandSource, create: Boolean): SourcePaginations {
    val uuid = src.entity?.uuid
    return if (src.isExecutedByPlayer && uuid != null) {
      playerPaginations.get(uuid) { if (create) SourcePaginations() else null }
    } else {
      var out = activePaginations[src]
      if (out == null && create) {
        out = SourcePaginations()
        val existing = activePaginations.putIfAbsent(src, out)
        if (existing != null) out = existing
      }
      out ?: throw NO_PAGINATIONS_EXCEPTION.create()
    }
  }

  internal fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
    val node = dispatcher.register(literal("sc-text:pagination")
      // With pagination ID argument
      .then(argument("pagination-id", uuid())
        .suggests { ctx, builder -> suggestMatching(paginationState(ctx.source, false).keysString(), builder) }
        .then(literal("next").executes { pagination(it).nextPage(); 1 })
        .then(literal("prev").executes { pagination(it).previousPage(); 1 })
        .then(argument("page", integer()).executes { pagination(it).specificPage(getInteger(it, "page")); 1 }))

      // Without pagination ID argument (last active pagination, if available)
      .then(literal("next").executes { pagination(it, true).nextPage(); 1 })
      .then(literal("prev").executes { pagination(it, true).previousPage(); 1 })
      .then(argument("page", integer()).executes { pagination(it, true).specificPage(getInteger(it, "page")); 1 })
    )

    dispatcher.register(literal("pagination").redirect(node))
    dispatcher.register(literal("page").redirect(node))
    dispatcher.register(literal("sc-text:page").redirect(node))
  }

  private fun pagination(ctx: CommandContext<ServerCommandSource>, last: Boolean = false): ActivePagination {
    val state = paginationState(ctx.source, false)
    val id = (if (last) state.lastUuid else UuidArgumentType.getUuid(ctx, "pagination-id"))
      ?: throw NO_PAGINATIONS_EXCEPTION.create()
    return state.pagination(id)
      ?: throw PAGINATION_NOT_FOUND_EXCEPTION.create(id.toString())
  }

  class SourcePaginations {
    private val paginations = ConcurrentHashMap<UUID, ActivePagination>()
    var lastUuid: UUID? = null

    fun pagination(id: UUID): ActivePagination? = paginations[id]

    fun put(pagination: ActivePagination) {
      paginations.compute(pagination.id) { _, _ ->
        lastUuid = pagination.id
        pagination
      }
    }

    fun keys(): Set<UUID> = paginations.keys.toSet()
    fun keysString(): Set<String> = paginations.keys.map { it.toString() }.toSet()
  }
}
