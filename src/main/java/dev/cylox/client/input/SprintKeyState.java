package dev.cylox.client.input;

/** Resolves the vanilla sprint binding state without changing movement or packet data. */
public final class SprintKeyState {
    private SprintKeyState() { }

    public static boolean shouldPress(boolean toggleSprintEnabled, boolean playerPresent,
                                      boolean physicalSprintKeyDown) {
        return physicalSprintKeyDown || (toggleSprintEnabled && playerPresent);
    }
}
