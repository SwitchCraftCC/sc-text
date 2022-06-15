package pw.switchcraft.text

import net.fabricmc.api.ModInitializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object ScText : ModInitializer {
  val LOG: Logger = LoggerFactory.getLogger("ScText")

  override fun onInitialize() {
    LOG.info("sc-text initializing")
    // TODO: Callback and pagination command registration here
  }
}