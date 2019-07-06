package com.kanawish.upvote

import android.app.Application
import timber.log.Timber

/**
 * @startuml
 * hide empty members
 * class Application
 * class Activity
 * class Fragment
 * @enduml
 */
class MainApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Logger init
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        Timber.i("%s %d", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
    }
}