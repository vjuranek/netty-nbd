package com.github.vjuranek.netty.nbd.protocol.command;

import com.github.vjuranek.netty.nbd.protocol.Constants;

/**
 * A disconnect request.
 */
public class DiscCmd extends NbdCmd {

    public DiscCmd() {
        super((short) 0, Constants.NBD_CMD_DISC, (short)0);
    }

    public DiscCmd(short flags, long handle) {
        super(flags, Constants.NBD_CMD_DISC, handle);
    }
}
