package com.kanawish.upvote.common

import io.reactivex.Observable
import io.reactivex.disposables.Disposable

/**
 * When a view subscribes to a model, use this interface to group up all
 * the consumers in one convenient call.
 */
interface ModelSubscriber<S> {
    fun Observable<S>.subscribeToModel(): Disposable
}