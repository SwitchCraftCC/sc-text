package pw.switchcraft.text

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.util.Formatting.GREEN
import net.minecraft.util.Formatting.RED
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import pw.switchcraft.text.font.FontCalculator
import pw.switchcraft.text.font.GlyphSizesCommand
import pw.switchcraft.text.font.GlyphSizesTest
import pw.switchcraft.text.pagination.Pagination
import pw.switchcraft.text.pagination.PaginationHandler

object ScText : ModInitializer {
  val LOG: Logger = LoggerFactory.getLogger("ScText")

  override fun onInitialize() {
    LOG.info("sc-text initializing")

    FontCalculator.init()

    CommandRegistrationCallback.EVENT.register { dispatcher, _, environment ->
      CallbackCommand.register(dispatcher)
      PaginationHandler.register(dispatcher)

      dispatcher.register(literal("pagetest").executes {
        val data = List(100) { n -> of("Row $n\nRow $n\nRow $n", if (n % 2 == 0) RED else GREEN) }

        Pagination(data, title=of("My favorite numbers", RED))
          .sendTo(it.source)

        1
      })

      if (environment.integrated) {
        GlyphSizesCommand.register(dispatcher)
        GlyphSizesTest.register(dispatcher)
      }
    }
  }
}
