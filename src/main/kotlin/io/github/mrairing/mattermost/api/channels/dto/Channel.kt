package io.github.mrairing.mattermost.api.channels.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.micronaut.core.annotation.Introspected
import java.time.Instant

/**
 *
 * @param id
 * @param createAt The time in milliseconds a channel was created
 * @param updateAt The time in milliseconds a channel was last updated
 * @param deleteAt The time in milliseconds a channel was deleted
 * @param teamId
 * @param type
 * @param displayName
 * @param name
 * @param header
 * @param purpose
 * @param lastPostAt The time in milliseconds of the last post of a channel
 * @param totalMsgCount
 * @param creatorId
 */
@Introspected
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class Channel(
    val id: String?,
    /* The time in milliseconds a channel was created */
    val createAt: Instant?,
    /* The time in milliseconds a channel was last updated */
    val updateAt: Instant?,
    /* The time in milliseconds a channel was deleted */
    val deleteAt: Instant?,
    val teamId: String?,
    val type: ChannelType?,
    val displayName: String?,
    val name: String?,
    val header: String?,
    val purpose: String?,
    /* The time in milliseconds of the last post of a channel */
    val lastPostAt: Instant?,
    val lastRootPostAt: Instant?,
    val totalMsgCount: Int?,
    val creatorId: String?,
)

enum class ChannelType {
    /** Open */
    O,

    /** Private */
    P,

    /** Direct */
    D,

    /** Group  */
    G,
}