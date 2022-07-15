package io.github.mrairing.mattermost.api.users.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.micronaut.core.annotation.Introspected

@Introspected
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class UserAccessTokenDescription(val description: String)