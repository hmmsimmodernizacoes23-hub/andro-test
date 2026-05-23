package com.example

import android.app.Application

class RhythmApplication : Application() {
    override fun getAttributionTag(): String? {
        return "default"
    }
}
