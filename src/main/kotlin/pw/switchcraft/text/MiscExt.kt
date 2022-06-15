package pw.switchcraft.text

import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting.*
import java.net.URL
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.regex.Pattern

// Extensions to convert other objects to Text

val FORMATTING_CODE_PATTERN: Pattern = Pattern.compile("&([\\da-fklmno])")
fun String.replaceLegacyFormatting(): String = FORMATTING_CODE_PATTERN.matcher(this).replaceAll("\u00A7$1")
fun String.legacyFormattingToText(): MutableText = of(this.replaceLegacyFormatting())

fun String.urlToText(contents: Text? = null): MutableText {
  val base = if (contents != null) {
    if (contents is MutableText) contents else contents.copy()
  } else of(this)

  return base.formatted(UNDERLINE, BLUE)
    .hover(of("Click to visit $this", AQUA))
    .openUrl(this)
}

fun URL.toText(contents: Text? = null): MutableText = toString().urlToText(contents)

// Date formatting - TODO: Add more formats, utilities for Text with hover (give full ISO-8601 timestamp)
val DATE_FORMAT_LONG: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z")
fun ZonedDateTime.formatScLong(): String = format(DATE_FORMAT_LONG)

// This one isn't strictly Text, but it's very often useful anyway
fun tryParseUuid(string: String): UUID? = try {
  UUID.fromString(string)
} catch (e: IllegalArgumentException) {
  null
}
