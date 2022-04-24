package io.github.mrairing.mattermost.services

import io.github.mrairing.mattermost.api.bots.BotsClient
import io.github.mrairing.mattermost.api.bots.dto.BotCreationRequest
import io.github.mrairing.mattermost.api.teams.TeamsClient
import io.github.mrairing.mattermost.api.teams.dto.AddTeamMemberRequest
import io.github.mrairing.mattermost.api.users.UsersClient
import io.github.mrairing.mattermost.api.users.dto.UserAccessTokenDescription
import io.github.mrairing.mattermost.dao.GroupsRepository
import io.github.mrairing.mattermost.dao.entities.GroupEntity
import io.github.mrairing.mattermost.endpoints.dto.WebhookCommandRequest
import io.github.mrairing.mattermost.endpoints.dto.WebhookCommandResponse
import io.github.mrairing.mattermost.properties.MattermostProperties
import io.github.mrairing.mattermost.utils.WebhookUtils.ephemeralResponse
import jakarta.inject.Singleton

@Singleton
class GroupsService(
    private val mattermostProperties: MattermostProperties,
    private val usersClient: UsersClient,
    private val botsClient: BotsClient,
    private val groupsRepository: GroupsRepository,
    private val teamsClient: TeamsClient,
) {
    private val deletePrefix = "yes i'm sure i want to delete "
    private val nameRegex = "[a-z_\\-.]{3,22}".toRegex()

    suspend fun groupCreate(data: WebhookCommandRequest): WebhookCommandResponse {
        val groupName = data.text.trim()
        return if (groupName.matches(nameRegex)) {
            val groupEntity = groupsRepository.findByName(groupName)
            if (groupEntity != null) {
                groupAlreadyExists(groupName)
            } else {
                createGroup(groupName)
            }
        } else {
            if (groupName.isEmpty()) {
                groupCreateHelp()
            } else {
                invalidGroupName(groupName)
            }
        }
    }

    private fun groupCreateHelp(): WebhookCommandResponse {
        return ephemeralResponse("Usage:\n/group-create my-group-name")
    }

    private fun invalidGroupName(text: String): WebhookCommandResponse {
        return ephemeralResponse("Group name `${text}` does not match regexp `$nameRegex`")
    }

    private suspend fun createGroup(groupName: String): WebhookCommandResponse {
        val bot = botsClient.createBot(
            BotCreationRequest(
                username = groupName,
                displayName = null,
                description = "Group $groupName"
            )
        )

        val accessToken =
            usersClient.createUserAccessToken(bot.userId, UserAccessTokenDescription("Group bot token"))

        groupsRepository.save(
            GroupEntity().apply {
                mmId = bot.userId
                token = accessToken.token
                name = groupName
            }
        )

        val teamId = mattermostProperties.teamId
        teamsClient.addTeamMember(teamId, AddTeamMemberRequest(teamId, bot.userId))

        return ephemeralResponse("Group with name `$groupName` created successfully")
    }

    private fun groupAlreadyExists(groupName: String): WebhookCommandResponse {
        return ephemeralResponse("Group with name `$groupName` already exists. Please choose another name")
    }

    suspend fun groupDelete(data: WebhookCommandRequest): WebhookCommandResponse {
        val text = data.text.trim()
        return if (text.startsWith(deletePrefix)) {
            val groupName = text.substringAfter(deletePrefix).trim()
            if (groupName.matches(nameRegex)) {
                val groupEntity = groupsRepository.findByName(groupName)
                if (groupEntity != null) {
                    deleteGroup(groupEntity)
                } else {
                    noSuchGroup(groupName)
                }
            } else {
                if (groupName.isEmpty()) {
                    groupDeleteHelp()
                } else {
                    invalidGroupName(groupName)
                }
            }
        } else {
            groupDeleteHelp()
        }
    }

    private fun groupDeleteHelp(): WebhookCommandResponse {
        return ephemeralResponse(
            "To delete a group, you must add the phrase `$deletePrefix` before the group name." +
                    " Example:\n" +
                    "/group-delete $deletePrefix my-group-name"
        )
    }

    private fun noSuchGroup(groupName: String): WebhookCommandResponse {
        return ephemeralResponse("Group with name `$groupName` not found. Nothing to delete")
    }

    private suspend fun deleteGroup(groupEntity: GroupEntity): WebhookCommandResponse {
        val botUserId = groupEntity.mmId
        teamsClient.removeTeamMember(mattermostProperties.teamId, botUserId)
        botsClient.updateBot(botUserId, BotCreationRequest("deleted_${System.currentTimeMillis()}_${groupEntity.name}", null, null))
        botsClient.disableBot(botUserId)
        groupsRepository.delete(groupEntity)
        return ephemeralResponse("Deleted ${groupEntity.name}")
    }

    suspend fun groupEdit(data: WebhookCommandRequest): WebhookCommandResponse {
        TODO("Not yet implemented")
    }
}
