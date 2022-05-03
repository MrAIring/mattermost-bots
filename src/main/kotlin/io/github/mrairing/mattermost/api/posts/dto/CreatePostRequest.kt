package io.github.mrairing.mattermost.api.posts.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.micronaut.core.annotation.Introspected
import io.micronaut.json.tree.JsonObject

/**
 *
 * @param channelId The channel ID to post in
 * @param message The message contents, can be formatted with Markdown
 * @param rootId The post ID to comment on
 * @param fileIds A list of file IDs to associate with the post. Note that posts are limited to 5 files maximum. Please use additional posts for more files.
 * @param props A general JSON property bag to attach to the post
 */
@Introspected
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class CreatePostRequest (
    /* The channel ID to post in */
    val channelId: String,
    /* The message contents, can be formatted with Markdown */
    val message: String,
    /* The post ID to comment on */
    val rootId: String?,
    /* A list of file IDs to associate with the post. Note that posts are limited to 5 files maximum. Please use additional posts for more files. */
    val fileIds: List<String>?,
    /* A general JSON property bag to attach to the post */
    val props: JsonObject?
)