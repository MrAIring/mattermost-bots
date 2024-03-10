package io.github.mrairing.mattermostbots.properties

import org.springframework.boot.context.properties.ConfigurationProperties


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