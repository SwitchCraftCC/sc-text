package pw.switchcraft.text.pagination

import com.google.common.collect.Iterators
import com.google.common.collect.Lists
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import pw.switchcraft.text.of

/**
 * Based on the Sponge implementation of Pagination, originally licensed under MIT.
 * See: https://github.com/SpongePowered/Sponge/blob/api-8/LICENSE.txt
 */
internal class IterablePagination(
  src: ServerCommandSource,
  counts: Iterable<Map.Entry<Text, Int>>,
  title: Text? = null,
  header: Text? = null,
  footer: Text? = null,
  padding: Text = of("="),
  linesPerPage: Int = 20
) : ActivePagination(src, title, header, footer, padding, linesPerPage) {
  private val countIterator = Iterators.peekingIterator(counts.iterator())
  private var lastPage = 1

  override fun lines(page: Int): Iterable<Text> {
    if (!countIterator.hasNext()) throw PAGE_END_EXCEPTION.create()
    if (page < 1) throw PAGE_TOO_LOW_EXCEPTION.create(page)

    if (page <= lastPage) throw PAGE_BACKWARDS_EXCEPTION.create()
    else if (page > lastPage + 1) lines(page - 1) // Force iteration
    this.lastPage = page

    if (contentLinesPerPage <= 0) {
      return Lists.newArrayList(Iterators.transform(countIterator) { it.key })
    }

    val out = ArrayList<Text>(contentLinesPerPage)
    var addedLines = 0
    while (addedLines <= contentLinesPerPage) {
      if (!countIterator.hasNext()) {
        // Pad the last page, but only if it isn't the first
        if (page > 1) padPage(out, addedLines, false)
        break
      }

      if (addedLines + this.countIterator.peek().value > contentLinesPerPage) {
        // Add the continuation marker, pad if required
        padPage(out, addedLines, true)
        break
      }

      val next = countIterator.next()
      out.add(next.key)
      addedLines += next.value
    }
    return out
  }

  override fun hasPrevious(page: Int): Boolean = false
  override fun hasNext(page: Int): Boolean = page == currentPage && countIterator.hasNext()
  override fun totalPages(): Int = -1

  companion object {
    private val PAGE_END_EXCEPTION = SimpleCommandExceptionType(of("End of pagination reached."))
    private val PAGE_TOO_LOW_EXCEPTION = DynamicCommandExceptionType { of("Page $it does not exist.") }
    private val PAGE_BACKWARDS_EXCEPTION = SimpleCommandExceptionType(
      of("You cannot go to the previous page in this pagination.")
    )
  }
}
