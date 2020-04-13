package com.kanawish.upvote.common

import timber.log.Timber

val ESC = "\u001B"

enum class Ansi(val code: String) {
    BLACK("$ESC[30m"), RED("$ESC[31m"), GREEN("$ESC[32m"), YELLOW("$ESC[33m"),
    BLUE("$ESC[34m"), PURPLE("$ESC[35m"), CYAN("$ESC[36m"), WHITE("$ESC[37m"),
    BRIGHT_BLACK("$ESC[90m"), BRIGHT_RED("$ESC[91m"), BRIGHT_GREEN("$ESC[92m"), BRIGHT_YELLOW("$ESC[93m"),
    BRIGHT_BLUE("$ESC[94m"), BRIGHT_PURPLE("$ESC[95m"), BRIGHT_CYAN("$ESC[96m"), BRIGHT_WHITE("$ESC[97m"),
    RESET("$ESC[0m");

    override fun toString(): String {
        return code
    }
}

/**
 * Use in live demos to illustrate what thread coroutines are operating from.
 */
fun infoWorkingIn(msg:String) {
    val thread = "[🧵 ${Thread.currentThread().name}]"
    Timber.i("${msg.padEnd(54)} $thread")
}
