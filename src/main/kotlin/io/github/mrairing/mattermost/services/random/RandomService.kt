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
import mu.KotlinLogging.logger
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger


@Singleton
class RandomService(
    private val usersClient: UsersClient,
    private val channelsClient: ChannelsClient,
    private val groupsService: GroupsService,
) {
    private val log = logger { }

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
        log.info { "Random from channel $channelId" }
        val users = getAllUsersInChannel(channelId)
        log.info { "There are ${users.size} members of channel $channelId" }
        if (users.isNotEmpty()) {
            val randomUser = users.random()
            val sender = usersClient.getUser(senderUserId)

            log.info { "Selected random user ${randomUser.username}" }
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
        log.info { "Random from group ${group.name} members of channel $channelId" }
        val usersMMIds = groupsService.findAllUsersMMIds(group)
        log.info { "There are ${usersMMIds.size} members of ${group.name}" }
        if (usersMMIds.isNotEmpty()) {
            val channelMembers = channelsClient.getChannelMembersByIds(channelId, usersMMIds)
            log.info { "There are ${channelMembers.size} members of ${group.name} in channel $channelId" }
            if (channelMembers.isNotEmpty()) {
                val randomMember = channelMembers.random()
                val sender = usersClient.getUser(senderUserId)
                val randomUser = usersClient.getUser(randomMember.userId)

                log.info { "Selected random user ${randomUser.username}" }

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

    private suspend fun getAllUsersInChannel(channelId: String): List<User> {
        var usersPage: List<User>
        val page = AtomicInteger(0)
        val excludedIds = groupsService.getAllGroupsMMIds()
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