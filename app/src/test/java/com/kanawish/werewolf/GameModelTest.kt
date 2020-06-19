package com.kanawish.werewolf

import com.kanawish.upvote.common.Ansi
import com.kanawish.upvote.common.ModelStore
import com.kanawish.werewolf.Election.*
import com.kanawish.werewolf.Role.Doctor
import com.kanawish.werewolf.Role.Seer
import com.kanawish.werewolf.Role.Team
import com.kanawish.werewolf.Role.Team.VILLAGERS
import com.kanawish.werewolf.Role.Team.WEREWOLVES
import com.kanawish.werewolf.Role.Villager
import com.kanawish.werewolf.Role.Werewolf
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class GameModelTest {

    @After
    fun resetColor() {
        println("${Ansi.RESET}")
    }

    @Test
    fun foo() {
        println("${Ansi.GREEN}fooTest")
        assertTrue(true)
    }

    @Test
    fun bar() {
        println("${Ansi.YELLOW}barTest")
        assertFalse(false)
    }

    @Test
    fun randomizeRoleTest() {
        println("${Ansi.BLUE}randomizeRoleTest()")
        // Take a list of 7 players, shuffled with a seeded random sequence.
        val random = Random(42)

        val players = playerNames
            .shuffled(random)
            .take(7)
            .map { shuffledName -> Player(shuffledName) }
            .randomizeRoles(random)
            .sortedBy { player -> player.name }
            .onEach { player -> println("🎲 $player") }

        assertEquals(players.count { it.role == Werewolf }, 2)
        assertEquals(players.count { it.role == Doctor }, 1)
        assertEquals(players.count { it.role == Seer }, 1)
        assertEquals(players.count { it.role == Villager }, 3)

        players
            .groupingBy { it.role }
            .eachCount()
            .map { entry -> "${entry.value} ${entry.key.emoji()}" }
            .joinToString(prefix = "\nTally: ", separator = ", ")
            .also { result -> println(result) }

    }

    @Test
    fun lobbyTest() {

    }

    fun electionTest() {
//        ModelStore<>
    }
}

// ----------------------------------------------------
// ----------------------------------------------------

/**
 * @startuml
 * [*] --> Lobby
 * Lobby -> Lobby
 * Lobby -> Lobby : ready(player)
 * Lobby: playerQueue
 *
 * @enduml
 */


sealed class Game {
    data class Lobby(val registration: List<Player>) : Game() {
        fun add(player: Player): Lobby = copy(registration = registration + player)
        fun remove(player: Player): Lobby = copy(registration = registration - player)
        fun startGame(): Playing {
            return Playing(registration.randomizeRoles())
        }
    }

    data class Completed(val finalResults: List<Player>) : Game() {
    }

    /**
     * This extension function gives us a tally of who's alive for each team.
     * [code reuse]
     */
    fun List<Player>.survivors(): Map<Team, Int> {
        val survivors = filter { player -> player.alive }

        return survivors
            .groupingBy { player -> player.role.team() }
            .eachCount()
    }

    /**
     * Villagers win when the wolves are gone,
     * the wolves win if they match the villager headcount.
     * [code reuse]
     */
    fun Map<Team, Int>.winningTeam(): Team? {
        val wolves = this[WEREWOLVES] ?: 0
        val villagers = this[VILLAGERS] ?: 0
        return when {
            wolves == 0 -> VILLAGERS
            wolves >= villagers -> WEREWOLVES
            else -> null
        }
    }

}

/**
 * Election is complete if more than half the voters picked the
 * a candidate from the ballot.
 *
 * Otherwise this function returns null.
 */
private fun Map<Player,Player>.findMajority(electorateSize:Int): Player? {
    // Group all the votes for our candidates.
    return this.values
        .groupingBy { it }
        .eachCount()
        // We're done when a candidate has 50%+1 of the vote.
        .filter { it.value > electorateSize/2 }
        .keys.firstOrNull()
}

sealed class Election {
    data class Electing(
        val electorate: List<Player>,
        val candidates: List<Player>,
        val ballots: Map<Player, Player> = emptyMap()
    ) : Election() {


        fun vote(ballot: Pair<Player, Player>): Election {
            // A small example of "Soft" validation, we ignore bad attempts.
            if(!electorate.contains(ballot.first) || !candidates.contains(ballot.second)) {
                println("Illegal voting attempt.")
                return this
            }

            // Add the new ballot
            val updatedBallots = ballots + ballot

            // If we have an elected candidate, return it, otherwise we update `Electing` state.
            return updatedBallots
                .findMajority(electorate.size)
                ?.let { Elected(it) }
                ?: copy(ballots = updatedBallots)
        }
    }

    data class Elected(val result: Player) : Election()

}


// NOTE: Reactively, can trigger the transition by listening to current state.
// TODO: Check idea to return transition when valid, wrt above ^


// -> votes
// -> next turn
// ->

sealed class Turn {
    data class WerewolfTurn(val election:Election) : Turn() {
        fun vote(pick: Pair<Player, Player>): WerewolfTurn {
            return copy() // ...
        }

        fun isComplete(electorate: List<Player>) {
            electorate
                .filter { it.role == Werewolf }
            // ...
        }

        fun finish() = DoctorTurn()
    }

    data class DoctorTurn(val pick: Player? = null) : Turn() {
        fun pick(pick: Player) = copy(pick = pick)
        fun finish() = SeerTurn()
    }

    data class SeerTurn(val pick: Player? = null) : Turn() {
        fun pick(pick: Player) = copy(pick = pick)
        fun finish() = VillagerTurn()
    }

    data class VillagerTurn(val votes: Map<Player, Player> = emptyMap()) :
        Turn() {
        fun pick(pick: Pair<Player, Player>) = copy(votes = votes + pick)
    }
}


data class Playing(
    val players: List<Player>
//    val currentTurn: Turn = startWerewolfTurn()
) : Game() {
/*
    fun startWerewolfTurn(): Playing.Turn {
        val teams = players
            .filter{ player -> player.alive }
            .groupBy { player -> player.role.team() }

        return WerewolfTurn(
                Electing(
                        teams[WEREWOLVES] ?: error("Missed a game ending condition."),
                        teams[VILLAGERS] ?: error("Missed a game ending condition.")
                )
        )
    }
*/

}


interface ElectionRule {
    // Is the election complete?
    fun isComplete(votes: Map<Player, Player>): Boolean

    // What is the result?
    fun result(votes: Map<Player, Player>): Player?
}

class Majority(val players: List<Player>) : ElectionRule {
    val playerCount: Int = players.size

    override fun isComplete(votes: Map<Player, Player>): Boolean {
        votes.values.groupingBy { it }.eachCount()
        return votes.size > (playerCount / 2)
    }

    override fun result(votes: Map<Player, Player>): Player? {
        val talliedVotes = votes.values
            .groupingBy { it }
            .eachCount()

        talliedVotes
            .filterValues { it > playerCount / 2 } // We want 50% + 1
            .maxBy { it.value } // Logically,

        return talliedVotes.maxBy { it.value }?.let { if (it.value > (playerCount / 2)) it.key else null }
    }
}


sealed class Phase() {

    data class LobbyPhase(val players: List<Player>) : Phase() {
        fun addPlayer(player: Player): LobbyPhase {
            return copy(players = players + player)
        }

        fun removePlayer(player: Player): LobbyPhase {
            return copy(players = players - player)
        }

        fun startGame(): WerewolfPhase {
            // TODO: Add no-less-or-more-than checks + error msgs.
            // NOTE: Also a "ready" mechanism could be interesting.
            return WerewolfPhase(players.randomizeRoles())
        }

        override fun toString(): String {
            return "[LOBBY]\n" + players
                .sortedBy { player -> player.name }
                .joinToString(separator = "\n") { player -> player.toString() }
        }
    }

    data class WerewolfPhase(val players: List<Player>, val votes: Map<Player, Player> = emptyMap()) {
        // Werewolf players vote, must be unanimous.
/*
        fun vote(wolf: Player, target: Player) {
            return DoctorPhase()
        } : Game
*/

        // Force-finish the phase.
        fun timeout() {
        }
    }

    data class DoctorPhase(val players: List<Player>)
}

sealed class Round {
    // TODO: Intro "asymmetric play". I.e. night phases could occur simultaneously?
    object Night // Werewolve(s) vote for kill, Doctor picks a patient, Seer asks the spirits about someone

    object Day // Villagers (optionally) pick someone to hang. Time limit. 50% + 1
}

sealed class Night