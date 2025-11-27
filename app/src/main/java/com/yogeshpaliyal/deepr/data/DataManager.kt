package com.yogeshpaliyal.deepr.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.yogeshpaliyal.deepr.DeeprQueries
import com.yogeshpaliyal.deepr.GetAllTagsWithCount
import com.yogeshpaliyal.deepr.server.LinksListData
import com.yogeshpaliyal.deepr.viewmodel.SortType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class DataProvider(
    private val deeprQueries: DeeprQueries,
) {
    fun getLinks(
        coroutineScope: CoroutineScope,
        searchQuery: Flow<String>,
        sortOrder: Flow<@SortType String>,
        selectedTagFilter: Flow<List<GetAllTagsWithCount>>,
        favouriteFilter: Flow<Int>,
    ): StateFlow<LinksListData?> =
        combine(
            searchQuery,
            sortOrder,
            selectedTagFilter,
            favouriteFilter,
        ) { query, sorting, tags, favourite ->
            listOf(query, sorting, tags, favourite)
        }.flatMapLatest { combined ->
            val query = combined[0] as String
            val sorting = (combined[1] as String).split("_")
            val tags = combined[2] as List<GetAllTagsWithCount>
            val favourite = combined[3] as Int
            val sortField = sorting.getOrNull(0) ?: "createdAt"
            val sortType = sorting.getOrNull(1) ?: "DESC"

            // Prepare tag filter parameters
            val tagIdsString =
                if (tags.isEmpty()) "" else tags.joinToString(",") { it.id.toString() }

            deeprQueries
                .getLinksAndTags(
                    query,
                    query,
                    query,
                    favourite.toLong(),
                    favourite.toLong(),
                    tagIdsString,
                    tagIdsString,
                    sortType,
                    sortField,
                    sortType,
                    sortField,
                ).asFlow()
                .map {
                    val dbList = it.executeAsList()
                    val mappedList = dbList.map { dbObj -> DeeprLink(dbObj) }
                    LinksListData(mappedList)
                }
        }.stateIn(coroutineScope, SharingStarted.WhileSubscribed(5000), null)

    fun getAllTags(coroutineScope: CoroutineScope): StateFlow<List<GetAllTagsWithCount>> =
        deeprQueries
            .getAllTagsWithCount()
            .asFlow()
            .mapToList(
                coroutineScope.coroutineContext,
            ).stateIn(coroutineScope, SharingStarted.WhileSubscribed(5000), listOf())
}
