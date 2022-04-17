package io.github.mrairing.mattermost.properties

import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.core.annotation.Introspected

@ConfigurationProperties("webhook")
class WebhookProperties {
    lateinit var baseUrl: String
    lateinit var commands: List<GroupCommandProperties>

    @Introspected
    class GroupCommandProperties {
        lateinit var trigger: String
        lateinit var autoCompleteHint: String
        lateinit var autoCompleteDesc: String
        lateinit var displayName: String
        lateinit var description: String
    }
}