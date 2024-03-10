package io.github.mrairing.mattermostbots.services.groups

import io.github.mrairing.mattermostbots.api.dto.Channel
import io.github.mrairing.mattermostbots.api.dto.User
import io.github.mrairing.mattermostbots.endpoints.dto.WebhookCommandRequest
import io.github.mrairing.mattermostbots.endpoints.dto.WebhookCommandResponse
import io.github.mrairing.mattermostbots.utils.WebhookUtils.ephemeralResponse
import io.github.mrairing.mattermostbots.utils.WebhookUtils.inChannelResponse

object GroupsResponses {
    fun groupCreateHelp(): WebhookCommandResponse {
        return ephemeralResponse("Usage:\n/group-create groupname")
    }

    fun invalidGroupName(text: String): WebhookCommandResponse {
        return ephemeralResponse("Group name `${text}` does not match regexp `$nameRegexStr`")
    }

    fun groupAlreadyExists(groupName: String): WebhookCommandResponse {
        return ephemeralResponse("Group with name `$groupName` already exists. Please choose another name")
    }

    fun groupDeleteHelp(): WebhookCommandResponse {
        return ephemeralResponse(
            "To delete a group, you io.github.oshai.kotlinloggingst add the phrase `$deletePrefix` before the group name." +
                    " Example:\n" +
                    "/group-delete $deletePrefix @groupname"
        )
    }

    fun noSuchGroup(groupName: String): WebhookCommandResponse {
        return ephemeralResponse("Group with name `$groupName` not found")
    }

    fun groupEditHelp(): WebhookCommandResponse {
        return ephemeralResponse(
            " Example:\n" +
                    "/group-edit my-group-name add @user1 @user2 @group1\n" +
                    "/group-edit my-group-name remove @user1 @user2 @group1"
        )
    }

    fun groupCreateResponse(groupName: String): WebhookCommandResponse {
        return inChannelResponse(":white_check_mark: Group with name `$groupName` created successfully")
    }

    fun groupDeletedResponse(groupName: String): WebhookCommandResponse {
        return inChannelResponse(":negative_squared_cross_mark: Group with name `$groupName` deleted successfully")
    }

    data class GroupInfo(
        val groupName: String,
        val usersInGroup: List<User>,
        val groupsDefaultChannels: List<Channel>
    )

    fun groupsInfoResponse(infos: List<GroupInfo>): WebhookCommandResponse {
        return ephemeralResponse(infos.joinToString("\n") { groupTitle(it) + groupInfo(it) })
    }

    private fun groupTitle(info: GroupInfo) = "### ${info.groupName}\n"

    private fun groupInfo(info: GroupInfo) =
        "There are currently `${info.usersInGroup.size}` users in the group `${info.groupName}`" +
            if (info.usersInGroup.isNotEmpty())
                info.usersInGroup
                    .sortedBy { it.username }
                    .joinToString(separator = " ", prefix = ":\n") { "@${it.username}" }
            else
                ""

    fun groupEditResponse(
        groupInfo: GroupInfo,
        usersAdded: List<String>?,
        usersAlreadyExisted: List<String>?,
        usersRemoved: List<String>?,
        usersNotExisted: List<String>?,
        data: WebhookCommandRequest
    ): WebhookCommandResponse {
        fun getMentionList(
            userMmIds: List<String>?,
            userMentionsById: MutableMap<String, String>
        ) = userMmIds?.joinToString(", ") { "@${userMentionsById[it]}" }?.ifEmpty { null }

        val userMentionsById = mutableMapOf<String, String>()
        data.userMentionsIds?.forEachIndexed { index, mmId ->
            userMentionsById[mmId] = data.userMentions?.get(index) ?: ""
        }

        val addedMentions = getMentionList(usersAdded, userMentionsById)
        val alreadyInGroupMentions = getMentionList(usersAlreadyExisted, userMentionsById)

        val notInGroupMentions = getMentionList(usersNotExisted, userMentionsById)
        val removedMentions = getMentionList(usersRemoved, userMentionsById)

        val rows = mutableListOf(
            "Group `${groupInfo.groupName}`:"
        )

        if (addedMentions != null) {
            rows += "* Users added: $addedMentions"
        }
        if (alreadyInGroupMentions != null) {
            rows += "* Already in group: $alreadyInGroupMentions"
        }
        if (removedMentions != null) {
            rows += "* Users removed: $removedMentions"
        }
        if (notInGroupMentions != null) {
            rows += "* Was not in group: $notInGroupMentions"
        }

        rows += groupInfo(groupInfo)

        return inChannelResponse(rows.joinToString("\n"))
    }
}