package io.github.mrairing.mattermost.api.users.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.micronaut.core.annotation.Introspected

/**
 *
 * @param id Unique identifier for the token
 * @param token The token used for authentication
 * @param userId The user the token authenticates for
 * @param description A description of the token usage
 */
@Introspected
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class UserAccessToken (
    /* Unique identifier for the token */
    val id: String,
    /* The token used for authentication */
    val token: String,
    /* The user the token authenticates for */
    val userId: String?,
    /* A description of the token usage */
    val description: String?
)