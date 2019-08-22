package com.kanawish.upvote

import com.kanawish.upvote.common.Intent
import com.kanawish.upvote.common.intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking

// `-Dkotlinx.coroutines.debug` for coroutine info in thread-name
fun log(msg: String) = println("[${Thread.currentThread().name}] $msg")

@FlowPreview
val sequenceFlow: Flow<Intent<Int>> = flow {
    for (counter in 1..10) {
        log("sequenceFlow.delay(500)")
        delay(500)
        log("sequenceFlow.emit( intent{this+$counter} )")
        val intent:Intent<Int> = intent {
            (this + counter).also { log("intent ($this)->$it") }
        }
        emit(intent)
    }
}

@FlowPreview @ExperimentalCoroutinesApi
fun main() = runBlocking(newSingleThreadContext("fake-main")) {
    val storeCounter = SandboxModelStore(0 )
    launch {
        log("sequenceFlow.collect {}")
        // Outputs into the storeCounter
        sequenceFlow.collect { intent ->
            log("storeCounter.process(intent)")
            storeCounter.process(intent)
        }
        log("sequenceFlow.collect {} completed")
    }

    log("main.delay(500)")
    delay(500)
    val j1 = launchCollector("j1", storeCounter)

    log("main.delay(1000)")
    delay(1000)
    val j2 = launchCollector("j2", storeCounter)

    log("main.delay(500)")
    delay(500)

    // Cancel j1 collector job.
    log("j1.cancel()")
    j1.cancel()

    log("main.delay(1500)")
    delay(1500)
    val j3 = launchCollector("j3", storeCounter)

    // Cancel remaining collector jobs.
    log("j2.cancel()")
    j2.cancel()

    log("main.delay(100)")
    delay(100)

//    log("j3.cancel()")
//    j3.cancel()
//    delay(1000)

    // Suspends current coroutine until all given jobs are complete.
    log("joinAll()")
    joinAll()

    log("end of main() -> this.cancel()")
    cancel("cancel fake-main coroutine scope")
}

@FlowPreview @ExperimentalCoroutinesApi
private fun CoroutineScope.launchCollector(name: String, storeCounter: SandboxModelStore<Int>): Job {
    log("$name Launching")
    return launch {
        log("$name storeCounter.modelState()")
        storeCounter.modelState()
            .onEach { log("$name[$it]") }
            .onCompletion { log("$name storeCounter.modelState().onCompletion {}") }
            .catch { e -> log(e.message ?: "Empty exception.") }
            .collect()

        // Never reached if job is cancelled.
        log("$name storeCounter.modelState().collect() completed")
    }.also {
        log("$name Launched")
    }
}

@FlowPreview @ExperimentalCoroutinesApi
open class SandboxModelStore<S>(startingState: S) {
    private val store = ConflatedBroadcastChannel(startingState)

    fun process(intent: Intent<S>) { store.offer(intent.reduce(store.value)) }
    fun modelState(): Flow<S> { return store.asFlow() }
    fun cancel() { store.cancel() }

    fun logState() {
        log("$store.")
    }
}
