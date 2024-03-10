package io.github.mrairing.mattermostbots.endpoints

import io.github.mrairing.mattermostbots.endpoints.dto.WebhookCommandRequest
import io.github.mrairing.mattermostbots.endpoints.dto.WebhookCommandResponse
import io.github.mrairing.mattermostbots.services.commands.CommandsTokenVerifier
import io.github.mrairing.mattermostbots.services.random.RandomService
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController("/commands")
class RandomEndpoint(
    private val verifier: CommandsTokenVerifier,
    private val randomService: RandomService,
) {
    private val log = logger {}

    @PostMapping("/random", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun random(@RequestBody data: WebhookCommandRequest): WebhookCommandResponse {
        log.info { "random request $data" }
        verifier.assertToken("random", data.token)
        return randomService.random(data)
    }
}