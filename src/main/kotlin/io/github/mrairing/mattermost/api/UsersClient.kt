package io.github.mrairing.mattermost.api

import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Put
import io.micronaut.http.client.annotation.Client
import java.time.Instant

@Client("\${mattermost.base-url}/api/v4/users")
interface UsersClient {
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
    data class User(
        @JsonProperty("id")
        val id: String? = null,
        /* The time in milliseconds a user was created */
        @JsonProperty("create_at")
        val createAt: Instant? = null,
        /* The time in milliseconds a user was last updated */
        @JsonProperty("update_at")
        val updateAt: Instant? = null,
        /* The time in milliseconds a user was deleted */
        @JsonProperty("delete_at")
        val deleteAt: Instant? = null,
        @JsonProperty("username")
        val username: String? = null,
        @JsonProperty("first_name")
        val firstName: String? = null,
        @JsonProperty("last_name")
        val lastName: String? = null,
        @JsonProperty("nickname")
        val nickname: String? = null,
        @JsonProperty("email")
        val email: String? = null,
        @JsonProperty("email_verified")
        val emailVerified: Boolean? = null,
        @JsonProperty("auth_service")
        val authService: String? = null,
        @JsonProperty("roles")
        val roles: String? = null,
        @JsonProperty("locale")
        val locale: String? = null,
        @JsonProperty("notify_props")
        val notifyProps: UserNotifyProps? = null,
        @JsonProperty("props")
        val props: Any? = null,
        @JsonProperty("last_password_update")
        val lastPasswordUpdate: Instant? = null,
        @JsonProperty("last_picture_update")
        val lastPictureUpdate: Instant? = null,
        @JsonProperty("failed_attempts")
        val failedAttempts: Int? = null,
        @JsonProperty("mfa_active")
        val mfaActive: Boolean? = null,
        @JsonProperty("timezone")
        val timezone: Timezone? = null,
        /* ID of accepted terms of service, if any. This field is not present if empty. */
        @JsonProperty("terms_of_service_id")
        val termsOfServiceId: String? = null,
        /* The time in milliseconds the user accepted the terms of service */
        @JsonProperty("terms_of_service_create_at")
        val termsOfServiceCreateAt: Long? = null
    )

    /**
     *
     * @param email Set to \"true\" to enable email notifications, \"false\" to disable. Defaults to \"true\".
     * @param push Set to \"all\" to receive push notifications for all activity, \"mention\" for mentions and direct messages only, and \"none\" to disable. Defaults to \"mention\".
     * @param desktop Set to \"all\" to receive desktop notifications for all activity, \"mention\" for mentions and direct messages only, and \"none\" to disable. Defaults to \"all\".
     * @param desktopSound Set to \"true\" to enable sound on desktop notifications, \"false\" to disable. Defaults to \"true\".
     * @param mentionKeys A comma-separated list of words to count as mentions. Defaults to username and @username.
     * @param channel Set to \"true\" to enable channel-wide notifications (@channel, @all, etc.), \"false\" to disable. Defaults to \"true\".
     * @param firstName Set to \"true\" to enable mentions for first name. Defaults to \"true\" if a first name is set, \"false\" otherwise.
     */
    @Introspected
    data class UserNotifyProps(
        /* Set to \"true\" to enable email notifications, \"false\" to disable. Defaults to \"true\". */
        @JsonProperty("email")
        val email: Boolean? = null,
        /* Set to \"all\" to receive push notifications for all activity, \"mention\" for mentions and direct messages only, and \"none\" to disable. Defaults to \"mention\". */
        @JsonProperty("push")
        val push: String? = null,
        /* Set to \"all\" to receive desktop notifications for all activity, \"mention\" for mentions and direct messages only, and \"none\" to disable. Defaults to \"all\". */
        @JsonProperty("desktop")
        val desktop: String? = null,
        /* Set to \"true\" to enable sound on desktop notifications, \"false\" to disable. Defaults to \"true\". */
        @JsonProperty("desktop_sound")
        val desktopSound: Boolean? = null,
        /* A comma-separated list of words to count as mentions. Defaults to username and @username. */
        @JsonProperty("mention_keys")
        val mentionKeys: String? = null,
        /* Set to \"true\" to enable channel-wide notifications (@channel, @all, etc.), \"false\" to disable. Defaults to \"true\". */
        @JsonProperty("channel")
        val channel: Boolean? = null,
        /* Set to \"true\" to enable mentions for first name. Defaults to \"true\" if a first name is set, \"false\" otherwise. */
        @JsonProperty("first_name")
        val firstName: Boolean? = null
    )

    /**
     *
     * @param useAutomaticTimezone Set to \"true\" to use the browser/system timezone, \"false\" to set manually. Defaults to \"true\".
     * @param manualTimezone Value when setting manually the timezone, i.e. \"Europe/Berlin\".
     * @param automaticTimezone This value is set automatically when the \"useAutomaticTimezone\" is set to \"true\".
     */
    @Introspected
    data class Timezone(
        /* Set to \"true\" to use the browser/system timezone, \"false\" to set manually. Defaults to \"true\". */
        @JsonProperty("useAutomaticTimezone")
        val useAutomaticTimezone: Boolean? = null,
        /* Value when setting manually the timezone, i.e. \"Europe/Berlin\". */
        @JsonProperty("manualTimezone")
        val manualTimezone: String? = null,
        /* This value is set automatically when the \"useAutomaticTimezone\" is set to \"true\". */
        @JsonProperty("automaticTimezone")
        val automaticTimezone: String? = null
    )

    @Get("/me")
    suspend fun getMe(): User

    @Get("/{id}")
    suspend fun getUser(id: String): User

    @Put("/{id}/patch")
    suspend fun patchUser(id: String, @Body patch: User): User
}