package io.github.mrairing.mattermost.api.teams.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.micronaut.core.annotation.Introspected

@Introspected
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class AddTeamMemberRequest (
    val teamId: String,
    val userId: String
)