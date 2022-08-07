package pw.switchcraft.text.font

import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceType
import net.minecraft.text.MutableText
import net.minecraft.text.StringVisitable
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import pw.switchcraft.text.of
import kotlin.math.ceil
import kotlin.math.floor

val SIZES_ID = Identifier("sc-text", "sc_glyph_sizes.bin")
const val LINE_WIDTH = 320
const val DEFAULT_PADDING = "="

object FontCalculator {
  var glyphSizes: ByteArray = ByteArray(65536)
  var boldGlyphSizes: ByteArray = ByteArray(65536)

  fun width(code: Int, bold: Boolean = false): Int {
    if (code == ' '.code) return 4
    return (if (bold) boldGlyphSizes[code] else glyphSizes[code]).toInt()
  }

  fun width(text: StringVisitable): Int {
    var total = 0
    var newLine = false
    TextVisitHelpers.visitFormatted(text, Style.EMPTY) { _, style, code ->
      if (code == '\n'.code) {
        if (newLine) { // Previous character is also \n
          total += LINE_WIDTH
        } else {
          total = ceil(total.toDouble() / LINE_WIDTH).toInt() * LINE_WIDTH
          newLine = true
        }
      } else {
        total += width(code, style.isBold)
        newLine = false
      }

      true
    }

    return total
  }

  fun lineCount(text: StringVisitable): Int = ceil(width(text).toDouble() / LINE_WIDTH).toInt()

  /**
   * Centers a text within the middle of the chat box.
   *
   * Generally used for titles and footers.
   *
   * To use no heading, just pass in a 0 width text for the first argument.
   *
   * @param text The text to center
   * @param padding A padding character with a width >1
   * @return The centered text, or if too big, the original text
   */
  fun center(text: Text, padding: Text = of(DEFAULT_PADDING)): Text {
    val width = width(text)
    if (width >= LINE_WIDTH) return text

    var innerText = text
    var innerTextWidth = width

    val withSpaces = surroundWithSpaces(text)
    val withSpacesWidth = width(withSpaces)
    if (withSpacesWidth <= LINE_WIDTH) {
      innerText = withSpaces
      innerTextWidth = withSpacesWidth
    }

    var padChar = padding.string
    var padWidth = width(padding)
    if (padWidth < 1) {
      padChar = DEFAULT_PADDING
      padWidth = width(padding)
    }
    val padStyle = padding.style

    if (padWidth == 0) {
      throw IllegalArgumentException("Padding character has width 0")
    }

    val out = of("")

    if (width == 0) { // Only padding wanted, no inner text
      pad(out, padChar, padStyle, LINE_WIDTH / padWidth)
    } else {
      val padNeeded = LINE_WIDTH - innerTextWidth
      val padCount = floor(padNeeded.toDouble() / padWidth).toInt()
      val beforePad = floor(padCount.toDouble() / 2).toInt()
      val afterPad = padCount - beforePad - 1 // No ceil (avoid floating point errors)

      pad(out, padChar, padStyle, beforePad)
      out.append(innerText)
      pad(out, padChar, padStyle, afterPad)
    }

    return out
  }

  private fun surroundWithSpaces(text: Text): Text = of(" ").append(text.copy()).append(" ")
  private fun pad(out: MutableText, char: String, style: Style, count: Int) {
    if (count <= 0) return
    out.append(Text.literal(char.repeat(count)).setStyle(style))
  }

  internal fun init() {
    ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(object : SimpleSynchronousResourceReloadListener {
      override fun reload(rm: ResourceManager) {
        rm.getResourceOrThrow(SIZES_ID).inputStream.use {
          glyphSizes = it.readNBytes(65536)
          boldGlyphSizes = it.readNBytes(65536)
        }
        LOG.info("Loaded glyph sizes for {} glyphs", glyphSizes.size)
      }

      override fun getFabricId(): Identifier = SIZES_ID
    })
  }

  var LOG: Logger = LoggerFactory.getLogger("ScText/FontCalculator")
}
