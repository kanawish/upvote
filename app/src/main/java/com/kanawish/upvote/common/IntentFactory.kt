package com.kanawish.upvote.common

interface IntentFactory<E> {
    suspend fun process(viewEvent:E)
}