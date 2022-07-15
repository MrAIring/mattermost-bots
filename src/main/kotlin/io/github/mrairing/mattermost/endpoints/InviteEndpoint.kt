package io.github.mrairing.mattermost.endpoints

import io.github.mrairing.mattermost.endpoints.dto.WebhookCommandRequest
import io.github.mrairing.mattermost.endpoints.dto.WebhookCommandResponse
import io.github.mrairing.mattermost.services.commands.CommandsTokenVerifier
import io.github.mrairing.mattermost.services.invite.InviteService
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import mu.KotlinLogging.logger

@Controller("/commands")
class InviteEndpoint(
    private val verifier: CommandsTokenVerifier,
    private val inviteService: InviteService,
) {
    private val log = logger {}

    @Post(uri = "/invite-all-except", consumes = [MediaType.APPLICATION_FORM_URLENCODED])
    suspend fun inviteAllExcept(@Body data: WebhookCommandRequest): WebhookCommandResponse {
        log.info { "invite-all-except request $data" }
        verifier.assertToken("invite-all-except", data.token)
        return inviteService.inviteAllExcept(data)
    }
}