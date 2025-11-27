package com.yogeshpaliyal.deepr.server

import com.yogeshpaliyal.deepr.data.DeeprLink
import io.ktor.server.websocket.WebSocketServerSession
import io.ktor.server.websocket.receiveDeserialized
import io.ktor.server.websocket.sendSerialized
import io.ktor.util.reflect.typeInfo
import kotlinx.serialization.Serializable

@Serializable
sealed class WebSocketData(
    val dataType: String,
)

@Serializable
data class LinksListData(
    val links: List<DeeprLink>,
) : WebSocketData(dataType = "links_list")

@Serializable
data class TagsListData(
    val tags: List<DeeprTag>,
) : WebSocketData(dataType = "tags_list")


@Serializable
data class CountData(
    val count: Long?,
    val type: @CountType String,
) : WebSocketData(dataType = "links_count")


@Retention(AnnotationRetention.SOURCE)
@Target(
    AnnotationTarget.TYPE,
)
annotation class CountType {
    companion object {
        const val TOTAL = "total"
        const val FAVOURITE = "favourite"
    }
}

suspend inline fun WebSocketServerSession.sendDeeprSerialized(data: WebSocketData) {
    sendSerialized(data, typeInfo<WebSocketData>())
}

suspend inline fun WebSocketServerSession.receiveDeeprDeserialized(): WebSocketData? = receiveDeserialized(typeInfo<WebSocketData?>())

@Serializable
data class LinkResponse(
    val id: Long,
    val link: String,
    val name: String,
    val createdAt: String,
    val openedCount: Long,
    val notes: String,
    val thumbnail: String,
    val isFavourite: Long,
    val tags: List<String>,
)

@Serializable
data class DeeprTag(
    val id: Long,
    val name: String,
    val count: Long,
)
