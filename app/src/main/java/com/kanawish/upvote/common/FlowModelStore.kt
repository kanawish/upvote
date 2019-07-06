package com.kanawish.upvote.common

import io.reactivex.Observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield

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
    val job = launch {
        withTimeout(1300L) {
            repeat(1000) { i ->
                println("I'm sleeping $i ...")
                delay(500L)
            }
        }
    }
    println("job was launched.")
}