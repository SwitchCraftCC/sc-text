package io.sc3.text

import net.minecraft.text.StringVisitable
import net.minecraft.text.Style
import net.minecraft.text.TextContent
import java.util.*

class TokenTextContent(val token: String) : TextContent {
  override fun toString() = "token{***}"

  override fun <T> visit(visitor: StringVisitable.StyledVisitor<T>, style: Style): Optional<T> {
    // For styled visitors (e.g. rendering in-game), render the token as normal
    return visitor.accept(style, token)
  }

  override fun <T> visit(visitor: StringVisitable.Visitor<T>): Optional<T> {
    // For unstyled visitors (e.g. logs), censor the token
    return visitor.accept("***")
  }
}
