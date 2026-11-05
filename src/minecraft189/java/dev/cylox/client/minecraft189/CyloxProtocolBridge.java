package dev.cylox.client.minecraft189;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.connection.UserConnectionImpl;
import com.viaversion.viaversion.protocol.ProtocolPipelineImpl;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import de.florianmichael.vialoadingbase.netty.event.CompressionReorderEvent;
import de.florianmichael.viamcp.MCPVLBPipeline;
import dev.cylox.client.protocol.CyloxProtocolTargetResolver;
import dev.cylox.client.protocol.ServerStatus;
import dev.cylox.client.protocol.ServerStatusClient;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.socket.SocketChannel;
import java.io.File;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Optional 1.8.9 client-side protocol bridge powered by ViaVersion + ViaBackwards + ViaRewind.
 * Auto mode checks the server status off the Netty event loop, selects its advertised protocol,
 * installs Via's per-connection handlers, then allows the original vanilla socket connect to run.
 */
public final class CyloxProtocolBridge {
    private static final int NATIVE_PROTOCOL = 47;
    private static final int STATUS_TIMEOUT_MS = 2500;
    private static volatile boolean available;
    private static final ServerStatusClient STATUS_CLIENT = new ServerStatusClient();
    private static final ExecutorService DETECTOR = Executors.newCachedThreadPool(new ThreadFactory() {
        private int sequence;
        @Override public Thread newThread(Runnable task) {
            Thread thread = new Thread(task, "Cylox-protocol-detect-" + (++sequence));
            thread.setDaemon(true);
            return thread;
        }
    });

    private CyloxProtocolBridge() { }

    /** Must run after Cylox settings load and before Minecraft networking classes are initialized. */
    public static synchronized void initialize(File gameDirectory) {
        if (available || !bridgeEnabled()) return;
        try {
            File root = gameDirectory == null ? new File(".") : gameDirectory;
            ViaLoadingBase.ViaLoadingBaseBuilder.create()
                .runDirectory(new File(root, "cylox"))
                .nativeVersion(NATIVE_PROTOCOL)
                .build();
            available = ViaLoadingBase.getInstance() != null;
            if (available) {
                setTargetProtocol(NATIVE_PROTOCOL);
                System.out.println("[Cylox] Via protocol bridge ready (native protocol 47).");
            }
        } catch (Throwable error) {
            available = false;
            System.err.println("[Cylox] Via bridge could not initialize; native 1.8.x remains available: "
                + error.getClass().getSimpleName() + ": " + error.getMessage());
        }
    }

    /** Called by the bytecode hook after vanilla has installed its standard Netty handlers. */
    public static void install(Object candidate) {
        if (!available || !(candidate instanceof SocketChannel)) return;
        try {
            Channel channel = (Channel) candidate;
            if (channel.pipeline().get("cylox-protocol-connect") == null) {
                channel.pipeline().addLast("cylox-protocol-connect", new ProtocolConnectHandler());
            }
        } catch (Throwable error) {
            System.err.println("[Cylox] Could not install protocol connect hook: " + error.getMessage());
        }
    }

    /** Called after vanilla inserts compression handlers; Via must be reordered after them. */
    public static void onCompressionEnabled(Object networkManager) {
        if (!available || networkManager == null) return;
        try {
            for (Class<?> current = networkManager.getClass(); current != null; current = current.getSuperclass()) {
                for (Field field : current.getDeclaredFields()) {
                    if (!Channel.class.isAssignableFrom(field.getType())) continue;
                    field.setAccessible(true);
                    Object value = field.get(networkManager);
                    if (value instanceof Channel) {
                        ((Channel) value).pipeline().fireUserEventTriggered(new CompressionReorderEvent());
                        return;
                    }
                }
            }
        } catch (Throwable error) {
            System.err.println("[Cylox] Compression pipeline reorder failed: " + error.getMessage());
        }
    }

    private static boolean bridgeEnabled() {
        return available;
    }

    private static String targetMode() {
        return "Auto";
    }

    private static void setTargetProtocol(int protocolId) {
        if (!available || ViaLoadingBase.getInstance() == null) return;
        ProtocolVersion target = ProtocolVersion.getProtocol(protocolId);
        if (target == null) {
            target = ProtocolVersion.getProtocol(NATIVE_PROTOCOL);
        }
        if (target == null) throw new IllegalStateException("ViaVersion does not expose native protocol 47");
        ViaLoadingBase.getInstance().reload(target);
    }

    private static void attachVia(Channel channel) {
        if (channel.pipeline().get("via-decoder") != null) return;
        UserConnection user = new UserConnectionImpl(channel, true);
        new ProtocolPipelineImpl(user);
        channel.pipeline().addLast(new MCPVLBPipeline(user));
    }

    private static final class ProtocolConnectHandler extends ChannelOutboundHandlerAdapter {
        @Override
        public void connect(final ChannelHandlerContext context, final SocketAddress remoteAddress,
                            final SocketAddress localAddress, final ChannelPromise promise) throws Exception {
            if (!bridgeEnabled() || "Native".equalsIgnoreCase(targetMode())) {
                setTargetProtocol(NATIVE_PROTOCOL);
                context.connect(remoteAddress, localAddress, promise);
                return;
            }

            // Never keep a previous server's protocol when the next status ping fails.
            setTargetProtocol(NATIVE_PROTOCOL);
            if (!(remoteAddress instanceof InetSocketAddress)) {
                context.connect(remoteAddress, localAddress, promise);
                return;
            }

            final InetSocketAddress endpoint = (InetSocketAddress) remoteAddress;
            final String host = endpoint.getHostString();
            final int port = endpoint.getPort();
            DETECTOR.execute(new Runnable() {
                @Override public void run() {
                    int advertisedProtocol = NATIVE_PROTOCOL;
                    String detection = "status ping unavailable";
                    try {
                        ServerStatus status = STATUS_CLIENT.query(host, port, STATUS_TIMEOUT_MS);
                        advertisedProtocol = status.getProtocolId();
                        detection = "server advertised protocol " + advertisedProtocol;
                    } catch (Exception error) {
                        System.err.println("[Cylox] Auto protocol detection failed for " + host + ":" + port
                            + " (" + error.getMessage() + "); using native 1.8.x.");
                    }

                    final int selected = CyloxProtocolTargetResolver.resolve(
                        bridgeEnabled(), targetMode(), advertisedProtocol);
                    final String detected = detection;
                    try {
                        context.executor().execute(new Runnable() {
                            @Override public void run() {
                                if (promise.isDone()) return;
                                try {
                                    setTargetProtocol(selected);
                                    if (selected != NATIVE_PROTOCOL) {
                                        attachVia(context.channel());
                                        System.out.println("[Cylox] Via bridge active: " + detected
                                            + ", translating to protocol 47.");
                                    } else if (bridgeEnabled()) {
                                        System.out.println("[Cylox] Using native protocol 47 (" + detected + ").");
                                    }
                                    context.connect(remoteAddress, localAddress, promise);
                                } catch (Throwable error) {
                                    promise.tryFailure(new IllegalStateException(
                                        "Cylox could not initialize the selected protocol translator", error));
                                }
                            }
                        });
                    } catch (Throwable rejected) {
                        promise.tryFailure(rejected);
                    }
                }
            });
        }
    }
}
