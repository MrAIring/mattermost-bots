package io.github.mrairing.mattermost.api.commands.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.micronaut.core.annotation.Introspected

@Introspected
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class CommandCreationRequest(
    val teamId: String,
    val method: Method,
    val trigger: String,
    val url: String
)