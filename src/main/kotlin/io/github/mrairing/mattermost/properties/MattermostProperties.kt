package io.github.mrairing.mattermost.properties

import io.micronaut.context.annotation.ConfigurationProperties

@ConfigurationProperties("mattermost")
class MattermostProperties {
    lateinit var baseUrl: String
    lateinit var auth: AuthProperties
    lateinit var teamId: String

    @ConfigurationProperties("auth")
    class AuthProperties {
        lateinit var token: String
    }
}

