package io.github.mrairing.mattermost.properties

import io.micronaut.context.annotation.ConfigurationProperties

@ConfigurationProperties("webhook")
class WebhookProperties {
    lateinit var baseUrl: String
    lateinit var commands: List<GroupCommandProperties>

    class GroupCommandProperties {
        lateinit var trigger: String
        lateinit var autoCompleteHint: String
        lateinit var autoCompleteDesc: String
        lateinit var displayName: String
        lateinit var description: String
    }
}