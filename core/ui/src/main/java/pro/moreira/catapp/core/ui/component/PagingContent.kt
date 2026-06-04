package pro.moreira.catapp.core.ui.component

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems

@Composable
fun <T : Any> PagingContent(
    items: LazyPagingItems<T>,
    modifier: Modifier = Modifier,
    empty: @Composable (Modifier) -> Unit = {},
    error: @Composable (Throwable, () -> Unit, Modifier) -> Unit = { _, _, _ -> },
    content: @Composable (LazyPagingItems<T>, Modifier) -> Unit,
) {
    when {
        items.loadState.refresh is LoadState.Loading -> {
            FullScreenLoading(modifier = modifier)
        }

        items.loadState.refresh is LoadState.Error && items.itemCount == 0 -> {
            val throwable = (items.loadState.refresh as LoadState.Error).error
            error(throwable, items::retry, modifier)
        }

        items.itemCount == 0 -> {
            empty(modifier)
        }

        else -> {
            content(items, modifier)
        }
    }
}

fun <T : Any> LazyListScope.pagingAppendState(
    items: LazyPagingItems<T>,
    error: @Composable (Throwable, () -> Unit) -> Unit = { _, _ -> },
) {
    when (val append = items.loadState.append) {
        is LoadState.Loading -> item { AppendLoading() }
        is LoadState.Error -> item { error(append.error, items::retry) }
        else -> Unit
    }
}
