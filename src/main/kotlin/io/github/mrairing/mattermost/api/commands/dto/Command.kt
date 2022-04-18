package io.github.mrairing.mattermost.api.commands.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.micronaut.core.annotation.Introspected
import net.afanasev.sekret.Secret
import java.time.Instant

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
@Introspected
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class Command(
    /* The ID of the slash command */
    val id: String,
    /* The token which is used to verify the source of the payload */
    @Secret
    val token: String?,
    /* The time in milliseconds the command was created */
    val createAt: Instant?,
    /* The time in milliseconds the command was last updated */
    val updateAt: Instant?,
    /* The time in milliseconds the command was deleted, 0 if never deleted */
    val deleteAt: Instant?,
    /* The user id for the commands creator */
    val creatorId: String?,
    /* The team id for which this command is configured */
    val teamId: String,
    /* The string that triggers this command */
    val trigger: String,
    /* Is the trigger done with HTTP Get ('G') or HTTP Post ('P') */
    val method: Method,
    /* What is the username for the response post */
    val username: String?,
    /* The url to find the icon for this users avatar */
    val iconUrl: String?,
    /* Use auto complete for this command */
    val autoComplete: Boolean,
    /* The description for this command shown when selecting the command */
    val autoCompleteDesc: String,
    /* The hint for this command */
    val autoCompleteHint: String,
    /* Display name for the command */
    val displayName: String,
    /* Description for this command */
    val description: String,
    /* The URL that is triggered */
    val url: String
)