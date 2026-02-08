package dev.mahiro.trollhack.event.events.input;

public final class KeyPressEvent {
    private final int keyCode;

    public KeyPressEvent(int keyCode) {
        this.keyCode = keyCode;
    }

    public int getKeyCode() {
        return keyCode;
    }
}

