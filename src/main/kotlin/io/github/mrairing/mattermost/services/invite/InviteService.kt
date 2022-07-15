package io.github.mrairing.mattermost.services.invite

import io.github.mrairing.mattermost.api.channels.ChannelsClient
import io.github.mrairing.mattermost.api.channels.dto.AddUserToChannelRequest
import io.github.mrairing.mattermost.api.users.UsersClient
import io.github.mrairing.mattermost.api.users.dto.User
import io.github.mrairing.mattermost.endpoints.dto.WebhookCommandRequest
import io.github.mrairing.mattermost.endpoints.dto.WebhookCommandResponse
import io.github.mrairing.mattermost.properties.MattermostProperties
import io.github.mrairing.mattermost.services.groups.GroupsService
import io.github.mrairing.mattermost.utils.WebhookUtils.ephemeralResponse
import jakarta.inject.Singleton
import java.util.concurrent.atomic.AtomicInteger

@Singleton
class InviteService(
    private val usersClient: UsersClient,
    private val channelsClient: ChannelsClient,
    private val groupsService: GroupsService,
    private val mattermostProperties: MattermostProperties,
) {
    suspend fun inviteAllExcept(data: WebhookCommandRequest): WebhookCommandResponse {
        if (data.userMentionsIds.isNullOrEmpty()) {
            return inviteAllExceptHelp()
        }

        doInviteAllExcept(data.channelId, data.userMentionsIds.toSet())

        return ephemeralResponse("Invited")
    }

    private suspend fun doInviteAllExcept(channelId: String, excludeUsersIds: Set<String>) {
        var usersPage: List<User>
        val page = AtomicInteger(0)
        val excludedIds = groupsService.getAllGroupsMMIds() + excludeUsersIds
        while (true) {
            usersPage = usersClient.getUsers(
                page = page.getAndIncrement(),
                inTeam = mattermostProperties.teamId,
                active = true
            )
            if (usersPage.isEmpty()) {
                break
            }

            usersPage.forEach { u ->
                val userId = checkNotNull(u.id)
                if (userId !in excludedIds) {
                    channelsClient.addUserToChannel(
                        channelId,
                        AddUserToChannelRequest(
                            userId = userId,
                            postRootId = null
                        )
                    )
                }
            }
        }
    }

    private fun inviteAllExceptHelp(): WebhookCommandResponse {
        return ephemeralResponse(
            """This command adds all users to the current channel except those specified in the command's input.
               |Examples:
               |  1. `/invite-all-except @john`
               |  2. `/invite-all-except @user1 @user2 @user3`
               |  3. `/invite-all-except @user1 and @user2 and @user3`
               |The first example demonstrates how to add all team members to the current channel except @john.
               |Second example shows how to exclude several users from invention.
               |Third example demonstrates that any other words or symbols besides mentions of users would be ignored.
            """.trimMargin()
        )
    }
}