package io.github.mrairing.mattermost.services.random

import io.github.mrairing.mattermost.api.channels.ChannelsClient
import io.github.mrairing.mattermost.api.users.UsersClient
import io.github.mrairing.mattermost.api.users.dto.User
import io.github.mrairing.mattermost.endpoints.dto.WebhookCommandRequest
import io.github.mrairing.mattermost.endpoints.dto.WebhookCommandResponse
import io.github.mrairing.mattermost.jooq.tables.records.GroupsRecord
import io.github.mrairing.mattermost.services.groups.GroupsResponses
import io.github.mrairing.mattermost.services.groups.GroupsService
import io.github.mrairing.mattermost.services.groups.nameRegexStr
import io.github.mrairing.mattermost.utils.WebhookUtils.ephemeralResponse
import io.github.mrairing.mattermost.utils.WebhookUtils.inChannelResponse
import jakarta.inject.Singleton
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger


@Singleton
class RandomService(
    private val usersClient: UsersClient,
    private val channelsClient: ChannelsClient,
    private val groupsService: GroupsService,
) {

    private val commandRegex = "(@(?<groupName>${nameRegexStr})\\s+)?(?<message>.+)".toRegex()

    suspend fun random(data: WebhookCommandRequest): WebhookCommandResponse {
        val text = data.text.trim()
        val matchResult = commandRegex.matchEntire(text)
        return if (matchResult != null) {
            val message = matchResult.groups["message"]!!.value
            val groupName = matchResult.groups["groupName"]?.value
            if (groupName != null) {
                val group = groupsService.findByName(groupName)
                if (group != null) {
                    randomFromGroup(data.userId, group, data.channelId, message)
                } else {
                    GroupsResponses.noSuchGroup(groupName)
                }
            } else {
                randomFromChannel(data.userId, data.channelId, message)
            }
        } else {
            randomHelp()
        }
    }

    private suspend fun randomFromChannel(
        senderUserId: String,
        channelId: String,
        message: String
    ): WebhookCommandResponse {
        val users = getAllUsersInChannelExcept(channelId, senderUserId)
        if (users.isNotEmpty()) {
            val randomUser = users.random()
            val sender = usersClient.getUser(senderUserId)

            return randomResponse(randomUser, message, sender)
        }
        return emptyResponse()
    }

    private suspend fun randomFromGroup(
        senderUserId: String,
        group: GroupsRecord,
        channelId: String,
        message: String
    ): WebhookCommandResponse {
        val usersMMIds = groupsService.findAllUsersMMIds(group)
        if (usersMMIds.isNotEmpty()) {
            val channelMembers = channelsClient.getChannelMembersByIds(channelId, usersMMIds)
            if (channelMembers.isNotEmpty()) {
                val randomMember = channelMembers.random()
                val sender = usersClient.getUser(senderUserId)
                val randomUser = usersClient.getUser(randomMember.userId)

                return randomResponse(randomUser, message, sender)
            }
        }

        return emptyResponse()
    }

    private fun randomResponse(randomUser: User, message: String, sender: User) = inChannelResponse(
        "@${randomUser.username} $message",
        "@${sender.username} random",
        "/api/v4/users/${sender.id}/image"
    )

    private suspend fun getAllUsersInChannelExcept(channelId: String, userId: String): List<User> {
        var usersPage: List<User>
        val page = AtomicInteger(0)
        val excludedIds = groupsService.getAllGroupsMMIds() + userId
        val result = CopyOnWriteArrayList<User>()
        while (true) {
            usersPage = usersClient.getUsers(
                page = page.getAndIncrement(),
                inChannel = channelId,
                active = true
            )
            if (usersPage.isEmpty()) {
                break
            }

            result.addAll(usersPage.filter { it.id !in excludedIds })
        }

        return result
    }

    private fun emptyResponse() = ephemeralResponse("")

    private fun randomHelp(): WebhookCommandResponse {
        return ephemeralResponse(
            """This commands sends message to random user channel.
               |Examples:
               |  1. `/random message to random user in channel`
               |  2. `/random @group message to random user of group in channel`
            """.trimMargin()
        )
    }
}