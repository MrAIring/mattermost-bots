package io.github.mrairing.mattermost.api.bots

import io.github.mrairing.mattermost.api.bots.dto.Bot
import io.github.mrairing.mattermost.api.bots.dto.BotCreationRequest
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.http.client.annotation.Client

@Client("\${mattermost.base-url}/api/v4/bots")
interface BotsClient {

    @Post
    suspend fun createBot(@Body botCreationRequest: BotCreationRequest): Bot

    @Put("/{userId}")
    suspend fun updateBot(userId: String, @Body botCreationRequest: BotCreationRequest): Bot

    @Post("/{userId}/disable")
    suspend fun disableBot(userId: String): Bot

    @Get
    suspend fun getBots(): List<Bot>
}