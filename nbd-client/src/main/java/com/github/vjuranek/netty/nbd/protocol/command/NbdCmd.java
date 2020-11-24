package com.github.vjuranek.netty.nbd.protocol.command;

import com.github.vjuranek.netty.nbd.protocol.Constants;

import java.nio.ByteBuffer;

/**
 *
 * 32 bits, 0x25609513, magic (NBD_REQUEST_MAGIC)
 * 16 bits, command flags
 * 16 bits, type
 * 64 bits, handle
 * 64 bits, offset (unsigned)
 * 32 bits, length (unsigned)
 * (length bytes of data if the request is of type NBD_CMD_WRITE)
 *
 * @see <a href="https://github.com/NetworkBlockDevice/nbd/blob/master/doc/proto.md#request-message">NBD protocol
 * request message</a>
 */
public class NbdCmd {
    protected final short flags;
    protected final short type;
    protected final long handle;
    protected final long offset;
    protected final int length;


    public NbdCmd(short flags, short type, long handle) {
        this(flags, type, handle, 0, 0);
    }

    public NbdCmd(short flags, short type, long handle, long offset, int length) {
        this.flags = flags;
        this.type = type;
        this.handle = handle;
        this.offset = offset;
        this.length = length;
    }

    public int length() {
        return 158; // 32 + 16 + 16 + 64 + 64 + 32
    }

    public byte[] encode() {
        ByteBuffer cmd = ByteBuffer.allocate(length());
        cmd.putInt(Constants.NBD_REQUEST_MAGIC);
        cmd.putShort(flags);
        cmd.putShort(type);
        cmd.putLong(handle);
        cmd.putLong(offset);
        cmd.putInt(length);
        return cmd.array();
    }
}