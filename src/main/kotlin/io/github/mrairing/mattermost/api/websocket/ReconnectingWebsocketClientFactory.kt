package io.github.mrairing.mattermost.api.websocket

import io.github.mrairing.mattermost.properties.MattermostProperties
import io.micronaut.http.uri.UriBuilder
import io.micronaut.websocket.exceptions.WebSocketException
import jakarta.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.future.await
import kotlinx.coroutines.future.future
import mu.KLogger
import mu.KotlinLogging.logger
import java.net.URI
import java.net.http.HttpClient
import java.net.http.WebSocket
import java.util.concurrent.CompletionStage
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToLong

private class HttpClientWebsocketThreadFactory : ThreadFactory {
    private val namePrefix: String = "HttpClient-Websocket-Worker-"
    private val nextId = AtomicInteger()
    override fun newThread(r: Runnable): Thread {
        val name = namePrefix + nextId.getAndIncrement()
        return Thread(r, name).apply { isDaemon = true }
    }
}

@Singleton
class ReconnectingWebsocketClientFactory(
    private val properties: MattermostProperties,
) {
    private val httpClient = HttpClient.newBuilder()
        .executor(
            ThreadPoolExecutor(
                1, Runtime.getRuntime().availableProcessors(),
                10L, TimeUnit.SECONDS,
                LinkedBlockingQueue(),
                HttpClientWebsocketThreadFactory()
            )
        )
        .build()

    suspend fun create(name: String, token: String): ReconnectingWebsocketClient {
        val webSocketReference = AtomicReference<WebSocket?>()
        val log = logger("websocket.$name")

        val retryFlow = callbackFlow {
            val webSocket = httpClient.newWebSocketBuilder()
                .buildAsync(URI(uri), object : WebSocket.Listener {
                    override fun onOpen(webSocket: WebSocket) {
                        auth(log, name, webSocket, token)
                    }

                    @Volatile
                    private var buffer: StringBuffer? = null

                    override fun onText(
                        webSocket: WebSocket,
                        data: CharSequence,
                        last: Boolean
                    ): CompletionStage<*>? {
                        log.debug { "Websocket data: $data" }
                        val buffer = this.buffer
                        if (last) {
                            val msg = if (buffer == null) {
                                data.toString()
                            } else {
                                buffer.append(data)
                                buffer.toString()
                            }
                            this.buffer = null

                            webSocket.request(1)
                            return future { send(msg) }
                        } else {
                            if (buffer == null) {
                                this.buffer = StringBuffer(data)
                            } else {
                                buffer.append(data)
                            }

                            webSocket.request(1)
                            return null
                        }
                    }

                    override fun onClose(
                        webSocket: WebSocket,
                        statusCode: Int,
                        reason: String
                    ): CompletionStage<*>? {
                        val errorMsg = "Websocket is closed. Reason: $reason. Status code: $statusCode"
                        log.error { errorMsg }
                        close(WebSocketException(errorMsg))
                        return null
                    }

                    override fun onError(webSocket: WebSocket, error: Throwable) {
                        log.error(error) { "Websocket error" }
                        close(error)
                    }
                }).await()
            webSocketReference.set(webSocket)

            awaitClose { webSocket.abort() }
        }.retryWhen { cause, attempt ->
            val backoffPower = min(20, attempt.toInt())
            val backoffBase = 1.4
            val backoffInitialDelay = 100
            val exponentialBackoff = (backoffInitialDelay * backoffBase.pow(backoffPower)).roundToLong()
            val delay = (exponentialBackoff + (Math.random() - 0.5) * exponentialBackoff * 0.2).roundToLong()
            log.error(cause) { "Websocket error. $attempt attempt to reconnect after $delay ms" }
            delay(delay)
            return@retryWhen true
        }

        return ReconnectingWebsocketClient(webSocketReference, retryFlow)
    }

    private fun auth(log: KLogger, name: String, webSocket: WebSocket, token: String) {
        webSocket.request(1)
        log.info { "Connection is open. Authenticating $name" }
        webSocket.sendText(
            """{
                      "seq": 1,
                      "action": "authentication_challenge",
                      "data": {
                        "token": "$token"
                      }
                    }""",
            true
        ).thenRun {
            log.info { "authenticating $name... DONE" }
        }
    }

    private val uri: String = run {
        val baseUrl = URI(properties.baseUrl)
        val uriBuilder = UriBuilder.of(baseUrl)
        if (baseUrl.scheme == "https") {
            uriBuilder.scheme("wss")
        } else {
            uriBuilder.scheme("ws")
        }
        val baseWsUrl = uriBuilder.build().toString()

        "$baseWsUrl/api/v4/websocket"
    }
}

class ReconnectingWebsocketClient(
    private val client: AtomicReference<WebSocket?>,
    val messages: Flow<String>
) {
    suspend fun send(msg: String) {
        val lowLevelWebsocketClient = client.get()
        checkNotNull(lowLevelWebsocketClient)

        lowLevelWebsocketClient.sendText(msg, true).await()
    }
}