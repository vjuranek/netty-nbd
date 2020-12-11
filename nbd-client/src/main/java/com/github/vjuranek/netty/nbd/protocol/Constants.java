package com.github.vjuranek.netty.nbd.protocol;

/**
 * NBD protocol constants.
 *
 */
public final class Constants {
    public static final int NBD_PORT = 10809;

    public static final long NBD_MAGIC = 0x4e42444d41474943L;
    public static final int NBD_REQUEST_MAGIC = 0x25609513;
    public static final long I_HAVE_OPT = 0x49484156454F5054L;

    public static final int NBD_FLAG_FIXED_NEWSTYLE = 0b00000001;
    public static final int NBD_FLAG_C_FIXED_NEWSTYLE = 0b00000001;

    public static final long OPTION_REPLAY_MAGIC = 0x3e889045565a9L;

    // Options
    public static final int NBD_OPT_ABORT = 2;
    public static final int NBD_OPT_GO = 7;
    public static final int NBD_OPT_STRUCTURED_REPLY = 8;

    // Option reply types
    public static final int NBD_REP_ACK = 1;
    public static final int NBD_INFO_BLOCK_SIZE = 3;

    public static final int NBD_INFO_EXPORT = 0;

    public static final int INFO_BLOCK_SIZE_REPLY_LENGTH = 14;
    public static final int INFO_EXPORT_REPLY_LENGTH = 12;


    // NBD commands
    public static final short NBD_CMD_READ = 0;
    public static final short NBD_CMD_WRITE = 1;
    public static final short NBD_CMD_DISC = 2;

    // Replies
    public static final int NBD_SIMPLE_REPLY_MAGIC = 0x67446698;
    public static final int NBD_STRUCTURED_REPLY_MAGIC = 0x668e33ef;
}
