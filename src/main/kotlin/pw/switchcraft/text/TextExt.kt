package pw.switchcraft.text

import com.mojang.brigadier.context.CommandContext
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.*
import net.minecraft.text.ClickEvent.Action.*
import net.minecraft.text.HoverEvent.Action.*
import net.minecraft.text.HoverEvent.EntityContent
import net.minecraft.text.HoverEvent.ItemStackContent
import net.minecraft.util.Formatting
import net.minecraft.util.Formatting.*
import pw.switchcraft.text.CallbackCommand.makeCommand
import java.net.URL
import java.util.*

fun of(text: String?, formatting: List<Formatting>): MutableText = of(text, *formatting.toTypedArray())
fun of(text: String?, vararg formatting: Formatting): MutableText = Text.literal(text ?: "").formatted(*formatting)

operator fun MutableText.plus(other: Text): MutableText = append(other)
operator fun MutableText.plus(other: String): MutableText = append(other)
operator fun MutableText.plusAssign(other: Text) { append(other) }
operator fun MutableText.plusAssign(other: String) { append(other) }

fun <T> MutableText.hoverEvent(action: HoverEvent.Action<T>, value: T): MutableText =
  styled { it.withHoverEvent(if (value != null) HoverEvent(action, value) else null) }

fun MutableText.clickEvent(action: ClickEvent.Action, value: String?): MutableText =
  styled { it.withClickEvent(if (value != null) ClickEvent(action, value) else null) }

// HoverEvent wrappers
fun MutableText.hover(hover: Text?): MutableText = hoverEvent(SHOW_TEXT, hover)
fun MutableText.hover(hover: ItemStackContent?): MutableText = hoverEvent(SHOW_ITEM, hover)
fun MutableText.hover(hover: EntityContent?): MutableText = hoverEvent(SHOW_ENTITY, hover)

// ClickEvent wrappers
fun MutableText.openUrl(url: String?): MutableText = clickEvent(OPEN_URL, url)
fun MutableText.openUrl(url: URL?): MutableText = openUrl(url.toString())
fun MutableText.openFile(file: String?): MutableText = clickEvent(OPEN_FILE, file)
fun MutableText.runCommand(cmd: String?): MutableText = clickEvent(RUN_COMMAND, cmd)
fun MutableText.suggestCommand(cmd: String?): MutableText = clickEvent(SUGGEST_COMMAND, cmd)
fun MutableText.changePage(page: String?): MutableText = clickEvent(CHANGE_PAGE, page)
fun MutableText.copyToClipboard(text: String?): MutableText = clickEvent(COPY_TO_CLIPBOARD, text)

// Other style wrappers
fun MutableText.shiftInsertText(text: String?): MutableText = styled { it.withInsertion(text) }
fun MutableText.color(color: Int): MutableText = styled { it.withColor(color) }
fun MutableText.color(color: Formatting?): MutableText = styled { it.withColor(color) }
fun MutableText.color(color: TextColor?): MutableText = styled { it.withColor(color) }

fun MutableText.copyable(clipboardText: String? = this.string): MutableText =
  hover(copyHint()).copyToClipboard(clipboardText)
fun copyable(text: String?, vararg formatting: Formatting, clipboardText: String? = text): MutableText =
  of(text, *formatting).hover(copyHint()).copyToClipboard(clipboardText)

val linkFormatting = arrayOf(UNDERLINE, BLUE)
fun openableLink(text: String?, vararg formatting: Formatting = linkFormatting, link: String? = text): MutableText =
  of(text, *formatting).openUrl(link).hover(of("Click to visit: ") + of(link, AQUA))
fun runnableCommand(text: String?, vararg formatting: Formatting = linkFormatting, cmd: String? = text): MutableText =
  of(text, *formatting).runCommand(cmd).hover(of("Click to run: ") + of(cmd, AQUA))
fun suggestedCommand(text: String?, vararg formatting: Formatting = linkFormatting, cmd: String? = text): MutableText =
  of(text, *formatting).suggestCommand(cmd).hover(of("Click to suggest: ") + of(cmd, AQUA))

fun MutableText.callback(callback: (CommandContext<ServerCommandSource>) -> Unit): MutableText =
  runCommand(makeCommand(callback))

// Common text patterns
const val KRIST_SYMBOL = "\ue000"
const val SMALL_SPACE = "\ue001"

const val MID_GREEN = 0x2BD52B
fun success(): MutableText = of("", GREEN) + of("Success! ", DARK_GREEN)
fun copyHint(): MutableText = of("Click to copy to clipboard.")

val kristFormatting = arrayOf(GREEN)
fun formatKristValue(value: Int, long: Boolean = false, vararg formatting: Formatting = kristFormatting): MutableText =
  of("%s%,d%s".format(Locale.ENGLISH, KRIST_SYMBOL, value, if (long) " KST" else ""), *formatting)

fun formatKristValue(value: Int, long: Boolean = false, formatting: List<Formatting>)
  = formatKristValue(value, long, *formatting.toTypedArray())
