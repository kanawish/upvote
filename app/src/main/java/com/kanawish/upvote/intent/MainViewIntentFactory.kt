package com.kanawish.upvote.intent

import com.kanawish.upvote.common.Intent
import com.kanawish.upvote.common.IntentFactory
import com.kanawish.upvote.common.intent
import com.kanawish.upvote.common.sideEffect
import com.kanawish.upvote.intent.MainViewIntentFactory.cloudSideEffectIntent
import com.kanawish.upvote.model.UpvoteModel
import com.kanawish.upvote.model.UpvoteModelStore
import com.kanawish.upvote.view.MainViewEvent
import com.kanawish.upvote.view.MainViewEvent.CloudClick
import com.kanawish.upvote.view.MainViewEvent.LoveItClick
import com.kanawish.upvote.view.MainViewEvent.ThumbsUpClick
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

@FlowPreview @ExperimentalCoroutinesApi
object MainViewIntentFactory : IntentFactory<MainViewEvent> {

    override suspend fun process(viewEvent: MainViewEvent) {
        UpvoteModelStore.process(toIntent(viewEvent))
    }

    private fun toIntent(viewEvent: MainViewEvent): Intent<UpvoteModel> {
        return when (viewEvent) {
            LoveItClick -> AddHeart()
            ThumbsUpClick -> AddThumbsUp()
            is CloudClick -> viewEvent.scope.cloudSideEffectIntent()
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

    // https://medium.com/@elizarov/coroutine-context-and-scope-c8b255d59055
    // TODO: Finish exploring this.
    fun CoroutineScope.cloudSideEffectIntent(): Intent<UpvoteModel> = sideEffect {
        // CPU side-effecting.
        launch(Dispatchers.Default) {
            fakeWebsocketSource().collect { cpuScopeIntent ->
                UpvoteModelStore.process(cpuScopeIntent)
            }
        }
    }
}

private fun fakeWebsocketSource(): Flow<Intent<UpvoteModel>> {
    return flow {
        val heartIntent = intent<UpvoteModel> { copy(hearts = hearts + 1) }
        while (true) {
            emit(heartIntent)
            delay(500)
        }
    }
}

private fun fibonacci(): Flow<Int> {
    return flow {
        generateSequence(Pair(0, 1), { Pair(it.second, it.first + it.second) })
            .map { it.first }
            .iterator()
            .forEach {
                emit(it)
                delay(1000)
            }
    }
}

/**
 * An example of using a simple DSL.
 *
 * Adding one class per intent can become a bit tedious when
 * your app becomes more complex, DSLs are useful for cutting
 * down boilerplate.
 */
@FlowPreview @ExperimentalCoroutinesApi
private fun toIntentWithDsl(viewEvent: MainViewEvent): Intent<UpvoteModel> {
    return when (viewEvent) {
        LoveItClick -> intent {
            copy(hearts = hearts + 1)
        }
        ThumbsUpClick -> intent {
            copy(thumbs = thumbs + 1)
        }
        is CloudClick -> sideEffect {
            // CPU side-effecting.
            viewEvent.scope.launch(Dispatchers.Default) {
                fakeWebsocketSource().collect { cpuScopeIntent ->
                    UpvoteModelStore.process(cpuScopeIntent)
                }
            }
        }
    }
}

