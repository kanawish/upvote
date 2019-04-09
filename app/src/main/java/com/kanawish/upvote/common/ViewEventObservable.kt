package com.kanawish.upvote.common

import io.reactivex.Observable

/**
 * This allows us to group all the viewEvents from
 * one view in a single Observable.
 */
interface ViewEventObservable<E> {
    fun viewEvents(): Observable<E>
}