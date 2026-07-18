package com.zenodotus.ringer

import android.media.MediaPlayer

object AlarmSoundManager {
    var mediaPlayer: MediaPlayer? = null

    fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}