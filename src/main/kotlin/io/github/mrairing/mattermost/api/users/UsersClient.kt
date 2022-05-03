package io.github.mrairing.mattermost.api.users

import io.github.mrairing.mattermost.api.users.dto.User
import io.github.mrairing.mattermost.api.users.dto.UserAccessToken
import io.github.mrairing.mattermost.api.users.dto.UserAccessTokenDescription
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.micronaut.http.annotation.QueryValue
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

    @Post("/ids")
    suspend fun getUsersByIds(@Body ids: List<String>): List<User>

    /**
     * Get users
     * fGet a page of a list of users. Based on query string parameters, select users from a team, channel, or select users not in a specific channel.  Since server version 4.0, some basic sorting is available using the &#x60;sort&#x60; query parameter. Sorting is currently only supported when selecting users on a team. ##### Permissions Requires an active session and (if specified) membership to the channel or team being selected from.
     * @param page The page to select. (optional, default to 0)
     * @param perPage The number of users per page. There is a maximum limit of 200 users per page. (optional, default to 60)
     * @param inTeam The ID of the team to get users for. (optional)
     * @param notInTeam The ID of the team to exclude users for. Must not be used with \&quot;in_team\&quot; query parameter. (optional)
     * @param inChannel The ID of the channel to get users for. (optional)
     * @param notInChannel The ID of the channel to exclude users for. Must be used with \&quot;in_channel\&quot; query parameter. (optional)
     * @param inGroup The ID of the group to get users for. Must have &#x60;manage_system&#x60; permission. (optional)
     * @param groupConstrained When used with &#x60;not_in_channel&#x60; or &#x60;not_in_team&#x60;, returns only the users that are allowed to join the channel or team based on its group constrains. (optional)
     * @param withoutTeam Whether or not to list users that are not on any team. This option takes precendence over &#x60;in_team&#x60;, &#x60;in_channel&#x60;, and &#x60;not_in_channel&#x60;. (optional)
     * @param active Whether or not to list only users that are active. This option cannot be used along with the &#x60;inactive&#x60; option. (optional)
     * @param inactive Whether or not to list only users that are deactivated. This option cannot be used along with the &#x60;active&#x60; option. (optional)
     * @param role Returns users that have this role. (optional)
     * @param sort Sort is only available in conjunction with certain options below. The paging parameter is also always available.  ##### &#x60;in_team&#x60; Can be \&quot;\&quot;, \&quot;last_activity_at\&quot; or \&quot;create_at\&quot;. When left blank, sorting is done by username. __Minimum server version__: 4.0 ##### &#x60;in_channel&#x60; Can be \&quot;\&quot;, \&quot;status\&quot;. When left blank, sorting is done by username. &#x60;status&#x60; will sort by User&#39;s current status (Online, Away, DND, Offline), then by Username. __Minimum server version__: 4.7  (optional)
     * @param roles Comma separated string used to filter users based on any of the specified system roles  Example: &#x60;?roles&#x3D;system_admin,system_user&#x60; will return users that are either system admins or system users  __Minimum server version__: 5.26  (optional)
     * @param channelRoles Comma separated string used to filter users based on any of the specified channel roles, can only be used in conjunction with &#x60;in_channel&#x60;  Example: &#x60;?in_channel&#x3D;4eb6axxw7fg3je5iyasnfudc5y&amp;channel_roles&#x3D;channel_user&#x60; will return users that are only channel users and not admins or guests  __Minimum server version__: 5.26  (optional)
     * @param teamRoles Comma separated string used to filter users based on any of the specified team roles, can only be used in conjunction with &#x60;in_team&#x60;  Example: &#x60;?in_team&#x3D;4eb6axxw7fg3je5iyasnfudc5y&amp;team_roles&#x3D;team_user&#x60; will return users that are only team users and not admins or guests  __Minimum server version__: 5.26  (optional)
     * @return kotlin.collections.List<User>
     */
    @Get
    suspend fun getUsers(
        @QueryValue("page") page: Int? = null,
        @QueryValue("per_page") perPage: Int? = null,
        @QueryValue("in_team") inTeam: String? = null,
        @QueryValue("not_in_team") notInTeam: String? = null,
        @QueryValue("in_channel") inChannel: String? = null,
        @QueryValue("not_in_channel") notInChannel: String? = null,
        @QueryValue("in_group") inGroup: String? = null,
        @QueryValue("group_constrained") groupConstrained: Boolean? = null,
        @QueryValue("without_team") withoutTeam: Boolean? = null,
        @QueryValue("active") active: Boolean? = null,
        @QueryValue("inactive") inactive: Boolean? = null,
        @QueryValue("role") role: String? = null,
        @QueryValue("sort") sort: String? = null,
        @QueryValue("roles") roles: String? = null,
        @QueryValue("channel_roles") channelRoles: String? = null,
        @QueryValue("team_roles") teamRoles: String? = null
    ): List<User>
}