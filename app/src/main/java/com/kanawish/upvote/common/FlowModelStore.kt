package com.kanawish.upvote.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Call cancel() on this to kill off processing, etc.
 */
@FlowPreview @ExperimentalCoroutinesApi
open class FlowModelStore<S>(startingState: S) : ModelStore<S> {
    private val scope = MainScope()
    private val intents = Channel<Intent<S>>()
    private val store = ConflatedBroadcastChannel(startingState)

    init {
        // Reduce from MainScope()
        scope.launch { while (isActive) store.offer(intents.receive().reduce(store.value)) }
    }

    // Could be called from any coroutine scope/context.
    override suspend fun process(intent: Intent<S>) {
        intents.send(intent)
    }

    override fun modelState(): Flow<S> {
        return store.asFlow()
    }

    fun close() {
        intents.close()
        store.close()
        scope.cancel()
    }
}
