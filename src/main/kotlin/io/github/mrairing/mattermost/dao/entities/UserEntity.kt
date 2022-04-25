package io.github.mrairing.mattermost.dao.entities

import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity

@MappedEntity("users")
class UserEntity {
    @GeneratedValue
    @Id
    var id: Int? = null
    lateinit var mmId: String
}