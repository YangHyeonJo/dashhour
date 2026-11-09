package com.dashhour.app

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.dashhour.app.vehicle.GearSource
import com.dashhour.app.vehicle.PleosGearSource
import org.json.JSONObject

/**
 * DASHHOUR 메인 화면.
 * 화면과 주행 상태 머신은 assets/web/index.html(웹뷰)이 담당하고,
 * 네이티브는 ① Vehicle SDK 기어 신호 전달 ② 안드로이드 TTS 음성 안내만 맡는다.
 */
class MainActivity : Activity() {

    private lateinit var web: WebView
    private lateinit var tts: TtsSpeaker
    private var gear: GearSource? = null
    @Volatile private var vehicleStatus: String = "연결 전"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WebView.setWebContentsDebuggingEnabled(true) // chrome://inspect 로 웹 화면 디버깅
        tts = TtsSpeaker(this)

        web = WebView(this).apply {
            setBackgroundColor(0xFF0E1420.toInt())
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            addJavascriptInterface(NativeBridge(tts) { vehicleStatus }, "DashNative")
            webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(m: ConsoleMessage): Boolean {
                    Log.d(TAG, "web: ${m.message()} (${m.sourceId()}:${m.lineNumber()})")
                    return true
                }
            }
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    if (gear == null) startGear()
                    pushStatus()
                }
            }
        }
        setContentView(web)
        web.loadUrl("file:///android_asset/web/index.html")
    }

    /** Vehicle SDK 기어 구독 시작. 실패해도 웹 화면은 데모 모드로 계속 동작한다. */
    private fun startGear() {
        val source = PleosGearSource(this)
        gear = source
        source.start(
            onGear = { g -> runOnUiThread { pushGear(g) } },
            onStatus = { s -> vehicleStatus = s; runOnUiThread { pushStatus() } }
        )
    }

    private fun pushGear(g: String) {
        Log.i(TAG, "gear -> $g")
        web.evaluateJavascript("window.dashhour && window.dashhour.vehicle({gear:${JSONObject.quote(g)}})", null)
    }

    private fun pushStatus() {
        web.evaluateJavascript("window.dashhour && window.dashhour.vehicleStatus(${JSONObject.quote(vehicleStatus)})", null)
    }

    override fun onDestroy() {
        gear?.stop()
        tts.shutdown()
        web.destroy()
        super.onDestroy()
    }

    companion object { const val TAG = "DashHour" }
}
