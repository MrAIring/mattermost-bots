package io.github.mrairing.mattermostbots.services.invite

import io.github.mrairing.mattermostbots.api.ChannelsApi
import io.github.mrairing.mattermostbots.api.UsersApi
import io.github.mrairing.mattermostbots.api.dto.AddChannelMemberRequest
import io.github.mrairing.mattermostbots.api.dto.User
import io.github.mrairing.mattermostbots.endpoints.dto.WebhookCommandRequest
import io.github.mrairing.mattermostbots.endpoints.dto.WebhookCommandResponse
import io.github.mrairing.mattermostbots.properties.MattermostProperties
import io.github.mrairing.mattermostbots.services.groups.GroupsService
import io.github.mrairing.mattermostbots.utils.WebhookUtils.ephemeralResponse
import io.github.mrairing.mattermostbots.utils.getUsers
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicInteger

@Service
class InviteService(
    private val usersApi: UsersApi,
    private val channelsApi: ChannelsApi,
    private val groupsService: GroupsService,
    private val mattermostProperties: MattermostProperties,
) {
    fun inviteAllExcept(data: WebhookCommandRequest): WebhookCommandResponse {
        if (data.userMentionsIds.isNullOrEmpty()) {
            return inviteAllExceptHelp()
        }

        doInviteAllExcept(data.channelId, data.userMentionsIds.toSet())

        return ephemeralResponse("Invited")
    }

    private fun doInviteAllExcept(channelId: String, excludeUsersIds: Set<String>) {
        var usersPage: List<User>
        val page = AtomicInteger(0)
        val excludedIds = groupsService.getAllGroupsMMIds() + excludeUsersIds
        while (true) {
            usersPage = usersApi.getUsers(
                page = page.getAndIncrement(),
                teamId = mattermostProperties.teamId,
            )
            if (usersPage.isEmpty()) {
                break
            }

            usersPage.forEach { u ->
                val userId = checkNotNull(u.id)
                if (userId !in excludedIds) {
                    channelsApi.addChannelMember(
                        channelId,
                        AddChannelMemberRequest().userId(userId)
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