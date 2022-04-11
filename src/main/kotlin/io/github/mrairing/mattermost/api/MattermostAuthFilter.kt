package io.github.mrairing.mattermost.api

import io.github.mrairing.mattermost.properties.MattermostProperties
import io.micronaut.http.HttpResponse
import io.micronaut.http.MutableHttpRequest
import io.micronaut.http.annotation.Filter
import io.micronaut.http.filter.ClientFilterChain
import io.micronaut.http.filter.HttpClientFilter
import org.reactivestreams.Publisher

@Filter(patterns = ["/api/v4/**"])
internal class MattermostAuthFilter(private val auth: MattermostProperties.AuthProperties) : HttpClientFilter {
    override fun doFilter(request: MutableHttpRequest<*>, chain: ClientFilterChain): Publisher<out HttpResponse<*>> {
        return chain.proceed(request.bearerAuth(auth.token))
    }
}