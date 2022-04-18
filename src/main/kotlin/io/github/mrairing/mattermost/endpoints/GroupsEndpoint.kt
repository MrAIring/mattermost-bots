package io.github.mrairing.mattermost.endpoints

import io.github.mrairing.mattermost.endpoints.dto.WebhookCommandRequest
import io.github.mrairing.mattermost.services.CommandsTokenVerifier
import io.github.mrairing.mattermost.services.GroupsService
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post
import mu.KotlinLogging.logger

@Controller("/commands")
class GroupsEndpoint(
    private val verifier: CommandsTokenVerifier,
    private val groupsService: GroupsService,
) {
    private val log = logger {}

    @Post(uri = "/group-create", consumes = [MediaType.APPLICATION_FORM_URLENCODED])
    suspend fun groupCreate(@Body data: WebhookCommandRequest) {
        log.info { "group-create request $data" }
        verifier.assertToken("group-create", data.token)
        groupsService.groupCreate(data)
    }

    @Post(uri = "/group-delete", consumes = [MediaType.APPLICATION_FORM_URLENCODED])
    suspend fun groupDelete(@Body data: WebhookCommandRequest) {
        log.info { "group-delete request $data" }
        verifier.assertToken("group-delete", data.token)
        groupsService.groupDelete(data)
    }

    @Post(uri = "/group-edit", consumes = [MediaType.APPLICATION_FORM_URLENCODED])
    suspend fun groupEdit(@Body data: WebhookCommandRequest) {
        log.info { "group-edit request $data" }
        verifier.assertToken("group-edit", data.token)
        groupsService.groupEdit(data)
    }
}