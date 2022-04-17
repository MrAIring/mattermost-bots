package io.github.mrairing.mattermost.api

import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client
import java.time.Instant

@Client("\${mattermost.base-url}/api/v4/bots")
interface BotsClient {
    @Introspected
    data class BotCreationRequest(
        val username: String,
        @JsonProperty("display_name")
        val displayName: String?,
        val description: String?
    )

    @Introspected
    data class Bot(
        @JsonProperty("user_id")
        val userId: String,
        @JsonProperty("create_at")
        val createAt: Instant,
        @JsonProperty("update_at")
        val updateAt: Instant?,
        @JsonProperty("delete_at")
        val deleteAt: Instant?,
        val username: String,
        @JsonProperty("display_name")
        val displayName: String?,
        val description: String?,
        @JsonProperty("owner_id")
        val ownerId: String?
    )

    @Post
    suspend fun createBot(@Body botCreationRequest: BotCreationRequest): Bot

    @Get
    suspend fun getBots(): List<Bot>
}