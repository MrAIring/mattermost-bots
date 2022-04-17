package io.github.mrairing.mattermost.endpoints.dto

import io.micronaut.core.annotation.Introspected
import java.net.URL

@Introspected
data class WebhookCommandRequest(
    val channel_id: String,
    val channel_name: String,
    val command: String,
    val response_url: URL,
    val team_domain: String,
    val team_id: String,
    val text: String,
    val token: String,
    val user_id: String,
    val trigger_id: String?,
    val user_name: String,
    val channel_mentions: List<String>?,
    val channel_mentions_ids: List<String>?,
    val user_mentions: List<String>?,
    val user_mentions_ids: List<String>?,
)