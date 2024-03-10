package io.github.mrairing.mattermostbots.services.random

import io.github.mrairing.mattermostbots.api.ChannelsApi
import io.github.mrairing.mattermostbots.api.UsersApi
import io.github.mrairing.mattermostbots.api.dto.User
import io.github.mrairing.mattermostbots.endpoints.dto.WebhookCommandRequest
import io.github.mrairing.mattermostbots.endpoints.dto.WebhookCommandResponse
import io.github.mrairing.mattermostbots.jooq.tables.records.GroupsRecord
import io.github.mrairing.mattermostbots.services.groups.GroupsResponses
import io.github.mrairing.mattermostbots.services.groups.GroupsService
import io.github.mrairing.mattermostbots.services.groups.nameRegexStr
import io.github.mrairing.mattermostbots.utils.WebhookUtils.ephemeralResponse
import io.github.mrairing.mattermostbots.utils.WebhookUtils.inChannelResponse
import io.github.mrairing.mattermostbots.utils.getUsers
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.springframework.stereotype.Service
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger


@Service
class RandomService(
    private val usersApi: UsersApi,
    private val channelsApi: ChannelsApi,
    private val groupsService: GroupsService,
) {
    private val log = logger { }

    private val commandRegex = "(@(?<groupName>${nameRegexStr})\\s+)?(?<message>.+)".toRegex()

    fun random(data: WebhookCommandRequest): WebhookCommandResponse {
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

    private fun randomFromChannel(
        senderUserId: String,
        channelId: String,
        message: String
    ): WebhookCommandResponse {
        log.info { "Random from channel $channelId" }
        val users = getAllUsersInChannel(channelId)
        log.info { "There are ${users.size} members of channel $channelId" }
        if (users.isNotEmpty()) {
            val randomUser = users.random()
            val sender = usersApi.getUser(senderUserId)

            log.info { "Selected random user ${randomUser.username}" }
            return randomResponse(randomUser, message, sender)
        }
        return emptyResponse()
    }

    private fun randomFromGroup(
        senderUserId: String,
        group: GroupsRecord,
        channelId: String,
        message: String
    ): WebhookCommandResponse {
        log.info { "Random from group ${group.name} members of channel $channelId" }
        val usersMMIds = groupsService.findAllUsersMMIds(group)
        log.info { "There are ${usersMMIds.size} members of ${group.name}" }
        if (usersMMIds.isNotEmpty()) {
            val channelMembers = channelsApi.getChannelMembersByIds(channelId, usersMMIds)
            log.info { "There are ${channelMembers.size} members of ${group.name} in channel $channelId" }
            if (channelMembers.isNotEmpty()) {
                val randomMember = channelMembers.random()
                val sender = usersApi.getUser(senderUserId)
                val randomUser = usersApi.getUser(randomMember.userId)

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

    private fun getAllUsersInChannel(channelId: String): List<User> {
        var usersPage: List<User>
        val page = AtomicInteger(0)
        val excludedIds = groupsService.getAllGroupsMMIds()
        val result = CopyOnWriteArrayList<User>()
        while (true) {
            usersPage = usersApi.getUsers(
                page = page.getAndIncrement(),
                inChannel = channelId,
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