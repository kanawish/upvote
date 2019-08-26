package com.kanawish.upvote.common

import kotlinx.coroutines.flow.Flow

/**
 * TODO: TBD
 * This allows us to group all the viewEvents from
 * one view in a single source.
 */
interface ViewEventFlow<E> {
    fun viewEvents(): Flow<E>
}