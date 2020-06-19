package com.kanawish.werewolf

import com.kanawish.upvote.common.Ansi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking

val range: IntRange = (1..5)

fun imperativeDeclarativeExample() {
    // Imperative
    val imperativeResult = mutableListOf<Int>()
    for (current in range) {
        if (current % 2 == 0) {
            imperativeResult.add(current)
        }
    }
    println("\n${Ansi.BRIGHT_GREEN}IMPERATIVE: $imperativeResult")

    // Declarative
    val declarativeResult = range.filter { it % 2 == 0 }

    println("\n${Ansi.BRIGHT_CYAN}DECLARATIVE: $declarativeResult")
    println("${Ansi.RESET}")
}

// Re: asynchrony
suspend fun calculatedValues( ): IntRange {
    println("${Ansi.BRIGHT_RED}SUSPEND")
    print("⏲ ")
    delay(1500) // pretend some asynchronous work is going on.
    return (1..5)
}

val delayedRange = (1..5).asFlow().onEach {
    print("⏲")
    delay(500)
}

@FlowPreview @ExperimentalCoroutinesApi
fun main() = runBlocking() {

    // Imparative and declarative compared.
    println( "${Ansi.YELLOW}Imperative and declarative compared${Ansi.RESET}" )
    imperativeDeclarativeExample()

    // Suspend and flows

    // Getting the final result of an async computation.
    println(calculatedValues())

    // Getting stream of async computations.
    println("\n${Ansi.BRIGHT_YELLOW}FLOW")
    delayedRange.collect { println(" $it") }
}
