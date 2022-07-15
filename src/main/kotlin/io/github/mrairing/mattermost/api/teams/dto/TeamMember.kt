package io.github.mrairing.mattermost.api.teams.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.micronaut.core.annotation.Introspected

/**
 *
 * @param teamId The ID of the team this member belongs to.
 * @param userId The ID of the user this member relates to.
 * @param roles The complete list of roles assigned to this team member, as a space-separated list of role names, including any roles granted implicitly through permissions schemes.
 * @param deleteAt The time in milliseconds that this team member was deleted.
 * @param schemeUser Whether this team member holds the default user role defined by the team's permissions scheme.
 * @param schemeAdmin Whether this team member holds the default admin role defined by the team's permissions scheme.
 * @param explicitRoles The list of roles explicitly assigned to this team member, as a space separated list of role names. This list does *not* include any roles granted implicitly through permissions schemes.
 */
@Introspected
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class TeamMember(
    /* The ID of the team this member belongs to. */
    val teamId: String?,
    /* The ID of the user this member relates to. */
    val userId: String?,
    /* The complete list of roles assigned to this team member, as a space-separated list of role names, including any roles granted implicitly through permissions schemes. */
    val roles: String?,
    /* The time in milliseconds that this team member was deleted. */
    val deleteAt: Int?,
    /* Whether this team member holds the default user role defined by the team's permissions scheme. */
    val schemeUser: Boolean?,
    /* Whether this team member holds the default admin role defined by the team's permissions scheme. */
    val schemeAdmin: Boolean?,
    /* The list of roles explicitly assigned to this team member, as a space separated list of role names. This list does *not* include any roles granted implicitly through permissions schemes. */
    val explicitRoles: String?
)