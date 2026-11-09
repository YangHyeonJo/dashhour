package com.dashhour.app.vehicle;

import ai.pleos.playground.vehicle.constant.control.VehicleGear;
import ai.pleos.playground.vehicle.listener.GearSelectionListener;

/**
 * Pleos SDK의 GearSelectionListener를 Kotlin에서 직접 구현하면 메타데이터 문제로
 * "onSelected overrides nothing" 컴파일 오류가 난다. Java로 감싸서 우회한다.
 */
public final class GearListenerAdapter implements GearSelectionListener {

    public interface Callback {
        void onGear(VehicleGear gear);
        void onError(Exception e);
    }

    private final Callback callback;

    public GearListenerAdapter(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void onSelected(VehicleGear gear) {
        callback.onGear(gear);
    }

    @Override
    public void onFailed(Exception e) {
        callback.onError(e);
    }
}
