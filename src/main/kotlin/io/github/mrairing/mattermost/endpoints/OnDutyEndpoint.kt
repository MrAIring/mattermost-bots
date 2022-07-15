package io.github.mrairing.mattermost.endpoints

import io.github.mrairing.mattermost.endpoints.dto.WebhookCommandRequest
import io.github.mrairing.mattermost.endpoints.dto.WebhookCommandResponse
import io.github.mrairing.mattermost.services.commands.CommandsTokenVerifier
import io.github.mrairing.mattermost.services.onduty.OnDutyService
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import mu.KotlinLogging.logger

@Controller("/commands")
class OnDutyEndpoint(
    private val verifier: CommandsTokenVerifier,
    private val onDutyService: OnDutyService,
) {
    private val log = logger { }

    @Post(uri = "/on-duty", consumes = [MediaType.APPLICATION_FORM_URLENCODED])
    suspend fun random(@Body data: WebhookCommandRequest): WebhookCommandResponse {
        log.info { "on-duty request $data" }
        verifier.assertToken("on-duty", data.token)
        return onDutyService.onDuty(data)
    }
}