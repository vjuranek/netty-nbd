package com.github.vjuranek.netty.nbd.protocol.command;

import com.github.vjuranek.netty.nbd.protocol.Constants;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Read command (NBD_CMD_READ).
 *
 * @author vjuranek
 * @see <a href="https://github.com/NetworkBlockDevice/nbd/blob/master/doc/proto.md#request-types">NBD_CMD_READ</a>
 */
public class ReadCmd extends NbdCmd {

    private static AtomicLong handle = new AtomicLong(0);

    public static long nextHandle() {
        return handle.incrementAndGet();
    }

    public ReadCmd(final long offset, final int length) {
        super((short) 0, Constants.NBD_CMD_READ, nextHandle(), offset, length);
    }

    public ReadCmd(final short flags, final long handle, final long offset, final int length) {
        super(flags, Constants.NBD_CMD_READ, handle, offset, length);
    }
}
