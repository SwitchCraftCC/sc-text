package pw.switchcraft.text

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import pw.switchcraft.text.font.GlyphSizesCommand
import pw.switchcraft.text.font.GlyphSizesTest

object ScTextClient : ClientModInitializer {
  override fun onInitializeClient() {
    ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
      GlyphSizesCommand.register(dispatcher)
      GlyphSizesTest.register(dispatcher)
    }
  }
}
