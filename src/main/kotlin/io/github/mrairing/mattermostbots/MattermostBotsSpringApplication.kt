package io.github.mrairing.mattermostbots

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MattermostBotsSpringApplication

fun main(args: Array<String>) {
    runApplication<MattermostBotsSpringApplication>(*args)
}
