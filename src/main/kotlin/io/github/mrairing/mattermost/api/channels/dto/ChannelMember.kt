package io.github.mrairing.mattermost.api.channels.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.micronaut.core.annotation.Introspected
import java.time.Instant

/**
 *
 * @param channelId
 * @param userId
 * @param roles
 * @param lastViewedAt The time in milliseconds the channel was last viewed by the user
 * @param msgCount
 * @param mentionCount
 * @param notifyProps
 * @param lastUpdateAt The time in milliseconds the channel member was last updated
 */
@Introspected
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ChannelMember (
    val channelId: String,
    val userId: String,
    val roles: String?,
    /* The time in milliseconds the channel was last viewed by the user */
    val lastViewedAt: Instant?,
    val msgCount: Int?,
    val mentionCount: Int?,
    val notifyProps: ChannelNotifyProps?,
    /* The time in milliseconds the channel member was last updated */
    val lastUpdateAt: Instant?
)

/**
 *
 * @param email Set to \"true\" to enable email notifications, \"false\" to disable, or \"default\" to use the global user notification setting.
 * @param push Set to \"all\" to receive push notifications for all activity, \"mention\" for mentions and direct messages only, \"none\" to disable, or \"default\" to use the global user notification setting.
 * @param desktop Set to \"all\" to receive desktop notifications for all activity, \"mention\" for mentions and direct messages only, \"none\" to disable, or \"default\" to use the global user notification setting.
 * @param markUnread Set to \"all\" to mark the channel unread for any new message, \"mention\" to mark unread for new mentions only. Defaults to \"all\".
 */
@Introspected
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ChannelNotifyProps (
    /* Set to \"true\" to enable email notifications, \"false\" to disable, or \"default\" to use the global user notification setting. */
    val email: String?,
    /* Set to \"all\" to receive push notifications for all activity, \"mention\" for mentions and direct messages only, \"none\" to disable, or \"default\" to use the global user notification setting. */
    val push: String?,
    /* Set to \"all\" to receive desktop notifications for all activity, \"mention\" for mentions and direct messages only, \"none\" to disable, or \"default\" to use the global user notification setting. */
    val desktop: String?,
    /* Set to \"all\" to mark the channel unread for any new message, \"mention\" to mark unread for new mentions only. Defaults to \"all\". */
    val markUnread: String? 
)