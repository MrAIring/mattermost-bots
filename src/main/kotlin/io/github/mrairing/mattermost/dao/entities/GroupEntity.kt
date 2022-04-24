package io.github.mrairing.mattermost.dao.entities

import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity

@MappedEntity("groups")
class GroupEntity {
    @GeneratedValue
    @Id
    var id: Int? = null
    lateinit var mmId: String
    lateinit var token: String
    lateinit var name: String
}