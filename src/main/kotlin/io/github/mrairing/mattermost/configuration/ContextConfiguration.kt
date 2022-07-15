package io.github.mrairing.mattermost.configuration

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton

@Factory
class ContextConfiguration {
    @Requires(property = "jackson.module-scan", value = "false")
    @Singleton
    fun javaTimeModuleFactory() : JavaTimeModule {
        return JavaTimeModule()
    }
}