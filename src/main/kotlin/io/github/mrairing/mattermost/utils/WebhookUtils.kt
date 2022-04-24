package io.github.mrairing.mattermost.utils

import io.github.mrairing.mattermost.endpoints.dto.WebhookCommandResponse
import io.github.mrairing.mattermost.endpoints.dto.WebhookCommandResponseType

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
}