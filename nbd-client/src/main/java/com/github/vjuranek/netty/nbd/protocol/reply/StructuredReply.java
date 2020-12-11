package com.github.vjuranek.netty.nbd.protocol.reply;

import com.github.vjuranek.netty.nbd.protocol.Constants;
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
public class StructuredReply {

    private final short flags;
    private final short type;
    private final long handle;
    private final int length;
    private final byte[] data;

    public StructuredReply(ByteBuf msg) throws IllegalStateException {
        int magic = msg.readInt();
        if (magic != Constants.NBD_STRUCTURED_REPLY_MAGIC) {
            throw new IllegalStateException(String.format("Unexpected reply magic, expected %x, but got %x.",
                    Constants.NBD_STRUCTURED_REPLY_MAGIC, magic));
        }
        this.flags = msg.readShort();
        this.type = msg.readShort();
        this.handle = msg.readLong();
        this.length = msg.readInt();
        if (this.length != 0) {
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

    public long getHandle() {
        return handle;
    }

    public int getLength() {
        return length;
    }

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
