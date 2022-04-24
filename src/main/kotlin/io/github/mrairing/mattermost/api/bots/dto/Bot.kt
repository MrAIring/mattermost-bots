package io.github.mrairing.mattermost.api.bots.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.micronaut.core.annotation.Introspected
import java.time.Instant

@Introspected
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class Bot(
    val userId: String,
    val createAt: Instant,
    val updateAt: Instant?,
    val deleteAt: Instant?,
    val username: String,
    val displayName: String?,
    val description: String?,
    val ownerId: String?
)