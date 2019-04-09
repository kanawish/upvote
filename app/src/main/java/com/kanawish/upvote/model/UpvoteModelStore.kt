package com.kanawish.upvote.model

import com.kanawish.upvote.common.RxModelStore

/**
 * @startuml
 * hide empty members
 * interface ModelStore
 * abstract class RxModelStore
 * object UpvoteModelStore
 *
 * ModelStore <|-- RxModelStore
 * RxModelStore -- UpvoteModelStore
 * @enduml
 */
object UpvoteModelStore :
    RxModelStore<UpvoteModel>(UpvoteModel(0, 0))