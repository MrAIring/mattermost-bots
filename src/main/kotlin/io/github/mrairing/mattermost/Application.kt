package io.github.mrairing.mattermost

import io.micronaut.runtime.Micronaut.*
fun main(args: Array<String>) {
	build()
	    .args(*args)
		.packages("io.github.mrairing.mattermost")
		.start()
}

