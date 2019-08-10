package com.kanawish.upvote.common

import hu.akarnokd.kotlin.flow.BehaviorSubject
import hu.akarnokd.kotlin.flow.PublishSubject
import hu.akarnokd.kotlin.flow.replay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
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
import kotlinx.coroutines.test.setMain

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
 */

@FlowPreview @ExperimentalCoroutinesApi
open class FlowModelStore<S>(startingState: S) : CoroutineScope by MainScope()  {

    val i2 = Channel<Intent<S>>()
        .also { channel ->
            launch {
                channel.consumeAsFlow()
                    .scan(startingState) { oldState, intent ->
                        intent.reduce(oldState)
                    }
                    .collect {
                        println("store[$it]")
                        s2.emit(it)
                    }
            }
        }

    // Multicaster
    val s2 = BehaviorSubject<S>()

    suspend fun foo() {
        i2.send( intent { this } )
    }

    val intents = PublishSubject<Intent<S>>()

    val store: Flow<S> = intents
        .scan(startingState) { oldState, intent ->
            intent.reduce(oldState)
        }
        .replay(1) { it }
        .apply { launch { collect { println("store[$it]") } } }

    /**
     * Model will receive intents to be processed via this function.
     *
     * ModelState is immutable. Processed intents will work much like `copy()`
     * and create a new (modified) modelState from an old one.
     */
    fun process(intent: Intent<S>)
    {
        launch {
            intents.emit(intent)
        }
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

    fun destroy() {
        launch {
            intents.complete()
        }
        cancel()
    }
}

fun log(msg: String) = println("[${Thread.currentThread().name}] $msg")

@FlowPreview
val flow: Flow<Int> = flow {
    var i = 0
    while (i < 6) {
        delay(500)
        println("Emit $i")
        emit(i++)
    }
}

@FlowPreview @ExperimentalCoroutinesApi
fun main() = runBlocking<Unit> {
    val mainThreadSurrogate = newSingleThreadContext("UI thread")
    Dispatchers.setMain(mainThreadSurrogate)

    val storeCounter = FlowModelStore(0)

//    launch { flow.collect { println("A: got $it") } }
//    launch { flow.collect { println("B: got $it") } }

    launch {
        // Outputs into the storeCounter
        flow
            .onCompletion {
                println("completion -> intents.complete()")
                storeCounter.intents.complete()
            }.collect { counter ->
                println("intent{this+$counter}")
                storeCounter.process(intent { this+counter })
            }

    }

    delay(2000)
    println("Launch j1")
    launch {
        storeCounter.modelState().collect {
            println("j1[$it]")
        }
    }
    delay(1000)
    println("Launch j2")
    launch {
        storeCounter.modelState().collect {
            println("j2[$it]")
        }
    }

    delay(100)

    println("joinAll()")
    joinAll()

    println("end of main()")
    mainThreadSurrogate.close()
}