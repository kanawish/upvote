package com.kanawish.upvote.view

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.kanawish.upvote.R
import com.kanawish.upvote.common.ViewEventFlow
import com.kanawish.upvote.common.clicks
import com.kanawish.upvote.common.infoWorkingIn
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flattenConcat
import kotlinx.coroutines.flow.flattenMerge
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import timber.log.Timber

@ExperimentalCoroutinesApi @FlowPreview
class MainActivity :
    AppCompatActivity(),
    ViewEventFlow<MainViewEvent> {

    private val scope: CoroutineScope = MainScope()

    private lateinit var counterTextView:TextView

    private lateinit var heartButton:Button
    private lateinit var thumbButton:Button
    private lateinit var cloudButton:Button

    fun <S> Flow<S>.lifecycleLog(name: String): Flow<S> = this
        .onStart { infoWorkingIn("$name.onStart {}") }
        .onEach {
            infoWorkingIn("👁  $name.onEach { $it }")
            Timber.i("∎")
        }
        .onCompletion { infoWorkingIn("$name.onCompletion {}") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        counterTextView = findViewById(R.id.counterTextView)
        heartButton = findViewById(R.id.heartButton)
        thumbButton = findViewById(R.id.thumbButton)
        cloudButton = findViewById(R.id.cloudButton)

        // Output
        viewEvents()
            .onEach { event -> MainViewIntentFactory.process(event) }
            .launchIn(scope)

        // TODO: Improve with a more complex example, with filtering and/or grouping.
        // Input(s)
        UpvoteModelStore
            .modelState()
            .lifecycleLog("modelState()")
            .forCounterTextView()
            .launchIn(scope)
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel() // CoroutineScope.cancel()
    }

    /**
     * https://github.com/Kotlin/kotlinx.coroutines/blob/1.3.0/docs/flow.md#composing-multiple-flows
     * Based off 1.3.0 release guide and such...
     */
    override fun viewEvents(): Flow<MainViewEvent> {
        val flows = listOf(
                heartButton.clicks().map { MainViewEvent.LoveItClick },
                thumbButton.clicks().map { MainViewEvent.ThumbsUpClick },
                cloudButton.clicks().map { MainViewEvent.CloudClick(scope) }
        )

        return flows.asFlow().flattenMerge(flows.size)
    }

    /**
     * NOTE: Decided to get rid of interface contract.
     *   Given you could have multiple consumers for various model Flow(s),
     *   and how that interface was just a 'convention' thing, didn't make
     *   sense to keep it.
     */
    private fun Flow<UpvoteModel>.forCounterTextView() =
        onEach { model ->
            counterTextView.text =
                resources.getString(
                        R.string.upvotes,
                        model.hearts,
                        model.thumbs
                )
        }

    /**
     * Sometimes a counter example can be useful.
     *
     * This is an equivalent non-reactive approach.
     */
    fun registerListeners() {
        heartButton.setOnClickListener {
            infoWorkingIn("≅ 'manual' clickListener ❤️")
            MainViewIntentFactory.process(MainViewEvent.LoveItClick)
        }
        thumbButton.setOnClickListener {
            infoWorkingIn("≅ 'manual' clickListener 👍️")
            MainViewIntentFactory.process(MainViewEvent.ThumbsUpClick)
        }
        cloudButton.setOnClickListener {
            infoWorkingIn("≅ 'manual' clickListener 💌☁️️")
            MainViewIntentFactory.process(MainViewEvent.CloudClick(scope))
        }
    }

}
