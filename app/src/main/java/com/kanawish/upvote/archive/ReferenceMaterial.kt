package com.kanawish.upvote.archive

import android.view.View
import com.kanawish.upvote.common.clicks
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/*
 * Approach originally taken from https://github.com/ZacSweers/CatchUp/blob/master/libraries/flowbinding/
 * used this as well.
 *
 * As far as I can tell at this point, the channel closure should only take place
 * at a point where we can't receive more offers. So I'm taking it out of the samples for now.
 */
@ExperimentalCoroutinesApi
fun <E> SendChannel<E>.safeOffer(value: E) = !isClosedForSend && try {
    offer(value)
} catch (t: Throwable) {
    // Ignore all
    false
}

@ExperimentalCoroutinesApi suspend fun foo(buttonView:View ) {
    fun main() = runBlocking<Unit> {
        // Convert an integer range to a flow
        (1..3).asFlow().collect { value -> println(value) }
    }

    val buttonClicks = buttonView.clicks()
    MainScope().launch {
        buttonClicks.collect {
            // DoSomething
        }
    }
}

