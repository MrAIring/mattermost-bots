package io.github.mrairing.mattermost.api.channels

import io.github.mrairing.mattermost.api.channels.dto.AddUserToChannelRequest
import io.github.mrairing.mattermost.api.channels.dto.Channel
import io.github.mrairing.mattermost.api.channels.dto.ChannelMember
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.annotation.Client

@Client("\${mattermost.base-url}/api/v4")
interface ChannelsClient {
    @Post("/teams/{teamId}/channels/ids")
    suspend fun getListOfChannelsByIds(teamId: String, @Body ids: List<String>): List<Channel>

    @Post("/channels/{channelId}/members")
    suspend fun addUserToChannel(channelId: String, @Body request: AddUserToChannelRequest): ChannelMember
}