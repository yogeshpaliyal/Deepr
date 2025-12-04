package com.yogeshpaliyal.deepr.server

import com.yogeshpaliyal.shared.data.DeeprLink
import com.yogeshpaliyal.shared.data.WebSocketData
import io.ktor.server.websocket.WebSocketServerSession
import io.ktor.server.websocket.receiveDeserialized
import io.ktor.server.websocket.sendSerialized
import io.ktor.util.reflect.typeInfo
import kotlinx.serialization.Serializable


suspend inline fun WebSocketServerSession.sendDeeprSerialized(data: WebSocketData) {
    sendSerialized(data, typeInfo<WebSocketData>())
}

suspend inline fun WebSocketServerSession.receiveDeeprDeserialized(): WebSocketData? = receiveDeserialized(typeInfo<WebSocketData?>())
