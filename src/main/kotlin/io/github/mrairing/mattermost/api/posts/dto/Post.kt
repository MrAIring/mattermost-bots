package io.github.mrairing.mattermost.api.posts.dto

import io.micronaut.json.tree.JsonObject
import java.time.Instant

/**
 *
 * @param id
 * @param createAt The time in milliseconds a post was created
 * @param updateAt The time in milliseconds a post was last updated
 * @param deleteAt The time in milliseconds a post was deleted
 * @param editAt
 * @param userId
 * @param channelId
 * @param rootId
 * @param originalId
 * @param message
 * @param type
 * @param props
 * @param hashtag
 * @param fileIds
 * @param pendingPostId
 * @param metadata
 */

data class Post(
    val id: String?,
    /* The time in milliseconds a post was created */
    val createAt: Instant?,
    /* The time in milliseconds a post was last updated */
    val updateAt: Instant?,
    /* The time in milliseconds a post was deleted */
    val deleteAt: Instant?,
    val editAt: Instant?,
    val userId: String?,
    val channelId: String?,
    val rootId: String?,
    val originalId: String?,
    val message: String?,
    val type: String?,
    val props: JsonObject?,
    val hashtag: String?,
    val fileIds: List<String>?,
    val pendingPostId: String?,
//    val metadata: PostMetadata?
)