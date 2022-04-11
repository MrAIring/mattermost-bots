package io.github.mrairing.mattermost.api

import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.http.client.annotation.Client
import java.time.Instant

@Client("\${mattermost.base-url}/api/v4/commands")
interface CommandsClient {
    enum class Method {
        @JsonProperty("P")
        POST,
        @JsonProperty("G")
        GET
    }

    data class CommandCreationRequest(
        @JsonProperty("team_id")
        val teamId: String,
        val method: Method,
        val trigger: String,
        val url: String
    )

    /**
     *
     * @param id The ID of the slash command
     * @param token The token which is used to verify the source of the payload
     * @param createAt The time in milliseconds the command was created
     * @param updateAt The time in milliseconds the command was last updated
     * @param deleteAt The time in milliseconds the command was deleted, 0 if never deleted
     * @param creatorId The user id for the commands creator
     * @param teamId The team id for which this command is configured
     * @param trigger The string that triggers this command
     * @param method Is the trigger done with HTTP Get ('G') or HTTP Post ('P')
     * @param username What is the username for the response post
     * @param iconUrl The url to find the icon for this users avatar
     * @param autoComplete Use auto complete for this command
     * @param autoCompleteDesc The description for this command shown when selecting the command
     * @param autoCompleteHint The hint for this command
     * @param displayName Display name for the command
     * @param description Description for this command
     * @param url The URL that is triggered
     */
    data class Command(
        /* The ID of the slash command */
        @JsonProperty("id")
        val id: String,
        /* The token which is used to verify the source of the payload */
        @JsonProperty("token")
        val token: String,
        /* The time in milliseconds the command was created */
        @JsonProperty("create_at")
        val createAt: Instant,
        /* The time in milliseconds the command was last updated */
        @JsonProperty("update_at")
        val updateAt: Instant,
        /* The time in milliseconds the command was deleted, 0 if never deleted */
        @JsonProperty("delete_at")
        val deleteAt: Instant,
        /* The user id for the commands creator */
        @JsonProperty("creator_id")
        val creatorId: String,
        /* The team id for which this command is configured */
        @JsonProperty("team_id")
        val teamId: String,
        /* The string that triggers this command */
        @JsonProperty("trigger")
        val trigger: String,
        /* Is the trigger done with HTTP Get ('G') or HTTP Post ('P') */
        @JsonProperty("method")
        val method: String,
        /* What is the username for the response post */
        @JsonProperty("username")
        val username: String,
        /* The url to find the icon for this users avatar */
        @JsonProperty("icon_url")
        val iconUrl: String,
        /* Use auto complete for this command */
        @JsonProperty("auto_complete")
        val autoComplete: Boolean,
        /* The description for this command shown when selecting the command */
        @JsonProperty("auto_complete_desc")
        val autoCompleteDesc: String,
        /* The hint for this command */
        @JsonProperty("auto_complete_hint")
        val autoCompleteHint: String,
        /* Display name for the command */
        @JsonProperty("display_name")
        val displayName: String,
        /* Description for this command */
        @JsonProperty("description")
        val description: String,
        /* The URL that is triggered */
        @JsonProperty("url")
        val url: String
    )

    @Post
    suspend fun createCommand(@Body request: CommandCreationRequest): Command

    @Put("/{command.id}")
    suspend fun updateCommand(@Body command: Command)

    @Delete("/{id}")
    suspend fun deleteCommand(id: String)
}