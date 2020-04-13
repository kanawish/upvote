package com.kanawish.upvote

import com.kanawish.upvote.common.Ansi
import com.kanawish.upvote.common.Ansi.RED
import com.kanawish.upvote.common.Ansi.RESET
import com.kanawish.upvote.common.Ansi.YELLOW

fun main() {
    println("${YELLOW}Y${RED}o$RESET.")
    for(x in Ansi.values()) {
        val code = x.code.removePrefix("\u001B")
        println("$x"+x.name.padEnd(16)+" \\u001B$code")
    }
}