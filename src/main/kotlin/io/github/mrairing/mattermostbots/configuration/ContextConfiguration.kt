package io.github.mrairing.mattermostbots.configuration

import io.github.mrairing.mattermostbots.properties.MattermostProperties
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.Configuration

@ConfigurationPropertiesScan(basePackageClasses = [MattermostProperties::class])
@Configuration(proxyBeanMethods = false)
class ContextConfiguration {
}