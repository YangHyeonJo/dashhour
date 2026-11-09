package com.dashhour.app

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

/**
 * 안드로이드 TTS. 기본 엔진이 없으면 Pleos 음성 엔진(ai.pleos.playground.caas)을 직접 지정해 다시 시도한다.
 * 한국어 음성이 없으면 ready=false 가 되고, 웹 화면은 자막만 보여준다.
 */
class TtsSpeaker(context: Context) {
    private val app = context.applicationContext
    private var tts: TextToSpeech? = null
    private var triedFallback = false

    @Volatile var ready = false
        private set

    init { open(null) }

    private fun open(engine: String?) {
        var created: TextToSpeech? = null
        val listener = TextToSpeech.OnInitListener { status -> onInit(created, engine, status) }
        created = if (engine == null) TextToSpeech(app, listener) else TextToSpeech(app, listener, engine)
        tts = created
    }

    private fun onInit(t: TextToSpeech?, engine: String?, status: Int) {
        if (t == null) return
        val engines = try { t.engines.joinToString { it.name } } catch (e: Throwable) { "?" }
        if (status != TextToSpeech.SUCCESS) {
            Log.w(TAG, "TTS init failed: $status (engine=${engine ?: "default"}, engines=[$engines])")
            if (!triedFallback) {
                triedFallback = true
                val alt = try { t.engines.firstOrNull()?.name } catch (e: Throwable) { null } ?: PLEOS_ENGINE
                try { t.shutdown() } catch (_: Throwable) {}
                Log.i(TAG, "retry with engine $alt")
                open(alt)
            }
            return
        }
        val r = try { t.setLanguage(Locale.KOREAN) } catch (e: Throwable) { TextToSpeech.LANG_NOT_SUPPORTED }
        ready = r != TextToSpeech.LANG_MISSING_DATA && r != TextToSpeech.LANG_NOT_SUPPORTED
        t.setSpeechRate(1.05f)
        Log.i(TAG, "TTS ready=$ready (engine=${engine ?: t.defaultEngine}, setLanguage=$r, engines=[$engines])")
    }

    fun speak(text: String) {
        if (ready) tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "dash-${System.nanoTime()}")
    }

    fun stop() { tts?.stop() }

    fun shutdown() { tts?.shutdown() }

    companion object {
        private const val TAG = "DashTts"
        private const val PLEOS_ENGINE = "ai.pleos.playground.caas"
    }
}
