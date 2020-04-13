package com.kanawish.upvote.model

data class UpvoteModel(val hearts:Int, val thumbs:Int) {
    override fun toString(): String {
        return "UpvoteModel(❤️ =$hearts, 👍=$thumbs)"
    }
}
