package com.kanawish.upvote

import com.kanawish.upvote.Ansi.RED
import com.kanawish.upvote.Ansi.RESET
import com.kanawish.upvote.Ansi.YELLOW

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

fun main() {
    println("${YELLOW}Y${RED}o$RESET.")
    for(x in Ansi.values()) {
        val code = x.code.removePrefix("\u001B")
        println("$x"+x.name.padEnd(16)+" \\u001B$code")
    }
}