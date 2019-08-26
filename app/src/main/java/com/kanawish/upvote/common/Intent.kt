package com.kanawish.upvote.common

/**
 * NOTE: Intent are supposed to be quick and non blocking.
 *   It's fine if they launch async co-routines of their own,
 *   but they're responsible for scoping/cancellation and so forth.
 */
interface Intent<T> {
    fun reduce(oldState: T): T
}

/**
 * DSL function to help build intents from code blocks.
 *
 * NOTE: Magic of extension functions, (T)->T and T.()->T interchangeable.
 */
/*
fun <T> intent(block: T.() -> T) : Intent<T> = object : Intent<T> {
    override fun reduce(oldState: T): T = block(oldState)
}
*/

fun <T> intent(block: T.() -> T) = BlockIntent(block)

class BlockIntent<T>(val block:T.()->T) : Intent<T> {
    override fun reduce(oldState: T): T = block(oldState)
}

/**
 * By delegating work to other models, repositories or services, we
 * end up with situations where we don't need to update our ModelStore
 * state until the delegated work completes.
 *
 * Use the `sideEffect {}` DSL function for those situations.
 */
fun <T> sideEffect(block: T.() -> Unit) : Intent<T> = object :
    Intent<T> {
    override fun reduce(oldState: T): T = oldState.apply(block)
}
