package io.github.mrairing.mattermost.api.teams

import io.github.mrairing.mattermost.api.teams.dto.AddTeamMemberRequest
import io.github.mrairing.mattermost.api.teams.dto.TeamMember
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client

@Client("\${mattermost.base-url}/api/v4/teams")
interface TeamsClient {
    @Post("/{teamId}/members")
    suspend fun addTeamMember(teamId: String, @Body request: AddTeamMemberRequest): TeamMember

    @Delete("/{teamId}/members/{userId}")
    suspend fun removeTeamMember(teamId: String, userId: String)
}