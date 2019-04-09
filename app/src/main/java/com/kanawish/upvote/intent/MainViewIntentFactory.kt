package com.kanawish.upvote.intent

import com.kanawish.upvote.common.Intent
import com.kanawish.upvote.common.IntentFactory
import com.kanawish.upvote.common.intent
import com.kanawish.upvote.model.UpvoteModel
import com.kanawish.upvote.model.UpvoteModelStore
import com.kanawish.upvote.view.MainViewEvent
import com.kanawish.upvote.view.MainViewEvent.LoveItClick
import com.kanawish.upvote.view.MainViewEvent.ThumbsUpClick

object MainViewIntentFactory : IntentFactory<MainViewEvent> {

    override fun process(viewEvent: MainViewEvent) {
        UpvoteModelStore.process(toIntent(viewEvent))
    }

    private fun toIntent(viewEvent: MainViewEvent): Intent<UpvoteModel> {
        return when (viewEvent) {
            LoveItClick -> AddHeart()
            ThumbsUpClick -> AddThumbsUp()
        }
    }

    class AddHeart :Intent<UpvoteModel> {
        override fun reduce(oldState: UpvoteModel) =
            oldState.copy(hearts = oldState.hearts + 1)
    }

    class AddThumbsUp :Intent<UpvoteModel> {
        override fun reduce(oldState: UpvoteModel) =
            oldState.copy(thumbs = oldState.thumbs + 1)
    }

}

/**
 * An example of using a simple DSL.
 *
 * Adding one class per intent can become a bit tedious when
 * your app becomes more complex, DSLs are useful for cutting
 * down boilerplate.
 */
private fun toIntentWithDsl(viewEvent: MainViewEvent): Intent<UpvoteModel> {
    return when (viewEvent) {
        LoveItClick -> intent {
            copy(hearts = hearts + 1)
        }
        ThumbsUpClick -> intent {
            copy(thumbs = thumbs + 1)
        }
    }
}

