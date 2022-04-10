package io.github.mrairing.mattermost.api

import io.github.mrairing.mattermost.properties.MattermostProperties
import io.micronaut.http.HttpResponse
import io.micronaut.http.MutableHttpRequest
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Filter
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.filter.ClientFilterChain
import io.micronaut.http.filter.HttpClientFilter
import org.reactivestreams.Publisher
import java.time.Instant

@Filter(patterns = ["/api/v4/**"])
internal class MattermostAuthFilter(private val auth: MattermostProperties.AuthProperties) : HttpClientFilter {
    override fun doFilter(request: MutableHttpRequest<*>, chain: ClientFilterChain): Publisher<out HttpResponse<*>> {
        return chain.proceed(request.bearerAuth(auth.token))
    }
}

@Client("\${mattermost.base-url}/api/v4/bots")
interface BotsClient {
    data class BotCreationRequest(val username: String, val display_name: String?, val description: String?)
    data class Bot(
        val user_id: String,
        val create_at: Instant,
        val update_at: Instant?,
        val delete_at: Instant?,
        val username: String,
        val display_name: String?,
        val description: String?,
        val owner_id: String?
    )

    @Post
    suspend fun createBot(@Body botCreationRequest: BotCreationRequest): Bot

    @Get
    suspend fun getBots(): List<Bot>
}

@Client("\${mattermost.base-url}/api/v4/users")
interface UsersClient {
    @Get("/me")
    suspend fun getMe(): String
}