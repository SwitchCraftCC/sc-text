package pw.switchcraft.text.pagination

import com.google.common.collect.ImmutableList
import com.google.common.collect.Maps
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import pw.switchcraft.text.font.FontCalculator
import pw.switchcraft.text.of
import java.util.stream.Collectors
import java.util.stream.StreamSupport

/**
 * Based on the Sponge implementation of Pagination, originally licensed under MIT.
 * See: https://github.com/SpongePowered/Sponge/blob/api-8/LICENSE.txt
 */
class Pagination(
  private val contents: Iterable<Text>,
  private val title: Text? = null,
  private val header: Text? = null,
  private val footer: Text? = null,
  private val padding: Text = of("="),
  private val linesPerPage: Int = 20
) {
  fun sendTo(src: ServerCommandSource, page: Int = 1) {
    val counts = StreamSupport.stream(contents.spliterator(), false).map {
      val lines = FontCalculator.lineCount(it)
      Maps.immutableEntry(it, lines)
    }.collect(Collectors.toList())

    // If the contents is a list, copy it
    val pagination = if (contents is List<*>) {
      ListPagination(src, ImmutableList.copyOf(counts), title, header, footer, padding, linesPerPage)
    } else {
      IterablePagination(src, counts, title, header, footer, padding, linesPerPage)
    }

    PaginationHandler.paginationState(src, true).put(pagination)
    pagination.specificPage(page)
  }
}
