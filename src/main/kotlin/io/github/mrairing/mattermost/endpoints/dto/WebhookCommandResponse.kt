package io.github.mrairing.mattermost.endpoints.dto

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.fasterxml.jackson.databind.node.ObjectNode
import io.micronaut.core.annotation.Introspected
import java.time.Instant

@Introspected
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class WebhookCommandResponse(
    val responseType: WebhookCommandResponseType,
    val text: String,
    val username: String?,
    val channelId: String?,
    val iconURL: String?,
    val type: String?,
    val props: ObjectNode?,
    val gotoLocation: String?,
    val triggerId: String?,
    val skipSlackParsing: Boolean,
    val attachments: List<SlackAttachment>?,
    val extraResponses: List<WebhookCommandResponse>?
)

enum class WebhookCommandResponseType {
    in_channel, ephemeral
}

@Introspected
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class SlackAttachment(
    val id: Long?,
    val fallback: String?,
    val color: String?,
    val pretext: String?,
    val authorName: String?,
    val authorLink: String?,
    val authorIcon: String?,
    val title: String?,
    val titleLink: String?,
    val text: String?,
    val fields: List<SlackAttachmentField>?,
    val imageURL: String?,
    val thumbURL: String?,
    val footer: String?,
    val footerIcon: String?,
    val timestamp: Instant?,
//    val actions: List<PostAction>,
)

@Introspected
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class SlackAttachmentField(
    val title: String,
    val value: JsonNode,
    val short: Boolean,
)