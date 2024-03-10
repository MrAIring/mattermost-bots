package io.github.mrairing.mattermostbots.endpoints

import io.github.mrairing.mattermostbots.endpoints.dto.WebhookCommandRequest
import io.github.mrairing.mattermostbots.endpoints.dto.WebhookCommandResponse
import io.github.mrairing.mattermostbots.services.commands.CommandsTokenVerifier
import io.github.mrairing.mattermostbots.services.invite.InviteService
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController("/commands")
class InviteEndpoint(
    private val verifier: CommandsTokenVerifier,
    private val inviteService: InviteService,
) {
    private val log = logger {}

    @PostMapping("/invite-all-except", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun inviteAllExcept(@RequestBody data: WebhookCommandRequest): WebhookCommandResponse {
        log.info { "invite-all-except request $data" }
        verifier.assertToken("invite-all-except", data.token)
        return inviteService.inviteAllExcept(data)
    }
}