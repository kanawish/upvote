package com.kanawish.werewolf

import kotlin.random.Random

/**
 * NOTE: Good slide material
 *
 * From a given random instance, generates a randomized "stack" of
 * roles to be given out to a list of players.
 */
fun List<Player>.randomizeRoles(random: Random = Random.Default): List<Player> {
    val playerCount = this.size
    val wolfCount = if (playerCount < 16) 2 else 3

    // Build a "shuffled deck" of Roles
    val roleDeck = List<Role>(wolfCount) { Role.Werewolf }
        .plus(Role.Seer)
        .plus(Role.Doctor)
        .run {
            val villagerRoles = List(playerCount - size) { Role.Villager }
            plus(villagerRoles)
        }
        .shuffled(random)

    // Assign a "role card" to each player.
    return this.zip(roleDeck) { player: Player, role: Role -> player.copy(role = role) }
}

// https://www.prdh-igd.com/fr/palmares/tous-les-prenoms-composes
val playerNames = listOf(
        "Angélique",
        "Bob",
        "Charles",
        "Dominique",
        "Éric",
        "François",
        "George",
        "Harold",
        "Isabelle",
        "Jacob",
        "Karl",
        "Louise",
        "Marguerite",
        "Nicolas",
        "Ophélie",
        "Paul"
)

fun Role?.emoji():String = when( this ) {
    Role.Seer -> "🔮"
    Role.Doctor -> "🥼"
    Role.Werewolf -> "🐺"
    Role.Villager -> "👤"
    else -> "❓"
}

/**
 * rabbit 🐇🕳
 * https://stackoverflow.com/questions/53194987/how-do-i-insert-an-emoji-in-a-java-string
 * https://apps.timwhitlock.info/emoji/tables/unicode
 * https://stackoverflow.com/questions/43618487/why-are-emoji-characters-like-treated-so-strangely-in-swift-strings
 */
fun Array<Int>.fromCodePoints(): String = fold(StringBuilder()) { sb, i -> sb.appendCodePoint(i) }.toString()

