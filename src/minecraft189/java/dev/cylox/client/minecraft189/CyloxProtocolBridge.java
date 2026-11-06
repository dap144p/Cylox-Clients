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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import org.lwjgl.opengl.GL11;
import java.util.Properties;

/** Manual client-side protocol selection and background-compatible Via pipeline hooks. */
public final class CyloxProtocolBridge {
    private static final int NATIVE_PROTOCOL = 47;
    private static final int SELECTOR_BUTTON_ID = 20189;
    private static final int OPTION_FIRST_ID = 20200;
    private static final int SCROLL_UP_ID = 20290;
    private static final int SCROLL_DOWN_ID = 20291;
    private static final int MENU_VISIBLE_ROWS = 5;
    private static final int PANEL_WIDTH = 360;
    private static final int PANEL_HEIGHT = 250;
    private static final int PANEL_ROW_HEIGHT = 24;
    private static final int PANEL_LIST_TOP = 49;
        private static final String[][] BUTTON_INT_FIELDS = new String[][] {
        {"id", "field_146127_k", "k"},
        {"xPosition", "field_146128_h", "h"},
        {"yPosition", "field_146129_i", "i"},
        {"width", "field_146120_f", "f"},
        {"height", "field_146121_g", "g"}
    };
    private static volatile boolean available;
    private static volatile int selectedProtocolId = NATIVE_PROTOCOL;
    private static volatile boolean selectorMenuOpen;
    private static volatile int selectorScrollOffset;
    private static File settingsFile;

    private static final class ProtocolChoice {
        final String label;
        final ProtocolVersion version;
        final boolean exactRelease;

        ProtocolChoice(String label, ProtocolVersion version, boolean exactRelease) {
            this.label = label;
            this.version = version;
            this.exactRelease = exactRelease;
        }
    }

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
                logProtocolReadiness();
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

    /** Adds Select beside vanilla Cancel without changing any vanilla button. */
    public static void addSelectorButton(Object screen) {
        if (!available || screen == null) return;
        try {
            List<Object> buttons = findButtonList(screen);
            if (buttons == null || buttons.isEmpty()) {
                System.err.println("[Cylox] Multiplayer button list was not found.");
                return;
            }
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
            int cancelX = cancel == null ? -1 : buttonInt(cancel, 1, -1);
            int cancelY = cancel == null ? -1 : buttonInt(cancel, 2, -1);
            int cancelWidth = cancel == null ? -1 : buttonInt(cancel, 3, -1);
            int cancelHeight = cancel == null ? 20 : buttonInt(cancel, 4, 20);
            if (cancelX < 0 || cancelY < 0 || cancelWidth <= 0) {
                int screenWidth = screenInt(screen, "width", "field_146294_l", "l");
                int screenHeight = screenInt(screen, "height", "field_146295_m", "m");
                if (screenWidth <= 0 || screenHeight <= 0) {
                    System.err.println("[Cylox] Could not resolve vanilla Cancel button coordinates.");
                    return;
                }
                // GuiMultiplayer.createButtons(): Cancel is at width/2 + 80, height - 28.
                cancelX = screenWidth / 2 + 80;
                cancelY = screenHeight - 28;
                cancelWidth = 75;
                cancelHeight = 20;
            }
            final int gap = 4;
            int buttonWidth = 120;
            int x = cancelX + cancelWidth + gap;
            int y = cancelY;
            int screenWidth = screenInt(screen, "width", "field_146294_l", "l");
            if (screenWidth > 0 && x + buttonWidth > screenWidth - 4) {
                buttonWidth = Math.max(80, screenWidth - 4 - x);
            }
            if (buttonWidth < 80) {
                buttonWidth = Math.min(120, Math.max(80, cancelX - 8));
                x = Math.max(4, cancelX - buttonWidth - gap);
            }
            buttons.add(createButton(template, SELECTOR_BUTTON_ID, x, y,
                buttonWidth, cancelHeight, selectorLabel()));
            System.out.println("[Cylox] Multiplayer Cancel preserved at (" + cancelX + "," + cancelY
                + "); Select placed at (" + x + "," + y + "), width " + buttonWidth + ".");
        } catch (Throwable error) {
            System.err.println("[Cylox] Could not add protocol selector UI: "
                + error.getClass().getSimpleName() + ": " + error.getMessage());
        }
    }

    /** Handles Cylox selector and scroll controls; vanilla actions continue normally. */
    public static void handleSelectorAction(Object screen, Object button) {
        if (!available || screen == null || button == null) return;
        try {
            int id = buttonId(button);
            List<Object> buttons = findButtonList(screen);
            if (buttons == null) return;

            if (id == SELECTOR_BUTTON_ID) {
                selectorMenuOpen = !selectorMenuOpen;
                if (selectorMenuOpen) {
                    selectorScrollOffset = scrollOffsetForSelected();
                    rebuildSelectorMenu(screen, buttons, button);
                    System.out.println("[Cylox] Protocol family menu opened. Selected: "
                        + getSelectedProtocolName() + ".");
                } else {
                    removeMenuButtons(buttons);
                }
                return;
            }
            if (id == SCROLL_UP_ID || id == SCROLL_DOWN_ID) {
                selectorScrollOffset = clampScrollOffset(selectorScrollOffset + (id == SCROLL_UP_ID ? -1 : 1));
                rebuildSelectorMenu(screen, buttons, findButton(buttons, SELECTOR_BUTTON_ID));
                return;
            }
            if (id >= OPTION_FIRST_ID && id < OPTION_FIRST_ID + MENU_VISIBLE_ROWS) {
                List<ProtocolChoice> choices = getProtocolChoices();
                int index = selectorScrollOffset + id - OPTION_FIRST_ID;
                if (index < 0 || index >= choices.size()) return;
                ProtocolChoice chosen = choices.get(index);
                setTargetProtocol(chosen.version.getVersion());
                selectedProtocolId = chosen.version.getVersion();
                saveSelectedProtocol();
                selectorMenuOpen = false;
                removeMenuButtons(buttons);
                Object selector = findButton(buttons, SELECTOR_BUTTON_ID);
                if (selector != null) setButtonLabel(selector, selectorLabel());
                System.out.println("[Cylox] Protocol target selected: " + chosen.label
                    + " (protocol " + chosen.version.getVersion() + ").");
            }
        } catch (Throwable error) {
            System.err.println("[Cylox] Protocol menu action failed: "
                + error.getClass().getSimpleName() + ": " + error.getMessage());
        }
    }

    /** Receives the actual LWJGL wheel delta from GuiScreen.handleMouseInput. */
    public static void handleSelectorMouseWheel(Object screen, int wheelDelta) {
        if (!available || !selectorMenuOpen || screen == null || wheelDelta == 0) return;
        try {
            List<Object> buttons = findButtonList(screen);
            if (buttons == null || findButton(buttons, SELECTOR_BUTTON_ID) == null) return;
            selectorScrollOffset = clampScrollOffset(selectorScrollOffset + (wheelDelta < 0 ? 1 : -1));
            rebuildSelectorMenu(screen, buttons, findButton(buttons, SELECTOR_BUTTON_ID));
        } catch (Throwable error) {
            System.err.println("[Cylox] Protocol menu scroll failed: " + error.getMessage());
        }
    }

    /** Draws the modern selector panel before vanilla draws its buttons. */
    public static void drawSelectorPanel(Object screen, int mouseX, int mouseY) {
        if (!available || !selectorMenuOpen || screen == null) return;
        int width = screenInt(screen, "width", "field_146294_l", "l");
        int height = screenInt(screen, "height", "field_146295_m", "m");
        if (width <= 0 || height <= 0) return;

        int panelWidth = Math.min(PANEL_WIDTH, width - 12);
        int panelHeight = Math.min(PANEL_HEIGHT, height - 12);
        if (panelWidth < 220 || panelHeight < 160) return;
        int panelX = (width - panelWidth) / 2;
        int panelY = (height - panelHeight) / 2;

        try {
            GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            drawPanelRect(panelX, panelY, panelX + panelWidth, panelY + panelHeight,
                0.035F, 0.055F, 0.075F, 0.94F);
            drawPanelRect(panelX, panelY, panelX + panelWidth, panelY + 2,
                0.16F, 0.72F, 0.88F, 1.0F);
            drawPanelRect(panelX + 1, panelY + 39, panelX + panelWidth - 1, panelY + 40,
                0.25F, 0.35F, 0.42F, 0.85F);

            int listTop = panelY + PANEL_LIST_TOP;
            int trackX = panelX + panelWidth - 19;
            int trackTop = listTop + 2;
            int trackHeight = Math.min(MENU_VISIBLE_ROWS * PANEL_ROW_HEIGHT - 4,
                panelY + panelHeight - 36 - trackTop);
            drawPanelRect(trackX, trackTop, trackX + 5, trackTop + trackHeight,
                0.18F, 0.23F, 0.28F, 0.95F);
            List<ProtocolChoice> choices = getProtocolChoices();
            int maxOffset = Math.max(0, choices.size() - MENU_VISIBLE_ROWS);
            int thumbHeight = choices.isEmpty() ? trackHeight
                : Math.max(18, trackHeight * Math.min(MENU_VISIBLE_ROWS, choices.size()) / choices.size());
            int thumbTravel = Math.max(0, trackHeight - thumbHeight);
            int thumbY = trackTop + (maxOffset == 0 ? 0 : thumbTravel * selectorScrollOffset / maxOffset);
            drawPanelRect(trackX - 1, thumbY, trackX + 6, thumbY + thumbHeight,
                0.18F, 0.72F, 0.88F, 1.0F);
            GL11.glPopAttrib();

            drawPanelText(screen, "CYLOX  /  PROTOCOL SELECTOR", panelX + 15, panelY + 10, 0xFFFFFFFF);
            drawPanelText(screen, "Choose the server version family", panelX + 15, panelY + 25, 0xFF9DB4C2);
            drawPanelText(screen, "Selected: " + getSelectedProtocolName(),
                panelX + 15, panelY + panelHeight - 27, 0xFF52D5B8);
            drawPanelText(screen, "Scroll to browse  |  tap a version to apply",
                panelX + 15, panelY + panelHeight - 14, 0xFF8296A3);
        } catch (Throwable error) {
            System.err.println("[Cylox] Could not draw protocol selector panel: " + error.getMessage());
        }
    }

    private static void drawPanelRect(int left, int top, int right, int bottom,
                                      float red, float green, float blue, float alpha) {
        GL11.glColor4f(red, green, blue, alpha);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2f(left, bottom);
        GL11.glVertex2f(right, bottom);
        GL11.glVertex2f(right, top);
        GL11.glVertex2f(left, top);
        GL11.glEnd();
    }

    private static void drawPanelText(Object screen, String text, float x, float y, int color) {
        Field fontField = findField(screen, "fontRendererObj", "field_146289_q", "q");
        if (fontField == null) return;
        try {
            Object font = fontField.get(screen);
            if (font == null) return;
            Method fallback = null;
            for (Class<?> type = font.getClass(); type != null; type = type.getSuperclass()) {
                for (Method method : type.getDeclaredMethods()) {
                    Class<?>[] p = method.getParameterTypes();
                    if (p.length == 4 && p[0] == String.class
                        && p[1] == Float.TYPE && p[2] == Float.TYPE && p[3] == Integer.TYPE) {
                        method.setAccessible(true);
                        if (method.getName().equals("drawStringWithShadow")
                            || method.getName().equals("func_175063_a")) {
                            method.invoke(font, text, x, y, color);
                            return;
                        }
                        if (fallback == null) fallback = method;
                    }
                }
            }
            if (fallback != null) fallback.invoke(font, text, x, y, color);
        } catch (Throwable ignored) { }
    }

    private static int clampScrollOffset(int requested) {
        return Math.max(0, Math.min(requested, Math.max(0, getProtocolChoices().size() - MENU_VISIBLE_ROWS)));
    }

    private static int scrollOffsetForSelected() {
        List<ProtocolChoice> choices = getProtocolChoices();
        for (int index = 0; index < choices.size(); index++) {
            if (choices.get(index).version.getVersion() == selectedProtocolId) {
                return clampScrollOffset(Math.max(0, index - MENU_VISIBLE_ROWS / 2));
            }
        }
        return 0;
    }

    private static void rebuildSelectorMenu(Object screen, List<Object> buttons, Object selector) throws Exception {
        removeMenuButtons(buttons);
        if (selector == null) return;
        int screenWidth = screenInt(screen, "width", "field_146294_l", "l");
        int screenHeight = screenInt(screen, "height", "field_146295_m", "m");
        int panelWidth = Math.min(PANEL_WIDTH, Math.max(220, screenWidth - 12));
        int panelHeight = Math.min(PANEL_HEIGHT, Math.max(160, screenHeight - 12));
        int panelX = Math.max(4, (screenWidth - panelWidth) / 2);
        int panelY = Math.max(4, (screenHeight - panelHeight) / 2);
        int rowWidth = panelWidth - 48;
        int rowX = panelX + 12;
        int rowY = panelY + PANEL_LIST_TOP;
        List<ProtocolChoice> choices = getProtocolChoices();
        selectorScrollOffset = clampScrollOffset(selectorScrollOffset);

        buttons.add(createButton(selector, SCROLL_UP_ID, panelX + panelWidth - 32,
            rowY + 2, 22, 20, "^"));
        buttons.add(createButton(selector, SCROLL_DOWN_ID, panelX + panelWidth - 32,
            panelY + panelHeight - 33, 22, 20, "v"));

        int end = Math.min(choices.size(), selectorScrollOffset + MENU_VISIBLE_ROWS);
        for (int index = selectorScrollOffset; index < end; index++) {
            int row = index - selectorScrollOffset;
            ProtocolChoice choice = choices.get(index);
            String label = choice.label;
            if (choice.version.getVersion() == selectedProtocolId) label = "* " + label;
            buttons.add(createButton(selector, OPTION_FIRST_ID + row, rowX,
                rowY + row * PANEL_ROW_HEIGHT, rowWidth, 20, label));
        }
        System.out.println("[Cylox] Protocol family menu opened: " + choices.size()
            + " families; scroll " + selectorScrollOffset + "/"
            + Math.max(0, choices.size() - MENU_VISIBLE_ROWS) + ".");
    }

    private static void removeMenuButtons(List<Object> buttons) {
        for (int index = buttons.size() - 1; index >= 0; index--) {
            int id = buttonId(buttons.get(index));
            if ((id >= OPTION_FIRST_ID && id < OPTION_FIRST_ID + MENU_VISIBLE_ROWS)
                || id == SCROLL_UP_ID || id == SCROLL_DOWN_ID) {
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

    private static Field findField(Object target, String... names) {
        if (target == null) return null;
        for (Class<?> type = target.getClass(); type != null; type = type.getSuperclass()) {
            for (String name : names) {
                try {
                    Field field = type.getDeclaredField(name);
                    field.setAccessible(true);
                    return field;
                } catch (NoSuchFieldException ignored) {
                    // Try the next MCP, SRG or obfuscated field name.
                } catch (Throwable ignored) {
                    // Inaccessible field; try another mapping.
                }
            }
        }
        return null;
    }

    private static int readIntField(Object target, int fallback, String... names) {
        Field field = findField(target, names);
        if (field == null || field.getType() != Integer.TYPE) return fallback;
        try {
            return field.getInt(target);
        } catch (Throwable ignored) {
            return fallback;
        }
    }

    private static int screenInt(Object screen, String mcpName, String srgName, String obfuscatedName) {
        return readIntField(screen, 0, mcpName, srgName, obfuscatedName);
    }

    private static int buttonId(Object button) {
        return buttonInt(button, 0, -1);
    }

    private static int buttonInt(Object button, int index, int fallback) {
        if (index < 0 || index >= BUTTON_INT_FIELDS.length) return fallback;
        return readIntField(button, fallback, BUTTON_INT_FIELDS[index]);
    }

    private static String buttonLabel(Object button) {
        Field field = findField(button, "displayString", "field_146126_j", "j");
        if (field != null && field.getType() == String.class) {
            try {
                return (String) field.get(button);
            } catch (Throwable ignored) { }
        }
        for (Class<?> type = button == null ? null : button.getClass(); type != null; type = type.getSuperclass()) {
            for (Field candidate : type.getDeclaredFields()) {
                if (candidate.getType() != String.class) continue;
                try {
                    candidate.setAccessible(true);
                    Object value = candidate.get(button);
                    if (value instanceof String) return (String) value;
                } catch (Throwable ignored) { }
            }
        }
        return null;
    }

    public static String getSelectedProtocolName() {
        for (ProtocolChoice choice : getProtocolChoices()) {
            if (choice.version.getVersion() == selectedProtocolId) return choice.label;
        }
        ProtocolVersion version = ProtocolVersion.getProtocol(selectedProtocolId);
        return version == null ? "1.8x" : familyLabel(version.getName());
    }

    private static String selectorLabel() {
        return "Select: " + getSelectedProtocolName();
    }

    private static String familyLabel(String versionName) {
        if (versionName == null) return null;
        int firstDot = versionName.indexOf('.');
        if (firstDot <= 0 || firstDot == versionName.length() - 1) return null;
        try {
            int major = Integer.parseInt(versionName.substring(0, firstDot));
            int nextDot = versionName.indexOf('.', firstDot + 1);
            String minorText = nextDot < 0
                ? versionName.substring(firstDot + 1)
                : versionName.substring(firstDot + 1, nextDot);
            int minor = Integer.parseInt(minorText);
            if (major == 1) {
                if (minor < 8) return null;
                return "1." + minor + "x";
            }
            // New Minecraft release naming (26.1, 26.2, 26.3, ...) is grouped by major.minor.
            if (major >= 26) return major + "." + minor + "x";
            return null;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static List<ProtocolChoice> getProtocolChoices() {
        Map<String, ProtocolChoice> families = new HashMap<String, ProtocolChoice>();
        ProtocolVersion nativeVersion = ProtocolVersion.getProtocol(NATIVE_PROTOCOL);
        for (ProtocolVersion version : getSupportedProtocols()) {
            if (!hasTranslationRoute(nativeVersion, version)) continue;
            String label = familyLabel(version.getName());
            if (label == null) continue;
            String exactName = label.substring(0, label.length() - 1);
            boolean exact = exactName.equals(version.getName());
            ProtocolChoice current = families.get(label);
            if (current == null || (exact && !current.exactRelease)
                || (exact == current.exactRelease && version.getVersion() < current.version.getVersion())) {
                families.put(label, new ProtocolChoice(label, version, exact));
            }
        }
        List<ProtocolChoice> result = new ArrayList<ProtocolChoice>(families.values());
        Collections.sort(result, new Comparator<ProtocolChoice>() {
            @Override public int compare(ProtocolChoice left, ProtocolChoice right) {
                return Integer.compare(left.version.getVersion(), right.version.getVersion());
            }
        });
        return result;
    }

    private static boolean hasTranslationRoute(ProtocolVersion source, ProtocolVersion target) {
        if (source == null || target == null) return false;
        if (source.getVersion() == target.getVersion()) return true;
        return Via.getManager().getProtocolManager().getProtocolPath(source, target) != null;
    }

    private static int normalizeProtocol(int requestedProtocolId) {
        ProtocolVersion requested = ProtocolVersion.getProtocol(requestedProtocolId);
        ProtocolVersion nativeVersion = ProtocolVersion.getProtocol(NATIVE_PROTOCOL);
        if (requested != null && hasTranslationRoute(nativeVersion, requested)) return requestedProtocolId;
        System.err.println("[Cylox] Saved target protocol " + requestedProtocolId
            + " has no 1.8.9 translation route; reset to native protocol " + NATIVE_PROTOCOL + ".");
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
        List<?> route = protocolId == NATIVE_PROTOCOL ? Collections.emptyList()
            : Via.getManager().getProtocolManager().getProtocolPath(ProtocolVersion.getProtocol(NATIVE_PROTOCOL), target);
        System.out.println("[Cylox] Via target applied: " + target.getName() + " (protocol "
            + target.getVersion() + "), route steps=" + (route == null ? "MISSING" : route.size()) + ".");
    }

    private static void logProtocolReadiness() {
        System.out.println("[Cylox] Via stack: versions=" + ViaLoadingBase.PROTOCOLS.size()
            + ", ViaBackwards=" + ViaLoadingBase.inClassPath("com.viaversion.viabackwards.api.ViaBackwardsPlatform")
            + ", ViaRewind=" + ViaLoadingBase.inClassPath("com.viaversion.viarewind.api.ViaRewindPlatform") + ".");
        ProtocolVersion target = ProtocolVersion.getProtocol(393);
        ProtocolVersion nativeVersion = ProtocolVersion.getProtocol(NATIVE_PROTOCOL);
        List<?> route = target == null || nativeVersion == null ? null
            : Via.getManager().getProtocolManager().getProtocolPath(nativeVersion, target);
        System.out.println("[Cylox] Route audit 1.8.9 (47) -> 1.13 (393): "
            + (route == null ? "MISSING" : route.size() + " translation steps") + ".");
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
        return buttonId(button) == expectedId;
    }

    private static void setButtonLabel(Object button, String label) {
        Field field = findField(button, "displayString", "field_146126_j", "j");
        if (field != null && field.getType() == String.class) {
            try {
                field.set(button, label);
                return;
            } catch (Throwable ignored) { }
        }
        for (Class<?> type = button == null ? null : button.getClass(); type != null; type = type.getSuperclass()) {
            for (Field candidate : type.getDeclaredFields()) {
                if (candidate.getType() != String.class) continue;
                try {
                    candidate.setAccessible(true);
                    candidate.set(button, label);
                    return;
                } catch (Throwable ignored) { }
            }
        }
    }

    /**
     * Called at the end of the vanilla Netty ChannelInitializer, before handshake.
     * The channel may be exposed as Channel rather than SocketChannel by some launchers.
     */
    public static void install(Object candidate) {
        System.out.println("[Cylox] Via initializer reached: available=" + available
            + ", candidate=" + (candidate == null ? "null" : candidate.getClass().getName()));
        if (!available) {
            System.err.println("[Cylox] Via pipeline skipped: Via bridge is not initialized.");
            return;
        }
        if (!(candidate instanceof Channel)) {
            ClassLoader expectedLoader = Channel.class.getClassLoader();
            ClassLoader actualLoader = candidate.getClass().getClassLoader();
            System.err.println("[Cylox] Netty classloader mismatch: bridge Channel loader="
                + expectedLoader + ", candidate loader=" + actualLoader
                + ", candidate interfaces=" + java.util.Arrays.toString(candidate.getClass().getInterfaces()) + ".");
            System.err.println("[Cylox] Via pipeline skipped because LaunchWrapper loaded duplicate Netty classes.");
            return;
        }
        System.out.println("[Cylox] Netty channel type verified: "
            + candidate.getClass().getName() + " is instance of " + Channel.class.getName() + ".");
        try {
            final Channel channel = (Channel) candidate;
            final List<String> names = channel.pipeline().names();
            System.out.println("[Cylox] Vanilla Netty pipeline: " + names + ".");
            if (channel.pipeline().get("decoder") == null || channel.pipeline().get("encoder") == null) {
                System.err.println("[Cylox] Via pipeline skipped: vanilla decoder/encoder not ready; pipeline=" + names + ".");
                return;
            }

            final int selected = CyloxProtocolTargetResolver.resolve(true, selectedProtocolId);
            if (selected == NATIVE_PROTOCOL) {
                System.out.println("[Cylox] Native 1.8.9 selected; Via translation is not required.");
                return;
            }

            setTargetProtocol(selected);
            attachVia(channel);

            final boolean decoderReady = channel.pipeline().get("via-decoder") != null;
            final boolean encoderReady = channel.pipeline().get("via-encoder") != null;
            if (!decoderReady || !encoderReady) {
                throw new IllegalStateException("Via handlers missing after attach (decoder="
                    + decoderReady + ", encoder=" + encoderReady + "); pipeline=" + channel.pipeline().names());
            }

            ProtocolVersion target = ProtocolVersion.getProtocol(selected);
            System.out.println("[Cylox] Via pipeline attached before initial handshake: "
                + "1.8.9 (protocol " + NATIVE_PROTOCOL + ") -> "
                + (target == null ? selected : target.getName())
                + " (protocol " + selected + ").");
            System.out.println("[Cylox] Active Netty pipeline: " + channel.pipeline().names() + ".");
        } catch (Throwable error) {
            System.err.println("[Cylox] Could not attach Via before initial handshake: "
                + error.getClass().getSimpleName() + ": " + error.getMessage());
            error.printStackTrace();
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
        if (channel.pipeline().get("via-decoder") != null
            && channel.pipeline().get("via-encoder") != null) return;
        UserConnection user = new UserConnectionImpl(channel, true);
        new ProtocolPipelineImpl(user);
        // This adds Via's encoder and decoder before Minecraft's vanilla handlers.
        channel.pipeline().addLast(new MCPVLBPipeline(user));
    }
}
