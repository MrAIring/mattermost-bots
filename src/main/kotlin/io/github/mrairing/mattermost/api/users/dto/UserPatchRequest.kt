package io.github.mrairing.mattermost.api.users.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.fasterxml.jackson.databind.node.ObjectNode
import io.micronaut.core.annotation.Introspected
import io.micronaut.json.tree.JsonObject

@Introspected
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class UserPatchRequest (
    val email: String? = null,
    val username: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val nickname: String? = null,
    val locale: String? = null,
    val position: String? = null,
    val props: JsonObject? = null,
    val notifyProps: ObjectNode? = null
)