package com.kanawish.upvote.model

import com.kanawish.upvote.common.FlowModelStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

/**
 * @startuml
 * hide empty members
 * interface ModelStore
 * abstract class FlowModelStore
 * object UpvoteModelStore
 *
 * ModelStore <|-- FlowModelStore
 * FlowModelStore -- UpvoteModelStore
 * @enduml
 */
@ExperimentalCoroutinesApi @FlowPreview
object UpvoteModelStore :
    FlowModelStore<UpvoteModel>(UpvoteModel(0, 0))