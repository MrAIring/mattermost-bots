package io.github.mrairing.mattermost.api.commands

import io.github.mrairing.mattermost.api.commands.dto.Command
import io.github.mrairing.mattermost.api.commands.dto.CommandCreationRequest
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("\${mattermost.base-url}/api/v4/commands")
interface CommandsClient {

    @Get
    suspend fun listCommands(
        @QueryValue("team_id") teamId: String,
        @QueryValue("custom_only") customOnly: Boolean
    ): List<Command>

    @Post
    suspend fun createCommand(@Body request: CommandCreationRequest): Command

    @Put("/{id}")
    suspend fun updateCommand(id: String, @Body command: Command): Command

    @Delete("/{id}")
    suspend fun deleteCommand(id: String)
}