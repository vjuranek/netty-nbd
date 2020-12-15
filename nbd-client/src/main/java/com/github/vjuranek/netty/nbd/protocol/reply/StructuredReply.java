package com.github.vjuranek.netty.nbd.protocol.reply;

import io.netty.buffer.ByteBuf;

import java.util.Arrays;

/**
 * Structured reply chunk message.
 * <p>
 * S: 32 bits, 0x668e33ef, magic (NBD_STRUCTURED_REPLY_MAGIC)
 * S: 16 bits, flags
 * S: 16 bits, type
 * S: 64 bits, handle
 * S: 32 bits, length of payload (unsigned)
 * S: length bytes of payload data (if length is nonzero)
 *
 * @author vjuranek
 * @see <a href="https://github.com/NetworkBlockDevice/nbd/blob/master/doc/proto.md#structured-reply-chunk-message">
 * Structured reply chunk message.</a>
 */
public class StructuredReply implements NbdReply {

    private final short flags;
    private final short type;
    private final long handle;
    private final int length;
    private final byte[] data;

    public StructuredReply(ByteBuf msg) throws IllegalStateException {
        this.flags = msg.readShort();
        this.type = msg.readShort();
        this.handle = msg.readLong();
        this.length = msg.readInt();
        if (this.length > 0) {
            this.data = new byte[msg.readableBytes()];
            msg.readBytes(this.data);
        } else {
            this.data = null;
        }
    }

    public StructuredReply(ByteBuf msg, short flags, short type, long handle) throws IllegalStateException {
        this.flags = flags;
        this.type = type;
        this.handle = handle;
        this.length = msg.readableBytes();
        if (this.length > 0) {
            this.data = new byte[this.length];
            msg.readBytes(this.data);
        } else {
            this.data = null;
        }
    }

    public short getFlags() {
        return flags;
    }

    public short getType() {
        return type;
    }

    @Override
    public long getHandle() {
        return handle;
    }

    public int getLength() {
        return length;
    }

    @Override
    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        return "StructuredReply{" +
                "flags=" + flags +
                ", type=" + type +
                ", handle=" + handle +
                ", length=" + length +
                ", data=" + Arrays.toString(data) +
                '}';
    }
}
