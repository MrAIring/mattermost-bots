package io.github.mrairing.mattermost.endpoints

import io.github.mrairing.mattermost.endpoints.dto.WebhookCommandRequest
import io.github.mrairing.mattermost.services.CommandsTokenVerifier
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import mu.KotlinLogging.logger

@Controller("/commands")
class CommandsEndpoint(
    private val verifier: CommandsTokenVerifier,
) {
    private val log = logger {}

    @Post(uri = "/group-create", consumes = [MediaType.APPLICATION_FORM_URLENCODED])
    suspend fun group(request: HttpRequest<*>, @Body data: WebhookCommandRequest) {
        verifier.assertToken("group-create", data.token)

        log.info { "Request: ${request.parameters}" }
        log.info { "Data: $data" }
    }
}