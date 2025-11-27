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
