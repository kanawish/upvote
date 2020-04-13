package com.kanawish.upvote.common

import kotlinx.coroutines.flow.Flow

interface ModelStore<S> {
    /**
     * Model will receive intents to be processed via this function.
     *
     * ModelState is immutable. Processed intents will work much like `copy()`
     * and create a new (modified) modelState from an old one.
     */
    fun process(intent: Intent<S>)

    /**
     * Stream of changes to ModelState
     *
     * Every time a modelState is replaced by a new one, this Flow will
     * emit.
     *
     * This is what views subscribe to.
     */
    fun modelState(): Flow<S>
}