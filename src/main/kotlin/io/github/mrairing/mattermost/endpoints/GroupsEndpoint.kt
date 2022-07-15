package io.github.mrairing.mattermost.endpoints

import io.github.mrairing.mattermost.endpoints.dto.WebhookCommandRequest
import io.github.mrairing.mattermost.endpoints.dto.WebhookCommandResponse
import io.github.mrairing.mattermost.services.commands.CommandsTokenVerifier
import io.github.mrairing.mattermost.services.groups.GroupsService
import io.github.mrairing.mattermost.utils.WebhookUtils.ephemeralResponse
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Error
import io.micronaut.http.annotation.Post
import mu.KotlinLogging.logger

@Controller("/commands")
class GroupsEndpoint(
    private val verifier: CommandsTokenVerifier,
    private val groupsService: GroupsService,
) {
    private val log = logger {}

    @Post(uri = "/group-create", consumes = [MediaType.APPLICATION_FORM_URLENCODED])
    suspend fun groupCreate(@Body data: WebhookCommandRequest): WebhookCommandResponse {
        log.info { "group-create request $data" }
        verifier.assertToken("group-create", data.token)
        return groupsService.groupCreate(data)
    }

    @Post(uri = "/group-delete", consumes = [MediaType.APPLICATION_FORM_URLENCODED])
    suspend fun groupDelete(@Body data: WebhookCommandRequest): WebhookCommandResponse {
        log.info { "group-delete request $data" }
        verifier.assertToken("group-delete", data.token)
        return groupsService.groupDelete(data)
    }

    @Post(uri = "/group-edit", consumes = [MediaType.APPLICATION_FORM_URLENCODED])
    suspend fun groupEdit(@Body data: WebhookCommandRequest): WebhookCommandResponse {
        log.info { "group-edit request $data" }
        verifier.assertToken("group-edit", data.token)
        return groupsService.groupEdit(data)
    }

    @Post(uri = "/group-info", consumes = [MediaType.APPLICATION_FORM_URLENCODED])
    suspend fun groupInfo(@Body data: WebhookCommandRequest): WebhookCommandResponse {
        log.info { "group-info request $data" }
        verifier.assertToken("group-info", data.token)
        return groupsService.groupInfo(data)
    }

    @Error(global = true)
    fun webhookError(request: HttpRequest<*>, e: Throwable): HttpResponse<WebhookCommandResponse> {
        log.error(e) { "error" }
        return HttpResponse.ok(ephemeralResponse("Error:\n${e.message}"))
    }
}