package com.example.nearhelp.data.api.ws

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test

class LiveTrackingWebSocketClientTest {
    @Test fun sendsChatToSelectedIncidentAndReconnectsAfterServerClosure() = runBlocking {
        val server = MockWebServer()
        val firstSocket = CompletableDeferred<WebSocket>()
        val received = CompletableDeferred<String>()
        val reconnected = CompletableDeferred<Unit>()
        server.enqueue(MockResponse().withWebSocketUpgrade(object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) { firstSocket.complete(webSocket) }
            override fun onMessage(webSocket: WebSocket, text: String) { received.complete(text) }
            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) { webSocket.close(code, reason) }
        }))
        server.enqueue(MockResponse().withWebSocketUpgrade(object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) { reconnected.complete(Unit) }
            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) { webSocket.close(code, reason) }
        }))
        server.start()
        val client = LiveTrackingWebSocketClient()
        try {
            client.setBaseWsUrl(server.url("/").toString())
            client.connect("f6bf9fde-6198-44dd-8ee8-75466dbf9971", "test-token")
            withTimeout(5000) { client.connectionStatus.first { it == ConnectionStatus.CONNECTED } }
            assertTrue(client.sendChatMessage("At the entrance"))
            assertTrue(withTimeout(5000) { received.await() }.contains("At the entrance"))
            val request = server.takeRequest()
            assertEquals("/ws/tracking/f6bf9fde-6198-44dd-8ee8-75466dbf9971?token=test-token", request.path)
            firstSocket.await().close(1000, "Reconnect test")
            withTimeout(8000) { reconnected.await() }
            assertEquals(2, server.requestCount)
            client.disconnect()
            delay(3500)
            assertEquals("An intentional disconnect must not open another socket", 2, server.requestCount)
        } finally {
            client.close()
            server.shutdown()
        }
    }
}
