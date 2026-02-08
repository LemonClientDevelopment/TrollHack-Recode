package dev.mahiro.trollhack.event.events.input;

public final class MouseButtonEvent {
    private final int button;
    private final int modifiers;
    private final boolean pressed;

    public MouseButtonEvent(int button, int modifiers, boolean pressed) {
        this.button = button;
        this.modifiers = modifiers;
        this.pressed = pressed;
    }

    public int getButton() {
        return button;
    }

    public int getModifiers() {
        return modifiers;
    }

    public boolean isPressed() {
        return pressed;
    }
}
