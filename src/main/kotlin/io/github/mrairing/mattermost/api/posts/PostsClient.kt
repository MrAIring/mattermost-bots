package io.github.mrairing.mattermost.api.posts

import io.github.mrairing.mattermost.api.posts.dto.CreatePostRequest
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client

@Client("\${mattermost.base-url}/api/v4/posts")
interface PostsClient {
    @Post
    suspend fun createPost(@Body request: CreatePostRequest): Post
}