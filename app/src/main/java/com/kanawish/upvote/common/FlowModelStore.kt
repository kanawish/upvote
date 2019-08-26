package com.kanawish.upvote.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.MainScope
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
open class FlowModelStore<S>(startingState: S) : ModelStore<S>, CoroutineScope by MainScope() {
    private val intents = Channel<Intent<S>>()
    private val store = ConflatedBroadcastChannel(startingState)

    init {
        // Reduce from MainScope()
        launch { while (isActive) store.offer(intents.receive().reduce(store.value)) }
    }

    // Process from any coroutine.
    // TODO: Consider re-introducing the reducer-launching, since the store should control it...
    override suspend fun process(intent: Intent<S>) {
        intents.send(intent)
    }

    override fun modelState(): Flow<S> {
        return store.asFlow()
    }
}
