package dev.cylox.client.minecraft189;

import com.viaversion.viarewind.protocol.v1_9to1_8.Protocol1_9To1_8;
import com.viaversion.viaversion.api.data.entity.EntityTracker;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.type.Types;

/** Compatibility hook for ViaRewind's 1.9-to-1.8 local-player equipment packets. */
public final class CyloxViaRewindHooks {
    private static volatile boolean failureReported;

    private CyloxViaRewindHooks() { }

    /**
     * A 1.8 client already receives its own equipment through window-0 slot updates.
     * Forwarding SET_EQUIPPED_ITEM for the local player can index the legacy armor
     * array with an incompatible slot, so cancel that redundant packet.
     *
     * @return true when the packet was identified as the local player's packet
     *         and cancelled; false when normal ViaRewind handling should continue.
     */
    public static boolean cancelOwnPlayerEquipment(Object packetObject) {
        try {
            PacketWrapper wrapper = (PacketWrapper) packetObject;
            EntityTracker tracker = wrapper.user().getEntityTracker(Protocol1_9To1_8.class);
            if (tracker == null) return false;

            int entityId = wrapper.get(Types.VAR_INT, 0);
            if (entityId != tracker.clientEntityId()) return false;

            wrapper.cancel();
            return true;
        } catch (Throwable error) {
            if (!failureReported) {
                failureReported = true;
                System.err.println("[Cylox] ViaRewind equipment compatibility hook failed: "
                    + error.getClass().getSimpleName() + ": " + error.getMessage());
            }
            return false;
        }
    }
}
