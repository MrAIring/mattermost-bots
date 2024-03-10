package io.github.mrairing.mattermostbots.endpoints.exeptions

import io.github.mrairing.mattermostbots.endpoints.dto.WebhookCommandResponse
import io.github.mrairing.mattermostbots.utils.WebhookUtils
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ErrorHandlingControllerAdvice {
    private val log = KotlinLogging.logger {}

    @ExceptionHandler
    fun webhookError(e: Throwable): ResponseEntity<WebhookCommandResponse> {
        log.error(e) { "error" }
        return ResponseEntity.ok(WebhookUtils.ephemeralResponse("Error:\n${e.message}"))
    }
}