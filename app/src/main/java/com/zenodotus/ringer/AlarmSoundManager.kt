package com.zenodotus.ringer

import android.media.MediaPlayer

object AlarmSoundManager {

    private val players = mutableMapOf<Int, MediaPlayer>()

    fun play(taskId: Int, mediaPlayer: MediaPlayer) {
        // Als dit alarm al bestaat, eerst opruimen
        players[taskId]?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }

        players[taskId] = mediaPlayer
    }

    fun stop(taskId: Int) {
        players.remove(taskId)?.let {
            if (it.isPlaying) it.stop()
            it.release()
        }
    }

    fun stopAll() {
        players.values.forEach {
            if (it.isPlaying) it.stop()
            it.release()
        }
        players.clear()
    }
}