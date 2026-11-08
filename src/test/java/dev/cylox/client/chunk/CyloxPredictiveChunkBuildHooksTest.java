package dev.cylox.client.chunk;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class CyloxPredictiveChunkBuildHooksTest {
    @Test
    public void prioritizesNearestChunksAheadOfMovementAndPreservesTheRest() throws Exception {
        FakeRenderChunk side = new FakeRenderChunk(0, 0, 32);
        FakeRenderChunk behind = new FakeRenderChunk(-32, 0, 0);
        FakeRenderChunk forwardFar = new FakeRenderChunk(48, 0, 0);
        FakeRenderChunk forwardNear = new FakeRenderChunk(16, 0, 0);
        FakeRenderChunk forwardSide = new FakeRenderChunk(32, 0, 16);

        LinkedHashSet<Object> queue = new LinkedHashSet<Object>();
        queue.add(side);
        queue.add(behind);
        queue.add(forwardFar);
        queue.add(forwardNear);
        queue.add(forwardSide);

        CyloxPredictiveChunkBuildHooks.prioritizeAheadForTest(queue, 0.0D, 0.0D, 1.0D, 0.0D);

        List<Object> expected = new ArrayList<Object>();
        expected.add(forwardNear);
        expected.add(forwardSide);
        expected.add(forwardFar);
        expected.add(side);
        expected.add(behind);
        assertEquals(expected, new ArrayList<Object>(queue));
        assertEquals("No dirty chunk may be dropped or duplicated", 5, queue.size());
    }

    @Test
    public void handlesNegativeChunkCoordinates() throws Exception {
        FakeRenderChunk behind = new FakeRenderChunk(0, 0, 32);
        FakeRenderChunk forward = new FakeRenderChunk(0, 0, -32);
        FakeRenderChunk side = new FakeRenderChunk(32, 0, 0);

        LinkedHashSet<Object> queue = new LinkedHashSet<Object>();
        queue.add(behind);
        queue.add(side);
        queue.add(forward);

        CyloxPredictiveChunkBuildHooks.prioritizeAheadForTest(queue, 0.0D, 0.0D, 0.0D, -1.0D);

        List<Object> expected = new ArrayList<Object>();
        expected.add(forward);
        expected.add(behind);
        expected.add(side);
        assertEquals(expected, new ArrayList<Object>(queue));
    }

    @Test
    public void prioritizesInitialCameraViewThenFlanksSidesAndRear() throws Exception {
        FakeRenderChunk rear = new FakeRenderChunk(0, 0, -64);
        FakeRenderChunk side = new FakeRenderChunk(48, 0, -8);
        FakeRenderChunk frontFar = new FakeRenderChunk(0, 0, 64);
        FakeRenderChunk frontFlank = new FakeRenderChunk(32, 0, 16);
        FakeRenderChunk frontNear = new FakeRenderChunk(0, 0, 16);
        FakeRenderChunk rearFlank = new FakeRenderChunk(32, 0, -32);

        LinkedHashSet<Object> queue = new LinkedHashSet<Object>();
        queue.add(rear);
        queue.add(side);
        queue.add(frontFar);
        queue.add(frontFlank);
        queue.add(frontNear);
        queue.add(rearFlank);

        // A +Z camera heading: center-front, front flanks, side/rear flanks, rear.
        CyloxPredictiveChunkBuildHooks.prioritizeCameraForTest(
            queue, 0.0D, 0.0D, 0.0D, 1.0D);

        List<Object> expected = new ArrayList<Object>();
        expected.add(frontNear);
        expected.add(frontFar);
        expected.add(frontFlank);
        expected.add(rearFlank);
        expected.add(side);
        expected.add(rear);
        assertEquals(expected, new ArrayList<Object>(queue));
        assertEquals("Every queued dirty chunk must remain exactly once", 6, queue.size());
    }

    @Test
    public void leavesVanillaInsertionOrderWhenThereIsNoMovementDirection() throws Exception {
        LinkedHashSet<Object> queue = new LinkedHashSet<Object>();
        FakeRenderChunk first = new FakeRenderChunk(32, 0, 0);
        FakeRenderChunk second = new FakeRenderChunk(0, 0, 32);
        FakeRenderChunk third = new FakeRenderChunk(-32, 0, 0);
        queue.add(first);
        queue.add(second);
        queue.add(third);

        CyloxPredictiveChunkBuildHooks.prioritizeAheadForTest(queue, 0.0D, 0.0D, 0.0D, 0.0D);

        List<Object> expected = new ArrayList<Object>();
        expected.add(first);
        expected.add(second);
        expected.add(third);
        assertEquals(expected, new ArrayList<Object>(queue));
    }

    private static final class FakeRenderChunk {
        private final FakeBlockPos position;

        FakeRenderChunk(int x, int y, int z) {
            position = new FakeBlockPos(x, y, z);
        }

        FakeBlockPos getPosition() {
            return position;
        }
    }

    private static final class FakeBlockPos {
        private final int x;
        private final int y;
        private final int z;

        FakeBlockPos(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        long toLong() {
            return ((long) x & 0x3FFFFFFL) << 38
                | ((long) y & 0xFFFL) << 26
                | ((long) z & 0x3FFFFFFL);
        }
    }
}
