package pw.switchcraft.text

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object ScText : ModInitializer {
  val LOG: Logger = LoggerFactory.getLogger("ScText")

  override fun onInitialize() {
    LOG.info("sc-text initializing")
    // TODO: Pagination

    CommandRegistrationCallback.EVENT.register { dispatcher, _, environment ->
      if (environment.dedicated) { // TODO: Client callback command
        CallbackCommand.register(dispatcher)
      }
    }
  }
}
