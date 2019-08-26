package com.kanawish.upvote.common

import hu.akarnokd.kotlin.flow.BehaviorSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking

interface ReceiverChannelModelStore<S> {
    fun modelState(): ReceiveChannel<S>
}

@FlowPreview @ExperimentalCoroutinesApi
open class CakeModelStore<S>(startingState: S):ReceiverChannelModelStore<S> {

    private val store = ConflatedBroadcastChannel(startingState)

    // NOTE: Looks good, but doesn't safeguard vs access outside main thread.
    fun process(intent: Intent<S>) {
        store.offer( intent.reduce(store.value) )
    }

    /**
     * TODO: Check exactly the behaviour, in context of:
     *  https://github.com/Kotlin/kotlinx.coroutines/blob/master/ui/coroutines-guide-ui.md
     *  and https://developer.android.com/kotlin/coroutines
     */
    override fun modelState(): ReceiveChannel<S> {
        return store.openSubscription()
    }

    /**
     * By calling cancel, all receiving channels will be closed, effectively
     * disposing of all subscriptions on this store.
     */
    fun cancel() {
        store.cancel()
    }
}

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
@FlowPreview @ExperimentalCoroutinesApi
open class ChannelModelStore<S>(
    startingState: S,
    parentScope: CoroutineScope
) : CoroutineScope by parentScope, ReceiverChannelModelStore<S>  {

    /**
     * https://github.com/Kotlin/kotlinx.coroutines/issues/1340
     * https://github.com/Kotlin/kotlinx.coroutines/issues/1261
     */
    private val intents = Channel<Intent<S>>()
    private val store = ConflatedBroadcastChannel<S>()

    val reducerJob = launch {
        intents
            .consumeAsFlow()
            .onCompletion { log("reducerJob completed") }
            .scan(startingState) { oldState, intent -> intent.reduce(oldState) }
            .collect { state ->
                log("store.send($state)")
                store.send(state)
            }

    }

    suspend fun process(intent: Intent<S>) {
        intents.send(intent)
    }

    // TODO: Continue here
    override fun modelState(): ReceiveChannel<S> {
        return store.openSubscription()
    }

    // TODO: Ask a few questions on ASG re: need to cancel, etc.
    fun cancel() {
        intents.cancel()
        store.cancel()
    }
}


/**
 * Using https://github.com/akarnokd/kotlin-flow-extensions
 */
@FlowPreview @ExperimentalCoroutinesApi
open class FlowyModelStore<S>(
    startingState: S,
    parentScope: CoroutineScope
) : CoroutineScope by parentScope {

    private val intents = Channel<Intent<S>>()
    private val store = BehaviorSubject<S>()

    init {
        launch {
            intents.consumeAsFlow()
                .scan(startingState) { oldState, intent -> intent.reduce(oldState) }
                .collect { state -> store.emit(state) }
        }
    }

    suspend fun process(intent: Intent<S>) {
        intents.send(intent)
    }

    fun launchStoreCollect(action: suspend (value: S) -> Unit): Job {
        return launch {
            store.collect {
                ensureActive()
                action(it)
            }
        }
    }

    suspend fun close(): Boolean {
        return intents.close().also { store.complete() }
    }
}

fun log(msg: String) = println("[${Thread.currentThread().name}] $msg")

@ExperimentalCoroutinesApi
fun CoroutineScope.producer(): ReceiveChannel<Int> = produce {
    for (i in 1..15) {
        log("producer.delay(500)")
        delay(500)
        log("producer.send($i)")
        send(i)
    }
}

@FlowPreview
val flow: Flow<Int> = flow {
    for (i in 1..15) {
        log("flow.delay(500)")
        delay(500)
        log("flow.emit($i)")
        emit(i)
    }
}

@FlowPreview @ExperimentalCoroutinesApi
fun main() = runBlocking(newSingleThreadContext("fake-main")) {

    val storeCounter = CakeModelStore(0 )

/*
    log("producer launching")
    val producerJob = launch { producer() }
        .apply { invokeOnCompletion { log("producer.completion") } }
*/

    launch {
        log("launching flow")
        // Outputs into the storeCounter
        flow.collect { counter ->
                log("storeCounter.process(intent{this+$counter})")
                storeCounter.process(intent {
                    (this + counter).also { log("intent ($this)->$it") }
                })
            }
        log("completed flow")
    }

    log("main.delay(500)")
    delay(500)
    val j1 = launchCollector("j1", storeCounter)

    log("main.delay(1000)")
    delay(1000)
    val j2 = launchCollector("j2", storeCounter)

    log("main.delay(500)")
    delay(500)
    log("j1.cancel()")
    j1.cancel()

    log("main.delay(1500)")
    delay(1500)
    val j3 = launchCollector("j3", storeCounter)

    log("main.delay(100)")
    delay(100)

    // Cancels all children of the [Job] in this context
//    log("coroutineContext.cancelChildren()")
//    coroutineContext.cancelChildren()

    // Cancel collector jobs, just to
//    log("j1.cancel()")
//    j1.cancel()
    log("j2.cancel()")
    j2.cancel()
    log("j3.cancel()")
    j3.cancel()
/*
    log("producerJob.dispose()")
    producerJob.cancel()
*/

    // Cancel scope, it's job and all it's children.
//    this.cancel("this.cancel()")

    // This cascades cancellation to all children collecting jobs.
//    log("storeCounter.cancel()")
//    storeCounter.cancel()

    // Suspends current coroutine until all given jobs are complete.
    log("joinAll()")
    joinAll()

    log("end of main()")
}

@FlowPreview @ExperimentalCoroutinesApi
private fun CoroutineScope.launchCollector2(name: String, storeCounter: ChannelModelStore<Int>){
    log("Launching $name")
    val receiveChannel = storeCounter.modelState()
    launch {
        receiveChannel
            .consumeAsFlow()
            .collect {
                log("$name[$it]")
            }
    }

}

@FlowPreview @ExperimentalCoroutinesApi
private fun CoroutineScope.launchCollector(name: String, storeCounter: ReceiverChannelModelStore<Int>): Job {
    log("Launching $name")

    return storeCounter.modelState().let { receiveChannel ->
        receiveChannel
            .consumeAsFlow()
            .onEach {
                log("$name[$it]")
                log( "$name onEach{} receiveChannel.isClosedForReceive = ${receiveChannel.isClosedForReceive}")
            }
            .onCompletion { log("$name consumeAs.onCompletion()") }
            .launchIn(this)
            .also { job ->
                log("Launched $name")
                job.invokeOnCompletion {
                    log("$name job.invokeOnCompletion()")
                    log( "$name invokeOnCompletion{} receiveChannel.isClosedForReceive = ${receiveChannel.isClosedForReceive}")
//                    log("$name job.receiveChannel.cancel()")
//                    receiveChannel.cancel()
                }
                log( "$name invokeOnCompletion{} receiveChannel.isClosedForReceive = ${receiveChannel.isClosedForReceive}")
            }
    }
}

@FlowPreview @ExperimentalCoroutinesApi
private fun CoroutineScope.launchCollector(name: String, storeCounter: FlowyModelStore<Int>): Job {
    log("Launching $name")
    return storeCounter.launchStoreCollect {
        log("$name[$it]")
    }.also {
        log("Launched $name")
    }
}