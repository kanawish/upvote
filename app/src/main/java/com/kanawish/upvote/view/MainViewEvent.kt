package com.kanawish.upvote.view

sealed class MainViewEvent {
    object ThumbsUpClick : MainViewEvent()
    object LoveItClick : MainViewEvent()
}