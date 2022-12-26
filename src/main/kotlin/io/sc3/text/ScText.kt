package io.sc3.text

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import io.sc3.text.font.FontCalculator
import io.sc3.text.pagination.PaginationHandler

object ScText : ModInitializer {
  val log: Logger = LoggerFactory.getLogger("ScText")

  override fun onInitialize() {
    log.info("sc-text initializing")

    FontCalculator.init()

    CommandRegistrationCallback.EVENT.register { dispatcher, _, _ ->
      CallbackCommand.register(dispatcher)
      PaginationHandler.register(dispatcher)
    }
  }
}
