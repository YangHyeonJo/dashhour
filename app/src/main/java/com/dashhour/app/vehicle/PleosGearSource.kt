package com.dashhour.app.vehicle

import ai.pleos.playground.vehicle.Vehicle
import ai.pleos.playground.vehicle.api.Gear
import ai.pleos.playground.vehicle.constant.control.VehicleGear
import ai.pleos.playground.vehicle.listener.GearSelectionListener
import android.content.Context
import android.util.Log

/**
 * Pleos Vehicle SDK(2.3.2)의 Gear 도메인에서 기어 신호를 받는다.
 * - 현재 기어 1회 조회: Gear.getCurrentGearSelectionState(onComplete, onFailed)
 * - 변경 구독: Gear.registerGearSelection(GearSelectionListener)
 * 기어는 DrivingMode 도메인이 아니라 Vehicle.getGear()에 있다.
 */
class PleosGearSource(context: Context) : GearSource {

    private val vehicle = Vehicle(context.applicationContext)
    private var initialized = false
    private var gear: Gear? = null
    private var listener: GearSelectionListener? = null

    override fun start(onGear: (String) -> Unit, onStatus: (String) -> Unit) {
        try {
            vehicle.initialize()
            initialized = true
        } catch (e: Throwable) {
            onStatus("Vehicle SDK 연결 실패: ${e.message}")
            Log.w(TAG, "initialize failed", e)
            return
        }

        val g = try {
            vehicle.getGear()
        } catch (e: Throwable) {
            onStatus("Gear 도메인 없음: ${e.message}")
            Log.w(TAG, "getGear failed", e)
            return
        }
        gear = g

        try {
            g.getCurrentGearSelectionState(
                { v -> Log.i(TAG, "current gear: $v"); toGear(v)?.let(onGear) },
                { e -> Log.w(TAG, "getCurrentGearSelectionState failed", e) }
            )
        } catch (e: Throwable) {
            Log.w(TAG, "getCurrentGearSelectionState threw", e)
        }

        try {
            val l = GearListenerAdapter(object : GearListenerAdapter.Callback {
                override fun onGear(v: VehicleGear?) {
                    Log.i(TAG, "onSelected: $v")
                    toGear(v)?.let(onGear)
                }
                override fun onError(e: Exception?) {
                    Log.w(TAG, "gear listener failed", e)
                }
            })
            g.registerGearSelection(l)
            listener = l
            onStatus("차량 기어 연결됨")
            Log.i(TAG, "registerGearSelection ok")
        } catch (e: Throwable) {
            onStatus("기어 구독 실패: ${e.message}")
            Log.w(TAG, "registerGearSelection failed", e)
        }
    }

    override fun stop() {
        try { listener?.let { gear?.unregisterGearSelection(it) } } catch (e: Throwable) { Log.w(TAG, "unregister failed", e) }
        listener = null
        try { if (initialized) vehicle.release() } catch (e: Throwable) { Log.w(TAG, "release failed", e) }
        initialized = false
    }

    /** VehicleGear → P/R/N/D. 수동 1~9단은 주행(D)으로 간주 */
    private fun toGear(v: VehicleGear?): String? = when (v) {
        VehicleGear.GEAR_PARK -> "P"
        VehicleGear.GEAR_REVERSE -> "R"
        VehicleGear.GEAR_NEUTRAL -> "N"
        VehicleGear.GEAR_DRIVE,
        VehicleGear.GEAR_1, VehicleGear.GEAR_2, VehicleGear.GEAR_3,
        VehicleGear.GEAR_4, VehicleGear.GEAR_5, VehicleGear.GEAR_6,
        VehicleGear.GEAR_7, VehicleGear.GEAR_8, VehicleGear.GEAR_9 -> "D"
        else -> null
    }

    companion object { private const val TAG = "DashGear" }
}
