package io.github.mrairing.mattermost.api.users.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import io.micronaut.core.annotation.Introspected

/**
 *
 * @param useAutomaticTimezone Set to \"true\" to use the browser/system timezone, \"false\" to set manually. Defaults to \"true\".
 * @param manualTimezone Value when setting manually the timezone, i.e. \"Europe/Berlin\".
 * @param automaticTimezone This value is set automatically when the \"useAutomaticTimezone\" is set to \"true\".
 */
@Introspected
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class Timezone(
    /* Set to \"true\" to use the browser/system timezone, \"false\" to set manually. Defaults to \"true\". */
    val useAutomaticTimezone: Boolean?,
    /* Value when setting manually the timezone, i.e. \"Europe/Berlin\". */
    val manualTimezone: String?,
    /* This value is set automatically when the \"useAutomaticTimezone\" is set to \"true\". */
    val automaticTimezone: String?
)