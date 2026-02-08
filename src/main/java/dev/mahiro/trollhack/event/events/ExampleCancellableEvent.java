package dev.mahiro.trollhack.event.events;

import dev.mahiro.trollhack.event.Cancellable;

public class ExampleCancellableEvent extends Cancellable {
    private float v1;
    private boolean v2;

    public ExampleCancellableEvent(float v1, boolean v2) {
        this.v1 = v1;
        this.v2 = v2;
    }

    public float getV1() {
        return this.v1;
    }

    public void setV1(float v1) {
        this.v1 = v1;
    }

    public boolean isV2() {
        return this.v2;
    }
}
