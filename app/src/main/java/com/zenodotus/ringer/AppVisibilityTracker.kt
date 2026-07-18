package com.zenodotus.ringer

object AppVisibilityTracker {

    @Volatile
    var isForeground: Boolean = false
}