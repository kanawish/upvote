package com.kanawish.upvote.common

import io.reactivex.Observable
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

@FlowPreview
open class FlowModelStore<S>(startingState: S) : ModelStore<S> {

    var foo: Flow<S>? = null

    override fun process(intent: Intent<S>) {
        TODO()
    }

    override fun modelState(): Observable<S> {
        TODO()
    }

}

fun log(msg: String) = println("[${Thread.currentThread().name}] $msg")

/**
 * Structure concurrency
 * Co-routine builders add an instance of CoroutineScope to
 * their code block, runBlocking{} does this here:
 */
fun main() = runBlocking {
    // launch{} a new coroutine in the scope of runBlocking:
    launch {
        delayTwo()
    }

    coroutineScope {
        launch {
            delayThree()
        }

        delayOne()
    }

    println("Coroutine scope is over")
}

private suspend fun delayOne() {
    delay(100)
    println("1 Task from coroutineScope{}")
}

private suspend fun delayThree() {
    delay(300)
    println("3 Nested Task in coroutineScope{}")
}

private suspend fun delayTwo() {
    delay(200)
    println("Task from runBlocking{}")
}