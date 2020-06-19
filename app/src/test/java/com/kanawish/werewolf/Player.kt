package com.kanawish.werewolf

import com.kanawish.werewolf.Role.Unassigned
import java.util.UUID

data class Player(val name: String, val role: Role = Unassigned, val alive: Boolean = true) {
    val uuid: UUID = UUID.randomUUID()
    override fun toString(): String {
        return "${role.emoji()} $name" + if(!alive) "💀" else ""
    }
}