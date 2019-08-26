package com.kanawish.upvote.common

import android.view.View
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate

/*
 * Approach taken from https://github.com/ZacSweers/CatchUp/blob/master/libraries/flowbinding/
 *
 * Effectively, it's fairly straightforward to write your own flow bindings for any callback based APIs.
 */

@ExperimentalCoroutinesApi
fun <E> SendChannel<E>.safeOffer(value: E) = !isClosedForSend && try {
    offer(value)
} catch (t: Throwable) {
    // Ignore all
    false
}

@ExperimentalCoroutinesApi
fun View.clicks(): Flow<Unit> = callbackFlow {
    val listener = View.OnClickListener { safeOffer(Unit) }
    setOnClickListener(listener)
    awaitClose {
        setOnClickListener(null)
    }
}

// Safeguard vs downstream laggy...(?) TBD if it's really 'needed'.
// I've basically been shaving away these extra co-routine layers in other places...
// TODO: I asked in ASG, checked on answer later... and looks like it's a bit uncertain ...
// .conflate()
