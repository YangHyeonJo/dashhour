package com.dashhour.app

import android.webkit.JavascriptInterface

/** 웹 화면(JS)에서 window.DashNative 로 호출하는 기능 */
class NativeBridge(
    private val tts: TtsSpeaker,
    private val status: () -> String
) {
    @JavascriptInterface fun speak(text: String) = tts.speak(text)
    @JavascriptInterface fun stopSpeaking() = tts.stop()
    @JavascriptInterface fun ttsReady(): Boolean = tts.ready
    @JavascriptInterface fun vehicleStatus(): String = status()
}
