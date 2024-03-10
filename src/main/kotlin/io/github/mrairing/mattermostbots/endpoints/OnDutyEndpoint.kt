package io.github.mrairing.mattermostbots.endpoints

import io.github.mrairing.mattermostbots.endpoints.dto.WebhookCommandRequest
import io.github.mrairing.mattermostbots.endpoints.dto.WebhookCommandResponse
import io.github.mrairing.mattermostbots.services.commands.CommandsTokenVerifier
import io.github.mrairing.mattermostbots.services.onduty.OnDutyService
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController("/commands")
class OnDutyEndpoint(
    private val verifier: CommandsTokenVerifier,
    private val onDutyService: OnDutyService,
) {
    private val log = logger { }

    @PostMapping("/on-duty", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun random(@RequestBody data: WebhookCommandRequest): WebhookCommandResponse {
        log.info { "on-duty request $data" }
        verifier.assertToken("on-duty", data.token)
        return onDutyService.onDuty(data)
    }
}