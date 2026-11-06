package dev.cylox.client.minecraft189;

import com.viaversion.viaversion.api.Via;
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
    private static final int OPTION_FIRST_ID = 20200;
    private static final int PAGE_PREVIOUS_ID = 20290;
    private static final int PAGE_LABEL_ID = 20291;
    private static final int PAGE_NEXT_ID = 20292;
    private static final int MENU_COLUMNS = 3;
    private static final int MENU_ROWS = 6;
    private static final int PAGE_SIZE = MENU_COLUMNS * MENU_ROWS;
    private static volatile boolean available;
    private static volatile int selectedProtocolId = NATIVE_PROTOCOL;
    private static volatile boolean selectorMenuOpen;
    private static volatile int selectorPage;
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

    /** Adds a Select menu beside the vanilla Cancel button on Multiplayer. */
    public static void addSelectorButton(Object screen) {
        if (!available || screen == null) return;
        try {
            List<Object> buttons = findButtonList(screen);
            if (buttons == null || buttons.isEmpty()) return;
            removeMenuButtons(buttons);
            selectorMenuOpen = false;
            for (Object button : buttons) {
                if (buttonId(button) == SELECTOR_BUTTON_ID) {
                    setButtonLabel(button, selectorLabel());
                    return;
                }
            }
            Object template = buttons.get(0);
            Object cancel = findCancelButton(buttons);
            int x = 8, y = 8, buttonWidth = 164, buttonHeight = 20;
            if (cancel != null) {
                int cancelX = buttonInt(cancel, 1, 8);
                int cancelY = buttonInt(cancel, 2, 8);
                int cancelWidth = buttonInt(cancel, 3, 100);
                int cancelHeight = buttonInt(cancel, 4, 20);
                y = cancelY;
                buttonHeight = cancelHeight;
                if (cancelWidth >= 160) {
                    int leftWidth = (cancelWidth - 4) / 2;
                    int rightWidth = cancelWidth - leftWidth - 4;
                    setButtonInt(cancel, 3, leftWidth);
                    x = cancelX + leftWidth + 4;
                    buttonWidth = rightWidth;
                } else {
                    buttonWidth = Math.max(80, Math.min(110, cancelWidth));
                    x = cancelX + cancelWidth + 4;
                }
            } else {
                System.err.println("[Cylox] Multiplayer Cancel button not found; using fallback selector position.");
            }
            buttons.add(createButton(template, SELECTOR_BUTTON_ID, x, y,
                buttonWidth, buttonHeight, selectorLabel()));
            System.out.println("[Cylox] Protocol Select menu added beside Cancel.");
        } catch (Throwable error) {
            System.err.println("[Cylox] Could not add protocol selector UI: " + error.getMessage());
        }
    }

    /** Handles Cylox menu controls; vanilla button actions continue normally. */
    public static void handleSelectorAction(Object screen, Object button) {
        if (!available || screen == null || button == null) return;
        try {
            int id = buttonId(button);
            List<Object> buttons = findButtonList(screen);
            if (buttons == null) return;
            if (id == SELECTOR_BUTTON_ID) {
                selectorMenuOpen = !selectorMenuOpen;
                if (selectorMenuOpen) {
                    selectorPage = pageForSelectedProtocol();
                    rebuildSelectorMenu(buttons, button);
                } else {
                    removeMenuButtons(buttons);
                }
                return;
            }
            if (id == PAGE_PREVIOUS_ID) {
                selectorPage = Math.max(0, selectorPage - 1);
                rebuildSelectorMenu(buttons, findButton(buttons, SELECTOR_BUTTON_ID));
                return;
            }
            if (id == PAGE_NEXT_ID) {
                selectorPage = Math.min(Math.max(0, pageCount() - 1), selectorPage + 1);
                rebuildSelectorMenu(buttons, findButton(buttons, SELECTOR_BUTTON_ID));
                return;
            }
            if (id >= OPTION_FIRST_ID && id < OPTION_FIRST_ID + PAGE_SIZE) {
                List<ProtocolVersion> versions = getSupportedProtocols();
                int index = selectorPage * PAGE_SIZE + id - OPTION_FIRST_ID;
                if (index < 0 || index >= versions.size()) return;
                ProtocolVersion chosen = versions.get(index);
                setTargetProtocol(chosen.getVersion());
                selectedProtocolId = chosen.getVersion();
                saveSelectedProtocol();
                selectorMenuOpen = false;
                removeMenuButtons(buttons);
                Object selector = findButton(buttons, SELECTOR_BUTTON_ID);
                if (selector != null) setButtonLabel(selector, selectorLabel());
                System.out.println("[Cylox] Protocol target selected: " + chosen.getName()
                    + " (protocol " + chosen.getVersion() + ").");
            }
        } catch (Throwable error) {
            System.err.println("[Cylox] Protocol menu action failed: "
                + error.getClass().getSimpleName() + ": " + error.getMessage());
        }
    }

    private static void rebuildSelectorMenu(List<Object> buttons, Object selector) throws Exception {
        removeMenuButtons(buttons);
        if (selector == null) return;
        List<ProtocolVersion> versions = getSupportedProtocols();
        if (versions.isEmpty()) return;
        int pages = Math.max(1, (versions.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        selectorPage = Math.max(0, Math.min(selectorPage, pages - 1));
        int selectorX = buttonInt(selector, 1, 8);
        int selectorY = buttonInt(selector, 2, 8);
        int selectorWidth = buttonInt(selector, 3, 100);
        int menuWidth = Math.max(300, Math.min(420, selectorWidth * 4));
        int columnWidth = (menuWidth - 8) / MENU_COLUMNS;
        int menuX = Math.max(4, selectorX + selectorWidth / 2 - menuWidth / 2);
        int navigationY = Math.max(28, selectorY - (MENU_ROWS * 24 + 28));
        buttons.add(createButton(selector, PAGE_PREVIOUS_ID, menuX, navigationY, columnWidth, 20, "Previous"));
        buttons.add(createButton(selector, PAGE_LABEL_ID, menuX + columnWidth + 4, navigationY,
            columnWidth, 20, "Page " + (selectorPage + 1) + "/" + pages));
        buttons.add(createButton(selector, PAGE_NEXT_ID, menuX + (columnWidth + 4) * 2,
            navigationY, columnWidth, 20, "Next"));
        int start = selectorPage * PAGE_SIZE;
        int end = Math.min(versions.size(), start + PAGE_SIZE);
        for (int index = start; index < end; index++) {
            int offset = index - start;
            int column = offset % MENU_COLUMNS;
            int row = offset / MENU_COLUMNS;
            ProtocolVersion version = versions.get(index);
            String label = version.getName();
            if (version.getVersion() == selectedProtocolId) label = "* " + label;
            int x = menuX + column * (columnWidth + 4);
            int y = navigationY + 24 + row * 24;
            buttons.add(createButton(selector, OPTION_FIRST_ID + offset, x, y, columnWidth, 20, label));
        }
    }

    private static int pageForSelectedProtocol() {
        List<ProtocolVersion> versions = getSupportedProtocols();
        for (int index = 0; index < versions.size(); index++) {
            if (versions.get(index).getVersion() == selectedProtocolId) return index / PAGE_SIZE;
        }
        return 0;
    }

    private static int pageCount() {
        int size = getSupportedProtocols().size();
        return Math.max(1, (size + PAGE_SIZE - 1) / PAGE_SIZE);
    }

    private static void removeMenuButtons(List<Object> buttons) {
        for (int index = buttons.size() - 1; index >= 0; index--) {
            int id = buttonId(buttons.get(index));
            if ((id >= OPTION_FIRST_ID && id < OPTION_FIRST_ID + PAGE_SIZE)
                || id == PAGE_PREVIOUS_ID || id == PAGE_LABEL_ID || id == PAGE_NEXT_ID) {
                buttons.remove(index);
            }
        }
    }

    private static Object findButton(List<Object> buttons, int expectedId) {
        for (Object button : buttons) if (buttonId(button) == expectedId) return button;
        return null;
    }

    private static Object findCancelButton(List<Object> buttons) {
        Object idZeroButton = null;
        for (Object button : buttons) {
            String label = buttonLabel(button);
            if (label != null) {
                String value = label.trim().toLowerCase();
                if (value.equals("cancel") || value.equals("batal")
                    || value.equals("back") || value.equals("kembali")) return button;
            }
            if (buttonId(button) == 0) idZeroButton = button;
        }
        return idZeroButton;
    }

    private static Object createButton(Object template, int id, int x, int y,
                                       int width, int height, String label) throws Exception {
        Constructor<?> constructor = template.getClass().getConstructor(
            int.class, int.class, int.class, int.class, int.class, String.class);
        return constructor.newInstance(id, x, y, width, height, label);
    }

    private static List<Field> integerFields(Object target) {
        List<Field> fields = new ArrayList<Field>();
        for (Class<?> type = target.getClass(); type != null; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                if (field.getType() != Integer.TYPE) continue;
                try {
                    field.setAccessible(true);
                    fields.add(field);
                } catch (Throwable ignored) { }
            }
        }
        return fields;
    }

    private static int buttonId(Object button) {
        List<Field> fields = integerFields(button);
        if (fields.isEmpty()) return -1;
        try {
            return fields.get(0).getInt(button);
        } catch (Throwable ignored) {
            return -1;
        }
    }

    private static int buttonInt(Object button, int index, int fallback) {
        List<Field> fields = integerFields(button);
        if (index < 0 || index >= fields.size()) return fallback;
        try {
            return fields.get(index).getInt(button);
        } catch (Throwable ignored) {
            return fallback;
        }
    }

    private static void setButtonInt(Object button, int index, int value) {
        List<Field> fields = integerFields(button);
        if (index < 0 || index >= fields.size()) return;
        try {
            fields.get(index).setInt(button, value);
        } catch (Throwable ignored) { }
    }

    private static String buttonLabel(Object button) {
        for (Class<?> type = button.getClass(); type != null; type = type.getSuperclass()) {
            for (Field field : type.getDeclaredFields()) {
                if (field.getType() != String.class) continue;
                try {
                    field.setAccessible(true);
                    Object value = field.get(button);
                    if (value instanceof String) return (String) value;
                } catch (Throwable ignored) { }
            }
        }
        return null;
    }

    public static String getSelectedProtocolName() {
        ProtocolVersion version = ProtocolVersion.getProtocol(selectedProtocolId);
        return version == null ? "1.8.9" : version.getName();
    }

    private static String selectorLabel() {
        return "Select: " + getSelectedProtocolName();
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
        if (target == null) throw new IllegalArgumentException("ViaVersion does not expose protocol " + protocolId);
        if (protocolId != NATIVE_PROTOCOL) {
            ProtocolVersion nativeVersion = ProtocolVersion.getProtocol(NATIVE_PROTOCOL);
            List<?> route = Via.getManager().getProtocolManager().getProtocolPath(nativeVersion, target);
            if (route == null) {
                throw new IllegalStateException("No Via translation route from 1.8.9 to " + target.getName());
            }
        }
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
                System.out.println("[Cylox] Via connect hook installed.");
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
                    System.out.println("[Cylox] Via active for connection to " + remoteAddress
                        + ": 1.8.9 -> " + getSelectedProtocolName()
                        + " (protocol " + selected + ").");
                }
                context.connect(remoteAddress, localAddress, promise);
            } catch (Throwable error) {
                promise.tryFailure(new IllegalStateException(
                    "Cylox could not initialize the selected Via protocol", error));
            }
        }
    }
}
