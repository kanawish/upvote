package com.kanawish.upvote.common

import android.view.View
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate

@ExperimentalCoroutinesApi
fun View.clicks(): Flow<Unit> = callbackFlow {
    val listener = View.OnClickListener { offer(Unit) }
    setOnClickListener(listener)
    awaitClose {
        setOnClickListener(null)
    }
}
