package com.yogeshpaliyal.deepr.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.yogeshpaliyal.deepr.DeeprQueries
import com.yogeshpaliyal.deepr.GetLinksAndTags
import com.yogeshpaliyal.deepr.Tags
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AccountPagingSource(
    private val deeprQueries: DeeprQueries,
    private val searchQuery: String,
    private val favouriteFilter: Int,
    private val selectedTags: List<Tags>,
    private val sortField: String,
    private val sortType: String,
) : PagingSource<Int, GetLinksAndTags>() {
    override fun getRefreshKey(state: PagingState<Int, GetLinksAndTags>): Int? =
        state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(state.config.pageSize)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(state.config.pageSize)
        }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, GetLinksAndTags> =
        try {
            val offset = params.key ?: 0
            val limit = params.loadSize

            val tagIdsString =
                if (selectedTags.isEmpty()) "" else selectedTags.joinToString(",") { it.id.toString() }
            val tagCount = selectedTags.size.toLong()

            // Fetch data using the paged query
            val dataFromPagedQuery =
                withContext(Dispatchers.IO) {
                    deeprQueries
                        .getLinksAndTagsPaged(
                            searchQuery = searchQuery,
                            favouriteFilter = favouriteFilter.toLong(),
                            tagIdsString = tagIdsString,
                            tagCount = tagCount,
                            sortField = sortField,
                            sortType = sortType,
                            limit = limit.toLong(),
                            offset = offset.toLong(),
                        ).executeAsList()
                }

            // Data mapping
            val mappedData =
                dataFromPagedQuery.map { pagedItem ->
                    GetLinksAndTags(
                        id = pagedItem.id,
                        link = pagedItem.link,
                        name = pagedItem.name,
                        createdAt = pagedItem.createdAt,
                        openedCount = pagedItem.openedCount,
                        isFavourite = pagedItem.isFavourite,
                        notes = pagedItem.notes,
                        lastOpenedAt = pagedItem.lastOpenedAt,
                        tagsNames = pagedItem.tagsNames,
                        tagsIds = pagedItem.tagsIds,
                    )
                }

            LoadResult.Page(
                data = mappedData,
                prevKey = if (offset == 0) null else offset - limit,
                nextKey = if (mappedData.isEmpty()) null else offset + limit,
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
}
