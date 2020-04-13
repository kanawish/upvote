package com.kanawish.upvote.common

interface IntentFactory<E> {
    fun process(viewEvent:E)
}