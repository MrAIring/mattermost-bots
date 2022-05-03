package io.github.mrairing.mattermost.endpoints

import io.github.mrairing.mattermost.endpoints.dto.WebhookCommandRequest
import io.github.mrairing.mattermost.endpoints.dto.WebhookCommandResponse
import io.github.mrairing.mattermost.services.commands.CommandsTokenVerifier
import io.github.mrairing.mattermost.services.random.RandomService
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import mu.KotlinLogging.logger

@Controller("/commands")
class RandomEndpoint(
    private val verifier: CommandsTokenVerifier,
    private val randomService: RandomService,
) {
    private val log = logger {}

    @Post(uri = "/random", consumes = [MediaType.APPLICATION_FORM_URLENCODED])
    suspend fun random(@Body data: WebhookCommandRequest): WebhookCommandResponse {
        log.info { "random request $data" }
        verifier.assertToken("random", data.token)
        return randomService.random(data)
    }
}