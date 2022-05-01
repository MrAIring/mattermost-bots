package io.github.mrairing.mattermost.services

import io.github.mrairing.mattermost.api.bots.BotsClient
import io.github.mrairing.mattermost.api.bots.dto.BotCreationRequest
import io.github.mrairing.mattermost.api.teams.TeamsClient
import io.github.mrairing.mattermost.api.teams.dto.AddTeamMemberRequest
import io.github.mrairing.mattermost.api.users.UsersClient
import io.github.mrairing.mattermost.api.users.dto.UserAccessTokenDescription
import io.github.mrairing.mattermost.dao.GroupsDao
import io.github.mrairing.mattermost.endpoints.dto.WebhookCommandRequest
import io.github.mrairing.mattermost.endpoints.dto.WebhookCommandResponse
import io.github.mrairing.mattermost.jooq.tables.records.GroupsRecord
import io.github.mrairing.mattermost.properties.MattermostProperties
import io.github.mrairing.mattermost.utils.WebhookUtils.ephemeralResponse
import jakarta.inject.Singleton

@Singleton
class GroupsService(
    private val mattermostProperties: MattermostProperties,
    private val usersClient: UsersClient,
    private val botsClient: BotsClient,
    private val teamsClient: TeamsClient,
    private val groupsDao: GroupsDao,
) {
    private val deletePrefix = "yes i'm sure i want to delete "
    private val nameRegex = "[a-z_\\-.]{3,22}".toRegex()
    private val operationRegex = "(?<groupName>[a-z.\\-_]{3,22})\\s+(?<operation>add|remove) .+".toRegex()

    suspend fun groupCreate(data: WebhookCommandRequest): WebhookCommandResponse {
        val groupName = data.text.trim()
        return if (groupName.matches(nameRegex)) {
            val groupEntity = groupsDao.findByName(groupName)
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

        groupsDao.save(
            GroupsRecord().apply {
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
                val groupEntity = groupsDao.findByName(groupName)
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
        return ephemeralResponse("Group with name `$groupName` not found")
    }

    private suspend fun deleteGroup(groupEntity: GroupsRecord): WebhookCommandResponse {
        val botUserId = groupEntity.mmId
        teamsClient.removeTeamMember(mattermostProperties.teamId, botUserId)
        botsClient.updateBot(
            botUserId,
            BotCreationRequest("deleted_${System.currentTimeMillis()}_${groupEntity.name}", null, null)
        )
        botsClient.disableBot(botUserId)
        groupsDao.delete(groupEntity)
        return ephemeralResponse("Deleted ${groupEntity.name}")
    }

    suspend fun groupEdit(data: WebhookCommandRequest): WebhookCommandResponse {
        val text = data.text.trim()
        val matchResult = operationRegex.matchEntire(text)
        return if (matchResult != null) {
            val groupName = matchResult.groups["groupName"]!!.value
            val operation = matchResult.groups["operation"]!!.value

            val groupEntity = groupsDao.findByName(groupName)
            if (groupEntity != null) {
                if (operation == "add") {
                    addToGroup(groupEntity, data)
                } else {
                    removeFromGroup(groupEntity, data)
                }
            } else {
                noSuchGroup(groupName)
            }
        } else {
            groupEditHelp()
        }
    }

    private fun groupEditHelp(): WebhookCommandResponse {
        return ephemeralResponse(
            " Example:\n" +
                    "/group-edit my-group-name add @user1 @user2 @group1\n" +
                    "/group-edit my-group-name remove @user1 @user2 @group1"
        )
    }

    private suspend fun removeFromGroup(groupEntity: GroupsRecord, data: WebhookCommandRequest): WebhookCommandResponse {
        val userMentionsIds = data.userMentionsIds?.distinct()
        if (userMentionsIds == null || userMentionsIds.isEmpty()) {
            return groupEditHelp()
        }

        val userMentionsById = mutableMapOf<String, String>()
        data.userMentionsIds.forEachIndexed { index, mmId ->
            userMentionsById[mmId] = data.userMentions?.get(index) ?: ""
        }

        val groupsNotUsers = groupsDao.findAllByMmIds(userMentionsIds)
        val groupsIds = groupsNotUsers.map { it.mmId }.toSet()

        val usersMmIds = userMentionsIds.filter { it !in groupsIds }

        val alreadyInGroup = groupsDao.findUserMMIdsAlreadyInGroup(usersMmIds)
        val notInGroup = usersMmIds - alreadyInGroup

        groupsDao.removeFromGroup(groupEntity.id, alreadyInGroup)

        val notInGroupMentions = getMentionList(notInGroup, userMentionsById)
        val removedMentions = getMentionList(alreadyInGroup, userMentionsById)

        return ephemeralResponse("Users removed from `${groupEntity.name}`: $removedMentions\n" +
                "Was not in group: $notInGroupMentions")

    }

    private suspend fun addToGroup(groupEntity: GroupsRecord, data: WebhookCommandRequest): WebhookCommandResponse {
        val userMentionsIds = data.userMentionsIds?.distinct()
        if (userMentionsIds == null || userMentionsIds.isEmpty()) {
            return groupEditHelp()
        }

        val userMentionsById = mutableMapOf<String, String>()
        data.userMentionsIds.forEachIndexed { index, mmId ->
            userMentionsById[mmId] = data.userMentions?.get(index) ?: ""
        }

        val groupsNotUsers = groupsDao.findAllByMmIds(userMentionsIds)
        val groupsIds = groupsNotUsers.map { it.mmId }.toSet()

        val usersMmIds = userMentionsIds.filter { it !in groupsIds }

        val alreadyInGroup = groupsDao.findUserMMIdsAlreadyInGroup(usersMmIds)
        val newToGroup = usersMmIds - alreadyInGroup

        groupsDao.addToGroup(groupEntity.id, newToGroup)

        val addedMentions = getMentionList(newToGroup, userMentionsById)
        val alreadyInGroupMentions = getMentionList(alreadyInGroup, userMentionsById)

        return ephemeralResponse("Users added to `${groupEntity.name}`: $addedMentions\n" +
                "Already in group: $alreadyInGroupMentions")
    }

    private fun getMentionList(
        userMmIds: List<String>,
        userMentionsById: MutableMap<String, String>
    ) = userMmIds.joinToString(", ") { "@${userMentionsById[it]}" }.ifEmpty { "none" }
}
