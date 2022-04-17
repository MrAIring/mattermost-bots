package io.github.mrairing.mattermost.api.users

import io.github.mrairing.mattermost.api.users.dto.User
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Put
import io.micronaut.http.client.annotation.Client

@Client("\${mattermost.base-url}/api/v4/users")
interface UsersClient {

    @Get("/me")
    suspend fun getMe(): User

    @Get("/{id}")
    suspend fun getUser(id: String): User

    @Put("/{id}/patch")
    suspend fun patchUser(id: String, @Body patch: User): User
}