package com.github.vjuranek.netty.nbd.client.command;

import com.github.vjuranek.netty.nbd.protocol.reply.DataChunk;
import com.github.vjuranek.netty.nbd.protocol.reply.NbdReply;
import com.github.vjuranek.netty.nbd.protocol.reply.SimpleReply;
import com.github.vjuranek.netty.nbd.protocol.reply.StructuredReply;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * TODO:
 * - handle errors
 * - redesign for parallel/async usage
 * - fix read handler to be able to read longer replies - with default setup
 *   Netty reads only 1024B which results into maximum data chunk of 1004B
 *   (20 bytes is header). Handler should be able to handle multiple
 *   channelRead events and continue with reading until whole reply is read.
 */
public class ReadHandler extends CommandHandler {

    public static final int NBD_SIMPLE_REPLY_MAGIC = 0x67446698;
    public static final int NBD_STRUCTURED_REPLY_MAGIC = 0x668e33ef;

    public static final short NBD_REPLY_TYPE_NONE = 0;
    public static final short NBD_REPLY_TYPE_OFFSET_DATA = 1;

    public static final short NBD_REPLY_FLAG_DONE = 0b0000000000000001;

    private final long handle;
    private final int length; // length of simple reply message

    public ReadHandler(long handle) {
        this.handle = handle;
        this.length = 0;
    }

    public ReadHandler(long handle, int length) {
        this.handle = handle;
        this.length = length;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
        NbdReply reply;
        int replyMagic = msg.readInt();
        switch (replyMagic) {
            case NBD_SIMPLE_REPLY_MAGIC:
                reply = new SimpleReply(msg, this.length);
                if (this.handle == reply.getHandle()) {
                    this.reply.offer(reply);
                }
                break;
            case NBD_STRUCTURED_REPLY_MAGIC:
                reply = new StructuredReply(msg);
                if (this.handle == reply.getHandle()) {
                    handleStructuredReply((StructuredReply) reply);
                }
                break;
            default:
                throw new IllegalStateException(String.format("Unknown read reply magic %x", replyMagic));
        }
    }

    private void handleStructuredReply(StructuredReply reply) {
        switch (reply.getType()) {
            case NBD_REPLY_TYPE_NONE:
                if ((NBD_REPLY_FLAG_DONE & reply.getFlags()) != NBD_REPLY_FLAG_DONE) {
                    throw new IllegalStateException("None type reply hasn't  NBD_REPLY_FLAG_DONE");
                }
                break;
            case NBD_REPLY_TYPE_OFFSET_DATA:
                this.reply.offer(new DataChunk(this.handle, reply.getData()));
                break;
            default:
                throw new IllegalStateException("Unknown reply type " + reply.getType());
        }
    }

}
