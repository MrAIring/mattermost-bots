package io.github.mrairing.mattermost.api.channels.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.micronaut.core.annotation.Introspected

/**
 *
 * @param userId The ID of user to add into the channel
 * @param postRootId The ID of root post where link to add channel member originates
 */
@Introspected
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class AddUserToChannelRequest(
    /* The ID of user to add into the channel */
    val userId: String,
    /* The ID of root post where link to add channel member originates */
    val postRootId: String?
)