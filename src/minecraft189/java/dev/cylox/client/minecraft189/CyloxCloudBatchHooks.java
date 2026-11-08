package dev.cylox.client.minecraft189;

/**
 * Batches the 64 vanilla Fancy Cloud tiles into one Tessellator draw per color-mask pass.
 * The caller preserves vanilla vertex order, format, masks, texture, color and transforms.
 */
public final class CyloxCloudBatchHooks {
    private static final int TILES_PER_PASS = 64;
    private static final ThreadLocal<int[]> TILES = new ThreadLocal<int[]>() {
        @Override protected int[] initialValue() { return new int[1]; }
    };

    private CyloxCloudBatchHooks() { }

    /** True only for the first tile in each vanilla Fancy Cloud pass. */
    public static boolean shouldBeginTile() {
        return TILES.get()[0] == 0;
    }

    /** True only for the last tile in each pass; resets the counter for the next pass. */
    public static boolean shouldDrawTile() {
        int[] count = TILES.get();
        int next = count[0] + 1;
        if (next >= TILES_PER_PASS) {
            count[0] = 0;
            return true;
        }
        count[0] = next;
        return false;
    }

    /** Allows the verifier to reset the thread-local counter between independent test cases. */
    public static void reset() {
        TILES.get()[0] = 0;
    }
}
