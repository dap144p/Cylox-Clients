package dev.cylox.client.minecraft189;

import org.lwjgl.opengl.GL11;

/**
 * Collects vanilla FontRenderer glyph strips and submits them as triangle batches.
 * The hook is render-thread local and flushes before every texture/color/decorative
 * state boundary, preserving vanilla glyph geometry, formatting and draw order.
 */
public final class CyloxFontBatchHooks {
    private static final int TRIANGLE_STRIP = 5;
    private static final int TRIANGLES = 4;
    private static final int FLOATS_PER_VERTEX = 5;
    private static final int FLOATS_PER_GLYPH = 6 * FLOATS_PER_VERTEX;

    private static final ThreadLocal<Context> CONTEXT = new ThreadLocal<Context>() {
        @Override
        protected Context initialValue() {
            return new Context();
        }
    };

    private CyloxFontBatchHooks() { }

    public static void beginText(Object renderer) {
        Context context = CONTEXT.get();
        if (context.textActive) {
            flush(context);
        }
        context.textActive = true;
        context.renderer = renderer;
        context.texture = null;
        context.capturing = false;
        context.glyphVertexCount = 0;
    }

    public static void endText() {
        Context context = CONTEXT.get();
        flush(context);
        context.textActive = false;
        context.renderer = null;
        context.texture = null;
        context.capturing = false;
        context.glyphVertexCount = 0;
    }

    /** Called immediately before vanilla binds a glyph texture. */
    public static void beforeTextureBind(Object texture) {
        Context context = CONTEXT.get();
        if (!context.textActive) return;
        Object previous = context.texture;
        if (previous != texture && (previous == null || !previous.equals(texture))) {
            flush(context);
        }
        context.texture = texture;
    }

    public static void beginGlyph(int mode) {
        Context context = CONTEXT.get();
        if (!context.textActive || mode != TRIANGLE_STRIP) {
            flush(context);
            context.capturing = false;
            GL11.glBegin(mode);
            return;
        }
        context.capturing = true;
        context.glyphVertexCount = 0;
    }

    public static void texCoord(float u, float v) {
        Context context = CONTEXT.get();
        if (!context.capturing) {
            GL11.glTexCoord2f(u, v);
            return;
        }
        context.textureU = u;
        context.textureV = v;
    }

    public static void vertex(float x, float y, float z) {
        Context context = CONTEXT.get();
        if (!context.capturing) {
            GL11.glVertex3f(x, y, z);
            return;
        }
        int offset = context.glyphVertexCount * FLOATS_PER_VERTEX;
        context.ensureGlyphCapacity(offset + FLOATS_PER_VERTEX);
        context.glyphVertices[offset] = x;
        context.glyphVertices[offset + 1] = y;
        context.glyphVertices[offset + 2] = z;
        context.glyphVertices[offset + 3] = context.textureU;
        context.glyphVertices[offset + 4] = context.textureV;
        context.glyphVertexCount++;
    }

    public static void endGlyph() {
        Context context = CONTEXT.get();
        if (!context.capturing) {
            GL11.glEnd();
            return;
        }

        context.capturing = false;
        if (context.glyphVertexCount == 4) {
            context.ensureBatchCapacity(FLOATS_PER_GLYPH);
            appendVertex(context, 0);
            appendVertex(context, 1);
            appendVertex(context, 2);
            appendVertex(context, 2);
            appendVertex(context, 1);
            appendVertex(context, 3);
        } else if (context.glyphVertexCount > 0) {
            // Fail-safe for an unexpected glyph shape: render its captured strip
            // immediately rather than dropping or reordering any submitted vertices.
            flush(context);
            GL11.glBegin(TRIANGLE_STRIP);
            for (int i = 0; i < context.glyphVertexCount; i++) {
                int offset = i * FLOATS_PER_VERTEX;
                GL11.glTexCoord2f(context.glyphVertices[offset + 3],
                    context.glyphVertices[offset + 4]);
                GL11.glVertex3f(context.glyphVertices[offset],
                    context.glyphVertices[offset + 1],
                    context.glyphVertices[offset + 2]);
            }
            GL11.glEnd();
        }
        context.glyphVertexCount = 0;
    }

    /** Flush before vanilla changes its current GL color or texture state. */
    public static void flush() {
        flush(CONTEXT.get());
    }

    private static void appendVertex(Context context, int vertexIndex) {
        int source = vertexIndex * FLOATS_PER_VERTEX;
        int target = context.batchSize;
        System.arraycopy(context.glyphVertices, source,
            context.batch, target, FLOATS_PER_VERTEX);
        context.batchSize = target + FLOATS_PER_VERTEX;
    }

    private static void flush(Context context) {
        if (context.batchSize == 0) return;
        GL11.glBegin(TRIANGLES);
        for (int offset = 0; offset < context.batchSize; offset += FLOATS_PER_VERTEX) {
            GL11.glTexCoord2f(context.batch[offset + 3], context.batch[offset + 4]);
            GL11.glVertex3f(context.batch[offset], context.batch[offset + 1],
                context.batch[offset + 2]);
        }
        GL11.glEnd();
        context.batchSize = 0;
    }

    private static final class Context {
        float[] glyphVertices = new float[4 * FLOATS_PER_VERTEX];
        float[] batch = new float[16384];
        int glyphVertexCount;
        int batchSize;
        float textureU;
        float textureV;
        boolean textActive;
        boolean capturing;
        Object renderer;
        Object texture;

        void ensureGlyphCapacity(int required) {
            if (required <= glyphVertices.length) return;
            int capacity = glyphVertices.length;
            while (capacity < required) capacity *= 2;
            float[] expanded = new float[capacity];
            System.arraycopy(glyphVertices, 0, expanded, 0, glyphVertices.length);
            glyphVertices = expanded;
        }

        void ensureBatchCapacity(int additional) {
            int required = batchSize + additional;
            if (required <= batch.length) return;
            flush(this);
            if (required <= batch.length) return;
            int capacity = batch.length;
            while (capacity < additional) capacity *= 2;
            batch = new float[capacity];
        }
    }
}
