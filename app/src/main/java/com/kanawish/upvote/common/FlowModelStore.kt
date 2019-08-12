package com.kanawish.upvote.common

import hu.akarnokd.kotlin.flow.BehaviorSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking

/*
 * Stuff to explore/fold in:
 *
 * - https://github.com/ZacSweers/CatchUp/tree/master/libraries/flowbinding
 * - https://github.com/akarnokd/kotlin-flow-extensions
 *
 * Channels / Actors / moar Android specifics
 * - https://github.com/Kotlin/kotlinx.coroutines/blob/master/ui/coroutines-guide-ui.md
 * - https://geoffreymetais.github.io/code/coroutines/#scope
 * - https://developer.android.com/kotlin/coroutines
 * - https://github.com/Kotlin/kotlinx.coroutines/tree/master/ui/kotlinx-coroutines-android
 *
 * - https://github.com/friendlyrobotnyc/Core
 */

@FlowPreview
open class FlowModelStore<S>(
    startingState: S,
    parentScope: CoroutineScope
) : CoroutineScope by parentScope {

    private val intents = Channel<Intent<S>>().also { channel ->
        launch {
            channel.consumeAsFlow()
                .scan(startingState) { oldState, intent ->
                    intent.reduce(oldState)
                }
                .onCompletion {
                    log("store.onCompletion()")
                }
                .collect { state ->
                    log("store:{$state}")
                    store.emit(state)
                }
        }.invokeOnCompletion { throwable ->
            log("intents.job.invokeOnCompletion()")
            log("throwable: $throwable")
        }
    }

    // Multicaster
    val store = BehaviorSubject<S>()

    /**
     * Model will receive intents to be processed via this function.
     *
     * ModelState is immutable. Processed intents will work much like `copy()`
     * and create a new (modified) modelState from an old one.
     */
    suspend fun process(intent: Intent<S>) {
        log("store.process(intent)")
        intents.send(intent)
    }

    /**
     * stream of changes to ModelState
     *
     * Every time a modelState is replaced by a new one, this observable will
     * fire.
     *
     * This is what views will subscribe to.
     */
    fun modelState(): Flow<S> = store

    suspend fun close(): Boolean {
        log("store.close()")
        return intents.close().also { store.complete() }
    }
}

fun log(msg: String) = println("[${Thread.currentThread().name}] $msg")

@FlowPreview
val flow: Flow<Int> = flow {
    var i = 1
    while (i < 6) {
        log("flow.delay(500)")
        delay(500)
        log("flow.emit($i)")
        emit(i++)
    }
}

@ExperimentalCoroutinesApi
fun CoroutineScope.producer() = produce {
    var i = 0
    while (i < 6) send(i++)
}

@FlowPreview @ExperimentalCoroutinesApi
fun main() = runBlocking(newSingleThreadContext("fake-main")) {
    val storeCounter = FlowModelStore(0, this)
    launch {
        log("launching flow")
        // Outputs into the storeCounter
        flow
            .onCompletion {
                log("flow.onCompletion()")
                storeCounter.close()
            }.collect { counter ->
                log("storeCounter.process(intent{this+$counter})")
                storeCounter.process(intent {
                    (this + counter).also { log("intent ($this)->$it") }
                })
            }
        log("completed flow")
    }

    log("delay")
    delay(500)
    val j1 = launchCollector("j1", storeCounter)

    log("delay")
    delay(600)
    log("cancelling j1")
    j1.cancel()

    log("delay")
    delay(1000)
    launchCollector("j2", storeCounter)

    log("delay")
    delay(1000)
    launchCollector("j3", storeCounter)

    log("delay")
    delay(100)

    log("joinAll()")
    joinAll()

    log("end of main()")
}

private fun CoroutineScope.launchCollector(name: String, storeCounter: FlowModelStore<Int>): Job {
    log("Launching $name")
    return launch {
        log("$name pre-collect")
        storeCounter.modelState().collect {
            log("$name ensureActive()")
            ensureActive()
            log("$name[$it]")
        }
        log("$name post-collect")
    }.also {
        log("Launched $name")
    }
}