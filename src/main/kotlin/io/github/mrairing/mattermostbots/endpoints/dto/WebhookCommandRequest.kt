package io.github.mrairing.mattermostbots.endpoints.dto

import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.net.URL

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class WebhookCommandRequest(
    val channelId: String,
    val channelName: String,
    val command: String,
    val responseUrl: URL,
    val teamDomain: String,
    val teamId: String,
    val text: String,
    val token: Token,
    val userId: String,
    val triggerId: String?,
    val userName: String,
    val channelMentions: List<String>?,
    val channelMentionsIds: List<String>?,
    val userMentions: List<String>?,
    val userMentionsIds: List<String>?,
)

data class Token(@get:JsonValue val value: String) {
    override fun toString() = "***"
}