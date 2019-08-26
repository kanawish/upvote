package com.kanawish.upvote.view

import kotlinx.coroutines.CoroutineScope

sealed class MainViewEvent {
    object ThumbsUpClick : MainViewEvent()
    object LoveItClick : MainViewEvent()

    /**
     * NOTE: Lots of possible alternatives re: scoping here.
     *  This one is interesting, since it cancels the side-effect automagically
     *  when the parent scope is cancelled.
     *  It's likely something we'd never use "for real", assume real intent factories
     *  will get their own lifecycle+scoping.
     */
    data class CloudClick(val scope: CoroutineScope) : MainViewEvent()
}