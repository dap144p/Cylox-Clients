package dev.cylox.client.minecraft189;

/**
 * Per-render-pass memoization for the render-view player's sleeping state.
 * RenderGlobal consults this same state inside its entity loops; the value cannot
 * change during one renderEntities invocation.
 */
public final class CyloxEntityRenderHooks {
    private static final class State {
        int sleeping = -1;
    }

    private static final ThreadLocal<State> STATE = new ThreadLocal<State>() {
        @Override
        protected State initialValue() {
            return new State();
        }
    };

    private CyloxEntityRenderHooks() { }

    public static void beginRenderPass() {
        STATE.get().sleeping = -1;
    }

    /** Returns -1 on the first lookup, otherwise the cached boolean as 0 or 1. */
    public static int getCachedPlayerSleeping() {
        return STATE.get().sleeping;
    }

    public static void cachePlayerSleeping(boolean sleeping) {
        State state = STATE.get();
        if (state.sleeping < 0) state.sleeping = sleeping ? 1 : 0;
    }

    public static void endRenderPass() {
        STATE.get().sleeping = -1;
    }
}
