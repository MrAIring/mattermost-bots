package io.github.mrairing.mattermostbots.utils

import io.github.mrairing.mattermostbots.endpoints.dto.WebhookCommandResponse
import io.github.mrairing.mattermostbots.endpoints.dto.WebhookCommandResponseType

object WebhookUtils {
    fun ephemeralResponse(text: String): WebhookCommandResponse {
        return WebhookCommandResponse(
            responseType = WebhookCommandResponseType.ephemeral,
            text = text,
            username = null,
            channelId = null,
            iconURL = null,
            type = null,
            props = null,
            gotoLocation = null,
            triggerId = null,
            skipSlackParsing = true,
            attachments = null,
            extraResponses = null,
        )
    }

    fun inChannelResponse(text: String, username: String? = null, iconUrl: String? = null): WebhookCommandResponse {
        return WebhookCommandResponse(
            responseType = WebhookCommandResponseType.in_channel,
            text = text,
            username = username,
            channelId = null,
            iconURL = iconUrl,
            type = null,
            props = null,
            gotoLocation = null,
            triggerId = null,
            skipSlackParsing = true,
            attachments = null,
            extraResponses = null,
        )
    }
}