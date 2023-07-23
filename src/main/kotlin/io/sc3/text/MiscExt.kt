package io.sc3.text

import com.google.gson.GsonBuilder
import net.minecraft.text.MutableText
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting.AQUA
import net.minecraft.util.Formatting.RED
import net.minecraft.util.LowercaseEnumTypeAdapterFactory
import org.apache.commons.lang3.time.DurationFormatUtils
import java.net.URL
import java.net.URLEncoder
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.regex.Pattern
import kotlin.math.abs

// Extensions to convert other objects to Text

val FORMATTING_CODE_PATTERN: Pattern = Pattern.compile("&([\\da-fklmno])")
fun String.replaceLegacyFormatting(): String = FORMATTING_CODE_PATTERN.matcher(this).replaceAll("\u00A7$1")
fun String.legacyFormattingToText(): MutableText = of(this.replaceLegacyFormatting())

fun String.urlToText(contents: Text? = null): MutableText {
  val base = if (contents != null) {
    if (contents is MutableText) contents else contents.copy()
  } else of(this)

  return base.formatted(*linkFormatting)
    .hover(of("Click to visit: ") + of(this).formatted(AQUA))
    .openUrl(this)
}

fun URL.toText(contents: Text? = null): MutableText = toString().urlToText(contents)

fun Boolean.toText(): MutableText = if (this) of("enabled").color(MID_GREEN) else of("disabled", RED)

// Date formatting - TODO: Add more formats, utilities for Text with hover (give full ISO-8601 timestamp)
val LOCAL_DATE_FORMAT_LONG: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
val ZONED_DATE_FORMAT_LONG: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss Z")
fun LocalDateTime.formatScLong(): String = format(LOCAL_DATE_FORMAT_LONG)
fun ZonedDateTime.formatScLong(): String = format(ZONED_DATE_FORMAT_LONG)

fun ZonedDateTime.formatDurationWords(): String {
  val duration = abs(toInstant().toEpochMilli() - ZonedDateTime.now().toInstant().toEpochMilli())
  return DurationFormatUtils.formatDurationWords(duration, true, true)
}

fun ZonedDateTime.formatDurationWordsShort(): String {
  // Takes the biggest two units (e.g. 24 hours 30 minutes)
  val base = formatDurationWords()
  val split = base.split(" ")
  return split.take(4).joinToString(" ")
}

// This one isn't strictly Text, but it's very often useful anyway
fun tryParseUuid(string: String): UUID? = try {
  UUID.fromString(string)
} catch (e: IllegalArgumentException) {
  null
}

fun String.plural(count: Int, plural: String = this + "s"): String = if (count == 1) this else plural
fun String.count(count: Int, plural: String = this + "s"): String = "$count ${plural(count, plural)}"
fun String.boldCount(count: Int, plural: String = this + "s"): String = "**$count** ${plural(count, plural)}"

fun String.urlEncode(): String = URLEncoder.encode(this, "UTF-8")

fun String?.orNull(): String? = if (this == null || this == "") null else this

// Based on the default Minecraft Text type adapter, Text.Serializer.GSON
fun GsonBuilder.registerMinecraftTextAdapter(): GsonBuilder = apply {
  disableHtmlEscaping()
  registerTypeHierarchyAdapter(Text::class.java, Text.Serializer())
  registerTypeHierarchyAdapter(Style::class.java, Style.Serializer())
  registerTypeAdapterFactory(LowercaseEnumTypeAdapterFactory())
}
