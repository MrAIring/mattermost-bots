package io.github.mrairing.mattermostbots.utils

import io.github.mrairing.mattermostbots.api.UsersApi
import io.github.mrairing.mattermostbots.api.dto.User

fun UsersApi.getUsers(
    page: Int,
    teamId: String? = null,
    inChannel: String? = null,
    active: Boolean = true
): List<User> {
    return getUsers(
        page,
        null,
        teamId,
        null,
        inChannel,
        null,
        null,
        null,
        null,
        active,
        null,
        null,
        null,
        null,
        null,
        null,
    )
}

fun UsersApi.getUsersByIds(ids: Collection<String>): List<User> {
    return getUsersByIds(if (ids is List<String>) ids else ids.toList(), 0)
}