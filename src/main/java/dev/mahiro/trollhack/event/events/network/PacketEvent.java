package dev.mahiro.trollhack.event.events.network;

import dev.mahiro.trollhack.event.Cancellable;
import net.minecraft.network.packet.Packet;

/**
 * Event fired when packets are sent or received
 */
public class PacketEvent extends Cancellable {
    private final Packet<?> packet;

    public PacketEvent(Packet<?> packet) {
        this.packet = packet;
    }

    public Packet<?> getPacket() {
        return packet;
    }

    /**
     * Event fired when a packet is being sent to the server
     */
    public static class Send extends PacketEvent {
        public Send(Packet<?> packet) {
            super(packet);
        }
    }

    /**
     * Event fired when a packet is received from the server
     */
    public static class Receive extends PacketEvent {
        public Receive(Packet<?> packet) {
            super(packet);
        }
    }
}
