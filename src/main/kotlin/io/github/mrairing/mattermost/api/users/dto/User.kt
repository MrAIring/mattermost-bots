package io.github.mrairing.mattermost.api.users.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.fasterxml.jackson.databind.node.ObjectNode
import io.micronaut.core.annotation.Introspected
import java.time.Instant

/**
 *
 * @param id
 * @param createAt The time in milliseconds a user was created
 * @param updateAt The time in milliseconds a user was last updated
 * @param deleteAt The time in milliseconds a user was deleted
 * @param username
 * @param firstName
 * @param lastName
 * @param nickname
 * @param email
 * @param emailVerified
 * @param authService
 * @param roles
 * @param locale
 * @param notifyProps
 * @param props
 * @param lastPasswordUpdate
 * @param lastPictureUpdate
 * @param failedAttempts
 * @param mfaActive
 * @param timezone
 * @param termsOfServiceId ID of accepted terms of service, if any. This field is not present if empty.
 * @param termsOfServiceCreateAt The time in milliseconds the user accepted the terms of service
 */
@Introspected
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class User(
    val id: String?,
    /* The time in milliseconds a user was created */
    val createAt: Instant?,
    /* The time in milliseconds a user was last updated */
    val updateAt: Instant?,
    /* The time in milliseconds a user was deleted */
    val deleteAt: Instant?,
    val username: String?,
    val firstName: String?,
    val lastName: String?,
    val nickname: String?,
    val email: String?,
    val emailVerified: Boolean?,
    val authService: String?,
    val roles: String?,
    val locale: String?,
    val notifyProps: ObjectNode?,
    val props: Any?,
    val lastPasswordUpdate: Instant?,
    val lastPictureUpdate: Instant?,
    val failedAttempts: Int?,
    val mfaActive: Boolean?,
    val timezone: Timezone?,
    /* ID of accepted terms of service, if any. This field is not present if empty. */
    val termsOfServiceId: String?,
    /* The time in milliseconds the user accepted the terms of service */
    val termsOfServiceCreateAt: Long?
)