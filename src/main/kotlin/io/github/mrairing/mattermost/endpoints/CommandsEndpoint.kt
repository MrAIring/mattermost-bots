package io.github.mrairing.mattermost.endpoints

import io.github.mrairing.mattermost.services.CommandsTokenVerifier
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import mu.KotlinLogging.logger
import java.net.URL

@Controller("/commands")
class CommandsEndpoint(
    private val verifier: CommandsTokenVerifier,
) {
    private val log = logger {}

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

    @Post(uri = "/group-create", consumes = [MediaType.APPLICATION_FORM_URLENCODED])
    suspend fun group(request: HttpRequest<*>, @Body data: WebhookCommandRequest) {
        verifier.assertToken("group-create", data.token)

        log.info { "Request: ${request.parameters}" }
        log.info { "Data: $data" }
    }
}