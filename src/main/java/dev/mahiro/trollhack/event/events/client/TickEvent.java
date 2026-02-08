package dev.mahiro.trollhack.event.events.client;

import dev.mahiro.trollhack.event.EventType;

public class TickEvent {
    private final EventType type;

    public TickEvent(EventType type) {
        this.type = type;
    }

    public EventType getType() {
        return type;
    }
}
