package pw.switchcraft.text.font

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType.getString
import com.mojang.brigadier.arguments.StringArgumentType.greedyString
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.MinecraftClient
import net.minecraft.client.render.*
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Formatting.GREEN
import pw.switchcraft.text.of

object GlyphSizesTest {
  private var testing = false
  private var registered = false

  private var cachedGlyphWidths : ByteArray? = null
  private var text = "Hello, world!"

  @Environment(EnvType.CLIENT)
  private fun render(matrices: MatrixStack) {
    val glyphWidths = cachedGlyphWidths ?: return

    var x = 16.0
    val y = 16.0

    val tessellator = Tessellator.getInstance()
    val buffer = tessellator.buffer

    val matrix = matrices.peek().positionMatrix
    val textRenderer = MinecraftClient.getInstance().textRenderer
    val immediate = VertexConsumerProvider.immediate(buffer)
    textRenderer.draw(text, x.toFloat(), y.toFloat(), 0xFF0000, false, matrix, immediate, false, 0,
      LightmapTextureManager.MAX_LIGHT_COORDINATE)
    immediate.draw()

    RenderSystem.enableBlend()
    RenderSystem.disableTexture()
    RenderSystem.defaultBlendFunc()
    RenderSystem.setShader(GameRenderer::getPositionColorProgram)

    buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR)
    text.forEachIndexed { i, char ->
      val advance = glyphWidths[char.code]
      x = line(buffer, i, x, y + 8.0, advance.toDouble())
    }
    tessellator.draw()

    RenderSystem.enableTexture()
    RenderSystem.disableBlend()
  }

  @Environment(EnvType.CLIENT)
  private fun line(buffer: BufferBuilder, i: Int, x: Double, y: Double, advance: Double): Double {
    val r = (i % 2) * 1.0f
    val g = ((i + 1) % 2) * 1.0f
    val b = 0.0f
    buffer.vertex(x, y, 0.0).color(r, g, b, 1.0f).next()
    buffer.vertex(x, y + 2, 0.0).color(r, g, b, 1.0f).next()
    buffer.vertex(x + advance, y + 2, 0.0).color(r, g, b, 1.0f).next()
    buffer.vertex(x + advance, y, 0.0).color(r, g, b, 1.0f).next()
    return x + advance
  }

  private fun toggle(text: String?) {
    if (text == null) {
      testing = false
      cachedGlyphWidths = null
      return
    }

    cachedGlyphWidths = GlyphSizesCommand.genGlyphSizes()
    testing = true
    this.text = text

    if (!registered) {
      registered = true
      HudRenderCallback.EVENT.register { matrixStack, _ ->
        if (testing) render(matrixStack)
      }
    }
  }

  internal fun register(dispatcher: CommandDispatcher<FabricClientCommandSource>) {
    dispatcher.register(ClientCommandManager.literal("sc-text:test_glyph_sizes")
      .then(ClientCommandManager.argument("text", greedyString())
        .executes {
          val text = getString(it, "text")
          toggle(text)

          val out = FontCalculator.center(of(text, GREEN))
          it.source.sendFeedback(out)

          Command.SINGLE_SUCCESS
        })
      .executes {
        toggle(null)
        Command.SINGLE_SUCCESS
      })
  }
}
