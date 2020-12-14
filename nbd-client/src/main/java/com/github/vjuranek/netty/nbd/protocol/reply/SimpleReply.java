package com.github.vjuranek.netty.nbd.protocol.reply;

import com.github.vjuranek.netty.nbd.protocol.Constants;
import io.netty.buffer.ByteBuf;

import java.util.Arrays;

/**
 * Simple reply message
 * <p>
 * S: 32 bits, 0x67446698, magic (NBD_SIMPLE_REPLY_MAGIC)
 * S: 32 bits, error (MAY be zero)
 * S: 64 bits, handle
 * S: (length bytes of data if the request is of type NBD_CMD_READ and error is zero)
 *
 * @author vjuranek
 * @see <a href="https://github.com/NetworkBlockDevice/nbd/blob/master/doc/proto.md#simple-reply-message">
 * Simple reply message</a>
 */
public class SimpleReply implements NbdReply {

    private final int error;
    private final long handle;
    private final byte[] data;

    public SimpleReply(ByteBuf msg, int length) throws IllegalStateException {
        this.error = msg.readInt();
        this.handle = msg.readLong();
        if ((length != 0) && (this.error == 0)) {
            this.data = new byte[length];
            msg.readBytes(this.data);
        } else {
            this.data = null;
        }
    }

    public int getError() {
        return error;
    }

    @Override
    public long getHandle() {
        return handle;
    }

    @Override
    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        return "SimpleReply{" +
                "error=" + error +
                ", handle=" + handle +
                ", data=" + Arrays.toString(data) +
                '}';
    }
}
