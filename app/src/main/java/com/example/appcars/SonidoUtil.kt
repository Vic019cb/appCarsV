package com.example.appcars

import android.content.Context
import android.media.MediaPlayer

object SonidoUtil {
    private var reproductor: MediaPlayer? = null

    fun reproducirCoche(context: Context) {
        reproducir(context, R.raw.sonido_coche)
    }

    fun reproducirMoto(context: Context) {
        reproducir(context, R.raw.sonido_moto)
    }

    private fun reproducir(context: Context, resId: Int) {
        try {
            reproductor?.stop()
            reproductor?.release()
            reproductor = MediaPlayer.create(context.applicationContext, resId)
            reproductor?.setOnCompletionListener { mp ->
                mp.release()
                if (reproductor == mp) {
                    reproductor = null
                }
            }
            reproductor?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun liberar() {
        try {
            reproductor?.stop()
            reproductor?.release()
            reproductor = null
        } catch (_: Exception) {}
    }
}
