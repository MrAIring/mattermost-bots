package io.github.mrairing.mattermost.api.users

import io.github.mrairing.mattermost.api.users.dto.User
import io.github.mrairing.mattermost.api.users.dto.UserAccessToken
import io.github.mrairing.mattermost.api.users.dto.UserAccessTokenDescription
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
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

    @Post("/{id}/tokens")
    suspend fun createUserAccessToken(id: String, @Body description: UserAccessTokenDescription): UserAccessToken
}