package com.github.vjuranek.netty.nbd.protocol;

/**
 * NBD protocol constants.
 *
 */
public final class Constants {
    public static final int NBD_PORT = 10809;

    public static final long NBD_MAGIC = 0x4e42444d41474943L;
    public static final long I_HAVE_OPT = 0x49484156454F5054L;

    public static final int NBD_FLAG_FIXED_NEWSTYLE = 0b00000001;
    public static final int NBD_FLAG_C_FIXED_NEWSTYLE = 0b00000001;

    public static final long OPTION_REPLAY_MAGIC = 0x3e889045565a9L;
    public static final int NBD_REP_ACK = 1;
    public static final int NBD_OPT_STRUCTURED_REPLY = 0x8;
}
