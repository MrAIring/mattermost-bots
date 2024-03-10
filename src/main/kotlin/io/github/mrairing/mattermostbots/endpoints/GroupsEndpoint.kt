package io.github.mrairing.mattermostbots.endpoints

import io.github.mrairing.mattermostbots.endpoints.dto.WebhookCommandRequest
import io.github.mrairing.mattermostbots.endpoints.dto.WebhookCommandResponse
import io.github.mrairing.mattermostbots.services.commands.CommandsTokenVerifier
import io.github.mrairing.mattermostbots.services.groups.GroupsService
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController("/commands")
class GroupsEndpoint(
    private val verifier: CommandsTokenVerifier,
    private val groupsService: GroupsService,
) {
    private val log = logger {}

    @PostMapping("/group-create", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun groupCreate(@RequestBody data: WebhookCommandRequest): WebhookCommandResponse {
        log.info { "group-create request $data" }
        verifier.assertToken("group-create", data.token)
        return groupsService.groupCreate(data)
    }

    @PostMapping("/group-delete", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun groupDelete(@RequestBody data: WebhookCommandRequest): WebhookCommandResponse {
        log.info { "group-delete request $data" }
        verifier.assertToken("group-delete", data.token)
        return groupsService.groupDelete(data)
    }

    @PostMapping("/group-edit", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun groupEdit(@RequestBody data: WebhookCommandRequest): WebhookCommandResponse {
        log.info { "group-edit request $data" }
        verifier.assertToken("group-edit", data.token)
        return groupsService.groupEdit(data)
    }

    @PostMapping("/group-info", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun groupInfo(@RequestBody data: WebhookCommandRequest): WebhookCommandResponse {
        log.info { "group-info request $data" }
        verifier.assertToken("group-info", data.token)
        return groupsService.groupInfo(data)
    }

}