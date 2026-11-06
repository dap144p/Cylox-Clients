/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.florianmichael.vialoadingbase.netty.handler;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.exception.CancelCodecException;
import com.viaversion.viaversion.exception.CancelEncoderException;
import com.viaversion.viaversion.util.PipelineUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

@ChannelHandler.Sharable
public class VLBViaEncodeHandler extends MessageToMessageEncoder<ByteBuf> {
    private final UserConnection user;
    private boolean handshakeAudited;

    public VLBViaEncodeHandler(UserConnection user) {
        this.user = user;
    }

    @Override
    protected void encode(final ChannelHandlerContext ctx, ByteBuf bytebuf, List<Object> out) throws Exception {
        if (!user.checkOutgoingPacket()) throw CancelEncoderException.generate(null);
        if (!user.shouldTransformPacket()) {
            out.add(bytebuf.retain());
            return;
        }

        ByteBuf transformedBuf = ctx.alloc().buffer(Math.max(256, bytebuf.readableBytes())).writeBytes(bytebuf);
        try {
            user.transformOutgoing(transformedBuf, CancelEncoderException::generate);
            auditInitialHandshake(transformedBuf);

            out.add(transformedBuf.retain());
        } finally {
            transformedBuf.release();
        }
    }

    private void auditInitialHandshake(ByteBuf buffer) {
        if (handshakeAudited || !user.isClientSide()) return;
        ProtocolVersion target = user.getProtocolInfo().serverProtocolVersion();
        if (target == null || !target.isKnown()) return;
        int[] next = new int[1];
        int packetId = readVarInt(buffer, buffer.readerIndex(), next);
        if (packetId != 0 || next[0] < 0) return;
        int protocolId = readVarInt(buffer, next[0], next);
        if (protocolId < 0) return;
        handshakeAudited = true;
        System.out.println("[Cylox] OUTGOING HANDSHAKE AFTER VIA: protocol="
            + protocolId + ", target=" + target.getName() + " (" + target.getVersion() + ").");
        if (protocolId != target.getOriginalVersion()) {
            System.err.println("[Cylox] HANDSHAKE MISMATCH: outgoing protocol " + protocolId
                + " does not equal selected target " + target.getOriginalVersion() + ".");
        } else {
            System.out.println("[Cylox] HANDSHAKE VERIFIED: selected protocol was written to the outgoing packet.");
        }
    }

    private static int readVarInt(ByteBuf buffer, int index, int[] nextIndex) {
        int value = 0;
        int position = 0;
        int cursor = index;
        while (cursor < buffer.writerIndex() && position < 35) {
            int current = buffer.getUnsignedByte(cursor++);
            value |= (current & 0x7F) << position;
            if ((current & 0x80) == 0) {
                nextIndex[0] = cursor;
                return value;
            }
            position += 7;
        }
        nextIndex[0] = -1;
        return -1;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (PipelineUtil.containsCause(cause, CancelCodecException.class)) return;
        super.exceptionCaught(ctx, cause);
    }
}
