package pw.switchcraft.text

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import pw.switchcraft.text.font.FontCalculator
import pw.switchcraft.text.font.GlyphSizesCommand
import pw.switchcraft.text.font.GlyphSizesTest
import pw.switchcraft.text.pagination.PaginationHandler

object ScText : ModInitializer {
  val LOG: Logger = LoggerFactory.getLogger("ScText")

  override fun onInitialize() {
    LOG.info("sc-text initializing")

    FontCalculator.init()

    CommandRegistrationCallback.EVENT.register { dispatcher, _, environment ->
      CallbackCommand.register(dispatcher)
      PaginationHandler.register(dispatcher)

      if (environment.integrated) {
        GlyphSizesCommand.register(dispatcher)
        GlyphSizesTest.register(dispatcher)
      }
    }
  }
}
