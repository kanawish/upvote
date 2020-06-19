package com.kanawish.upvote

import android.view.View
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@ExperimentalCoroutinesApi
fun View.clicks(): Flow<Unit> = callbackFlow {
    val listener = View.OnClickListener { offer(Unit) }
    setOnClickListener(listener)
    awaitClose {
        setOnClickListener(null)
    }
    this@clicks.apply {

    }
}

fun main() {
    data class Counter(val count:Int)
    
    val sum: Int = (1..5).reduce { acc, i -> acc + i }
    println(sum) //
}
