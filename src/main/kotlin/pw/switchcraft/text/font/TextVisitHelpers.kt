package pw.switchcraft.text.font

import net.minecraft.text.CharacterVisitor
import net.minecraft.text.StringVisitable
import net.minecraft.text.Style
import net.minecraft.util.Formatting
import net.minecraft.util.Formatting.FORMATTING_CODE_PREFIX
import net.minecraft.util.Formatting.RESET
import net.minecraft.util.Unit
import java.util.*

private const val REPLACEMENT = '�'.code
private val VISIT_TERMINATED = Optional.of(Unit.INSTANCE)

/** Server port of visitFormatted from the client's TextVisitFactory */
object TextVisitHelpers {
  private fun visitRegularCharacter(style: Style, visitor: CharacterVisitor, index: Int, c: Char): Boolean
    = if (Character.isSurrogate(c)) visitor.accept(index, style, REPLACEMENT)
      else visitor.accept(index, style, c.code)

  fun visitFormatted(text: String, startIndex: Int, startingStyle: Style, resetStyle: Style,
                     visitor: CharacterVisitor): Boolean {
    val i = text.length
    var style = startingStyle
    
    var j = startIndex
    while (j < i) {
      val c = text[j]
      if (c.code == FORMATTING_CODE_PREFIX.code) {
        if (j + 1 >= i) break
        
        val d = text[j + 1]
        val formatting = Formatting.byCode(d)
        if (formatting != null) {
          style = if (formatting == RESET) resetStyle else style.withExclusiveFormatting(formatting)
        }
        
        j++
      } else if (Character.isHighSurrogate(c)) {
        if (j + 1 >= i) {
          if (!visitor.accept(j, style, REPLACEMENT)) return false
          break
        }
        
        val d = text[j + 1]
        if (Character.isLowSurrogate(d)) {
          if (!visitor.accept(j, style, Character.toCodePoint(c, d))) return false
          j++
        } else if (!visitor.accept(j, style, REPLACEMENT)) {
          return false
        }
      } else if (!visitRegularCharacter(style, visitor, j, c)) {
        return false
      }

      j++
    }
    
    return true
  }

  fun visitFormatted(text: StringVisitable, style: Style, visitor: CharacterVisitor): Boolean {
    return !text.visit({ styleX: Style, string: String ->
      if (visitFormatted(string, 0, styleX, styleX, visitor)) Optional.empty()
      else VISIT_TERMINATED
    }, style).isPresent
  }
}
