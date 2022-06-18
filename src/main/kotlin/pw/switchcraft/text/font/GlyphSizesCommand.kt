package pw.switchcraft.text.font

import com.mojang.brigadier.Command.SINGLE_SUCCESS
import com.mojang.brigadier.CommandDispatcher
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.client.font.UnicodeTextureFont
import net.minecraft.server.command.CommandManager.literal
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.util.Identifier
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Paths
import kotlin.math.roundToInt

object GlyphSizesCommand {
  @Environment(EnvType.CLIENT)
  fun genGlyphSizes(): ByteArray {
    val textRenderer = MinecraftClient.getInstance().textRenderer

    val widths = ByteArray(65536)
    val boldWidths = ByteArray(65536)

    val fontStorage = textRenderer.getFontStorage(Identifier("minecraft", "default"))
    fontStorage.fonts.forEach { font ->
      font.providedGlyphs.forEach glyphs@ {
        if (it > 65535) return@glyphs
        val glyph = font.getGlyph(it) ?: return@glyphs

        val alreadyProvided = widths[it] != 0.toByte() && font is UnicodeTextureFont
        if (alreadyProvided) return@glyphs

        val advance = glyph.advance.roundToInt().toByte()
        if (advance > 0) {
          // LOG.info("Char $it: ${glyph.advance}")
          widths[it] = advance
          boldWidths[it] = glyph.getAdvance(true).roundToInt().toByte()
        }
      }
    }

    // Space is provided a bit differently, so we need to manually set it
    widths[' '.code] = 4
    boldWidths[' '.code] = 4 // TODO: Verify

    Paths.get("sc_glyph_sizes.bin").toFile().outputStream().use {
      it.write(widths)
      it.write(boldWidths)
    }
    return widths
  }

  internal fun register(dispatcher: CommandDispatcher<ServerCommandSource>) {
    dispatcher.register(literal("sc_text:gen_glyph_sizes").executes {
      genGlyphSizes()
      SINGLE_SUCCESS
    })
  }

  var LOG: Logger = LoggerFactory.getLogger("ScText/GlyphSizesCommand")
}
