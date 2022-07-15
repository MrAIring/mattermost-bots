package io.github.mrairing.mattermost.endpoints.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.micronaut.core.annotation.Introspected
import net.afanasev.sekret.Secret
import java.net.URL

@Introspected
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class WebhookCommandRequest(
    val channelId: String,
    val channelName: String,
    val command: String,
    val responseUrl: URL,
    val teamDomain: String,
    val teamId: String,
    val text: String,
    @Secret
    val token: String,
    val userId: String,
    val triggerId: String?,
    val userName: String,
    val channelMentions: List<String>?,
    val channelMentionsIds: List<String>?,
    val userMentions: List<String>?,
    val userMentionsIds: List<String>?,
)