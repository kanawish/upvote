package com.kanawish.upvote.view

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.kanawish.upvote.R
import com.kanawish.upvote.common.ViewEventFlow
import com.kanawish.upvote.common.clicks
import com.kanawish.upvote.intent.MainViewIntentFactory
import com.kanawish.upvote.model.UpvoteModel
import com.kanawish.upvote.model.UpvoteModelStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import timber.log.Timber

@FlowPreview @ExperimentalCoroutinesApi
class MainActivity :
    AppCompatActivity(),
    ViewEventFlow<MainViewEvent>,
    CoroutineScope by MainScope() {

    private lateinit var counterTextView:TextView

    private lateinit var heartButton:Button
    private lateinit var thumbButton:Button
    private lateinit var cloudButton:Button

    fun <S> Flow<S>.lifecycleLog(name: String): Flow<S> = this
        .onStart { Timber.i("$name.onStart {}") }
        .onEach { Timber.i("$name.onEach {$it}") }
        .onCompletion { Timber.i("$name.onCompletion {}") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        counterTextView = findViewById(R.id.counterTextView)
        heartButton = findViewById(R.id.heartButton)
        thumbButton = findViewById(R.id.thumbButton)
        cloudButton = findViewById(R.id.cloudButton)

        // Output
        viewEvents()
            .onCompletion {  }
            .onEach { event -> MainViewIntentFactory.process(event) }
            .lifecycleLog("viewEvents()")
            .launchIn(this)

        // Input(s)
        // TODO: Improve with a more complex example, with filtering and/or grouping.
        UpvoteModelStore
            .modelState()
            .lifecycleLog("modelState()")
            .forCounterTextView()
            .launchIn(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel() // CoroutineScope.cancel()
    }

    /**
     * https://github.com/Kotlin/kotlinx.coroutines/blob/1.3.0/docs/flow.md#composing-multiple-flows
     * Based off 1.3.0 release guide and such...
     */
    override fun viewEvents(): Flow<MainViewEvent> {
        val flows = listOf(
                heartButton.clicks().map { MainViewEvent.LoveItClick },
                thumbButton.clicks().map { MainViewEvent.ThumbsUpClick },
                cloudButton.clicks().map { MainViewEvent.CloudClick(this) }
        )

        return flows.asFlow().flattenMerge(flows.size)
    }

    /**
     * NOTE: Decided to get rid of interface contract.
     *   Given you could have multiple consumers for various model Flow(s),
     *   and how that interface was just a 'convention' thing, didn't make
     *   sense to keep it.
     */
    private fun Flow<UpvoteModel>.forCounterTextView() = onEach { model ->
        counterTextView.text =
            resources.getString(
                    R.string.upvotes,
                    model.hearts,
                    model.thumbs
            )
    }

}
