package com.aivideoeraser.android

import android.app.Application

class AiVideoEraserApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: AiVideoEraserApp
            private set
    }
}
