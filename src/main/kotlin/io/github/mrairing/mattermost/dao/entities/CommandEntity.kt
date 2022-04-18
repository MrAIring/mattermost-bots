package io.github.mrairing.mattermost.dao.entities

import io.micronaut.data.annotation.GeneratedValue
import io.micronaut.data.annotation.Id
import io.micronaut.data.annotation.MappedEntity

@MappedEntity("commands")
class CommandEntity {
    @GeneratedValue
    @Id
    var id: Int? = null
    lateinit var trigger: String
    lateinit var token: String
}