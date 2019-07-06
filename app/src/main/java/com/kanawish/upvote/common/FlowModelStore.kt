package com.kanawish.upvote.common

import io.reactivex.Observable
import kotlinx.coroutines.FlowPreview
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
        delay(1000)
        print("peeps.")
    }
    print("Sup', ")
}