package dev.cylox.client.input;

import org.junit.Test;
import static org.junit.Assert.*;

public class SprintKeyStateTest {
    @Test
    public void enabledModuleHoldsSprintOnlyWhenPlayerExists() {
        assertTrue(SprintKeyState.shouldPress(true, true, false));
        assertFalse(SprintKeyState.shouldPress(true, false, false));
    }

    @Test
    public void disabledModulePreservesThePhysicalSprintKey() {
        assertTrue(SprintKeyState.shouldPress(false, true, true));
        assertFalse(SprintKeyState.shouldPress(false, true, false));
    }
}
