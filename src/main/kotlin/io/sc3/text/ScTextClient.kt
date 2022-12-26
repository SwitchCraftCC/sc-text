package io.sc3.text

import io.sc3.text.font.GlyphSizesCommand
import io.sc3.text.font.GlyphSizesTest
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback

object ScTextClient : ClientModInitializer {
  override fun onInitializeClient() {
    ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
      GlyphSizesCommand.register(dispatcher)
      GlyphSizesTest.register(dispatcher)
    }
  }
}
