package dev.mahiro.trollhack.event.events.input;

public final class KeyActionEvent {
    private final int keyCode;
    private final int scanCode;
    private final int modifiers;
    private final boolean pressed;

    public KeyActionEvent(int keyCode, int scanCode, int modifiers, boolean pressed) {
        this.keyCode = keyCode;
        this.scanCode = scanCode;
        this.modifiers = modifiers;
        this.pressed = pressed;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public int getScanCode() {
        return scanCode;
    }

    public int getModifiers() {
        return modifiers;
    }

    public boolean isPressed() {
        return pressed;
    }
}
