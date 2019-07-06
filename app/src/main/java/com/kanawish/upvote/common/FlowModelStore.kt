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
    val job = launch(Dispatchers.Default) {
        summerTime()
    }
    delay(1300L) // delay a bit
    println("main: I'm tired of waiting!")
    job.cancel() // cancels the job
    job.join() // waits for job's completion
    println("main: Now I can quit.")
}

private suspend fun summerTime() {
    var nextPrintTime = System.currentTimeMillis()
    var i = 0
    try {
        while (true) { // cancellable computation loop
            // print a message twice a second
            if (System.currentTimeMillis() >= nextPrintTime) {
                yield()
                println("job: I'm sleeping ${i++} ...")
                nextPrintTime += 500L
            }
        }
    } finally {
        withContext(NonCancellable) {
            println("job: finally {}")
            println("job: gimme a sec ...")
            delay(500)
            println("job: Okay I swept the floors. We're good to go.")
        }
    }
}

