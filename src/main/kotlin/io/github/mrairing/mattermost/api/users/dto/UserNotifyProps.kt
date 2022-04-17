package io.github.mrairing.mattermost.api.users.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.micronaut.core.annotation.Introspected

/**
 *
 * @param email Set to \"true\" to enable email notifications, \"false\" to disable. Defaults to \"true\".
 * @param push Set to \"all\" to receive push notifications for all activity, \"mention\" for mentions and direct messages only, and \"none\" to disable. Defaults to \"mention\".
 * @param desktop Set to \"all\" to receive desktop notifications for all activity, \"mention\" for mentions and direct messages only, and \"none\" to disable. Defaults to \"all\".
 * @param desktopSound Set to \"true\" to enable sound on desktop notifications, \"false\" to disable. Defaults to \"true\".
 * @param mentionKeys A comma-separated list of words to count as mentions. Defaults to username and @username.
 * @param channel Set to \"true\" to enable channel-wide notifications (@channel, @all, etc.), \"false\" to disable. Defaults to \"true\".
 * @param firstName Set to \"true\" to enable mentions for first name. Defaults to \"true\" if a first name is set, \"false\" otherwise.
 */
@Introspected
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class UserNotifyProps(
    /* Set to \"true\" to enable email notifications, \"false\" to disable. Defaults to \"true\". */
    val email: Boolean?,
    /* Set to \"all\" to receive push notifications for all activity, \"mention\" for mentions and direct messages only, and \"none\" to disable. Defaults to \"mention\". */
    val push: String?,
    /* Set to \"all\" to receive desktop notifications for all activity, \"mention\" for mentions and direct messages only, and \"none\" to disable. Defaults to \"all\". */
    val desktop: String?,
    /* Set to \"true\" to enable sound on desktop notifications, \"false\" to disable. Defaults to \"true\". */
    val desktopSound: Boolean?,
    /* A comma-separated list of words to count as mentions. Defaults to username and @username. */
    val mentionKeys: String?,
    /* Set to \"true\" to enable channel-wide notifications (@channel, @all, etc.), \"false\" to disable. Defaults to \"true\". */
    val channel: Boolean?,
    /* Set to \"true\" to enable mentions for first name. Defaults to \"true\" if a first name is set, \"false\" otherwise. */
    val firstName: Boolean?
)