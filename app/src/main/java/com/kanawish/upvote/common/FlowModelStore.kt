package com.kanawish.upvote.common

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Call cancel() on this to kill off processing, etc.
 */
@ExperimentalCoroutinesApi @FlowPreview
open class FlowModelStore<S>(startingState: S) : ModelStore<S> {
    // NOTE: Loop off main is fine, as long as applying reducers is a low-cost operation.
    private val scope = MainScope()
    private val intents = Channel<Intent<S>>()
    private val store = ConflatedBroadcastChannel(startingState)

    init {
        // Reduce from MainScope()
        scope.launch {
            while (isActive)
                store.offer(
                        intents
                            .receive() // suspends only if `intents` is empty.
                            .reduce(store.value)
                )
        }
    }

    // Could be called from any coroutine scope/context.
    override fun process(intent: Intent<S>) {
        infoWorkingIn("↦ FlowModelStore.process( ${intent.hashCode()} )")
        intents.offer(intent) // non-blocking call.
    }

    override fun modelState(): Flow<S> {
        return store.asFlow().distinctUntilChanged()
    }

    fun close() {
        intents.close()
        store.close()
        scope.cancel()
    }
}
