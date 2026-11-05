package dev.cylox.client.minecraft189;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.connection.UserConnectionImpl;
import com.viaversion.viaversion.protocol.ProtocolPipelineImpl;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import de.florianmichael.vialoadingbase.netty.event.CompressionReorderEvent;
import de.florianmichael.viamcp.MCPVLBPipeline;
import dev.cylox.client.protocol.CyloxProtocolTargetResolver;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.socket.SocketChannel;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

/** Manual client-side protocol selection and background-compatible Via pipeline hooks. */
public final class CyloxProtocolBridge {
    private static final int NATIVE_PROTOCOL = 47;
    private static final int SELECTOR_BUTTON_ID = 20189;
    private static volatile boolean available;
    private static volatile int selectedProtocolId = NATIVE_PROTOCOL;
    private static File settingsFile;

    private CyloxProtocolBridge() { }

    /** Runs once from the LaunchWrapper tweaker before Minecraft networking is initialized. */
    public static synchronized void initialize(File gameDirectory) {
        if (available) return;
        File root = gameDirectory == null ? new File(".") : gameDirectory;
        File dataDirectory = new File(root, "cylox");
        settingsFile = new File(dataDirectory, "protocol.properties");
        loadSelectedProtocol();

        try {
            if (ViaLoadingBase.getInstance() == null) {
                ViaLoadingBase.ViaLoadingBaseBuilder.create()
                    .runDirectory(dataDirectory)
                    .nativeVersion(NATIVE_PROTOCOL)
                    .build();
            }
            available = ViaLoadingBase.getInstance() != null;
            if (available) {
                selectedProtocolId = normalizeProtocol(selectedProtocolId);
                setTargetProtocol(selectedProtocolId);
                System.out.println("[Cylox] Embedded Via bridge ready. Manual target: "
                    + getSelectedProtocolName() + ".");
            } else {
                System.err.println("[Cylox] Via bridge did not initialize; native 1.8.9 remains available.");
            }
        } catch (Throwable error) {
            available = false;
            selectedProtocolId = NATIVE_PROTOCOL;
            System.err.println("[Cylox] Via bridge initialization failed; using native 1.8.9: "
                + error.getClass().getSimpleName() + ": " + error.getMessage());
        }
    }

    /** Adds the manual protocol selector to the vanilla Multiplayer screen. */
    public static void addSelectorButton(Object screen) {
        if (!available || screen == null) return;
        try {
            List<Object> buttons = findButtonList(screen);
            if (buttons == null || buttons.isEmpty()) return;
            for (Object button : buttons) {
                if (hasButtonId(button, SELECTOR_BUTTON_ID)) return;
            }
            Object template = buttons.get(0);
            Constructor<?> constructor = template.getClass().getConstructor(
                int.class, int.class, int.class, int.class, int.class, String.class);
            Object selector = constructor.newInstance(
                SELECTOR_BUTTON_ID, 8, 8, 164, 20, selectorLabel());
            buttons.add(selector);
        } catch (Throwable error) {
            System.err.println("[Cylox] Could not add protocol selector UI: " + error.getMessage());
        }
    }

    /** Handles only Cylox's own Multiplayer button; all vanilla buttons continue normally. */
    public static void handleSelectorAction(Object screen, Object button) {
        if (!available || button == null || !hasButtonId(button, SELECTOR_BUTTON_ID)) return;
        try {
            selectedProtocolId = nextProtocol(selectedProtocolId);
            selectedProtocolId = normalizeProtocol(selectedProtocolId);
            setTargetProtocol(selectedProtocolId);
            saveSelectedProtocol();
            setButtonLabel(button, selectorLabel());
            System.out.println("[Cylox] Protocol target set to " + getSelectedProtocolName()
                + ". Choose the same version as the destination server.");
        } catch (Throwable error) {
            System.err.println("[Cylox] Could not change protocol target: " + error.getMessage());
        }
    }

    public static String getSelectedProtocolName() {
        ProtocolVersion version = ProtocolVersion.getProtocol(selectedProtocolId);
        return version == null ? "1.8.9" : version.getName();
    }

    private static String selectorLabel() {
        return "Via: " + getSelectedProtocolName();
    }

    private static int nextProtocol(int currentProtocolId) {
        List<ProtocolVersion> versions = getSupportedProtocols();
        if (versions.isEmpty()) return NATIVE_PROTOCOL;
        for (int index = 0; index < versions.size(); index++) {
            if (versions.get(index).getVersion() == currentProtocolId) {
                return versions.get((index + 1) % versions.size()).getVersion();
            }
        }
        return NATIVE_PROTOCOL;
    }

    private static int normalizeProtocol(int requestedProtocolId) {
        for (ProtocolVersion version : getSupportedProtocols()) {
            if (version.getVersion() == requestedProtocolId) return requestedProtocolId;
        }
        return NATIVE_PROTOCOL;
    }

    private static List<ProtocolVersion> getSupportedProtocols() {
        List<ProtocolVersion> versions = new ArrayList<ProtocolVersion>(ViaLoadingBase.PROTOCOLS);
        Collections.sort(versions, new Comparator<ProtocolVersion>() {
            @Override public int compare(ProtocolVersion left, ProtocolVersion right) {
                return Integer.compare(left.getVersion(), right.getVersion());
            }
        });
        return versions;
    }

    private static void setTargetProtocol(int protocolId) {
        ViaLoadingBase loadingBase = ViaLoadingBase.getInstance();
        if (!available || loadingBase == null) return;
        ProtocolVersion target = ProtocolVersion.getProtocol(protocolId);
        if (target == null) target = ProtocolVersion.getProtocol(NATIVE_PROTOCOL);
        if (target == null) throw new IllegalStateException("ViaVersion does not expose protocol 47");
        loadingBase.reload(target);
    }

    private static void loadSelectedProtocol() {
        if (settingsFile == null || !settingsFile.isFile()) return;
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream(settingsFile)) {
            properties.load(input);
            selectedProtocolId = Integer.parseInt(properties.getProperty("targetProtocol", "47").trim());
        } catch (Exception error) {
            selectedProtocolId = NATIVE_PROTOCOL;
            System.err.println("[Cylox] Invalid protocol settings; reset to 1.8.9.");
        }
    }

    private static void saveSelectedProtocol() {
        if (settingsFile == null) return;
        Properties properties = new Properties();
        properties.setProperty("targetProtocol", Integer.toString(selectedProtocolId));
        File parent = settingsFile.getParentFile();
        if (parent != null && !parent.isDirectory() && !parent.mkdirs() && !parent.isDirectory()) return;
        try (FileOutputStream output = new FileOutputStream(settingsFile)) {
            properties.store(output, "Cylox manual Via protocol selection");
        } catch (Exception error) {
            System.err.println("[Cylox] Could not save protocol selection: " + error.getMessage());
        }
    }

    private static List<Object> findButtonList(Object screen) throws IllegalAccessException {
        for (Class<?> type = screen.getClass(); type != null; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                if (!List.class.isAssignableFrom(field.getType())) continue;
                field.setAccessible(true);
                Object value = field.get(screen);
                if (!(value instanceof List)) continue;
                List<?> candidate = (List<?>) value;
                for (Object item : candidate) {
                    if (hasGuiButtonConstructor(item.getClass())) {
                        @SuppressWarnings("unchecked")
                        List<Object> buttons = (List<Object>) candidate;
                        return buttons;
                    }
                }
            }
        }
        return null;
    }

    private static boolean hasGuiButtonConstructor(Class<?> type) {
        try {
            type.getConstructor(int.class, int.class, int.class, int.class, int.class, String.class);
            return true;
        } catch (NoSuchMethodException ignored) {
            return false;
        }
    }

    private static boolean hasButtonId(Object button, int expectedId) {
        for (Class<?> type = button.getClass(); type != null; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                if (field.getType() != Integer.TYPE) continue;
                try {
                    field.setAccessible(true);
                    if (field.getInt(button) == expectedId) return true;
                } catch (Throwable ignored) { }
            }
        }
        return false;
    }

    private static void setButtonLabel(Object button, String label) {
        for (Class<?> type = button.getClass(); type != null; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                if (field.getType() != String.class) continue;
                try {
                    field.setAccessible(true);
                    field.set(button, label);
                    return;
                } catch (Throwable ignored) { }
            }
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

    private static void attachVia(Channel channel) {
        if (channel.pipeline().get("via-decoder") != null) return;
        UserConnection user = new UserConnectionImpl(channel, true);
        new ProtocolPipelineImpl(user);
        channel.pipeline().addLast(new MCPVLBPipeline(user));
    }

    private static final class ProtocolConnectHandler extends ChannelOutboundHandlerAdapter {
        @Override
        public void connect(ChannelHandlerContext context, SocketAddress remoteAddress,
                            SocketAddress localAddress, ChannelPromise promise) throws Exception {
            if (!available) {
                context.connect(remoteAddress, localAddress, promise);
                return;
            }
            int selected = CyloxProtocolTargetResolver.resolve(true, selectedProtocolId);
            try {
                setTargetProtocol(selected);
                if (selected != NATIVE_PROTOCOL) {
                    attachVia(context.channel());
                    System.out.println("[Cylox] Manual Via translation enabled: 1.8.9 -> "
                        + getSelectedProtocolName() + ".");
                }
                context.connect(remoteAddress, localAddress, promise);
            } catch (Throwable error) {
                promise.tryFailure(new IllegalStateException(
                    "Cylox could not initialize the selected Via protocol", error));
            }
        }
    }
}
