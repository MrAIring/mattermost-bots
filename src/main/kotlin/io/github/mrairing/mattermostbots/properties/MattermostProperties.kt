package io.github.mrairing.mattermostbots.properties

import org.springframework.boot.context.properties.ConfigurationProperties


@ConfigurationProperties("mattermost")
class MattermostProperties {
    lateinit var baseUrl: String
    lateinit var auth: AuthProperties
    lateinit var teamId: String

    class AuthProperties {
        lateinit var token: String
    }
}

