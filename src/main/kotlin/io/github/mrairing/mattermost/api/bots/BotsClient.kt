package io.github.mrairing.mattermost.api.bots

import com.fasterxml.jackson.annotation.JsonProperty
import io.github.mrairing.mattermost.api.bots.dto.BotCreationRequest
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client
import java.time.Instant

@Client("\${mattermost.base-url}/api/v4/bots")
interface BotsClient {

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