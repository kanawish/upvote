package com.kanawish.upvote.common

import io.reactivex.Observable
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull

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

fun main() = runBlocking {
    val result = withTimeoutOrNull(1300L) {
        repeat(1000) { i ->
            println("I'm sleeping $i ...")
            delay(500L)
        }
        "Heyo."
    }
    println("job was launched, result = $result")
}