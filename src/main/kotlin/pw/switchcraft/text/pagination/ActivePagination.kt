package pw.switchcraft.text.pagination

import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting.BLUE
import net.minecraft.util.Formatting.UNDERLINE
import pw.switchcraft.text.*
import pw.switchcraft.text.font.FontCalculator
import pw.switchcraft.text.font.FontCalculator.lineCount
import java.util.*

/**
 * Based on the Sponge implementation of Pagination, originally licensed under MIT.
 * See: https://github.com/SpongePowered/Sponge/blob/api-8/LICENSE.txt
 */
abstract class ActivePagination(
  private val src: ServerCommandSource,
  private val title: Text? = null,
  private val header: Text? = null,
  private val footer: Text? = null,
  private val padding: Text = of("="),
  private val linesPerPage: Int = 20
) {
  internal val id = UUID.randomUUID()
  protected var currentPage = 1

  private val titleCentered = if (title != null) FontCalculator.center(title, padding) else null

  private val prevPageText = pageText(id, "prev", "«")
  private val nextPageText = pageText(id, "next", "»")

  /** The lines per page, minus the title, header, page selector footer, and footer. */
  protected val contentLinesPerPage: Int by lazy {
    var lines = linesPerPage - 1 // Page selector line
    if (title != null) lines -= lineCount(title)
    if (header != null) lines -= lineCount(header)
    if (footer != null) lines -= lineCount(footer)
    lines
  }

  abstract fun lines(page: Int): Iterable<Text>
  abstract fun hasPrevious(page: Int): Boolean
  abstract fun hasNext(page: Int): Boolean
  abstract fun totalPages(): Int

  open fun nextPage() { specificPage(currentPage + 1) }
  open fun previousPage() { specificPage(currentPage - 1) }

  /** Changes the page to the specified page and sends it to the player. */
  fun specificPage(page: Int) {
    currentPage = page

    val out = mutableListOf<Text>()

    if (titleCentered != null) out.add(titleCentered)
    if (header != null) out.add(header)

    out.addAll(lines(page))

    out.add(calculateFooter(page))
    if (footer != null) out.add(footer)

    out.forEach { src.sendFeedback(it, false) }
  }

  private fun calculateFooter(page: Int): Text {
    val hasPrevious = hasPrevious(page)
    val hasNext = hasNext(page)
    val totalPages = totalPages()

    val out = of("")

    if (hasPrevious) {
      out + prevPageText + " "
    } else {
      out + "« "
    }

    if (totalPages > 1) {
      val currentPageText = pageText(id, page)
      val lastPageText = pageText(id, totalPages)
      out + currentPageText + "/" + lastPageText + " "
    }

    if (hasNext) {
      out + nextPageText + " "
    } else {
      out + "»"
    }

    out.color(padding.style.color)
    if (title != null) out.style = title.style

    return FontCalculator.center(out, padding)
  }

  fun padPage(currentPage: MutableList<Text>, currentPageLines: Int, addContinuation: Boolean) {
    val contentLinesPerPage = contentLinesPerPage
    for (i in currentPageLines until contentLinesPerPage) {
      if (addContinuation && i == contentLinesPerPage - 1) {
        currentPage.add(of("..."))
      } else {
        currentPage.add(0, Text.empty())
      }
    }
  }

  companion object {
    fun pageText(id: UUID, page: String, text: String = page, formatting: Boolean = true): MutableText
      = (if (formatting) of(text, BLUE, UNDERLINE) else of(text))
        .runCommand("/sc-text:pagination $id $page")
        .hover(of("/page $page"))
        .shiftInsertText("/sc-text:page $page")

    fun pageText(id: UUID, page: Int): MutableText
      = pageText(id, page.toString(), "%,d".format(page), false)
  }
}
