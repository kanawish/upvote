package com.kanawish.werewolf

import com.kanawish.werewolf.Role.Team.*


sealed class Role {
    enum class Team { VILLAGERS, WEREWOLVES, NEUTRAL }

    fun team() = when (this) {
        Villager, Seer, Doctor -> VILLAGERS
        Werewolf -> WEREWOLVES
        else -> NEUTRAL
    }

    object Unassigned : Role()
    object Seer : Role()
    object Doctor : Role()
    object Werewolf : Role()
    object Villager : Role()
}