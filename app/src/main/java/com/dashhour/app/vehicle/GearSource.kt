package com.dashhour.app.vehicle

/** 기어 신호 공급원. 값은 "P", "R", "N", "D" 중 하나 */
interface GearSource {
    fun start(onGear: (String) -> Unit, onStatus: (String) -> Unit)
    fun stop()
}
