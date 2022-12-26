package io.sc3.text.pagination

import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import io.sc3.text.of

/**
 * Based on the Sponge implementation of Pagination, originally licensed under MIT.
 * See: https://github.com/SpongePowered/Sponge/blob/api-8/LICENSE.txt
 */
internal class ListPagination(
  src: ServerCommandSource,
  private val lines: List<Map.Entry<Text, Int>>,
  title: Text? = null,
  header: Text? = null,
  footer: Text? = null,
  padding: Text = of("="),
  linesPerPage: Int = 20
) : ActivePagination(src, title, header, footer, padding, linesPerPage) {
  private val pages: List<List<Text>> by lazy {
    val pages = mutableListOf<List<Text>>()
    var currentPage = mutableListOf<Text>()
    var currentPageLines = 0

    lines.forEach {
      if (contentLinesPerPage > 0
        && it.value + currentPageLines > contentLinesPerPage
        && currentPageLines != 0) {
        padPage(currentPage, currentPageLines, true)
        currentPageLines = 0
        pages.add(currentPage)
        currentPage = mutableListOf()
      }

      currentPageLines += it.value
      currentPage.add(it.key)
    }

    if (currentPageLines > 0) {
      if (pages.isNotEmpty()) {
        // Only pad if we have a previous page
        padPage(currentPage, currentPageLines, false)
      }
      pages.add(currentPage)
    }

    pages
  }

  override fun lines(page: Int): Iterable<Text> {
    val size = pages.size
    if (size == 0) return emptyList()
    if (page < 1) throw PAGE_TOO_LOW_EXCEPTION.create(page)
    if (page > size) throw PAGE_TOO_HIGH_EXCEPTION.create(page, size)
    return pages[page - 1]
  }

  override fun hasPrevious(page: Int): Boolean = page > 1
  override fun hasNext(page: Int): Boolean = page < pages.size
  override fun totalPages(): Int = pages.size

  companion object {
    private val PAGE_TOO_LOW_EXCEPTION = DynamicCommandExceptionType { of("Page $it does not exist.") }
    private val PAGE_TOO_HIGH_EXCEPTION = Dynamic2CommandExceptionType { page, size ->
      of("Page $page is greater than the max of $size.")
    }
  }
}
