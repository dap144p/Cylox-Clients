package de.florianmichael.vialoadingbase.provider;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.protocol.version.BaseVersionProvider;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import java.util.List;

public class VLBBaseVersionProvider extends BaseVersionProvider {
    @Override
    public ProtocolVersion getClosestServerProtocol(UserConnection connection) throws Exception {
        if (connection.isClientSide()) {
            ProtocolVersion target = ViaLoadingBase.getInstance().getTargetVersion();
            if (target != null) {
                ProtocolVersion client = connection.getProtocolInfo().protocolVersion();
                List<?> route = Via.getManager().getProtocolManager().getProtocolPath(client, target);
                System.out.println("[Cylox] Via handshake decision: client="
                    + client.getName() + " (" + client.getVersion() + "), target="
                    + target.getName() + " (" + target.getVersion() + "), route="
                    + (route == null ? "MISSING" : route.size() + " steps") + ".");
                return target;
            }
        }
        return super.getClosestServerProtocol(connection);
    }
}
