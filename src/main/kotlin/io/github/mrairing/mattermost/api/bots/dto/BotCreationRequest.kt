package io.github.mrairing.mattermost.api.bots.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.micronaut.core.annotation.Introspected

@Introspected
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class BotCreationRequest(
    val username: String,
    val displayName: String?,
    val description: String?
)